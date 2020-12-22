package cn.edu.xmu.activity.dao;


import cn.edu.xmu.ooad.util.JacksonUtil;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import cn.edu.xmu.activity.mapper.CouponPoMapper;
import cn.edu.xmu.activity.model.bo.Coupon;
import cn.edu.xmu.activity.model.po.CouponActivityPo;
import cn.edu.xmu.activity.model.po.CouponPo;
import cn.edu.xmu.activity.model.po.CouponPoExample;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.messaging.support.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/3 17:03
 */
@Repository
public class CouponDao implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CouponActivityDao.class);
    @Autowired
    private CouponPoMapper couponMapper;

    @Autowired
    CouponActivityDao couponActivityDao;

    @Autowired
    RocketMQTemplate rocketMQTemplate;
    /**
     * 是否初始化，生成signature和加密
     */
    @Value("${couponservice.initialization}")
    private Boolean initialization;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * @description: 根据用户id获得优惠券列表
     * @param id
     * @param state
     * @return: java.util.List<cn.edu.xmu.coupon.model.po.CouponPo>
     * @author: Feiyan Liu
     * @date: Created at 2020/12/3 17:10
     */
    public PageInfo<VoObject> getCouponListByUserId(Long id, Integer state, Integer page, Integer pageSize)
    {
        PageHelper.startPage(page, pageSize);
        CouponPoExample example=new CouponPoExample();
        CouponPoExample.Criteria criteria=example.createCriteria();
        criteria.andCustomerIdEqualTo(id);
        if(state!=null)
            criteria.andStateEqualTo((byte)state.intValue());
        List<CouponPo> couponPos =couponPos = couponMapper.selectByExample(example);
        logger.debug("getCouponSpuByActivityId: retCouponSpu" + couponPos);
        List<VoObject> couponVos = new ArrayList<>(couponPos.size());
        for (CouponPo po : couponPos) {
            //根据活动id获取活动详情 和优惠券信息一起组装成vo返回
            CouponActivityPo couponActivityPo = couponActivityDao.getCouponActivityById(po.getActivityId());
            Coupon coupon = new Coupon(po, couponActivityPo);
            couponVos.add(coupon.createSimpleVo());
        }
        return new PageInfo<>(couponVos);
    }

    /**
     * @description: 更新优惠券状态
     * @param id
     * @param state
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @author: Feiyan Liu
     * @date: Created at 2020/12/3 19:04
     */
    public ReturnObject updateCouponState(Long id,Integer state) {
        ReturnObject returnObject = null;
        try {
            CouponPo po = couponMapper.selectByPrimaryKey(id);
            po.setState((byte) state.intValue());
            int ret = couponMapper.updateByPrimaryKeySelective(po);
            if (ret == 0) {
                logger.debug("updateCouponState: update fail. coupon id: " + id);
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateCouponState: update success. coupon id: " + id);
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }

    public ReturnObject addCoupon(CouponPo couponPo) {
        ReturnObject returnObject = null;
        try {
            int ret = couponMapper.insert(couponPo);
            if (ret == 0) {
                logger.debug("updateCouponState: update fail. coupon id: ");
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateCouponState: update success. coupon id: ");
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }

    public CouponPo getCouponById(Long id)
    {
        CouponPo po=null;
        try {
             po = couponMapper.selectByPrimaryKey(id);
             logger.debug("po的id"+po.getId());
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return po;
    }


   public ReturnObject deleteCouponByActivityId(Long id)
    {
        CouponPoExample example=new CouponPoExample();
        CouponPoExample.Criteria criteria=example.createCriteria();
        criteria.andActivityIdEqualTo(id);
        criteria.andStateBetween((byte)Coupon.State.NOT_CLAIMED.getCode(),(byte)Coupon.State.CLAIMED.getCode());
        try{
            List<CouponPo> couponPos=couponMapper.selectByExample(example);
            for(CouponPo po:couponPos)
            {
                couponMapper.deleteByPrimaryKey(po.getId());

            }
        }
        catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return null;
    }

}
