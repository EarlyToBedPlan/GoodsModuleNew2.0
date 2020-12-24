package cn.edu.xmu.goods.service;

import cn.edu.xmu.goods.dao.FloatPriceDao;
import cn.edu.xmu.goods.dao.GoodsSkuDao;
import cn.edu.xmu.goods.model.bo.FloatPrice;
import cn.edu.xmu.goods.model.po.FloatPricePo;
import cn.edu.xmu.goods.model.po.GoodsSkuPo;
import cn.edu.xmu.goods.model.vo.FloatPriceRetVo;
import cn.edu.xmu.goods.model.vo.FloatPriceVo;
import cn.edu.xmu.goods.model.vo.TimePoint;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.privilegeservice.client.IUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author：谢沛辰
 * @Date: 2020.12.01
 * @Description:价格浮动业务逻辑
 */
@Service
public class FloatPriceService{

    @Autowired
    FloatPriceDao floatPriceDao;
    @DubboReference(check=false, version = "2.5.0")
    IUserService iUserService;
    @Autowired
    GoodsSkuDao goodsSkuDao;

    public boolean timeClash(LocalDateTime beginTime1, LocalDateTime endTime1, LocalDateTime beginTime2, LocalDateTime endTime2) {
        if (beginTime1.isAfter(endTime2) || endTime1.isBefore(beginTime2)||beginTime1.isEqual(beginTime2)||endTime1.isEqual(endTime2))
            return false;
        return true;
    }

    /**
     * @Author：谢沛辰
     * @Date: 2020/12/8
      * @Param: Long userId, Long floatPriceId
     * @Return: ReturnObject<Object>
     * @Description:逻辑删除价格浮动
     */
    
    public ReturnObject<Object> logicallyDelete(Long userId, Long floatPriceId,Long shopId){
        ReturnObject<FloatPrice> target=floatPriceDao.getFloatPriceById(floatPriceId);
        if(target.getData()==null || target.getData().getValid()==0 ){
            return new ReturnObject<Object>(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else if (goodsSkuDao.checkSkuIdInShop(shopId,target.getData().getGoodsSkuId())==false){
            return new ReturnObject<Object>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        } else {
            FloatPrice selected = target.getData();
            selected.setInvaildBy(userId);
            selected.setValid((byte) 0);
            return floatPriceDao.logicallyDeleteFloatPrice(selected);
        }
    }

    /**
     * @Author：谢沛辰
     * @Date: 2020/12/8
      * @Param: Long userId,FloatPriceVo floatPriceVo,Long skuId
     * @Return: ReturnObject<FloatPriceRetVo>
     * @Description:创建价格浮动
     */
    //此处需要集成权限模块种通过userID查找用户的接口！
    
    public ReturnObject<FloatPriceRetVo> createFloatPrice(Long shopId,Long userId,FloatPriceVo floatPriceVo,Long skuId){
        ReturnObject<FloatPriceRetVo> retObj=null;
        GoodsSkuPo goodsSkuPo = goodsSkuDao.getSkuById(skuId).getData();
        if(goodsSkuPo == null ){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        if(goodsSkuDao.checkSkuIdStrictInShop(shopId,skuId)==false){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        if(floatPriceVo.getQuantity() > goodsSkuPo.getInventory()){
            return  new ReturnObject<>(ResponseCode.SKU_NOTENOUGH);
        }


        if(floatPriceVo.getBeginTime().isAfter(floatPriceVo.getEndTime())){
            return new ReturnObject<>(ResponseCode.Log_Bigger);
        }

        if(floatPriceVo.getBeginTime().isBefore(LocalDateTime.now())){
            return new ReturnObject<>(ResponseCode.FIELD_NOTVALID);
        }




        ReturnObject<List<FloatPricePo>> floatPriceInDataBase=floatPriceDao.getFloatPriceBySkuId(skuId);
        //check floatprice time conflict
        if(floatPriceInDataBase.getCode()!= ResponseCode.RESOURCE_ID_NOTEXIST && floatPriceInDataBase.getData().size()!=0){
            for(FloatPricePo floatPricePo:floatPriceInDataBase.getData()){
                if(timeClash(floatPriceVo.getBeginTime(),floatPriceVo.getEndTime(),floatPricePo.getBeginTime(),floatPricePo.getEndTime())){
                    return new ReturnObject<>(ResponseCode.SKUPRICE_CONFLICT);
                }
            }
        }




        FloatPrice floatPrice = new FloatPrice(floatPriceVo);
        boolean signal;
        floatPrice.setGoodsSkuId(skuId);
        if(floatPriceInDataBase.getCode()==ResponseCode.RESOURCE_ID_NOTEXIST){
            if(floatPriceDao.insertFloatPrice(floatPrice).getCode()==ResponseCode.OK){
                String userName=iUserService.getUserName(userId);
                //String userName = "default";
                retObj=new ReturnObject<>(new FloatPriceRetVo(floatPrice,userId,userId,userName));
            }
        }
        else if(floatPriceInDataBase.getCode()==ResponseCode.OK){
//            List<FloatPricePo> media=floatPriceInDataBase.getData();
//            List<TimePoint> timeList=new ArrayList<>();
//            for (FloatPricePo floatPricePo : media) {
//                if(floatPricePo.getValid()==1){
//                    timeList.add(new TimePoint(floatPricePo.getBeginTime(), true));
//                    timeList.add(new TimePoint(floatPricePo.getEndTime(), false));
//                }
//            }
//            timeList.add(new TimePoint(floatPriceVo.getBeginTime(),true));
//            timeList.add(new TimePoint(floatPriceVo.getEndTime(),false));
//            if(isOverLap(timeList)){
//                retObj=new ReturnObject<>(ResponseCode.SKUPRICE_CONFLICT);
//            }else{
                ReturnObject<FloatPrice> returnObject=floatPriceDao.insertFloatPrice(floatPrice);
                if(returnObject.getCode()==ResponseCode.OK){
                    //String userName=iUserService.getUserName(userId);
                    String userName="default";
                    retObj=new ReturnObject<>(new FloatPriceRetVo(floatPrice,userId,userId,userName));
                } else
                    retObj=new ReturnObject<>(returnObject.getCode());
            }
        //}
        return retObj;
    }
    /**
     * @Author：谢沛辰
     * @Date: 2020/12/8
      * @Param: List<TimePoint> list
     * @Return: boolean
     * @Description:判断时间段是否冲突
     */
    
    public boolean isOverLap(List<TimePoint> list){
        int max=0;
        int count=0;
        list.sort(new Comparator<TimePoint>() {
            
            public int compare(TimePoint timePoint, TimePoint t1) {
                if (timePoint.getPoint().equals(t1.getPoint()))
                    return 0;
                else {
                    if (timePoint.getPoint().isAfter(t1.getPoint()))
                        return 1;
                    else
                        return -1;
                }
            }
        });
        for (TimePoint timePoint : list) {
            if (timePoint.isIsbegin())
                count++;
            else
                count--;
            if (count > max)
                max = count;
        }
        return max != 1;
    }
}
