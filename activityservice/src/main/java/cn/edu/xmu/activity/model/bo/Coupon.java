package cn.edu.xmu.activity.model.bo;

import cn.edu.xmu.activity.model.bo.CouponActivity;
import cn.edu.xmu.activity.model.po.CouponActivityPo;
import cn.edu.xmu.activity.model.po.CouponPo;
import cn.edu.xmu.activity.model.vo.CouponRetVo;
import cn.edu.xmu.activity.model.vo.CouponSimpleRetVo;
import cn.edu.xmu.ooad.model.VoObject;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/3 16:30
 */
@Data
public class Coupon implements VoObject {
    Long id;
    VoObject couponActivityRetSimpleVo;
    Long customerId;
    String name;
    String  couponSn;
    byte state;
    private Logger logger = LoggerFactory.getLogger(Coupon.class);
    public enum State {
        NOT_CLAIMED(0,"未领取"),
        CLAIMED(1,"已领取"),
        USED(2,"已使用"),
        INVALID(3,"已失效");
        private static final Map<Integer, State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        State(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static State getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
public Coupon()
{}

    public Coupon(CouponPo po, CouponActivityPo couponActivityPo)
    {
        this.state=po.getState();
        this.id=po.getId();
        this.customerId=po.getCustomerId();
        this.name=po.getName();
        this.couponSn=po.getCouponSn();
        CouponActivity couponActivity=new CouponActivity(couponActivityPo);
       this.couponActivityRetSimpleVo=couponActivity.createSimpleVo();
    }

    @Override
    public VoObject createVo() {
        return new CouponRetVo(this);
    }

    @Override
    public VoObject createSimpleVo() {
        return new CouponSimpleRetVo(this);
    }
}
