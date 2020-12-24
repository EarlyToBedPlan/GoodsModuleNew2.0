package cn.edu.xmu.goods.dao;


import cn.edu.xmu.goods.mapper.CommentPoMapper;
import cn.edu.xmu.goods.model.bo.Comment;
import cn.edu.xmu.goods.model.bo.GoodsSku;
import cn.edu.xmu.goods.model.po.CommentPo;
import cn.edu.xmu.goods.model.po.CommentPoExample;
import cn.edu.xmu.goods.model.vo.StateVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Ruzhen Chang
 */
@Repository
public class CommentDao implements InitializingBean{
    @Autowired
    private CommentPoMapper commentPoMapper;
    @Autowired
    private GoodsSkuDao goodsSkuDao;

    private static final Logger logger=LoggerFactory.getLogger(CommentDao.class);

    @Value("${commentservice.initialization}")
    private Boolean initialization;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * @Description 获得评论详细信息
     * @author Ruzhen Chang
     */
    public CommentPo getCommentById(Long commentId){
        CommentPo commentPo=new CommentPo();
        try{
            commentPo=commentPoMapper.selectByPrimaryKey(commentId);
        } catch (Exception e){
            StringBuilder message=new StringBuilder().append("getCommentById:").append(e.getMessage());
            logger.debug(message.toString());
        }
        return commentPo;
    }


    public boolean checkCommentInsert(Comment comment)
    {
            CommentPoExample example=new CommentPoExample();
            CommentPoExample.Criteria criteria=example.createCriteria();
            criteria.andOrderitemIdEqualTo(comment.getOrderItemId());
            List<CommentPo> commentPos=commentPoMapper.selectByExample(example);
            if(commentPos==null||commentPos.size()==0)
                return false;
            else return true;

    }

    /**
     * @Description 新增sku评论
     * @author Ruzhen Chang
     */
    public CommentPo newGoodsSkuComment(Comment comment){
        CommentPo commentPo=comment.createPo();
        try {
            commentPoMapper.insert(commentPo);
            logger.debug("newGoodsSkuComment: insert comment = "+commentPo.toString());
        }catch (DataAccessException e){
            logger.debug("apply comment failed:"+e.getMessage());
        }
        return commentPo;
    }

    /**
     * @Description 由商品id获得评论列表
     * @Param shopId
     * @author Ruzhen Chang
     */
    public List<CommentPo> getGoodsSkuAllCommentList(long goodsSkuId,byte state){
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andGoodsSkuIdEqualTo(goodsSkuId);
        criteria.andStateEqualTo(state);
        List<CommentPo> commentPos=commentPoMapper.selectByExample(example);

        for(CommentPo commentPo:commentPos) {
            try {
                if((byte)commentPo.getState()==Comment.State.NORM.getCode()) {
                    commentPos.add(commentPo);
                    logger.debug("getGoodsSkuCommentList: goodsSkuId = " + commentPo.getGoodsSkuId());
                }else {
                    logger.debug("getGoodsSkuCommentList: id not exist = " + commentPo.getGoodsSkuId());
                }
            } catch (DataAccessException e) {
                logger.debug("getGoodsSkuCommentList:" + e.getMessage());
            }
        }
        return commentPos;
    }


    /**
     * @Description 根据商品skuId获得评论id列表
     * @author Ruzhen Chang
     */
    public PageInfo<VoObject> getCommentListByGoodsSkuId(Long goodsSkuId, Integer page, Integer pageSize){
        PageHelper.startPage(page,pageSize);
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andGoodsSkuIdEqualTo(goodsSkuId);
        criteria.andStateEqualTo((byte)Comment.State.NORM.getCode().intValue());
        List<CommentPo> commentPos=commentPoMapper.selectByExample(example);
        logger.debug("getCommentIdListByGoodsSkuId: retComments" +commentPos);
        List<VoObject> commmentVos=new ArrayList<>(commentPos.size());
        for(CommentPo po:commentPos){
            Comment comment= new Comment(po);
            commmentVos.add(comment.createSimpleVo());
        }
        return new PageInfo<>(commmentVos);
    }



    /**
     * @Description 根据用户id获得评论id列表
     * @author Ruzhen Chang
     */
     public PageInfo<VoObject> getCommentListByCustomerId(long customerId,Integer page,Integer pageSize){
        PageHelper.startPage(page,pageSize);
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andCustomerIdEqualTo(customerId);
        List<CommentPo> commentPos=commentPoMapper.selectByExample(example);
        logger.debug("getCommentIdListByCustomerId:" +customerId);
        List<VoObject> commmentVos=new ArrayList<>(commentPos.size());
        for(CommentPo po:commentPos){
            Comment comment= new Comment(po);
            commmentVos.add(comment.createSimpleVo());
        }
        return new PageInfo<>(commmentVos);
    }


    /**
     * @Description 修改评论状态
     * @author Ruzhen Chang
     */
    public ReturnObject updateCommentState(Comment comment){
        CommentPo commentPo=new CommentPo();
        commentPo.setId(comment.getId());
        commentPo.setState(comment.getState().getCode().byteValue());
        ReturnObject returnObject=new ReturnObject();
        try{
            int ret=commentPoMapper.updateByPrimaryKeySelective(commentPo);
            if(ret==0){
                logger.debug("updateCommentState: update faild. comment id:"+commentPo.getId());
                returnObject=new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateCommentState: update success:"+comment.toString());
                returnObject=new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }


    /**
     * @Description 获取评论所有状态
     * @author Ruzhen Chang
     */
    public List<StateVo> findCommentStates(){
        List<StateVo> stateVos= new  ArrayList<>();
        for (Comment.State s : Comment.State.values()) {
            StateVo vo=new StateVo( s.getCode().byteValue(),s.getDescription());
            stateVos.add(vo);
        }
        return stateVos;
    }

    /**
     * @Description 获取店铺内所有评论列表
     * @param page
     * @param pageSize
     * @return
     */
    public PageInfo<VoObject> getCommentListByState(Integer state,Integer page,Integer pageSize){
        PageHelper.startPage(page,pageSize);
        CommentPoExample example=new CommentPoExample();
        CommentPoExample.Criteria criteria=example.createCriteria();
        criteria.andStateEqualTo(state.byteValue());
        List<CommentPo> commentPos=commentPoMapper.selectByExample(example);
        List<VoObject> commmentVos=commentPos.stream().map(po->new Comment(po).createSimpleVo()).collect(Collectors.toList());
        return new PageInfo<>(commmentVos);
    }
}
