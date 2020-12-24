package cn.edu.xmu.activity.model.vo;


import cn.edu.xmu.activity.model.po.PreSalePo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * @author LJP_3424
 * @create 2020-12-02 15:44
 */
@Data
public class NewPreSaleVo {
    private String name;
    @NotNull
    private LocalDateTime beginTime;
    @NotNull
    private LocalDateTime payTime;
    @NotNull
    private LocalDateTime endTime;
    @Min(value = 0,message = "为自然数")
    private Integer quantity;
    @Min(value = 0,message = "不应小于0")
    private Long advancePayPrice;
    @Min(value = 0,message = "不应小于0")
    private Long restPayPrice;

    public PreSalePo createPreSalePo(){
        PreSalePo preSalePo = new PreSalePo();
        preSalePo.setName(this.name);
        preSalePo.setAdvancePayPrice(this.advancePayPrice);
        preSalePo.setRestPayPrice(this.restPayPrice);
        preSalePo.setQuantity(this.quantity);
        preSalePo.setBeginTime(this.beginTime);
        preSalePo.setPayTime(this.payTime);
        preSalePo.setEndTime(this.endTime);
        return preSalePo;
    }
}