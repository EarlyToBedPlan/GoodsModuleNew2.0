package cn.edu.xmu.activity.controller;


import cn.edu.xmu.activity.model.bo.CouponActivity;
import cn.edu.xmu.activity.model.vo.CouponActivitySimpleVo;
import cn.edu.xmu.activity.model.vo.CouponActivityVo;
import cn.edu.xmu.activity.model.vo.AdminVo;
import cn.edu.xmu.activity.service.CouponActivityService;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.vo.ShopVo;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.privilegeservice.client.IUserService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Feiyan Liu
 * @date Created at 2020/11/29 11:59
 */
@Api(value = "优惠服务", tags = "coupon")
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/coupon", produces = "application/json;charset=UTF-8")
public class CouponController {
    private static final Logger logger = LoggerFactory.getLogger(CouponController.class);
    @Autowired
    private CouponActivityService couponActivityService;
    @Autowired
    private HttpServletResponse httpServletResponse;
    @DubboReference(check=false)
    IUserService userService;
    @DubboReference(check=false,version = "2.7.8")
    IGoodsService goodsService;

    public enum Timeline {
        WAITING(0, "待上线"),
        TOMORROW_ONLINE(1, "明天开始"),
        ONLINE(2,"正在进行"),
        OFFLINE(3,"结束下线");
        private static final Map<Integer, Timeline> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (Timeline enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }
        private int code;
        private String description;
        Timeline(int code, String description) {
            this.code = code;
            this.description = description;
        }
        public static Timeline getTypeByTime(LocalDateTime beginTime,LocalDateTime endTime)
        {
            if(beginTime.isAfter(LocalDateTime.now()))
                return Timeline.WAITING;
            else if(LocalDateTime.now().minusDays(1).toLocalDate()==beginTime.toLocalDate())
                return Timeline.TOMORROW_ONLINE;
            else if(beginTime.isBefore(LocalDateTime.now())&&endTime.isAfter(LocalDateTime.now()))
                return Timeline.ONLINE;
            else
                return Timeline.OFFLINE;
        }
        public static LocalDateTime getBeginTimeByCode(Integer code)
        {
            //如果是待上线 则开始时间必然比当前时间晚
            if(code==Timeline.WAITING.getCode())
                return LocalDateTime.now();
            //如果是明天上线 则开始时间比当前时间+1天来得早，但又比当前时间来得晚
            else if(code==Timeline.TOMORROW_ONLINE.getCode())
                return LocalDateTime.now().plusDays(1);
            //如果是在线的活动 则开始时间比当前时间早 结束时间比当前时间晚
            else if(code==Timeline.ONLINE.getCode())
                return LocalDateTime.now();
            //如果是下线的活动 只要求结束时间比当前时间晚
            else
                return null;
        }
        public static LocalDateTime getEndTimeByCode(Integer code)
        {
            //如果是待上线 对结束时间没要求
            if(code==Timeline.WAITING.getCode())
                return null;
            //如果是明天上线 对结束时间也没要求
            else if(code==Timeline.TOMORROW_ONLINE.getCode())
                return null;
            //如果是在线的活动  结束时间比当前时间晚
            else if(code==Timeline.ONLINE.getCode())
                return LocalDateTime.now();
            else //如果是下线的活动 只要求结束时间比当前时间晚
            return LocalDateTime.now();
        }

