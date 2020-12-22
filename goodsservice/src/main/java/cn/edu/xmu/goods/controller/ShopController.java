package cn.edu.xmu.goods.controller;

import cn.edu.xmu.goods.model.bo.Shop;
import cn.edu.xmu.goods.model.vo.CommentConclusionVo;
import cn.edu.xmu.goods.service.ShopService;
import cn.edu.xmu.goods.model.vo.ShopVoBody;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

/**
* @author Ruzhen Chang
 */

@Api(value = "店铺",tags = "shop")
@RestController
@RequestMapping(value = "/shop",produces = "application/json;charset=UTF-8")

public class ShopController {
    private static final Logger logger =LoggerFactory.getLogger(ShopController.class);

    @Autowired
    private ShopService shopService;
    @Autowired
    private HttpServletResponse httpServletResponse;

    @ApiOperation(value = "店家申请店铺")
    @ApiImplicitParams({
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("/shops/states")
    public Object getShopState() {
        return Common.decorateReturnObject(shopService.getShopStates());
    }


    /**
     * 店家申请店铺
     * @author Ruzhen Chang
     */
    @ApiOperation(value = "店家申请店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "ShopVo", name = "vo", value = "店铺信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("/shops")
    public Object insertShop(@Validated @RequestBody ShopVoBody shopVoBody,
                             BindingResult bindingResult,
                             @PathVariable Long id,
                             @Depart Long departId) {
        Object errors = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != errors) {
            return errors;
        }
        if(departId != -1)
            return ResponseCode.USER_HASSHOP;
        Shop shop=new Shop(shopVoBody);
        shop.setGmtCreate(LocalDateTime.now());
        shop.setGmtModified(LocalDateTime.now());
        ReturnObject returnObject = shopService.createShop(shop);
        if(returnObject.equals(ResponseCode.OK))
            return new ResponseEntity(
                    ResponseUtil.fail(returnObject.getCode(), returnObject.getErrmsg()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);
    }




    @ApiOperation(value = "店家修改店铺信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopSimpleVo", value = "店铺信息", required = true, dataType = "shopSimpleVo", paramType = "body")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PutMapping("/shops/{shopId}")
    public Object updateShop(@PathVariable Long shopId,
                             @Depart Long departId,
                             @Validated @RequestBody ShopVoBody vo,
                             BindingResult bindingResult) {
        Object errors = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != errors) {
            return errors;
        }
        if(departId.equals(shopId) || departId == 0l){
            Shop shop = new Shop(vo);
            shop.setId(shopId);
            ReturnObject returnObject = shopService.updateShop(shop);
            return Common.decorateReturnObject(returnObject);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE), httpServletResponse);
        }

    }




    @ApiOperation(value = "管理员或店家关闭店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @DeleteMapping("/shops/{id}")
    public Object userCloseShop(@PathVariable Long shopId,
                                @Depart Long departId) {
        if (shopId.equals(departId)||departId.equals(0l)) {
            Shop shop=new Shop();
            shop.setId(shopId);
            if(shop.getState().equals(Shop.State.ONLINE)){
                shop.setState((byte)Shop.State.DELETE.getCode());
            }
            else{
                return ResponseCode.SHOP_STATENOTALLOW;
            }
            ReturnObject returnObject = shopService.closeShop(shopId);
            return Common.decorateReturnObject(returnObject);
        }
        else {
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE),httpServletResponse);
        }
    }



    @ApiOperation(value = "平台管理员审核店铺信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "部门id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "Boolean", name = "conclusion", value = "审核信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PutMapping("/shops/{shopId}/newshops/{id}/audit")
    public Object userAuditShop(@PathVariable Long id,
                                @PathVariable Long shopId,
                                @RequestBody CommentConclusionVo conclusion,
                                @Depart Long deptId) {
        if (id.equals(0l)) {
            Shop shop=new Shop();
            shop.setId(shopId);
            if(shop.getState().equals(Shop.State.UNAUDITED)){
                if(conclusion.getConclusion()==true){
                    shop.setState((byte)Shop.State.OFFLINE.getCode());
                }
                else {
                    shop.setState((byte)Shop.State.FAILED.getCode());
                }
            }
            else{
                return ResponseCode.SHOP_STATENOTALLOW;
            }
            ReturnObject returnObject = shopService.auditShop(shop);
            return Common.decorateReturnObject(returnObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE),httpServletResponse);
        }
    }




    @ApiOperation(value = "管理员上线店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PutMapping("/shops/{id}/onshelves")
    public Object userOnShelvesShop(@PathVariable Long shopId,
                                    @Depart Long departId) {

        if (departId.equals(0l)) {
            Shop shop=new Shop();
            shop.setId(shopId);
//            if(shop.getState().equals(Shop.State.OFFLINE)){
//                shop.setState((byte)Shop.State.ONLINE.getCode());
//            }
//            else{
//                return ResponseCode.SHOP_STATENOTALLOW;
//            }
            ReturnObject returnObject = shopService.onShelvesShop(shop);
            return Common.decorateReturnObject(returnObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE),httpServletResponse);
        }
    }



    @ApiOperation(value = "管理员下线店铺")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit
    @PutMapping("/shops/{id}/offshelves")
    public Object userOffShelvesShop(@PathVariable Long shopId, @Depart Long departId) {
        if (departId.equals(0l)) {
            Shop shop = new Shop();
            shop.setId(shopId);
//            if (shop.getState().equals(Shop.State.ONLINE)) {
//                shop.setState((byte) Shop.State.OFFLINE.getCode());
//            } else {
//                return ResponseCode.SHOP_STATENOTALLOW;
//            }
            ReturnObject returnObject = shopService.offShelvesShop(shop);
            return Common.decorateReturnObject(returnObject);
        } else {
            return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE), httpServletResponse);
        }
    }
}
