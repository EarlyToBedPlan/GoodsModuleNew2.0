package cn.edu.xmu.goods.controller;


import cn.edu.xmu.goods.model.bo.Comment;
import cn.edu.xmu.goods.model.vo.CommentConclusionVo;
import cn.edu.xmu.goods.model.vo.CommentVo;
import cn.edu.xmu.goods.model.vo.CommentVoBody;
import cn.edu.xmu.goods.model.vo.StateVo;
import cn.edu.xmu.goods.service.CommentService;
import cn.edu.xmu.ooad.annotation.Audit;
import cn.edu.xmu.ooad.annotation.Depart;
import cn.edu.xmu.ooad.annotation.LoginUser;
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
import java.util.List;

/**
 * @author Ruzhen Chang
 */

@Api(value = "评论服务",tags = "comment")
@RestController
@RequestMapping(value = "/comment",produces = "application/json;charset=UTF-8")

public class CommentController {


    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    private CommentService commentService;
    @Autowired
    private HttpServletResponse httpServletResponse;



    @ApiOperation(value = "买家新增sku的评论")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header",dataType = "String",name="authorization",value = "Token",required = true),
            @ApiImplicitParam(paramType = "path",dataType = "Integer",name="id",value = "订单明细id",required = true),
            @ApiImplicitParam(paramType = "body",dataType = "CommentVo",name="vo",value = "评价信息",required = true)
    })
    @ApiResponses({
            @ApiResponse(code=0,message = "成功"),
            @ApiResponse(code=903,message = "用户没有购买此商品")
    })
    @Audit
    @PostMapping("/orderitems/{id}/comments")
    public Object addComment(@PathVariable Long id,
                            @RequestBody CommentVoBody vo){
        if(vo.getType()!=1&&vo.getType()!=0&&vo.getType()!=2)
            return Common.decorateReturnObject(new ReturnObject(ResponseCode.FIELD_NOTVALID));
        Comment comment=new Comment(vo);
        comment.setOrderItemId(id);
        comment.setGmtCreate(LocalDateTime.now());
        ReturnObject returnObject = commentService.newGoodsSkuComment(comment);
        if(returnObject.equals(ResponseCode.OK))
            return new ResponseEntity(
                    ResponseUtil.ok(returnObject.getData()),
                    HttpStatus.CREATED);
        return Common.decorateReturnObject(returnObject);

    }


    /**
     * @description:查看sku的评论列表（已通过审核）（无需登录）
     * @author: Ruzhen Chang
     */
    @ApiOperation(value = "查看sku的评论列表（已通过审核）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name="page", required = false, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = false, dataType="Integer", paramType="query")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在"),
            @ApiResponse(code = 503, message = "字段不合法")
    })

    @GetMapping("/skus/{id}/comments")
    public Object getGoodsSkuCommentList(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize)
    {
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        ReturnObject<PageInfo<VoObject>> returnObject = commentService.getGoodsSkuCommentsList(id,page,pageSize);
        return Common.decorateReturnObject(returnObject);
    }


    @Audit
    @ApiOperation(value = "管理员审核评论")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "did", value = "店铺id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "Integer", name = "id", value = "评论id", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "CommentConclusionVo", name = "conclusion", value = "可修改的评论信息", required = true)    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @PutMapping("/shops/{did}/comments/{id}/confirm")
    public Object auditComment(@PathVariable Long did,
                               @PathVariable Long id,
                               @RequestBody CommentConclusionVo conclusion) {
        if(did.equals(0l)){
            Comment comment = new Comment();
            comment.setId(id);
            if(conclusion.getConclusion()==true){
                comment.setState(Comment.State.NORM);
            }
            else {
                comment.setState(Comment.State.FORBID);
            }
            ReturnObject returnObject = commentService.auditComment(comment);
           return Common.decorateReturnObject(returnObject);
        }
        else {
                return Common.getNullRetObj(new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE),httpServletResponse);
        }
    }


    /**
     * @description:买家查看自己的评价记录
     * @author: Ruzhen Chang
     */
    @ApiOperation(value = "买家查看自己的评价记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "用户token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name="page", required = false, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = false, dataType="Integer", paramType="query")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("/comments")
    public Object getUserCommentList(@LoginUser Long userId,
                                     @RequestParam(required = false) Integer page,
                                     @RequestParam(required = false) Integer pageSize
    ) {
        page = (page == null) ? 1 : page;
        pageSize = (pageSize == null) ? 10 : pageSize;
        ReturnObject<PageInfo<VoObject>> returnObject = commentService.getSelfCommentList(userId,page,pageSize);
        if (returnObject.getData() != null) {
            return ResponseUtil.ok(returnObject.getData());
        } else {
            return Common.getNullRetObj(new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg()), httpServletResponse);
        }
    }

    /**
     * @description:管理员查看未审核/已审核评论列表
     * @author: Ruzhen Chang
     */
    @ApiOperation(value = "管理员查看未审核/已审核评论列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "用户token", required = true, dataType = "string", paramType = "header"),
            @ApiImplicitParam(name="id",value = "店铺id",required = true,dataType = "Integer",paramType = "path"),
            @ApiImplicitParam(name="page", required = false, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = false, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="state", required = false, dataType="Integer", paramType="query")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504,message="操作id不存在")
    })
    @Audit
    @GetMapping("/shops/{id}/comments/all")
    public Object getCheckedAndUncheckComment(@Depart Long departId,
                                              @RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam(required = false) Integer state
    ) {
        if(departId.equals(0)){
            page = (page == null) ? 1 : page;
            pageSize = (pageSize == null) ? 10 : pageSize;
            ReturnObject<PageInfo<VoObject>> returnObject = commentService.getCommentListByState(state,page,pageSize);
            if (returnObject.getData() != null) {
                return ResponseUtil.ok(returnObject.getData());
            } else {
                return Common.getNullRetObj(new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg()), httpServletResponse);
            }
        }
        else {
            return ResponseCode.AUTH_NOT_ALLOW;
        }
    }


    /**
     * @description 获取评论的所有状态
     * @author Ruzhen Chang
     * @return
     */
    @ApiOperation(value = "获得评论的所有状态")
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @GetMapping("/comments/states")
    public  Object getCommentStates() {
        ReturnObject<List<StateVo>> returnObject = commentService.findCommentStates();
        return Common.decorateReturnObject(returnObject);
    }


}
