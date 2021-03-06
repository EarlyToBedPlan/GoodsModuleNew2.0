package cn.edu.xmu.activity.model.vo;

import cn.edu.xmu.activity.model.po.GrouponPo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author LJP_3424
 * @create 2020-12-07 20:25
 */
@Data
public class NewGrouponVo {
    @NotNull
    private LocalDateTime beginTime;
    @NotNull
    private LocalDateTime endTime;
    @NotEmpty
    private String strategy;
    public GrouponPo createGrouponPo(){
        GrouponPo grouponPo = new GrouponPo();
        grouponPo.setStrategy(this.strategy);
        grouponPo.setBeginTime(this.beginTime);
        grouponPo.setEndTime(this.endTime);
        return grouponPo;
    }

}