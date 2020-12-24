package cn.edu.xmu.activity.service;

import cn.edu.xmu.activity.dao.CouponActivityDao;
import cn.edu.xmu.activity.dao.CouponDao;
import cn.edu.xmu.activity.dao.CouponSkuDao;
import cn.edu.xmu.activity.model.bo.Coupon;
import cn.edu.xmu.activity.model.bo.CouponActivity;
import cn.edu.xmu.activity.model.po.CouponActivityPo;
import cn.edu.xmu.activity.model.po.CouponPo;
import cn.edu.xmu.activity.model.po.CouponSkuPo;
import cn.edu.xmu.activity.model.vo.CouponStateVo;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.vo.GoodsSkuSimpleRetVo;
import cn.edu.xmu.goodsservice.model.vo.ShopVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ImgHelper;
import cn.edu.xmu.ooad.util.JacksonUtil;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Feiyan Liu
 * @date Created at 2020/11/29 11:57
 */
@Service
public class CouponActivityService {
    @Autowired
    CouponActivityDao couponActivityDao;
    @Autowired
    CouponSkuDao couponSkuDao;
    @Autowired
    CouponDao couponDao;
    @DubboReference(check = false,version = "2.2.0")
    IGoodsService goodsService;
    @Resource
    RocketMQTemplate rocketMQTemplate;

    private Logger logger = LoggerFactory.getLogger(CouponActivityService.class);
    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * @author 24320182203218
     **/
    @Value("${couponservice.imglocation}")
    private String imgLocation;

    @Value("${couponservice.dav.username}")
    private String davUserName;

    @Value("${couponservice.dav.password}")
    private String davPassword;

    @Value("${couponservice.dav.baseUrl}")
    private String baseUrl;

    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Long getCouponByLuaScript(String couponCode,int num) {

        //lua脚本语法
        StringBuilder script = new StringBuilder();
        script.append(" local buyNum = ARGV[1]");//参数
        script.append(" local couponKey = KEYS[1] ");//key键
        script.append(" local couponNum = redis.call('get',couponKey) "); // 调用方式，必须是redis.call 或者pcall结合redis里的方法
        script.append(" if couponNum >= buyNum  ");
        script.append(" then redis.call('decrby',couponKey,buyNum)   ");
        script.append(" return couponNum   ");
        script.append(" else    ");
        script.append(" return '-1'  ");
        script.append(" end");

        DefaultRedisScript<String> longDefaultRedisScript = new DefaultRedisScript<>(script.toString(), String.class);
        String result = stringRedisTemplate.execute(longDefaultRedisScript, Collections.singletonList(couponCode), String.valueOf(num));
        return Long.valueOf(result);
    }


    /**
     * @description:获得优惠券的所有状态
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.util.List>
     * @author: Feiyan Liu
     * @date: Created at 2020/12/10 16:06
     */
    @Transactional
    public ReturnObject<List> getCouponAllState() {
        List<CouponStateVo> states = new ArrayList<>(Coupon.State.values().length);
        for (Coupon.State state : Coupon.State.values()) {
            CouponStateVo stateVo = new CouponStateVo(state);
            states.add(stateVo);
        }
        return new ReturnObject<>(states);
    }

