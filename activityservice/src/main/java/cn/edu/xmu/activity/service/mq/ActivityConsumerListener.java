package cn.edu.xmu.activity.service.mq;

import cn.edu.xmu.activity.mapper.CouponActivityPoMapper;
import cn.edu.xmu.activity.model.po.CouponActivityPo;
import cn.edu.xmu.activity.model.po.CouponPo;
import cn.edu.xmu.activity.util.JacksonUtil;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息消费者
 * @author Ming Qiu
 * @date Created in 2020/11/7 22:47
 **/
@Service
@RocketMQMessageListener(topic = "activity-topic",  consumerGroup = "activity-group")
public class ActivityConsumerListener implements RocketMQListener<String> {
    private static final Logger logger = LoggerFactory.getLogger(ActivityConsumerListener.class);
    @Autowired
    CouponActivityPoMapper couponActivityPoMapper;
    @Override
    public void onMessage(String message) {
        CouponActivityPo po = JacksonUtil.toObj(message, CouponActivityPo.class);
        logger.info("onMessage: update coupon quantity =" + po +" time = "+ LocalDateTime.now());
        couponActivityPoMapper.insertSelective(po);
    }
}
