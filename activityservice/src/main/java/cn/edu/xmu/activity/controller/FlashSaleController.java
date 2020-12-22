package cn.edu.xmu.activity.controller;

import cn.edu.xmu.activity.model.bo.FlashSale;
import cn.edu.xmu.activity.model.vo.FlashSaleRetItemVo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleItemVo;
import cn.edu.xmu.activity.model.vo.NewFlashSaleVo;
import cn.edu.xmu.activity.service.FlashSaleService;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.Common;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ResponseUtil;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import org.apache.ibatis.annotations.Delete;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-03 16:43
 */
@Api(value = "秒杀活动", tags = "flashsale")
@RestController
@RequestMapping(value = "flashsale", produces = "application/json;charset=UTF-8")
public class FlashSaleController {
    private static final Logger logger = LoggerFactory.getLogger(FlashSaleController.class);

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private HttpServletResponse httpServletResponse;

    /**
     * @param id
     * @Description: 查询某一时段秒杀活动详情
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/6 0:59
     */
    @ApiOperation(value = "查询某一时段秒杀活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "商品id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "state", value = "活动所处阶段", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    //@Audit //认证
    @GetMapping(value = "/timesegments/{id}/flashsales")
    public Flux<FlashSaleRetItemVo> getFlashSale(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("FlashSaleInfo: timeSegmentId = " + id);
        }
        return flashSaleService.getFlashSale(id).map(x -> (FlashSaleRetItemVo) x.createVo());
    }


