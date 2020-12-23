package cn.edu.xmu.goods.model.bo;


import cn.edu.xmu.goods.model.po.CommentPo;
import cn.edu.xmu.goods.model.vo.CommentRetVo;
import cn.edu.xmu.goods.model.vo.CommentVoBody;
import cn.edu.xmu.goods.model.vo.CustomerRetVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.goodsservice.model.vo.CustomerVo;
import io.lettuce.core.StrAlgoArgs;
import io.netty.channel.SimpleUserEventChannelHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruzhen Chang
 */
@Data
public class Comment implements VoObject{

    public Comment() {

    }

    public CommentPo createPo() {
        CommentPo commentPo=new CommentPo();
        commentPo.setContent(getContent());
        commentPo.setCustomerId(getCustomerId());
        commentPo.setState((byte) Comment.State.NEW.getCode().intValue());
        commentPo.setId(getId());
        commentPo.setGmtCreate(getGmtCreate());
        commentPo.setGmtModified(getGmtModified());
        commentPo.setType(getType());
        commentPo.setGoodsSkuId(getGoodsSkuId());
        commentPo.setOrderitemId(getOrderItemId());
        return commentPo;
    }

    /**
     * @author Ruzhen Chang
     */

    public enum State {
        NEW(0, "未审核"),
        NORM(1, "评论成功"),
        FORBID(2, "未通过");

        private static final Map<Integer, Comment.State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (Comment.State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        State(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static Comment.State getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private Long id;

    CustomerRetVo customerRetVo;
    CustomerVo customerVo;
    private Long customerId;

    private Long goodsSkuId;

    private Long orderItemId;

    private Byte type;
    /*评价等级 0好评1中评2差评*/

    private String content;

    private State state=State.NEW;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    public Comment(CommentPo po){
        this.id=po.getId();
        this.customerId= po.getCustomerId();
        this.goodsSkuId= po.getGoodsSkuId();
        this.orderItemId= po.getOrderitemId();
        this.type= po.getType();
        this.content= po.getContent();
//        if(null!=po.getState()){
//            this.state=State.getTypeByCode(po.getState().intValue());
//        }
    }

    public Comment(CommentVoBody vo){
        setType(vo.getType());
        setContent(vo.getContent());
    }

    @Override
    public Object createVo() {
        return null;
    }

    public VoObject createRetVo() {
        return new CommentRetVo(this);
    }

    @Override
    public VoObject createSimpleVo() {
        return createRetVo();
    }
}
