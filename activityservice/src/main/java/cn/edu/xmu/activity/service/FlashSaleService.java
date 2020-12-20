package cn.edu.xmu.activity.service;

import cn.edu.xmu.activity.dao.FlashSaleDao;
import cn.edu.xmu.activity.dao.FlashSaleItemDao;
import cn.edu.xmu.activity.model.bo.FlashSale;
import cn.edu.xmu.activity.model.bo.FlashSaleItem;
import cn.edu.xmu.activity.model.bo.FlashSale;
import cn.edu.xmu.activity.model.po.*;
import cn.edu.xmu.activity.model.po.FlashSalePo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleItemVo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleVo;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.bo.GoodsSku;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.otherservice.client.OtherService;
import cn.edu.xmu.otherservice.model.po.TimeSegmentPo;
import cn.edu.xmu.otherservice.model.po.TimeSegmentPoExample;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-03 16:45
 */
@Service
public class FlashSaleService {
    private Logger logger = LoggerFactory.getLogger(FlashSaleService.class);

    @Autowired
    FlashSaleDao flashSaleDao;


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    FlashSaleItemDao flashSaleItemDao;

    @Autowired
    IGoodsService goodsService;

    @DubboReference(check = false)
    OtherService otherService;
    @Autowired
    private ReactiveRedisTemplate<String, Serializable> reactiveRedisTemplate;

    public Flux<FlashSaleItem> getFlashSale(Long segId) {
        String segIdStr = "seg_" + segId;
        if (redisTemplate.opsForSet().size(segIdStr) == 0) {
            List<FlashSalePo> flashSalePo = flashSaleDao.getFlashSalesByTimeSegmentId(segId);
            if (flashSalePo.size() != 0) {
                List<FlashSaleItemPo> flashSaleItemPos = flashSaleItemDao.getFlashSaleItemPoFromSaleId(flashSalePo.get(0).getId());
                for (FlashSaleItemPo flashSaleItemPo : flashSaleItemPos) {
                    GoodsSku goodsSku = getGoodsSku(flashSaleItemPo.getGoodsSkuId());
                    FlashSaleItem flashSaleItem = new FlashSaleItem(flashSaleItemPo, goodsSku);
                    redisTemplate.opsForSet().add(segIdStr, flashSaleItem);
                }
            }
        }
        return reactiveRedisTemplate.opsForSet().members(segIdStr).map(x -> (FlashSaleItem) x);
    }