    @GetMapping("/test/test")
    public List<TimeSegmentVo> returnAllTimeSegmentTest(){
        return flashSaleService.returnAllTimeSegmentVo();
    }
    /**
     * @param id
     * @param vo
     * @param bindingResult
     * @Description: 新增秒杀活动
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/6 0:59
     */
    @ApiOperation(value = "新增秒杀活动", produces = "application/json;charset=UTF-8")
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @PostMapping("/shops/{did}/timesegments/{id}/flashsales")
    public Object insertFlashSale(
            @PathVariable Long did,
            @PathVariable Long id,
            @Validated @RequestBody NewFlashSaleVo vo,
            BindingResult bindingResult) {
        logger.debug("insert insertFlashSale by timeSegmentId:" + id + " and FlashSaleVo: " + vo.toString());
        if (bindingResult.hasErrors()) {
            httpServletResponse.setContentType("application/json;charset=utf-8");
            return Common.processFieldErrors(bindingResult, httpServletResponse);
        }
        LocalDateTime nowDateTime = LocalDateTime.now();
        if(vo.getFlashDate().compareTo(LocalDateTime.now()) < 0){
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        // 无法创建当天的秒杀
        if (vo.getFlashDate().compareTo(LocalDateTime.of(nowDateTime.getYear(), nowDateTime.getMonth(), nowDateTime.getDayOfMonth(), 23, 59, 59)) < 0) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject returnObject = flashSaleService.createNewFlashSale(vo, id);
        if (returnObject.getCode().equals(ResponseCode.OK)) {
            return Common.getRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }

    /**
     * @Description: 获取当前时间段秒杀活动详情
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/6 1:00
     */
    @ApiOperation(value = "获取当前时间段秒杀活动详情")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "商品id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "state", value = "活动所处阶段", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    // produces=MediaType.APPLICATION_STREAM_JSON_VALUE 使得返回的数据不会被[   ,    ]包裹起来
    @GetMapping(value = "/flashsales/current")
    public Flux<FlashSaleRetItemVo> getFlashSale() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return flashSaleService.getCurrentFlashSale(localDateTime).map(x -> (FlashSaleRetItemVo) x.createVo());
    }
    /**
     * public Flux<FlashSaleRetItemVo> getFlashSale(@PathVariable Long id) {
     *         if (logger.isDebugEnabled()) {
     *             logger.debug("FlashSaleInfo: timeSegmentId = " + id);
     *         }
     *         return flashSaleService.getFlashSale(id).map(x -> (FlashSaleRetItemVo) x.createVo());
     *     }
     */

    /**
     * @param id
     * @param vo
     * @param bindingResult
     * @Description: 修改秒杀活动信息
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/6 1:00
     */
    @ApiOperation(value = "修改秒杀活动", produces = "application/json;charset=UTF-8")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "id", value = "秒杀活动Id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "NewFlashSaleVo", name = "vo", value = "可修改的活动信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}")
    public Object updateFlashSale(
            @PathVariable Long did,
            @PathVariable Long id,
            @Validated @RequestBody NewFlashSaleVo vo,
            BindingResult bindingResult) {
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }
        LocalDateTime nowDateTime = LocalDateTime.now();
        if(vo.getFlashDate().compareTo(LocalDateTime.now()) < 0){
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        // 无法修改秒杀为当天
        if (vo.getFlashDate().compareTo(LocalDateTime.of(nowDateTime.getYear(), nowDateTime.getMonth(), nowDateTime.getDayOfMonth(), 23, 59, 59)) < 0) {
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        }
        ReturnObject retObject = flashSaleService.updateFlashSale(vo, id);
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.fail(retObject.getCode());
        }
    }

    /**
     * @param id
     * @param vo
     * @param bindingResult
     * @Description: 向秒杀活动添加SKU
     * @return: java.lang.Object
     * @Author: LJP_3424
     * @Date: 2020/12/6 1:00
     */
    @ApiOperation(value = "秒杀活动添加SKU", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "shopId", value = "商铺id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "id", value = "商品SPUId", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(paramType = "body", dataType = "NewFlashSaleVo", name = "vo", value = "可修改的活动信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("/shops/{did}/flashsales/{id}/flashitems")
    public Object insertNewFlashSaleItem(
            @PathVariable Long did,
            @PathVariable Long id,
            @Validated @RequestBody NewFlashSaleItemVo vo,
            BindingResult bindingResult) {
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }
        ReturnObject retObject = flashSaleService.insertSkuIntoPreSale(did,vo, id);
        if (retObject.getCode().equals(ResponseCode.OK)) {
            return new ResponseEntity(
                    ResponseUtil.ok(retObject.getData()),
                    HttpStatus.CREATED);
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }

    @ApiOperation(value = "去掉秒杀中的sku")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "fid", value = "秒杀活动Id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "id", value = "秒杀活动Item的Id", required = true, dataType = "Integer")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @DeleteMapping("/shops/{did}/flashsales/{fid}/flashitems/{id}")
    public Object deleteSkuFromFlashSale(
            @PathVariable(required = true) Long did,
            @PathVariable(required = true) Long fid,
            @PathVariable(required = true) Long id) {
        ReturnObject retObject = flashSaleService.deleteSkuFromFlashSale(fid, id);
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.fail(retObject.getCode());
        }
    }

    @ApiOperation(value = "删除秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "秒杀活动Id", required = true, dataType = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
    @DeleteMapping("/shops/{did}/flashsales/{id}")
    public Object deleteFlashSale(
            @PathVariable(required = true) Long did,
            @PathVariable(required = true) Long id
    ) {
        ReturnObject retObject = flashSaleService.changeFlashSaleStatus(id, FlashSale.State.DELETE.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return Common.decorateReturnObject(retObject);
        }
    }


    @ApiOperation(value = "上线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "秒杀活动Id", required = true, dataType = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}/onshelves")
    public Object onShelves(
            @PathVariable(required = true) Long did,
            @PathVariable(required = true) Long id
    ) {
        ReturnObject retObject = flashSaleService.changeFlashSaleStatus(id, FlashSale.State.ON.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.fail(retObject.getCode());
        }
    }

    @ApiOperation(value = "下线秒杀活动")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "秒杀活动Id", required = true, dataType = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit
    @PutMapping("/shops/{did}/flashsales/{id}/offshelves")
    public Object offShelves(
            @PathVariable(required = true) Long did,
            @PathVariable(required = true) Long id
    ) {
        ReturnObject retObject = flashSaleService.changeFlashSaleStatus(id, FlashSale.State.OFF.getCode());
        if (retObject.getCode() == ResponseCode.OK) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.fail(retObject.getCode());
        }
    }


}
