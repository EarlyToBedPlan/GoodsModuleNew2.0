package cn.edu.xmu.activity.dao;


import cn.edu.xmu.activity.mapper.CouponSkuPoMapper;
import cn.edu.xmu.activity.model.bo.CouponSku;
import cn.edu.xmu.activity.model.po.CouponSkuPo;
import cn.edu.xmu.activity.model.po.CouponSkuPoExample;
import cn.edu.xmu.goodsservice.model.vo.GoodsSkuSimpleRetVo;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/2 10:51
 */
@Repository
public class CouponSkuDao implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CouponActivityDao.class);
    @Autowired
    private CouponSkuPoMapper couponSkuMapper;
    /**
     * 是否初始化，生成signature和加密
     */
    @Value("${couponservice.initialization}")
    private Boolean initialization;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * @param activityId
     * @description:加入新的优惠商品
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 4:11
     */
    public ReturnObject addCouponSku(Long[] skuIds, Long activityId) {
        ReturnObject retObj = null;
        //数据插入优惠商品表
        try {
            for(Long skuId:skuIds)
            {
                CouponSku couponSku = new CouponSku();
                couponSku.setSkuId(skuId);
                couponSku.setActivityId(activityId);
                CouponSkuPo couponSkuPo = couponSku.createPo();
                couponSkuMapper.insert(couponSkuPo);
                //插入成功
                logger.debug("insertCouponSku: insert couponSku = " + couponSkuPo.toString());
            }
            retObj = new ReturnObject();
        } catch (Exception e) {
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的服务器内部错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /**
     * @param id
     * @description: 根据活动id获取活动商品列表
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.util.List>
     * @author: Feiyan Liu
     * @date: Created at 2020/11/30 22:17
     */
    public PageInfo<CouponSkuPo> getCouponSkuListByActivityId(Long id,Integer page,Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        CouponSkuPoExample example = new CouponSkuPoExample();
        CouponSkuPoExample.Criteria criteria = example.createCriteria();
        criteria.andActivityIdEqualTo(id);
        List<CouponSkuPo> couponSkuPos = couponSkuMapper.selectByExample(example);
        logger.debug("getCouponSkuByActivityId: retCouponSku" + couponSkuPos);
        return new PageInfo<>(couponSkuPos);
    }

    public List<CouponSkuPo> getCouponSkuListBySkuId(Long id) {
        CouponSkuPoExample example = new CouponSkuPoExample();
        CouponSkuPoExample.Criteria criteria = example.createCriteria();
        criteria.andSkuIdEqualTo(id);
        List<CouponSkuPo> couponSkuPos = couponSkuMapper.selectByExample(example);
        logger.debug("getCouponSkuByActivityId: retCouponSku" + couponSkuPos);
        return couponSkuPos;
    }

    public CouponSkuPo getCouponSkuById(Long id) {
        CouponSkuPo po = null;
        try {
            po = couponSkuMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return po;
    }

    public ReturnObject deleteCouponSku(Long id) {
        ReturnObject returnObject = null;
        try {
            int ret = couponSkuMapper.deleteByPrimaryKey(id);
            if (ret == 0) {
                logger.debug("deleteCouponSku: delete couponSku fail id: " + id);
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("deleteCouponSku: delete couponSku success id: " + id);
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }
}
