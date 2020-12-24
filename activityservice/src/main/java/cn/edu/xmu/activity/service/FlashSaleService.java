package cn.edu.xmu.activity.service;

import cn.edu.xmu.activity.dao.FlashSaleDao;
import cn.edu.xmu.activity.dao.FlashSaleItemDao;
import cn.edu.xmu.activity.model.bo.FlashSale;
import cn.edu.xmu.activity.model.bo.FlashSaleItem;
import cn.edu.xmu.activity.model.po.*;
import cn.edu.xmu.activity.model.po.FlashSalePo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleItemVo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleVo;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.bo.GoodsSimpleSpu;
import cn.edu.xmu.goodsservice.model.bo.GoodsSku;
import cn.edu.xmu.goodsservice.model.bo.ShopSimple;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.otherservice.client.OtherService;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;
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

    @DubboReference(check = false,version = "2.2.0")
    IGoodsService goodsService;

    @DubboReference(check = false,version = "2.3.0")
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
                    GoodsSku goodsSku = goodsService.getSkuById(flashSaleItemPo.getGoodsSkuId());
                    FlashSaleItem flashSaleItem = new FlashSaleItem(flashSaleItemPo, goodsSku);
                    redisTemplate.opsForSet().add(segIdStr, flashSaleItem);
                }
            }
        }
        return reactiveRedisTemplate.opsForSet().members(segIdStr).map(x -> (FlashSaleItem) x);
    }

    public List<TimeSegmentVo> returnAllTimeSegmentVo(){
        return otherService.getAllTimeSegment();
    }

    @Transactional
    public Flux<FlashSaleItem> getCurrentFlashSale(LocalDateTime localDateTime) {
        String currentNow = "flashSaleNow_" + localDateTime.toString();
        if (redisTemplate.opsForSet().size(currentNow) == 0) {
            TimeSegmentVo timeSegmentPo = new TimeSegmentVo();
            timeSegmentPo.setId(9L);
            if (timeSegmentPo != null) {
                List<FlashSalePo> flashSalePos = flashSaleDao.getFlashSalesByTimeSegmentId(timeSegmentPo.getId());
                if (flashSalePos.size() != 0) {
                    FlashSalePo flashSalePo = flashSalePos.get(1);
                    List<FlashSaleItemPo> flashSaleItemPoFromSaleId = flashSaleItemDao.getFlashSaleItemPoFromSaleId(flashSalePo.getId());
                    for (FlashSaleItemPo flashSaleItemPo : flashSaleItemPoFromSaleId) {
                        GoodsSku goodsSku = goodsService.getSkuById(flashSaleItemPo.getGoodsSkuId());
                        FlashSaleItem flashSaleItem = new FlashSaleItem(flashSaleItemPo, goodsSku);
                        redisTemplate.opsForSet().add(currentNow, flashSaleItem);
                    }
                }
            }
        }
        return reactiveRedisTemplate.opsForSet().members(currentNow).map(x -> (FlashSaleItem) x);
    }


    @Transactional
    public ReturnObject createNewFlashSale(NewFlashSaleVo vo, Long id) {
        TimeSegmentVo timeSegmentPo = getTimeSegmentVoById(id);
        // 时间段不存在
        if (timeSegmentPo == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 将flashdate的时分秒去掉,达到标准的格式
        vo.setFlashDate(createRealTime(vo.getFlashDate(), LocalDateTime.of(2020, 01, 01, 0, 0, 0)));
        // 检查同一天是否存在相同时段的秒杀
        ReturnObject<Boolean> checkResult = flashSaleDao.checkFlashSale(id, vo.getFlashDate());
        if (checkResult.getCode() != ResponseCode.OK) {
            return new ReturnObject(checkResult.getCode(), checkResult.getErrmsg());
        }
        // 如果检查到了该时段存在其他秒杀活动
        if (checkResult.getData()) {
            return new ReturnObject(ResponseCode.TIMESEG_CONFLICT);
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
        if (flashSalePo.getFlashDate().compareTo(LocalDateTime.now()) < 0) {
            return new ReturnObject(ResponseCode.ACTIVITYALTER_INVALID);
        }
        if (flashSalePo.getState() == FlashSale.State.ON.getCode()) {
            return new ReturnObject(ResponseCode.ACTIVITYALTER_INVALID);
        }

        // 修改日期需要注意,修改的结果是否可以
        // 将flashDate的时分秒去掉,达到标准的格式
        vo.setFlashDate(createRealTime(vo.getFlashDate(), LocalDateTime.of(2020, 01, 01, 0, 0, 0)));
        // 检查同一天是否存在相同时段的秒杀
        ReturnObject<Boolean> checkResult = flashSaleDao.checkFlashSale(id, vo.getFlashDate());
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
    public ReturnObject insertSkuIntoFlashSale(Long shopId, NewFlashSaleItemVo newFlashSaleItemVo, Long flashSaleId) {
        // 检查商品skuId是否为真
        GoodsSku goodsSku = goodsService.getSkuById(newFlashSaleItemVo.getSkuId());
        if (goodsSku == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        ShopSimple simpleShop = goodsService.getSimpleShopById(goodsService.getShopIdBySpuId(goodsSku.getGoodsSpuId()));
        if (simpleShop == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 检查秒杀是否存在
        ReturnObject<FlashSalePo> flashSale = flashSaleDao.getFlashSaleByFlashSaleId(flashSaleId);
        if (flashSale == null) {
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
        // SKU 库存不够
        if (goodsSku.getInventory() < newFlashSaleItemVo.getQuantity()) {
            return new ReturnObject(ResponseCode.SKU_NOTENOUGH);
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

    @Transactional
    public ReturnObject deleteSkuFromFlashSale(Long fid, Long itemId) {
        ReturnObject<Boolean> booleanReturnObject = flashSaleItemDao.checkItem(itemId);
        if (booleanReturnObject.getCode() != ResponseCode.OK) {
            return new ReturnObject(booleanReturnObject.getCode(), booleanReturnObject.getErrmsg());
        }
        if (booleanReturnObject.getData()) {
            ReturnObject returnObject = flashSaleItemDao.deleteFlashSaleItem(itemId);
            return returnObject;
        } else {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
    }


    /******************通用方法*********************/




/*
    @Autowired
    TimeSegmentVoMapper timeSegmentPoMapper;

    private List<TimeSegmentVo> getAllTimeSegment() {
        TimeSegmentVoExample timeSegmentPoExample = new TimeSegmentVoExample();
        TimeSegmentVoExample.Criteria criteria = timeSegmentPoExample.createCriteria();
        criteria.andTypeEqualTo((byte) 1);
        List<cn.edu.xmu.activity.model.po.TimeSegmentVo> timeSegmentPos = timeSegmentPoMapper.selectByExample(timeSegmentPoExample);
        List<TimeSegmentVo> timeSegmentPosReturn = new ArrayList<>(timeSegmentPos.size());
        for (cn.edu.xmu.activity.model.po.TimeSegmentVo timeSegmentPo : timeSegmentPos) {
            TimeSegmentVo timeSegmentPoReturn = new TimeSegmentVo();
            timeSegmentPoReturn.setBeginTime(timeSegmentPo.getBeginTime());
            timeSegmentPoReturn.setEndTime(timeSegmentPo.getBeginTime());
            timeSegmentPoReturn.setGmtCreate(timeSegmentPo.getGmtModified());
            timeSegmentPoReturn.setGmtModified(timeSegmentPo.getGmtModified());
            timeSegmentPoReturn.setType((byte) 1);
            timeSegmentPoReturn.setId(timeSegmentPo.getId());
            timeSegmentPosReturn.add(timeSegmentPoReturn);
        }
        return timeSegmentPosReturn;
    }
*/
    private TimeSegmentVo getTimeSegmentVoById(Long timeSegmentId) {
        List<TimeSegmentVo> allTimeSegment = otherService.getAllTimeSegment();
        for (TimeSegmentVo timeSegmentVo : allTimeSegment) {
            if (timeSegmentVo.getId().equals(timeSegmentId)) {
                return timeSegmentVo;
            }
        }
        return null;
    }

    public ReturnObject changeFlashSaleStatus(Long id, Byte state) {

        ReturnObject<FlashSalePo> returnObject = flashSaleDao.getFlashSaleByFlashSaleId(id);
        // 存在错误,往上层传
        if (returnObject.getCode() != ResponseCode.OK) {
            return new ReturnObject(returnObject.getCode(), returnObject.getErrmsg());
        }
        if (returnObject.getData() == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        FlashSalePo flashSalePo = returnObject.getData();
        // 确认状态:id存在性和权限以及是否下线
        Byte expectState = null;
        if (state == FlashSale.State.ON.getCode() || state == FlashSale.State.DELETE.getCode()) {
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
        ReturnObject returnObject1 = flashSaleDao.changeFlashSaleState(id, state);
        if (returnObject1.getCode() != ResponseCode.OK) {
            return new ReturnObject(returnObject1.getCode(), returnObject1.getErrmsg());
        } else {
            return new ReturnObject(ResponseCode.OK);
        }
    }

    /**
     * 传入日期和时间段,生成时间
     */
    private LocalDateTime createRealTime(LocalDateTime flashDate, LocalDateTime flashTime) {
        return LocalDateTime.of(flashDate.getYear(), flashDate.getMonth(), flashDate.getDayOfMonth(),
                flashTime.getHour(), flashTime.getMinute(), flashTime.getSecond());
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
            return new ReturnObject(ResponseCode.ACTIVITYALTER_INVALID);
        }

        // 校验成功,通过
        return new ReturnObject(ResponseCode.OK);
    }
    /****************通用方法结束********************/


    /******************时间段补丁*******************/
    public TimeSegmentVo returnCurrentTimeSegmentVo() {
        LocalDateTime nowTime = LocalDateTime.now();
        List<TimeSegmentVo> allTimeSegment = otherService.getAllTimeSegment();
        for (TimeSegmentVo timeSegmentPoPast : allTimeSegment) {
            TimeSegmentVo timeSegmentPo = new TimeSegmentVo();
            timeSegmentPo.setBeginTime(createRealTime(LocalDateTime.now(), timeSegmentPoPast.getBeginTime()));
            timeSegmentPo.setEndTime(createRealTime(LocalDateTime.now(),timeSegmentPoPast.getEndTime()));
            if (timeSegmentPo.getEndTime().compareTo(nowTime) > 0 &&
                    timeSegmentPo.getBeginTime().compareTo(nowTime) < 0) {
                return timeSegmentPo;
            }
        }
        return null;
    }

    public FlashSaleItemPo getFlashSaleItemPoByTimeSegmentAndGoodsSkuId(Long timeSegmentId, Long goodsSkuId) {
        List<FlashSalePo> flashSales = flashSaleDao.getFlashSalesByTimeSegmentId(timeSegmentId);
        if (flashSales.size() != 0) {
            FlashSalePo flashSalePo = flashSales.get(0);
            List<FlashSaleItemPo> flashSaleItemPos = flashSaleItemDao.getFlashSaleItemPoFromSaleId(flashSalePo.getId());
            for (FlashSaleItemPo flashSaleItemPo : flashSaleItemPos) {
                if (flashSaleItemPo.getGoodsSkuId().longValue() == goodsSkuId.longValue()) {
                    return flashSaleItemPo;
                }
            }
        }
        return null;
    }

    /********************************************/
}
