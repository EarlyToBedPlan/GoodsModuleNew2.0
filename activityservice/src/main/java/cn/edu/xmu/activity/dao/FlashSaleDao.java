package cn.edu.xmu.activity.dao;


import cn.edu.xmu.activity.mapper.FlashSaleItemPoMapper;
import cn.edu.xmu.activity.mapper.FlashSalePoMapper;
import cn.edu.xmu.activity.model.bo.FlashSale;
import cn.edu.xmu.activity.model.po.*;
import cn.edu.xmu.activity.model.vo.NewFlashSaleItemVo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleVo;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-03 16:44
 */
@Repository
public class FlashSaleDao implements InitializingBean {

    @Autowired
    private FlashSalePoMapper flashSalePoMapper;

    @Autowired
    private FlashSaleItemPoMapper flashSaleItemPoMapper;

    private static final Logger logger = LoggerFactory.getLogger(FlashSaleDao.class);


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * @Description: 获取当前时段的秒杀活动
     * @return: ReturnObject<Boolean>
     * @Author: LJP_3424
     * @Date: 2020/12/6 1:03
     */
    public ReturnObject<Boolean> checkFlashSale(Long id, LocalDateTime flashDate) {
        FlashSalePoExample example = new FlashSalePoExample();
        FlashSalePoExample.Criteria criteria = example.createCriteria();
        criteria.andTimeSegIdEqualTo(id);
        criteria.andFlashDateEqualTo(flashDate);
        criteria.andStateNotEqualTo(FlashSale.State.DELETE.getCode());

        logger.debug("findFlashSaleById: Time" + "SegmentId = " + id);

        List<FlashSalePo> flashSalePos = null;
        try {
            flashSalePos = flashSalePoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        // 查空
        if(flashSalePos == null || flashSalePos.size() == 0){
            return new ReturnObject<Boolean>(false);
        }
        return new ReturnObject<Boolean>(true);
    }

    public ReturnObject<FlashSalePo> getFlashSaleByFlashSaleId(Long flashSaleId) {
        try {
            FlashSalePo flashSalePo = flashSalePoMapper.selectByPrimaryKey(flashSaleId);
            if (flashSalePo == null || flashSalePo.getState() == FlashSale.State.DELETE.getCode()) {
                return new ReturnObject();
            } else {
                return new ReturnObject<>(flashSalePo);
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }

    /**
     * @param flashSaleVo
     * @param id
     * @Description: 修改秒杀信息
     * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
     * @Author: LJP_3424
     * @Date: 2020/12/6 1:04
     */
    public ReturnObject updateFlashSale(NewFlashSaleVo flashSaleVo, Long id) {
        FlashSalePo flashSalePo = new FlashSalePo();
        flashSalePo.setFlashDate(flashSaleVo.getFlashDate());
        flashSalePo.setGmtModified(LocalDateTime.now());
        flashSalePo.setId(id);
        try {
            int ret = flashSalePoMapper.updateByPrimaryKeySelective(flashSalePo);
            if (ret == 0) {
                return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                        "插入失败");
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        return new ReturnObject();
    }

    public ReturnObject<FlashSaleItemPo> insertSkuIntoFlashSale(NewFlashSaleItemVo newFlashSaleItemVo, Long id) {
        FlashSaleItemPo flashSaleItemPo = new FlashSaleItemPo();
        flashSaleItemPo.setQuantity(newFlashSaleItemVo.getQuantity());
        flashSaleItemPo.setSaleId(id);
        flashSaleItemPo.setGoodsSkuId(newFlashSaleItemVo.getSkuId());
        flashSaleItemPo.setPrice(newFlashSaleItemVo.getPrice());
        flashSaleItemPo.setGmtCreate(LocalDateTime.now());
        try {
            flashSaleItemPoMapper.insert(flashSaleItemPo);
            return new ReturnObject<>(flashSaleItemPo);
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }


    public List<FlashSalePo> getFlashSalesByTimeSegmentId(Long timeSegmentId) {
        FlashSalePoExample flashSalePoExample = new FlashSalePoExample();
        FlashSalePoExample.Criteria criteria = flashSalePoExample.createCriteria();
        criteria.andTimeSegIdEqualTo(timeSegmentId);
        return flashSalePoMapper.selectByExample(flashSalePoExample);
    }

    public ReturnObject deleteFlashSale(Long id) {
        int ret = flashSalePoMapper.deleteByPrimaryKey(id);
        if (ret == 0) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        return new ReturnObject(ResponseCode.OK);
    }

    public ReturnObject<Long> insertFlashSale(NewFlashSaleVo vo, Long timeSegmentId) {

        FlashSalePo flashSalePo = new FlashSalePo();
        flashSalePo.setFlashDate(vo.getFlashDate());
        flashSalePo.setGmtCreate(LocalDateTime.now());
        flashSalePo.setState(FlashSale.State.OFF.getCode());
        flashSalePo.setTimeSegId(timeSegmentId);

        try {
            int ret = flashSalePoMapper.insertSelective(flashSalePo);
            if (ret == 1) {
                return new ReturnObject<Long>(flashSalePo.getId());
            } else {
                return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, "创建失败");
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }

    public ReturnObject changeFlashSaleState(Long id, Byte state) {
        FlashSalePo flashSalePo = new FlashSalePo();
        flashSalePo.setId(id);
        flashSalePo.setState(state);
        try {
            int ret = flashSalePoMapper.updateByPrimaryKeySelective(flashSalePo);
            if (ret != 0) {
                return new ReturnObject(ResponseCode.OK);
            } else {
                return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                        String.format("插入失败"));
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }
}
