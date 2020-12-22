package cn.edu.xmu.activity.dao;


import cn.edu.xmu.activity.mapper.FlashSaleItemPoMapper;
import cn.edu.xmu.activity.model.po.FlashSaleItemPo;
import cn.edu.xmu.activity.model.po.FlashSaleItemPoExample;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-08 1:36
 */
@Repository
public class FlashSaleItemDao {
    @Autowired
    FlashSaleItemPoMapper flashSaleItemPoMapper;

    public List<FlashSaleItemPo> getFlashSaleItemPoFromSaleId(Long saleId) {
        FlashSaleItemPoExample example = new FlashSaleItemPoExample();
        FlashSaleItemPoExample.Criteria criteria = example.createCriteria();
        criteria.andSaleIdEqualTo(saleId);
        return flashSaleItemPoMapper.selectByExample(example);
    }

    public ReturnObject deleteFlashSaleItem(Long itemId) {
        FlashSaleItemPo flashSaleItemPo = flashSaleItemPoMapper.selectByPrimaryKey(itemId);
        try {
            flashSaleItemPoMapper.deleteByPrimaryKey(flashSaleItemPo.getId());
        } catch (DataAccessException e) {
            // 数据库错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        return new ReturnObject(ResponseCode.OK);
    }

    public ReturnObject<Boolean> checkSkuInFlashSale(Long flashSaleId, Long skuId) {
        FlashSaleItemPoExample example = new FlashSaleItemPoExample();
        FlashSaleItemPoExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsSkuIdEqualTo(skuId);
        criteria.andSaleIdEqualTo(flashSaleId);
        List<FlashSaleItemPo> flashSaleItemPos;
        try {
            flashSaleItemPos = flashSaleItemPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            // 数据库错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        if (flashSaleItemPos.size() != 0) {
            return new ReturnObject<Boolean>(true);
        } else {
            return new ReturnObject<Boolean>(false);
        }
    }

    public ReturnObject<Boolean> checkSkuInFlashSale(Long itemId) {

        FlashSaleItemPo flashSaleItemPos;
        try {
            flashSaleItemPos = flashSaleItemPoMapper.selectByPrimaryKey(itemId);
            if (flashSaleItemPos == null) {
                return new ReturnObject<>(false);
            } else {
                return new ReturnObject<>(true);
            }
        } catch (DataAccessException e) {
            // 数据库错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }

    public ReturnObject<Boolean> checkItem(Long itemId) {
        FlashSaleItemPo flashSaleItemPo = flashSaleItemPoMapper.selectByPrimaryKey(itemId);
        if(flashSaleItemPo != null){
            return new ReturnObject<>(true);
        }else{
            return new ReturnObject<>(false);
        }
    }
}