        public static Timeline getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    @ApiOperation(value = "获取优惠券的所有状态")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    //@Audit
    @GetMapping("/coupons/states")
    public Object getCouponAllState() {
        ReturnObject returnObject = couponActivityService.getCouponAllState();
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员新建己方优惠活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "CouponActivityVo", name = "vo", value = "优惠活动信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("/shops/{shopId}/couponactivities")
    public Object addCouponActivity(@RequestBody CouponActivityVo vo,
                                    @LoginUser Long id, @PathVariable Long shopId) {
//        CustomerVo customerVo=new CustomerVo();
//        customerVo.setId(id);
//        customerVo.setUserName(otherService.getUserById(id).getUserName());
        AdminVo adminVo =new AdminVo();
        adminVo.setId(id);
        adminVo.setName(userService.getUserName(id));
        CouponActivity couponActivity = vo.createCouponActivity();
        couponActivity.setCreatedBy(adminVo);
        ReturnObject returnObject = couponActivityService.createCouponActivity(shopId,couponActivity);
        if(returnObject.getCode()==ResponseCode.OK)
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "上传优惠活动图片",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "formData", dataType = "file", name = "img", value ="文件", required = true),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "shopId",value = "店铺id"),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "id",value = "spuid"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 506, message = "该目录文件夹没有写入的权限"),
            @ApiResponse(code = 508, message = "图片格式不正确"),
            @ApiResponse(code = 509, message = "图片大小超限")
    })
    @Audit
    @PostMapping("/shops/{shopId}/couponactivities/{id}/uploadImg")
    public Object uploadImg(@RequestParam("img") MultipartFile multipartFile,
                            @PathVariable("shopId") Long shopId,
                            @PathVariable("id") Long id){

        if(couponActivityService.checkCouponActivityShop(shopId,id)==false)
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE));
        ReturnObject returnObject = couponActivityService.uploadImg(id, multipartFile);
        if(returnObject.getCode()==ResponseCode.OK)
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "查看上线的优惠活动列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "timeline", value = "活动时间", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页码",  dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页数目", dataType = "Integer")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @GetMapping("/couponactivities")
    public Object getOnlineCouponActivity(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize,
                                          @RequestParam(required = false) Long shopId, @RequestParam(required = false) Integer timeline) {
        //校验timeline
        if(!(timeline==null||timeline==0||timeline==1||timeline==2||timeline==3))
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        LocalDateTime beginTime=null;
        LocalDateTime endTime=null;
        if(timeline!=null)
        {
            beginTime= Timeline.getBeginTimeByCode(timeline);
            endTime= Timeline.getEndTimeByCode(timeline);
        }
        ReturnObject<PageInfo<VoObject>> returnObject = couponActivityService.getOnlineCouponActivities(shopId, beginTime,endTime, page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    @ApiOperation(value = "查看本店下线的优惠活动列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页码", dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页数目", dataType = "Integer")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @GetMapping("/shops/{id}/couponactivities/invalid")
    public Object getInvalidCouponActivity(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize,
                                           @PathVariable Long id,@Depart Long shopId) {
        if(shopId!=id)
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE),httpServletResponse);
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        ReturnObject<PageInfo<VoObject>> returnObject = couponActivityService.getInvalidCouponActivities(page, pageSize,id);
        return Common.getPageRetObject(returnObject);
    }

    @ApiOperation(value = "查看优惠活动中的商品")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "活动id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页码", dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页数目", dataType = "Integer")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @GetMapping("/couponactivities/{id}/skus")
    public Object getCouponSku(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize,
                               @PathVariable Long id) {
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        ReturnObject<PageInfo<VoObject>> returnObject = couponActivityService.getCouponSku(id, page, pageSize);
        return Common.decorateReturnObject(returnObject);
    }
    @ApiOperation(value = "查看优惠活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠商品id", required = true, dataType = "Integer", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @GetMapping("/shops/{shopId}/couponactivities/{id}")
    public Object getCouponActivity(@PathVariable("shopId") Long shopId, @PathVariable("id") Long id, @Depart Long departId) {
        ReturnObject returnObject = couponActivityService.getCouponActivityById(id,shopId);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员修改己方优惠活动,下线状态才能修改")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠活动id", required = true, dataType = "Integer", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PutMapping("/shops/{shopId}/couponactivities/{id}")
    public Object updateCouponActivity(@PathVariable Long shopId, @PathVariable Long id,
                                       @Validated @RequestBody@NotNull CouponActivitySimpleVo vo,@LoginUser Long userId) {
        CouponActivity couponActivity = vo.createCouponActivity();
        AdminVo adminVo =new AdminVo();
        adminVo.setId(userId);
       // adminVo.setName(userService.getUserName(userId));
        couponActivity.setModifiedBy(adminVo);
        if(couponActivity.getBeginTime().isAfter(couponActivity.getEndTime()))
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        couponActivity.setId(id);
        couponActivity.setGmtModified(LocalDateTime.now());
//        ShopVo shopVo= goodsService.getShopById(id);
//        couponActivity.setShop(shopVo);
        couponActivity.setShopId(shopId);
        ReturnObject returnObject = couponActivityService.updateCouponActivity(couponActivity);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员删除己方优惠活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠活动id", required = true, dataType = "Integer", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @DeleteMapping("/shops/{shopId}/couponactivities/{id}")
    public Object deleteCouponActivity( @PathVariable Long id, @Depart Long departId) {
         ReturnObject returnObject = couponActivityService.deleteCouponActivity(departId,id);
        return Common.decorateReturnObject(returnObject);
        }

    @ApiOperation(value = "管理员为己方优惠活动新增限定范围")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠活动id", required = true, dataType = "Array", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PostMapping("/shops/{shopId}/couponactivities/{id}/skus")
    public Object addCouponSku(@PathVariable Long shopId, @PathVariable Long id, @Depart Long departId,
                               @Validated @RequestBody @NotBlank Long[] skuArray) {

            ReturnObject returnObject = couponActivityService.addCouponSku(shopId, skuArray, id);
        if(returnObject.getCode()==ResponseCode.OK)
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);
    }

    @ApiOperation(value = "管理员删除己方优惠活动的某限定范围")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠活动id", required = true, dataType = "Integer", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @DeleteMapping("/shops/{shopId}/couponskus/{id}")
    public Object deleteCouponSku(@PathVariable Long shopId, @PathVariable Long id, @Depart Long departId) {

         ReturnObject returnObject = couponActivityService.deleteCouponSku(id,shopId);
        return Common.decorateReturnObject(returnObject);
         }

    @ApiOperation(value = "买家查看优惠券列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "优惠券状态", value = "state", required = true),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页码", dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页数目", dataType = "Integer")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("/coupons")
    public Object getCouponByUserId(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize, @RequestParam(required = false) Integer state,
                                    @LoginUser Long id) {
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        ReturnObject<PageInfo<VoObject>> returnObject = couponActivityService.getCouponByUserId(id, state, page, pageSize);
        return Common.getPageRetObject(returnObject);
    }
//    内部API
//    @ApiOperation(value = "买家使用优惠券")
//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
//            @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "优惠券id", value = "state", required = true),
//    })
//    @ApiResponses({
//            @ApiResponse(code = 0, message = "成功"),
//    })
//    @Audit
//    @PutMapping("/coupons/{id}")
//    public Object useCoupon(@PathVariable Long id, @LoginUser Long userId) {
//
//        ReturnObject returnObject = couponActivityService.useCoupon(id,userId);
//        if (returnObject.getData() != null)
//            return ResponseUtil.ok(returnObject.getData());
//        else {
//            return Common.getNullRetObj(new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg()), httpServletResponse);
//        }
//    }
    @ApiOperation(value = "买家领取优惠券")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "优惠活动id", required = true, dataType = "Array", paramType = "path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PostMapping("/couponactivities/{id}/usercoupons")
    public Object userGetCoupon(@PathVariable Long id, @LoginUser Long userId) {
        ReturnObject returnObject = couponActivityService.getCoupon(userId,id);
        if(returnObject.getCode()==ResponseCode.OK)
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);

    }




    @ApiOperation(value = "上线优惠活动",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "shopId",value = "店铺id"),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "id",value = "活动id"),
    })
    @ApiResponses({
    })
   @Audit
    @PutMapping("/shops/{shopId}/couponactivities/{id}/onshelves")
    public Object putCouponActivityOnShelves(@PathVariable("shopId") Long shopId,
                            @PathVariable("id") Long id){

        ReturnObject returnObject = couponActivityService.putCouponActivityOnShelves(shopId, id);
        return Common.decorateReturnObject(returnObject);
    }


    @ApiOperation(value = "下线优惠活动",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "shopId",value = "店铺id"),
            @ApiImplicitParam(paramType = "path",dataType = "Long",name = "id",value = "活动id"),
    })
    @ApiResponses({
    })
    @Audit
    @PutMapping("/shops/{shopId}/couponactivities/{id}/offshelves")
    public Object putCouponActivityOffShelves(@PathVariable("shopId") Long shopId,
                            @PathVariable("id") Long id){
        ReturnObject returnObject = couponActivityService.putCouponActivityOffShelves(shopId, id);
        return Common.decorateReturnObject(returnObject);
    }


}
