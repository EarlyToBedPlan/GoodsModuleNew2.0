package cn.edu.xmu.orderservice.model.bo;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class OrderItem implements Serializable {

    private Long id;

    private Long orderId;

    private Long goodsSkuId;

    private Integer quantity;

    private Long price;

    private Long discount;

    private String name;

    private Long couponId;

    private Long couponActivityId;

    private Long beSharedId;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    /**
     * 默认构造函数
     */
    public OrderItem() {

    }


}
