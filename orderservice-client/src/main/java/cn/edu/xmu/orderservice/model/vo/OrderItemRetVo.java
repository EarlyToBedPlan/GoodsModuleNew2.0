package cn.edu.xmu.orderservice.model.vo;



import cn.edu.xmu.orderservice.model.bo.OrderItem;
import cn.edu.xmu.orderservice.model.po.OrderPo;
import lombok.Data;

@Data
public class OrderItemRetVo {
    private Long orderItemId;
    private Long customerId;

    public OrderItemRetVo(OrderItem orderItem, OrderPo order)
    {

        this.orderItemId=orderItem.getId();

        this.customerId=order.getCustomerId();
    }
}
