package cn.edu.xmu.otherservice.model.vo;

import cn.edu.xmu.otherservice.model.po.CustomerPo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerVo implements Serializable {

    Long id;
    String userName;
    String realName;
    String mobile;
    String email;
    String gender;
    LocalDateTime birthday;
    LocalDateTime gmt_created;
    LocalDateTime gmt_modified;

    public CustomerVo(){}


    public CustomerVo(CustomerPo customerPo){
        this.id = customerPo.getId();
        this.userName = customerPo.getUserName();
        this.realName = customerPo.getRealName();
        this.email = customerPo.getEmail();
        this.mobile = customerPo.getMobile();
        this.gender = customerPo.getGender().intValue()>0?"男":"女";
        this.birthday = customerPo.getBirthday();
        this.gmt_modified = customerPo.getGmtModified();
        this.gmt_created = customerPo.getGmtCreated();
    }


}