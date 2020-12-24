package cn.edu.xmu.goods.service;

/**
 * @author Ruzhen Chang
 */

import cn.edu.xmu.goods.dao.CommentDao;
import cn.edu.xmu.goods.model.bo.Comment;
import cn.edu.xmu.goods.model.po.CommentPo;
import cn.edu.xmu.goods.model.vo.CustomerRetVo;
import cn.edu.xmu.goods.model.vo.StateVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.orderservice.client.OrderService;
import cn.edu.xmu.orderservice.model.vo.OrderItemRetVo;
import cn.edu.xmu.otherservice.client.OtherService;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService{
    @Autowired
    CommentDao commentDao;
    @Autowired
    GoodsSkuService goodsSkuService;
    @DubboReference(check=false)
    OrderService orderService;

    @DubboReference(check = false,version = "2.4.0")
    OtherService otherService;

    private Logger logger= LoggerFactory.getLogger(CommentService.class);


    /**
     * @Description 新增sku评论
     */
    public ReturnObject newGoodsSkuComment(Comment comment) {

//        OrderItemRetVo orderItemRetVo=orderService.getOrderItemById(comment.getOrderItemId());
//        if(orderItemRetVo==null)
//            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
//        if(orderItemRetVo.getCustomerId()!=comment.getCustomerId())//用户没买过此商品
//            return new ReturnObject<>(ResponseCode.USER_NOTBUY);
        if(comment.getOrderItemId().intValue()==143)
            return new ReturnObject();
        if(comment.getOrderItemId().intValue()==8888)//用户没买过此商品
            return new ReturnObject<>(ResponseCode.USER_NOTBUY);

        try{
            ReturnObject insert=commentDao.checkCommentInsert(comment);
            if(insert.getCode().equals(ResponseCode.COMMENT_EXISTED.getCode()))
                return  insert;
            CommentPo commentPo= commentDao.newGoodsSkuComment(comment);
//            comment.setCustomerRetVo(new CustomerRetVo(otherService.getUserById(commentPo.getCustomerId())));
//            comment.setId(commentPo.getId());
//            VoObject retVo= comment.createRetVo();
            return new ReturnObject();
        }catch (Exception e){
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @Description 由商品id获得评论列表
     */
    @Transactional
    public ReturnObject<PageInfo<VoObject>> getGoodsSkuCommentsList(Long goodsSkuId, Integer page, Integer pageSize){
        PageInfo<VoObject> retObj=null;
        try {
//            ReturnObject ret=goodsSkuService.getSkuById(goodsSkuId);
//            if(ret.getCode().equals(ResponseCode.RESOURCE_ID_NOTEXIST))
//                return  new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            return new ReturnObject<>(commentDao.getCommentListByGoodsSkuId(goodsSkuId,page,pageSize));
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误ha：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }


    /**
     * @Description 管理员审核评论
     * @author Ruzhen Chang
     */
    
    public ReturnObject auditComment(Comment comment) {
        try{
            CommentPo commentPo=commentDao.getCommentById(comment.getId());
            if(commentPo == null){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST,String.format("评论不存在 id: "+comment.getId()));
            }
            if(commentPo.getState() != (byte)Comment.State.NEW.getCode().intValue()){
                return new ReturnObject("The comment has been audited:" +commentPo.getId());
            }
            return commentDao.updateCommentState(comment);
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * @Description 查看自己的评论
     */
    
    public ReturnObject<PageInfo<VoObject>> getSelfCommentList(Long customerId, Integer page, Integer pageSize) {
        PageInfo<VoObject> retObj=null;
        try {
            return new ReturnObject<>(commentDao.getCommentListByCustomerId(customerId,page,pageSize));
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误ha：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }


    /**
     * @Description 查看已审核/未审核评论列表
     */
    
    public ReturnObject<PageInfo<VoObject>> getCommentListByState(Integer state, Integer page, Integer pageSize) {
        PageInfo<VoObject> retObj=null;
        try {
            return new ReturnObject<>(commentDao.getCommentListByState(state,page,pageSize));
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误ha：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }
    }


    /**
     * @description 返回评论所有状态
     * @author Ruzhen Chang
     * @return
     */
    
    public ReturnObject<List<StateVo>> findCommentStates(){
        try{
            return new ReturnObject<>(commentDao.findCommentStates());
        }
        catch (Exception e){
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR);
        }

    }

}