    /**
     * @param couponActivity
     * @description:新建己方优惠活动 优惠券暂时是领一张生成一张
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 4:33
     */
    @Transactional
    public ReturnObject createCouponActivity(Long shopId, CouponActivity couponActivity) {
        ReturnObject ret = new ReturnObject();
        try {
            CouponActivityPo newPo = couponActivityDao.addCouponActivity(couponActivity);
            couponActivity.setId(newPo.getId());
            VoObject retVo = couponActivity.createRetVo();
            return new ReturnObject(retVo);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param id
     * @param multipartFile
     * @description: 优惠活动上传图片
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/1 13:39
     */
    @Transactional
    public ReturnObject uploadImg(Long id, MultipartFile multipartFile) {
        CouponActivity couponActivity = new CouponActivity(couponActivityDao.getCouponActivityById(id));
        if (couponActivity == null) {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("查询的优惠活动不存在 id：" + id));
        }
        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject = ImgHelper.remoteSaveImg(multipartFile, 2, davUserName, davPassword, baseUrl);

            //文件上传错误
            if (returnObject.getCode() != ResponseCode.OK) {
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }
            String oldFileName = couponActivity.getImg();

            couponActivity.setImg(returnObject.getData().toString());
            ReturnObject updateReturnObject = couponActivityDao.updateCouponActivityImg(couponActivity);

            //数据库更新失败，需删除新增的图片
            if (updateReturnObject.getCode() == ResponseCode.RESOURCE_ID_NOTEXIST) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUserName, davPassword, baseUrl);
                return updateReturnObject;
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldFileName != null) {
                ImgHelper.deleteRemoteImg(oldFileName, davUserName, davPassword, baseUrl);
            }
        } catch (IOException e) {
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ResponseCode.FILE_NO_WRITE_PERMISSION);
        }
        return new ReturnObject<>();
    }

    /**
     * @param page
     * @param pageSize
     * @description:获取上线活动列表 无需登录
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 9:43
     */
    @Transactional
    public ReturnObject<PageInfo<VoObject>> getOnlineCouponActivities(Long shopId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        try {
            PageInfo<CouponActivityPo> couponActivitiesPos = couponActivityDao.getOnlineCouponActivity(shopId, beginTime, endTime, page, pageSize);
            List<VoObject> couponActivities = couponActivitiesPos.getList().stream().map(po -> new CouponActivity(po).createSimpleVo()).collect(Collectors.toList());
            PageInfo<VoObject> returnObject = new PageInfo<>(couponActivities);
            returnObject.setPages(couponActivitiesPos.getPages());
            returnObject.setPageNum(couponActivitiesPos.getPageNum());
            returnObject.setPageSize(couponActivitiesPos.getPageSize());
            returnObject.setTotal(couponActivitiesPos.getTotal());
            return new ReturnObject<>(returnObject);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param page
     * @param pageSize
     * @description:获取本店下线活动列表
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 9:47
     */
    @Transactional
    public ReturnObject<PageInfo<VoObject>> getInvalidCouponActivities(Integer page, Integer pageSize, Long shopId) {
        try {
            PageInfo<CouponActivityPo> couponActivitiesPos = couponActivityDao.getInvalidCouponActivity(shopId, page, pageSize);
            List<VoObject> couponActivities = couponActivitiesPos.getList().stream().map(po -> new CouponActivity(po)).collect(Collectors.toList());
            PageInfo<VoObject> returnObject = new PageInfo<>(couponActivities);
            returnObject.setPages(couponActivitiesPos.getPages());
            returnObject.setPageNum(couponActivitiesPos.getPageNum());
            returnObject.setPageSize(couponActivitiesPos.getPageSize());
            returnObject.setTotal(couponActivitiesPos.getTotal());
            return new ReturnObject(returnObject);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param id
     * @param page
     * @param pageSize
     * @description: 查看优惠活动中的商品
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 22:11
     */
    @Transactional
    public ReturnObject<PageInfo<VoObject>> getCouponSku(Long id, Integer page, Integer pageSize) {
        try {
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(id);
            //判断活动是否存在
            if (couponActivityPo == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            //如果活动状态不是上线 不允许查看
            if (couponActivityPo.getState() == CouponActivity.State.OFFLINE.getCode())
                return new ReturnObject<>(ResponseCode.COUPONACT_STATENOTALLOW);
            if (couponActivityPo.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            PageInfo<CouponSkuPo> couponSkuPos = couponSkuDao.getCouponSkuListByActivityId(id, page, pageSize);
            List<Long> skuIds = couponSkuPos.getList().stream().map(couponSkuPo -> couponSkuPo.getSkuId()).collect(Collectors.toList());
            List<GoodsSkuSimpleRetVo> goodsSkuSimpleRetVos = goodsService.getGoodsSkuListBySkuIdList(skuIds);
            PageInfo<VoObject> returnObject = new PageInfo(goodsSkuSimpleRetVos);
            returnObject.setPages(couponSkuPos.getPages());
            returnObject.setPageNum(couponSkuPos.getPageNum());
            returnObject.setPageSize(couponSkuPos.getPageSize());
            returnObject.setTotal(couponSkuPos.getTotal());
            return new ReturnObject<>(returnObject);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param id
     * @description: 查看优惠活动详情
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/5 15:35
     */
    @Transactional
    public ReturnObject getCouponActivityById(Long id, Long shopId) {
        try {
            CouponActivityPo po = couponActivityDao.getCouponActivityById(id);
            //若活动不存在
            if (po == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if (po.getShopId() != shopId)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            CouponActivity couponActivity = new CouponActivity(po);
            return new ReturnObject(couponActivity.createRetVo());
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

    }

    /**
     * @param couponActivity 修改内容
     * @description: 修改优惠活动信息
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/2 10:25
     */
    @Transactional
    public ReturnObject updateCouponActivity(CouponActivity couponActivity) {
        try {
            CouponActivityPo po = couponActivityDao.getCouponActivityById(couponActivity.getId());
            //若活动不存在
            if (po == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            //若活动不属于此商店，不允许修改
            if (po.getShopId() != couponActivity.getShopId())
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            //若活动不是下线状态 不允许修改
            if (po.getState() == CouponActivity.State.ONLINE.getCode())
                return new ReturnObject(ResponseCode.COUPONACT_STATENOTALLOW);
            if (po.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            return couponActivityDao.updateCouponActivity(couponActivity);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param id 活动id
     * @description: 管理员删除优惠活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/2 10:34
     */
    @Transactional
    public ReturnObject deleteCouponActivity(Long shopId, Long id) {
        try {
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(id);
            if (couponActivityPo == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if (couponActivityPo.getShopId() != shopId)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            if (couponActivityPo.getState() == CouponActivity.State.ONLINE.getCode())
                return new ReturnObject(ResponseCode.COUPONACT_STATENOTALLOW);
            if (couponActivityPo.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            couponActivityDao.changeCouponActivityState(id, CouponActivity.State.DELETED.getCode());
            ;
            couponDao.deleteCouponByActivityId(id);
            redisTemplate.delete("coupon_"+id);//清除redis中缓存
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new ReturnObject();
    }

    /**
     * @param shopId
     * @param skuIds
     * @param activityId
     * @description: 管理员为己方已有优惠活动新增商品范围 可以批量新增
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/8 22:50
     */
    @Transactional
    public ReturnObject addCouponSku(Long shopId, Long[] skuIds, Long activityId) {
        try {

            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(activityId);
            if(couponActivityPo==null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if (couponActivityPo.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            //判断商品是否都存在
            List<GoodsSkuSimpleRetVo> goodsSkuSimpleRetVos = goodsService.getGoodsSkuListBySkuIdList(Arrays.asList(skuIds));
            if (goodsSkuSimpleRetVos.size() < skuIds.length)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            //判断商品是否都属于此店铺
            List<ShopVo> shopVos = goodsService.getShopVoBySkuIdList(Arrays.asList(skuIds));
            for (ShopVo vo : shopVos) {
                if (vo.getId() != shopId)
                    return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            for (Long id : skuIds) {
                //判断商品同一时段是否有其他活动（不同时间段有不同活动是可以的）
                Boolean result = (Boolean) checkCouponActivityParticipation(id, couponActivityPo.getBeginTime(), couponActivityPo.getEndTime()).getData();
                if (result == true) {
                    logger.debug("the sku id=" + id + " already has other activity at the same time.");
                    return new ReturnObject<>(ResponseCode.SKU_PARTICIPATE);
                }
            }
            couponSkuDao.addCouponSku(skuIds, activityId);

        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new ReturnObject();
    }


    /**
     * @param id
     * @description: 删除己方待上线的某优惠券活动对应的限定范围
     * bug:还需根据skuid获得shopId 判断操作权限
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/2 11:16
     */
    @Transactional
    public ReturnObject deleteCouponSku(Long id, Long shopId) {
        try {
            CouponSkuPo couponSkuPo = couponSkuDao.getCouponSkuById(id);
            if (couponSkuPo == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if(goodsService.getShopIdBySpuId(id)!=shopId)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(couponSkuPo.getActivityId());
            //判断活动状态是否为删除
            if (couponActivityPo.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            return couponSkuDao.deleteCouponSku(id);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param id
     * @param state
     * @param page
     * @param pageSize
     * @description: 买家获取自己的优惠券列表
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @author: Feiyan Liu
     * @date: Created at 2020/12/3 17:24
     */
    @Transactional
    public ReturnObject<PageInfo<VoObject>> getCouponByUserId(Long id, Integer state, Integer page, Integer pageSize) {
        try {
            return new ReturnObject<>(couponDao.getCouponListByUserId(id, state, page, pageSize));
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误ha：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param userId
     * @param id
     * @description: 用户领取优惠券
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/3 22:44
     */

    @Transactional
    public ReturnObject<List<String>> getCoupon(Long userId, Long id) {
        String activityKey = "couponactivity_" + id;
        if (!redisTemplate.hasKey(activityKey)) {
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(id);
            //检测活动是否存在
            if (couponActivityPo == null||couponActivityPo.getState()==CouponActivity.State.DELETED.getCode())
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            String json=JacksonUtil.toJson(couponActivityPo);
            redisTemplate.opsForValue().set(activityKey, json,600,TimeUnit.SECONDS);
//            logger.debug(JacksonUtil.toJson(couponActivityPo));
        }

        String json = (String) redisTemplate.opsForValue().get(activityKey);
        ObjectMapper objectMapper = new ObjectMapper();
        CouponActivityPo couponActivityPo = null;
        try {
            couponActivityPo = objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //优惠券活动不是上线状态
        if (couponActivityPo.getState().byteValue() != (byte) CouponActivity.State.ONLINE.getCode())
            return new ReturnObject(ResponseCode.COUPONACT_STATENOTALLOW);
        //若活动已结束
        if (couponActivityPo.getEndTime().isBefore(LocalDateTime.now()))
            return new ReturnObject(ResponseCode.COUPON_END);
        //领券时间还没到
        if (LocalDateTime.now().isBefore(couponActivityPo.getCouponTime()))
            return new ReturnObject(ResponseCode.COUPON_NOTBEGIN);
        int quantityType = couponActivityPo.getQuantitiyType();
        List<String> returnObject = new ArrayList<>(couponActivityPo.getQuantity());
        try {
            //判断优惠活动是否需要优惠券(即优惠券是否限量）
            //若不需要优惠券则不用领取
            if (couponActivityPo.getQuantity() == 0) {
                return new ReturnObject();
            }
            //限量优惠券
            else {
                //如果redis中没有 是第一位领券的 将优惠券信息放入redis
                String key = "coupon_" + couponActivityPo.getId();
                if(!redisTemplate.hasKey(key))
                    redisTemplate.opsForValue().set(key, couponActivityPo.getQuantity(),60*10, TimeUnit.SECONDS);
                if (quantityType == 0)//每人数量
                {

                    for (int i = 0; i < couponActivityPo.getQuantity(); i++)
                    {
                        CouponPo couponPo = createCoupon(userId, id, couponActivityPo);
                        //sendCouponMessage(couponPo);
                        couponDao.addCoupon(couponPo);
                        returnObject.add(couponPo.getCouponSn());
                    }
                    //设置结束时间
                    long second = couponActivityPo.getEndTime().toEpochSecond(ZoneOffset.ofHours(8))-LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                    redisTemplate.opsForValue().set("coupon_" + id + "_" + userId, 1, second, TimeUnit.SECONDS);
                } else if (quantityType == 1) {
                    // String couponQuantity=redisTemplate.opsForValue().get(key).toString();
                      Long result=  getCouponByLuaScript(key,1);
                  if(result.intValue()==-1)
                      return new ReturnObject<>(ResponseCode.COUPON_FINISH);
                    CouponPo couponPo = createCoupon(userId, id, couponActivityPo);
                    couponDao.addCoupon(couponPo);

                    //sendCouponMessage(couponPo);
                    CouponActivityPo couponActivityPo1=new CouponActivityPo();
                    couponActivityPo1.setId(id);
                    couponActivityPo1.setQuantity(result.intValue());
                    couponActivityDao.updateCouponActivityQuantity(result.intValue()-1,id);
                    //sendUpdateCouponQuantityMessage(couponActivityPo1);
                    returnObject.add(couponPo.getCouponSn());
                    //设置结束时间
                    long second = couponActivityPo.getEndTime().toEpochSecond(ZoneOffset.ofHours(8))-LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
                    redisTemplate.opsForValue().set("coupon_" + id + "_" + userId, 1, second, TimeUnit.SECONDS);
                }
            }
            return new ReturnObject<>(returnObject);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @param shopId
     * @param id
     * @description: 上线优惠活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/10 17:45
     */
    @Transactional
    public ReturnObject putCouponActivityOnShelves(Long shopId, Long id) {
        try {
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(id);
            if (couponActivityPo == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if (couponActivityPo.getShopId() != shopId)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            if (couponActivityPo.getState() != CouponActivity.State.OFFLINE.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            couponActivityDao.changeCouponActivityState(id, CouponActivity.State.ONLINE.getCode());
            couponDao.deleteCouponByActivityId(id);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new ReturnObject();
    }

    /**
     * @param shopId
     * @param id
     * @description: 下线优惠活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/10 17:45
     */
    @Transactional
    public ReturnObject putCouponActivityOffShelves(Long shopId, Long id) {
        try {
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(id);
            if (couponActivityPo == null)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            if (couponActivityPo.getShopId() != shopId)
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            if (couponActivityPo.getState() == CouponActivity.State.DELETED.getCode())
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            couponActivityDao.changeCouponActivityState(id, CouponActivity.State.OFFLINE.getCode());
            couponDao.deleteCouponByActivityId(id);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
        return new ReturnObject();
    }

    /**
     * @param userId
     * @param id
     * @param couponActivityPo
     * @description: 创建优惠券po对象
     * @return: cn.edu.xmu.coupon.model.po.CouponPo
     * @author: Feiyan Liu
     * @date: Created at 2020/12/3 22:44
     */
    private CouponPo createCoupon(Long userId, Long id, CouponActivityPo couponActivityPo) {
        CouponPo couponPo = new CouponPo();
        couponPo.setCouponSn(randomUUID());
        couponPo.setCustomerId(userId);
        couponPo.setActivityId(id);
        couponPo.setName(couponActivityPo.getName());
        couponPo.setGmtCreate(LocalDateTime.now());
        if (couponActivityPo.getValidTerm() == 0) {
            couponPo.setBeginTime(couponActivityPo.getCouponTime());
            couponPo.setEndTime(couponActivityPo.getEndTime());
        } else {
            couponPo.setBeginTime(LocalDateTime.now());
            couponPo.setEndTime(LocalDateTime.now().plusDays(couponActivityPo.getValidTerm().intValue()));
        }
        couponPo.setState((byte) Coupon.State.CLAIMED.getCode());
        return couponPo;
    }

    /**
     * @param id1
     * @param id2
     * @description: 判断两个优惠活动是否冲突
     * @return: boolean
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 19:29
     */
    public boolean activityClash(Long id1, Long id2) {
        CouponActivityPo po1 = couponActivityDao.getCouponActivityById(id1);
        CouponActivityPo po2 = couponActivityDao.getCouponActivityById(id2);
        //判断活动时间区间是否相交
        return timeClash(po1.getBeginTime(), po1.getEndTime(), po2.getBeginTime(), po2.getEndTime());
    }

    /**
     * @param beginTime1
     * @param endTime1
     * @param beginTime2
     * @param endTime2
     * @description: 判断两个时间区间是否相交
     * @return: boolean
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 20:02
     */
    public boolean timeClash(LocalDateTime beginTime1, LocalDateTime endTime1, LocalDateTime beginTime2, LocalDateTime endTime2) {
        if (beginTime1.isAfter(endTime2) || endTime1.isBefore(beginTime2))
            return false;
        return true;
    }

    /**
     * @param skuId
     * @param beginTime
     * @param endTime
     * @description: 判断商品在一个时间段内 是否已经参加了优惠活动
     * @return: ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 20:06
     */
    public ReturnObject checkCouponActivityParticipation(Long skuId, LocalDateTime beginTime, LocalDateTime endTime) {
        try {
            //检测活动商品表中，此商品是否已参加此活动
            List<CouponSkuPo> couponSkuPos = couponSkuDao.getCouponSkuListBySkuId(skuId);
            for (CouponSkuPo po : couponSkuPos) {
                //判断商品参与的活动与即将加入的活动时间是否冲突
                CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(po.getActivityId());
                if (couponActivityPo.getState() != CouponActivity.State.DELETED.getCode()
                        && timeClash(couponActivityPo.getBeginTime(), couponActivityPo.getEndTime(), beginTime, endTime)) {
                    return new ReturnObject<>(true);
                }
            }
            return new ReturnObject<>(false);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    public boolean checkCouponActivityShop(Long shopId, Long couponActivityId) {
        if (shopId == couponActivityDao.getShopId(couponActivityId))
            return true;
        else return false;
    }

    public void sendCouponMessage(CouponPo coupon) {
        rocketMQTemplate.asyncSend("coupon-topic", MessageBuilder.withPayload(coupon).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("sendCouponMessage: onSuccess result = " + sendResult + " time =" + LocalDateTime.now());
            }

            @Override
            public void onException(Throwable throwable) {
                logger.info("sendCouponMessage: onException e = " + throwable.getMessage() + " time =" + LocalDateTime.now());
            }
        });
    }
    public void sendUpdateCouponQuantityMessage(CouponActivityPo couponActivity) {
        rocketMQTemplate.asyncSend("activity-topic", MessageBuilder.withPayload(couponActivity).build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                logger.info("sendCouponMessage: onSuccess result = " + sendResult + " time =" + LocalDateTime.now());
            }

            @Override
            public void onException(Throwable throwable) {
                logger.info("sendCouponMessage: onException e = " + throwable.getMessage() + " time =" + LocalDateTime.now());
            }
        });
    }


//    /**
//     * @param id
//     * @description: 退回优惠券 这也是内部API
//     * @return: cn.edu.xmu.ooad.util.ReturnObject
//     * @author: Feiyan Liu
//     * @date: Created at 2020/12/3 23:18
//     */
//    
//    public ReturnObject returnCoupon(Long id) {
//        try {
//            CouponPo couponPo = couponDao.getCouponById(id);
//            if(couponPo==null)
//                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
//            return couponDao.updateCouponState(id, Coupon.State.CLAIMED.getCode());
//        } catch (Exception e) {
//            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
//            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
//        }
//    }
//    /**
//     * @param id
//     * @param userId
//     * @description: 用户使用优惠券下单 这个应该是内部API
//     * @return: cn.edu.xmu.ooad.util.ReturnObject
//     * @author: Feiyan Liu
//     * @date: Created at 2020/12/4 14:46
//     */
//    
//    @Transactional
//    public ReturnObject useCoupon(Long id, Long userId) {
//        CouponPo couponPo=null;
//        try {
//            couponPo = couponDao.getCouponById(id);
//            //判断优惠券id是否存在
//            if(couponPo==null)
//                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
//            //判断优惠券是否属于客户
//            if (couponPo.getCustomerId() != userId)
//                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
//            //若优惠券已使用或者是失效了
//            if (couponPo.getState() != (byte)Coupon.State.CLAIMED.getCode())
//                return new ReturnObject<>(ResponseCode.COUPON_STATENOTALLOW);
//            //若优惠券已过期或者是还未到有效时间
//            if(couponPo.getBeginTime().isAfter(LocalDateTime.now())||couponPo.getEndTime().isBefore(LocalDateTime.now()))
//                return new ReturnObject<>(ResponseCode.COUPON_STATENOTALLOW);
//            else return couponDao.updateCouponState(id, Coupon.State.USED.getCode());
//        } catch (Exception e) {
//            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
//            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
//        }
//    }

}
