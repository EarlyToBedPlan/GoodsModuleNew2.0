package cn.edu.xmu.activity.model.vo;

import cn.edu.xmu.activity.model.bo.Coupon;
import cn.edu.xmu.ooad.model.VoObject;
import lombok.Data;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/3 16:43
 */
@Data
public class CouponSimpleRetVo implements VoObject {
    Long id;
    VoObject couponActivityRetSimpleVo;
    String name;
    String  couponSn;

    public CouponSimpleRetVo(Coupon coupon)
    {
        this.id=coupon.getId();
        this.name=coupon.getName();
        this.couponSn=coupon.getCouponSn();
        this.couponActivityRetSimpleVo=coupon.getCouponActivityRetSimpleVo();
    }

    @Override
    public Object createVo() {
        return null;
    }

    @Override
    public Object createSimpleVo() {
        return null;
    }
}
