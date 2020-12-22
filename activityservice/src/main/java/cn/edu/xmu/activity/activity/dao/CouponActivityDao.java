package cn.edu.xmu.activity.dao;


import cn.edu.xmu.activity.mapper.CouponActivityPoMapper;
import cn.edu.xmu.activity.model.bo.CouponActivity;
import cn.edu.xmu.activity.model.po.CouponActivityPo;
import cn.edu.xmu.activity.model.po.CouponActivityPoExample;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

//import cn.edu.xmu.goods.mapper.GoodsSkuPoMapper;
//import cn.edu.xmu.goods.model.po.GoodsSkuPo;
//import cn.edu.xmu.goods.model.po.GoodsSkuPoExample;

/**
 * @author Feiyan Liu
 * @date Created at 2020/11/29 9:54
 */
@Repository
@Mapper
public class CouponActivityDao implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CouponActivityDao.class);
    @Autowired
    private CouponSkuDao couponSkuDao;
    @Autowired
    private CouponActivityPoMapper couponActivityMapper;
    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;
    /**
     * 是否初始化，生成signature和加密
     */
    @Value("${couponservice.initialization}")
    private Boolean initialization;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * @param id
     * @description: 根据活动id获取优惠活动详情
     * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.coupon.model.bo.CouponActivity>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 17:04
     */
    public CouponActivityPo getCouponActivityById(Long id) {
        CouponActivityPo po = new CouponActivityPo();
        try {
            po = couponActivityMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            StringBuilder message = new StringBuilder().append("getCouponActivityById: ").append(e.getMessage());
            logger.debug(message.toString());
        }
        return po;
    }

    /**
     * @param couponActivity
     * @description:插入新的优惠活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 3:49
     */
    public CouponActivityPo addCouponActivity(CouponActivity couponActivity) {
        CouponActivityPo couponActivityPo = couponActivity.createPo();
        couponActivityPo.setCreatedBy(couponActivity.getCreatedBy().getId());
        try {
            couponActivityMapper.insert(couponActivityPo);
            //插入成功
            logger.debug("insertCouponActivity: insert couponActivity = " + couponActivityPo.toString());
        } catch (Exception e) {
            logger.error("严重错误：" + e.getMessage());
        }
        return couponActivityPo;
    }

    /**
     * @param
     * @description:查看优惠活动列表
     * @return: com.github.pagehelper.PageInfo<cn.edu.xmu.coupon.model.po.CouponActivityPo>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 9:10
     */
    public PageInfo<CouponActivityPo> getOnlineCouponActivity(Long shopId,LocalDateTime beginTime,LocalDateTime endTime,Integer page,Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        CouponActivityPoExample example = new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo((byte) CouponActivity.State.ONLINE.getCode());//查询处于上线状态的活动
        if (shopId != null)
            criteria.andShopIdEqualTo(shopId);
        //查询明天开始的活动
        if(beginTime!=null&&endTime==null&&beginTime.isAfter(LocalDateTime.now()))
        {
            criteria.andBeginTimeLessThan(beginTime);
            criteria.andBeginTimeGreaterThan(LocalDateTime.now());
        }
        //查询待上线的活动
        else if(beginTime!=null&&endTime==null)
            criteria.andBeginTimeGreaterThan(LocalDateTime.now());
        //查询进行中的活动
        else if(beginTime!=null&&endTime!=null)
        {
            criteria.andBeginTimeLessThan(LocalDateTime.now());
            criteria.andEndTimeGreaterThan(LocalDateTime.now());
        }
        //查询已下线的活动
        else if(beginTime==null&&endTime!=null)
            criteria.andEndTimeLessThan(LocalDateTime.now());
        List<CouponActivityPo> couponActivityPos = couponActivityMapper.selectByExample(example);
        logger.debug("getOnlineCouponActivity: retCouponActivities" + couponActivityPos);
        return new PageInfo<>(couponActivityPos);
    }



    /**
     * @param shopId
     * @description:查看本店的已下线优惠活动列表
     * @return: com.github.pagehelper.PageInfo<cn.edu.xmu.coupon.model.bo.CouponActivity>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 16:54
     */
    public PageInfo<CouponActivityPo> getInvalidCouponActivity(Long shopId,Integer page,Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        CouponActivityPoExample example = new CouponActivityPoExample();
        CouponActivityPoExample.Criteria criteria = example.createCriteria();
        criteria.andStateEqualTo((byte) CouponActivity.State.OFFLINE.getCode());
        criteria.andShopIdEqualTo(shopId);
        List<CouponActivityPo> couponActivityPos = couponActivityMapper.selectByExample(example);
            logger.debug("getOfflineCouponActivity: retCouponActivities" + couponActivityPos);
        return new PageInfo<>(couponActivityPos);
    }
