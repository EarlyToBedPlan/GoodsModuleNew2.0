package cn.edu.xmu.provider.dao;


import cn.edu.xmu.orderservice.model.bo.FreightModel;
import cn.edu.xmu.orderservice.model.bo.OrderItem;
import cn.edu.xmu.orderservice.model.vo.OrderItemRetVo;
import cn.edu.xmu.provider.mapper.FreightModelPoMapper;
import cn.edu.xmu.provider.mapper.OrderItemPoMapper;
import cn.edu.xmu.provider.mapper.OrderPoMapper;
import cn.edu.xmu.provider.model.po.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDao{

    private  static  final Logger logger = LoggerFactory.getLogger(OrderDao.class);

    @Autowired
    private OrderPoMapper orderPoMapper;

    @Autowired
    private OrderItemPoMapper orderItemPoMapper;

    @Autowired
    private FreightModelPoMapper freightModelPoMapper;

    public OrderItemRetVo getOrderItemById(Long id){
        OrderItemPoExample example = new OrderItemPoExample();
        OrderItemPoExample.Criteria criteria1 = example.createCriteria();
        criteria1.andIdEqualTo(id);
        try {
            OrderItemPo orderItemPo = orderItemPoMapper.selectByPrimaryKey(id);
            if(orderItemPo == null)
                return null;
            Long orderId = orderItemPo.getOrderId();
            OrderPo orderPo = orderPoMapper.selectByPrimaryKey(orderId);
            OrderItemRetVo orderItemRetVo = new OrderItemRetVo();
            orderItemRetVo.setCustomerId(orderPo.getCustomerId());
            orderItemRetVo.setOrderItemId(id);
            return orderItemRetVo;
        } catch (DataAccessException e) {
            logger.error("getOrderItemById: DataAccessException:" + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("other exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<OrderItemRetVo> getOrderItemListByUserId(Long skuId, Long userId){
        OrderPoExample example=new OrderPoExample();
        OrderPoExample.Criteria criteria=example.createCriteria();
        criteria.andCustomerIdEqualTo(userId);//筛选给定useId的订单
        try {
            List<OrderPo> orderPos = orderPoMapper.selectByExample(example);//得到订单列表
            logger.debug("numbers"+orderPos.size());
            List<OrderItemRetVo> orderItems=new ArrayList<>();

            for(OrderPo orderPo : orderPos) {
                //筛选orderItem
                OrderItemPoExample itemPoExample = new OrderItemPoExample();
                OrderItemPoExample.Criteria criteria1 = itemPoExample.createCriteria();
                if(skuId != null)//skuId不为空
                    criteria1.andGoodsSkuIdEqualTo(skuId);//按照sku筛选orderItem
                criteria1.andOrderIdEqualTo(orderPo.getId());//按照各个orderId筛选orderItem
                List<OrderItemPo> orderItemPos = orderItemPoMapper.selectByExample(itemPoExample);
                for (OrderItemPo orderItemPo : orderItemPos) {
                    logger.debug("get orderItemPo id"+orderItemPo.getId());
                    OrderItemRetVo orderItem = new OrderItemRetVo();
                    orderItem.setOrderItemId(orderItem.getOrderItemId());
                    orderItem.setCustomerId(orderItem.getCustomerId());
                    orderItems.add(orderItem);
                }

            }
            return orderItems;
        }
        catch (DataAccessException e){
            logger.error("selectOrders: DataAccessException:" + e.getMessage());
            return null;
        }
        catch(Exception e)
        {
            logger.error("other exception:"+e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    //根据orderitemId获取userId
    public Long getUserIdByOrderItemId(Long id){
        OrderItemPoExample example = new OrderItemPoExample();
        OrderItemPoExample.Criteria criteria1 = example.createCriteria();
        criteria1.andIdEqualTo(id);//筛选给定id的orderItem
        try {
            OrderItemPo orderItemPo = orderItemPoMapper.selectByPrimaryKey(id);
            Long orderId = orderItemPo.getOrderId();//找到对应的orderId
            OrderPo orderPo = orderPoMapper.selectByPrimaryKey(orderId);//找到order
            return orderPo.getCustomerId();//找到userId
        } catch (DataAccessException e) {
            logger.error("getUserIdByOrderItemId: DataAccessException:" + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("other exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    //根据orderitemId获取skuid
    public Long getGoodsSkuIdByOrderItemId(Long id){
        OrderItemPoExample example = new OrderItemPoExample();
        OrderItemPoExample.Criteria criteria1 = example.createCriteria();
        criteria1.andIdEqualTo(id);//筛选给定id的orderItem
        try {
            OrderItemPo orderItemPo = orderItemPoMapper.selectByPrimaryKey(id);
            return orderItemPo.getGoodsSkuId();
        } catch (DataAccessException e) {
            logger.error("getGoodsSkuIdByOrderItemId: DataAccessException:" + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("other exception:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public FreightModel getFreightModelById(Long id) {
        return null;
    }
}