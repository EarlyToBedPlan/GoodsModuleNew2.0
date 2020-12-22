package cn.edu.xmu.activity.dao;

import cn.edu.xmu.activity.mapper.PreSalePoMapper;
import cn.edu.xmu.activity.model.bo.PreSale;
import cn.edu.xmu.activity.model.po.PreSalePo;
import cn.edu.xmu.activity.model.po.PreSalePoExample;
import cn.edu.xmu.activity.model.vo.NewPreSaleVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-01 9:59
 */
@Repository
public class PreSaleDao implements InitializingBean {


    @Autowired
    private PreSalePoMapper preSalePoMapper;

    private static final Logger logger = LoggerFactory.getLogger(PreSaleDao.class);


    @Override
    public void afterPropertiesSet() throws Exception {

    }


    public ReturnObject<Boolean> checkPreSaleInActivities(Long goodsSkuId, LocalDateTime beginTime, LocalDateTime endTime) {
        PreSalePoExample example = new PreSalePoExample();
        PreSalePoExample.Criteria criteria1 = example.createCriteria();

        criteria1.andEndTimeGreaterThan(beginTime);
        criteria1.andBeginTimeLessThan(endTime);
        criteria1.andGoodsSkuIdEqualTo(goodsSkuId);
        criteria1.andStateEqualTo(PreSale.State.ON.getCode());
        List<PreSalePo> preSalePos = null;
        // 这里使用select,实际上自己写count可以得到更高的效率
        try {
             preSalePos = preSalePoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            logger.error("selectAllPreSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        //返回为空则不存在,返回false ,不为空说明查询到了
        if (preSalePos.size() == 0) {
            return new ReturnObject<Boolean>(false);
        } else {
            return new ReturnObject<Boolean>(true);
        }
    }

    public ReturnObject<List> getPreSaleBySpuId(Long shopId, Long id, Byte state) {
        PreSalePoExample example = new PreSalePoExample();
        PreSalePoExample.Criteria criteria = example.createCriteria();
        // 只能查询自家的
        criteria.andShopIdEqualTo(shopId);
        // 删除态直接无视
        criteria.andStateNotEqualTo(PreSale.State.DELETE.getCode());

        if (id != null) {
            criteria.andGoodsSkuIdEqualTo(id);
        }
        if (state != null) {
            criteria.andStateEqualTo(state);
        }

        List<PreSalePo> preSalePos = null;
        try {
            preSalePos = preSalePoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            logger.error("selectAllPreSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }

        return new ReturnObject<>(preSalePos);
    }

    /**
     * 分页查询所有预售信息
     *
     * @param pageNum  页数
     * @param pageSize 每页大小
     * @return ReturnObject<List> 活动列表
     * @author LJP_3424
     */
    public ReturnObject<PageInfo<PreSalePo>> selectAllPreSale(Long shopId, Byte timeline, Long skuId, Integer pageNum, Integer pageSize) {
        PreSalePoExample example = new PreSalePoExample();
        PreSalePoExample.Criteria criteria = example.createCriteria();
        LocalDateTime tomorrow;
        if (timeline != null) {
            switch (timeline) {
                case 0:
                    //
                    criteria.andBeginTimeGreaterThan(LocalDateTime.now());
                    break;
                case 1:
                    // 明天
                    tomorrow = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN).minusDays(-1);
                    criteria.andBeginTimeBetween(tomorrow, tomorrow.minusDays(-1));
                    break;
                case 2:
                    criteria.andBeginTimeLessThanOrEqualTo(LocalDateTime.now());
                    criteria.andEndTimeGreaterThan(LocalDateTime.now());
                    break;
                case 3:
                    criteria.andEndTimeLessThan(LocalDateTime.now());
                    break;
            }
        }
        if (shopId != null) criteria.andShopIdEqualTo(shopId);
        if (skuId != null) criteria.andGoodsSkuIdEqualTo(skuId);

        criteria.andStateEqualTo(PreSale.State.ON.getCode());
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        try {
            //不加限定条件查询所有
            List<PreSalePo> preSalePos = preSalePoMapper.selectByExample(example);
            return new ReturnObject(PageInfo.of(preSalePos));
        } catch (DataAccessException e) {
            logger.error("selectAllPreSale: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }

    }

    /**
     * @param vo vo对象
     * @return ReturnObject
     * createdBy: LJP_3424
     */
    public ReturnObject<PreSalePo> createNewPreSale(NewPreSaleVo vo, Long shopId, Long id) {
        // vo方法创建简单的PreSalePO
        PreSalePo preSalePo = vo.createPreSalePo();

        // 完善PreSalePo的特点
        preSalePo.setState(PreSale.State.ON.getCode());
        preSalePo.setGoodsSkuId(id);
        preSalePo.setShopId(shopId);
        preSalePo.setGmtCreate(LocalDateTime.now());
        try {
            int insertResult = preSalePoMapper.insert(preSalePo);
            if (insertResult == 0) {
                return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR, "插入失败");
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
        return new ReturnObject<PreSalePo>(preSalePo);
    }


    /**
     * 修改一个预售活动信息
     *
     * @author LJP_3424
     */
    public ReturnObject updatePreSale(NewPreSaleVo preSaleVo, Long id) {
        PreSalePo preSalePo = new PreSalePo();
        preSalePo.setId(id);

        preSalePo.setName(preSaleVo.getName());
        preSalePo.setAdvancePayPrice(preSaleVo.getAdvancePayPrice());
        preSalePo.setRestPayPrice(preSaleVo.getRestPayPrice());
        preSalePo.setBeginTime(preSaleVo.getBeginTime());
        preSalePo.setPayTime(preSaleVo.getPayTime());
        preSalePo.setEndTime(preSaleVo.getEndTime());

        return updateValue(preSalePo);
    }


    /**
     * @param id
     * @param state
     * @Description:
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.lang.Object>
     * @Author: LJP_3424
     * @Date: 2020/12/11 16:26
     */
    public ReturnObject<Object> changePreSaleState(Long id, Byte state) {
        PreSalePo preSalePo = new PreSalePo();
        preSalePo.setId(id);
        preSalePo.setState(state);
        return updateValue(preSalePo);
    }


    public ReturnObject<PreSalePo> getPreSalePoByPreSaleId(Long preSaleId) {
        PreSalePo preSalePo = null;
        try {
            preSalePo = preSalePoMapper.selectByPrimaryKey(preSaleId);
            if (preSalePo == null) {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                return new ReturnObject(preSalePo);
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
     * 删除和更新都用update,两者结构相同,数据处理好后直接调用这个即可
     */
    private ReturnObject updateValue(PreSalePo preSalePo) {
        try {
            int ret = preSalePoMapper.updateByPrimaryKeySelective(preSalePo);
            if (ret == 1) {
                return new ReturnObject(ResponseCode.OK);
            } else {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
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