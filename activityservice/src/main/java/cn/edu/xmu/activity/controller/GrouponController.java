package cn.edu.xmu.activity.controller;

import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.vo.GrouponStateVo;
import cn.edu.xmu.activity.model.vo.NewGrouponVo;
import cn.edu.xmu.activity.service.GrouponService;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageInfo;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 团购服务
 *
 * @author LJP_3424
 * @create 2020-11-30 01:06
 */
@Api(value = "团购活动", tags = "groupon")
@RestController
@RequestMapping(value = "groupon", produces = "application/json;charset=UTF-8")
public class GrouponController {
    private static final Logger logger = LoggerFactory.getLogger(GrouponController.class);

    @Autowired
    private GrouponService grouponService;

    @Autowired
    private HttpServletResponse httpServletResponse;


    /**
     * 查询团购活动所有状态
     *
     * @return Object
     * createdBy: LJP_3424
     */
    @ApiOperation(value = "获得团购活动所有状态")
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("/groupons/states")
    public Object getAllGrouponsStates() {
        Groupon.State[] states = Groupon.State.class.getEnumConstants();
        List<GrouponStateVo> grouponStateVos = new ArrayList<GrouponStateVo>();
        for (int i = 0; i < states.length; i++) {
            grouponStateVos.add(new GrouponStateVo(states[i]));
        }
        return ResponseUtil.ok(new ReturnObject<List>(grouponStateVos).getData());
    }


    /**
     * @param shopId   店铺id
     * @param timeline 时间
     * @param spuId    商品ID
     * @param page     页码
     * @param pageSize 页面大小
     * @description:查看所有团购活动(条件筛选)
     * @return: Object
     * @author: LJP_3424
     */
    @ApiOperation(value = "筛选查询所有团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "shopId", value = "店铺id", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "timeline", value = "时间", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "spuId", value = "商品ID", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "page", value = "页码", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "Integer")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("/groupons")
    public Object selectAllGroupon(
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Byte timeline,
            @RequestParam(required = false) Long spuId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        logger.debug("selectAllGroupon: shopId = " + shopId + " timeline = " + timeline + "spuId = " + spuId + " page = " + page + "  pageSize =" + pageSize);
        //校验timeline
        if (!(timeline == null || timeline == 0 || timeline == 1 || timeline == 2 || timeline == 3))
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        ReturnObject<PageInfo<VoObject>> returnObject = grouponService.selectAllGroupon(shopId, timeline, spuId, page, pageSize);
        if (returnObject.getCode().equals(ResponseCode.OK)) {
            return Common.getPageRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }


    @ApiOperation(value = "筛选查询所有团购,包括下线的")

    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("/shops/{id}/groupons")
    public Object selectAllGroupon(
            @PathVariable Long id,
            @RequestParam(required = false) Byte state,
            @RequestParam(required = false) Long spuId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        if (startTime == null) {
            startTime = LocalDateTime.of(1900,1,1,0,0,0);
        }
        if (endTime == null) {
            endTime = LocalDateTime.of(2999,1,1,0,0,0);
        }
        ReturnObject<PageInfo<VoObject>> returnObject = grouponService.selectGroupon(id, state, spuId,
                startTime, endTime, page, pageSize);
        if (returnObject.getCode().equals(ResponseCode.OK)) {
            return Common.getPageRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }

    /**
     * @param state 活动状态
     * @description: 查看指定商品的历史团购活动
     * @return: java.lang.Object
     * @author: LJP_3424
     */
    @ApiOperation(value = "查询指定商品的历史团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "商品id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "state", value = "活动所处阶段", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit //认证
    @GetMapping("/shops/{shopId}/spus/{id}/groupons")
    public Object getGroupon(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @RequestParam(required = false) Byte state) {
        if (logger.isDebugEnabled()) {
            logger.debug("GrouponInfo: spuId = " + id + " shopId = " + shopId);
        }

        ReturnObject<List> returnObject = grouponService.getGrouponBySpuId(id, state);

        if (returnObject.getCode().equals(ResponseCode.OK)) {
            return Common.getListRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }


    /**
     * 新增团购活动
     *
     * @param vo 活动视图
     * @author LJP_3424
     */
    @ApiOperation(value = "新增团购活动", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "商铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "商品SPUId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "NewGrouponVo", name = "vo", value = "可修改的活动信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
    @PostMapping("/shops/{shopId}/spus/{id}/groupons")
    public Object insertGroupon(
            @PathVariable Long shopId,
            @PathVariable Long id,
            @Validated @RequestBody NewGrouponVo vo,
            @Depart Long departId,
            BindingResult bindingResult) {
        logger.debug("insert insertGroupon by shopId:" + shopId + " and spuId: " + id + " and GrouponVo: " + vo.toString());
        // 校前端数据
        httpServletResponse.setContentType("application/json;charset=utf-8");
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if(vo.getBeginTime().compareTo(LocalDateTime.now()) < 0){
            return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.FIELD_NOTVALID));
        }
        if (vo.getBeginTime().compareTo(vo.getEndTime()) > 0) {
            return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject retObject = grouponService.createNewGroupon(vo, shopId, id);
        if (retObject.getCode().equals(ResponseCode.OK)) {
            return new ResponseEntity(
                    ResponseUtil.ok(retObject.getData()),
                    HttpStatus.CREATED);
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }


    @ApiOperation(value = "修改团购活动信息", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "商铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "优惠活动Id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "NewGrouponVo", name = "vo", value = "可修改的活动信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
    @PutMapping("/shops/{shopId}/groupons/{id}")
    public Object updateGroupon(@PathVariable Long shopId,
                                @PathVariable Long id,
                                @Validated @RequestBody NewGrouponVo vo,
                                BindingResult bindingResult) {
        // 校前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        if(vo.getBeginTime().compareTo(LocalDateTime.now()) < 0){
            return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.FIELD_NOTVALID));
        }
        if (vo.getBeginTime().compareTo(vo.getEndTime()) > 0) {
            return Common.decorateReturnObject(new ReturnObject<>(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject retObject = grouponService.updateGroupon(vo, shopId, id);
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }

    /**
     * @param id
     * @param shopId
     * @Description:
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/15 16:55
     */
    @ApiOperation(value = "删除优惠活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 906, message = "优惠活动禁止")
    })
    @Audit // 需要认证
    @DeleteMapping("/shops/{shopId}/groupons/{id}")
    public Object deleteGroupon(@PathVariable Long id, @PathVariable Long shopId) {

        ReturnObject retObject = grouponService.changeGrouponState(shopId, id, Groupon.State.DELETE.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }



    @GetMapping("test/test")
    public Object godsServiceTest(){
        return Common.decorateReturnObject(grouponService.goodsGetTest());
    }

    /**
     * 上线团购活动
     */
    @ApiOperation(value = "上线团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 906, message = "优惠活动禁止")
    })
    @Audit // 需要认证
    @PutMapping("/shops/{shopId}/groupons/{id}/onshelves")
    public Object grouponOn(@PathVariable Long id, @PathVariable Long shopId) {
        ReturnObject retObject = grouponService.changeGrouponState(shopId, id, Groupon.State.ON.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }


    /**
     * 下线团购活动
     */
    @ApiOperation(value = "下线团购活动")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "Token", required = true, dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "shopId", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 906, message = "优惠活动禁止")
    })
    @Audit // 需要认证
    @PutMapping("/shops/{shopId}/groupons/{id}/offshelves")
    public Object grouponOFF(@PathVariable Long id, @PathVariable Long shopId) {
        ReturnObject retObject = grouponService.changeGrouponState(shopId, id, Groupon.State.OFF.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }

}