    @Transactional
    public ReturnObject<List> getFlashSaleById(Long id) {

        ReturnObject<List> flashSalePos = flashSaleDao.getFlashSaleById(id);
        if (flashSalePos != null) {
            //returnObject = new ReturnObject<FlashSalePo>(flashSalePo);
            return flashSalePos;
        } else {
            logger.debug("findFlashSaleById: Not Found");
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
    }


    // 等待goods的接口
    private List<Long> getGoodsSpuIdsToSkuIds(Long goodsSpuId) {
        List<Long> longs = new ArrayList<Long>();
        longs.add(1003L);
        longs.add(1004L);
        longs.add(1005L);
        return longs;
    }

    @Transactional
    public boolean checkFlashSaleInActivities(Long goodsSpuId, LocalDateTime beginTime, LocalDateTime endTime) {
        List<Long> goodsSkuIds = getGoodsSpuIdsToSkuIds(goodsSpuId);
        for (Long goodsSkuId : goodsSkuIds) {
            if (flashSaleDao.getFlashSaleItemBetweenTimeByGoodsSkuId(goodsSkuId, beginTime, endTime) != null) {

                return true;
            }
        }
        return false;
    }


    @Transactional
    public Flux<FlashSaleItem> getCurrentFlashSale(LocalDateTime localDateTime) {
        String currentNow = "flashSaleNow_" + localDateTime.toString();
        if (redisTemplate.opsForSet().size(currentNow) == 0) {
            TimeSegmentPo timeSegmentPo = getTimeSegmentPoByTime(localDateTime);
            List<FlashSalePo> flashSalePos = flashSaleDao.getFlashSalesByTimeSegmentId(timeSegmentPo.getId());
            for (FlashSalePo flashSalePo : flashSalePos) {
                List<FlashSaleItemPo> flashSaleItemPoFromSaleId = flashSaleItemDao.getFlashSaleItemPoFromSaleId(flashSalePo.getId());
                for (FlashSaleItemPo flashSaleItemPo : flashSaleItemPoFromSaleId) {
                    GoodsSku goodsSku = getGoodsSku(flashSaleItemPo.getGoodsSkuId());
                    FlashSaleItem flashSaleItem = new FlashSaleItem(flashSaleItemPo, goodsSku);
                    redisTemplate.opsForSet().add(currentNow, flashSaleItem);
                }
            }
        }
        return reactiveRedisTemplate.opsForSet().members(currentNow).map(x -> (FlashSaleItem) x);
    }

    public TimeSegmentPo getTimeSegmentPoByTime(LocalDateTime localDateTime) {
        localDateTime = LocalDateTime.of(2020, 01, 01, localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        TimeSegmentPoExample timeSegmentPoExample = new TimeSegmentPoExample();
        TimeSegmentPoExample.Criteria criteria = timeSegmentPoExample.createCriteria();
        criteria.andEndTimeGreaterThanOrEqualTo(localDateTime);
        criteria.andBeginTimeLessThanOrEqualTo(localDateTime);
        List<TimeSegmentPo> allTimeSegment = otherService.getAllTimeSegment();
        for (TimeSegmentPo timeSegmentPo : allTimeSegment) {
            if (timeSegmentPo.getBeginTime().compareTo(localDateTime) < 0 && timeSegmentPo.getEndTime().compareTo(localDateTime) > 0) {
                return timeSegmentPo;
            }
        }
        return null;
    }

    private GoodsSku getGoodsSku(Long id) {
        GoodsSku goodsSku = new GoodsSku();
        goodsSku.setSkuSn("skusntest11");
        goodsSku.setId(111L);
        goodsSku.setDetail("这个是临时测试,记得调用接口");
        return goodsSku;
    }

    @Transactional
    public ReturnObject createNewFlashSale(NewFlashSaleVo vo, Long id) {
        TimeSegmentPo timeSegmentPo = getTimeSegmentPoById(id);
        // 时间段不存在
        if (timeSegmentPo == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 将flashdate的时分秒去掉,达到标准的格式
        vo.setFlashDate(createRealTime(vo.getFlashDate(), LocalDateTime.of(2020, 01, 01, 0, 0, 0)));
        // 检查同一天是否存在相同时段的秒杀
        ReturnObject<Boolean> checkResult = flashSaleDao.checkFlashSaleEnough(id, vo.getFlashDate());
        if (checkResult.getCode() != ResponseCode.OK) {
            return new ReturnObject(checkResult.getCode(), checkResult.getErrmsg());
        }

        if (checkResult.getData()) {
            return new ReturnObject(ResponseCode.FLASHSALE_OUTLIMIT);
        }

        ReturnObject<Long> returnObject = flashSaleDao.insertFlashSale(vo, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            return new ReturnObject(returnObject.getCode(), returnObject.getErrmsg());
        }

        FlashSalePo flashSalePo = flashSaleDao.getFlashSaleByFlashSaleId(returnObject.getData()).getData();
        FlashSale flashSale = new FlashSale(flashSalePo, timeSegmentPo);
        return new ReturnObject(flashSale);
    }


    /**
     * 修改活动
     *
     * @author LJP_3424
     */
    @Transactional
    public ReturnObject updateFlashSale(NewFlashSaleVo vo, Long id) {
        FlashSalePo flashSalePo = flashSaleDao.getFlashSaleByFlashSaleId(id).getData();
        if (flashSalePo == null) {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 秒杀不需要判断当前是否下线,可以直接修改日期
        // 修改日期需要注意,修改的结果是否可以
        // 将flashDate的时分秒去掉,达到标准的格式
        vo.setFlashDate(createRealTime(vo.getFlashDate(), LocalDateTime.of(2020, 01, 01, 0, 0, 0)));
        // 检查同一天是否存在相同时段的秒杀
        ReturnObject<Boolean> checkResult = flashSaleDao.checkFlashSaleEnough(id, vo.getFlashDate());
        if (checkResult.getData()) {
            return new ReturnObject(ResponseCode.FLASHSALE_OUTLIMIT);
        }
        ReturnObject returnObject = flashSaleDao.updateFlashSale(vo, id);

        if (returnObject.getCode() == ResponseCode.OK) {
            return new ReturnObject();
        } else {
            return new ReturnObject(returnObject.getCode(), returnObject.getErrmsg());
        }
    }

    /**
     * 添加SKU
     *
     * @author LJP_3424
     */
    @Transactional
    public ReturnObject insertSkuIntoPreSale(NewFlashSaleItemVo newFlashSaleItemVo, Long flashSaleId) {
        // 检查商品skuId是否为真
        GoodsSku goodsSku = goodsServicegetSkuById(newFlashSaleItemVo.getSkuId());
        if (goodsSku == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        ReturnObject<Boolean> booleanReturnObject = flashSaleItemDao.checkSkuInFlashSale(flashSaleId, newFlashSaleItemVo.getSkuId());
        if (booleanReturnObject.getCode() != ResponseCode.OK) {
            return new ReturnObject(booleanReturnObject.getCode(), booleanReturnObject.getErrmsg());
        }
        // 已经加入该秒杀了,不能重复加入
        if (booleanReturnObject.getData() == true) {
            return new ReturnObject(ResponseCode.SKUPRICE_CONFLICT);
        }
        //这边调用接口,减少相应数量,减少成功返回true
        /*if(goodsService.cutGoodsSkuInventory(newFlashSaleItemVo.getQuantity())){
            return new ReturnObject(ResponseCode.SKU_NOTENOUGH);
        }*/
        ReturnObject<FlashSaleItemPo> retObj = flashSaleDao.insertSkuIntoFlashSale(newFlashSaleItemVo, flashSaleId);
        FlashSaleItemPo flashSaleItemPo = retObj.getData();
        FlashSaleItem flashSaleItem = new FlashSaleItem(flashSaleItemPo, goodsSku);
        return new ReturnObject(flashSaleItem);
    }

    private GoodsSku goodsServicegetSkuById(Long skuId) {
        GoodsSku goodsSku = new GoodsSku();
        goodsSku.setId(skuId);
        goodsSku.setName("测试,记得改");
        goodsSku.setDetail("描述");
        goodsSku.setSkuSn("123");
        return goodsSku;
    }

    @Transactional
    public ReturnObject deleteSkuFromFlashSale(Long fid, Long skuId) {
        ReturnObject<Boolean> booleanReturnObject = flashSaleItemDao.checkSkuInFlashSale(fid, skuId);
        if (booleanReturnObject.getCode() != ResponseCode.OK) {
            return new ReturnObject(booleanReturnObject.getCode(), booleanReturnObject.getErrmsg());
        }
        if (booleanReturnObject.getData()) {
            ReturnObject returnObject = flashSaleItemDao.deleteFlashSaleItem(fid, skuId);
            return returnObject;
        } else {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
    }

    @Transactional
    public ReturnObject deleteFlashSale(Long id) {
        flashSaleItemDao.deleteFlashSaleItem(id, null);
        ReturnObject returnObject = flashSaleDao.deleteFlashSale(id);
        return returnObject;
    }


    /******************通用方法*********************/


    private TimeSegmentPo getTimeSegmentPoById(Long timeSegmentId) {
        List<TimeSegmentPo> allTimeSegment = flashSaleDao.getAllTimeSegment();
        for (TimeSegmentPo timeSegmentPo : allTimeSegment) {
            if (timeSegmentPo.getId().equals(timeSegmentId)) {
                return timeSegmentPo;
            }
        }
        return null;
    }

    public ReturnObject changeFlashSaleStatus(Long id, Byte state) {

        ReturnObject<FlashSalePo> returnObject = flashSaleDao.getFlashSaleByFlashSaleId(id);
        // 存在错误,往上层传
        if (returnObject.getCode() != ResponseCode.OK) {
            return returnObject;
        }
        FlashSalePo flashSalePo = returnObject.getData();
        // 确认状态:id存在性和权限以及是否下线
        Byte expectState = null;
        if (state == FlashSale.State.ON.getCode()) {
            expectState = FlashSale.State.OFF.getCode();
        } else {
            expectState = FlashSale.State.ON.getCode();
        }
        ReturnObject confirmResult = confirmFlashSaleId(flashSalePo, expectState);
        if (confirmResult.getCode() != ResponseCode.OK) {
            return confirmResult;
        }
        // 状态相同,改不了,下线的无法再下线,正如上线的无法再上线
        if (returnObject.getData().getState() == state) {
            return new ReturnObject(ResponseCode.GROUPON_STATENOTALLOW);
        }
        return flashSaleDao.changeFlashSaleState(id, state);
    }

    /**
     * 传入日期和时间段,生成时间
     */
    private LocalDateTime createRealTime(LocalDateTime flashDate, LocalDateTime flashTime) {
        return LocalDateTime.of(flashDate.getYear(), flashDate.getMonth(), flashDate.getDayOfMonth(),
                flashTime.getHour(), flashTime.getMinute(), flashTime.getSecond());
    }

    private LocalDateTime getBeginTimeByTimeSegmentId(Long id) {
        TimeSegmentPo timeSegmentPo = getTimeSegmentPoById(id);
        return timeSegmentPo.getBeginTime();

    }

    // 可修改状体检测
    public ReturnObject confirmFlashSaleId(FlashSalePo flashSalePo, Byte expectState) {

        // 错误路径1,该id不存在
        if (flashSalePo == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 错误路径3,状态不允许,并且目前只需要下线就能修改,不需要管改的结果
        // 时段冲突也不需要考虑,因为在下线状态,考虑的事情,扔给上线吧
        // 参数校验方面 Vo检测未来 Controller检测开始大于结束
        if (expectState != null && flashSalePo.getState() != expectState) {
            return new ReturnObject(ResponseCode.GROUPON_STATENOTALLOW);
        }

        // 校验成功,通过
        return new ReturnObject(ResponseCode.OK);
    }
    /****************通用方法结束********************/

}
