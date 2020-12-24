package cn.edu.xmu.orderservice.model.vo;



import cn.edu.xmu.orderservice.model.bo.OrderItem;
import lombok.Data;

import java.io.Serializable;

@Data
public class OrderItemRetVo implements Serializable {
    private Long orderItemId;
    private Long customerId;

    public OrderItemRetVo(){};
}