//    public boolean checkSku(Long id)
//    {
//        if(getSkuById(id)==null)
//            return true;
//        else return false;
//    }
    /**
     * @description: 根据SkuId获取Sku具体信息
     * @param id
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 17:16
     */
//    public GoodsSkuPo getSkuById(Long id) {
//        GoodsSkuPo po=null;
//        try {
//            po=SkuMapper.selectByPrimaryKey(id);
//        } catch (Exception e) {
//            logger.error("严重错误：" + e.getMessage());
//        }
//        return po;
//    }

    /**
     * @param couponActivity
     * @description: 更新优惠活动图片
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/1 13:37
     */
    public ReturnObject updateCouponActivityImg(CouponActivity couponActivity) {
        ReturnObject returnObject = new ReturnObject();
        CouponActivityPo couponActivityPo = new CouponActivityPo();
        couponActivityPo.setImageUrl(couponActivity.getImg());
        couponActivityPo.setId(couponActivity.getId());
        try {
            int ret = couponActivityMapper.updateByPrimaryKeySelective(couponActivityPo);
            if (ret == 0) {
                logger.debug("updateCouponActivityImg: update fail. user id: " + couponActivity.getId());
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateCouponActivityImg: update user success : " + couponActivity.toString());
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }

    /**
     * @param couponActivity
     * @description: 修改优惠活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/1 14:55
     */
    public ReturnObject updateCouponActivity(CouponActivity couponActivity) {
        CouponActivityPo couponActivityPo = new CouponActivityPo();
        couponActivityPo.setId(couponActivity.getId());
        couponActivityPo.setName(couponActivity.getName());
        couponActivityPo.setQuantity(couponActivity.getQuantity());
        couponActivityPo.setBeginTime(couponActivity.getBeginTime());
        couponActivityPo.setEndTime(couponActivity.getEndTime());
        couponActivityPo.setStrategy(couponActivity.getStrategy());
        couponActivityPo.setModiBy(couponActivity.getModifiedBy().getId());
        ReturnObject returnObject = null;
        try {
            int ret = couponActivityMapper.updateByPrimaryKeySelective(couponActivityPo);
            if (ret == 0) {
                logger.debug("updateCouponActivity: update fail. user id: " + couponActivity.getId());
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateCouponActivity: update user success : " + couponActivity.toString());
                redisTemplate.delete("couponActivity_"+ couponActivityPo.getId());//从redis中删除数据
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }

    /**
     * @param id
     * @description: 修改优惠活动状态
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/2 10:35
     */
    public ReturnObject changeCouponActivityState(Long id,Integer state) {
        ReturnObject returnObject = null;
        CouponActivityPo po = new CouponActivityPo();
        po.setId(id);
        po.setState(state.byteValue());
        try {
            int ret = couponActivityMapper.updateByPrimaryKeySelective(po);
            if (ret == 0) {
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else{
                redisTemplate.delete("couponActivity_"+ po.getId());//从redis中删除数据
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }

    public Long getShopId(Long id) {
        CouponActivityPo po = null;
        try {
            po = couponActivityMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return po.getShopId();
    }
}
//    public ReturnObject getCouponSkuByActivityId(Long id)
//    {
//
//    }
