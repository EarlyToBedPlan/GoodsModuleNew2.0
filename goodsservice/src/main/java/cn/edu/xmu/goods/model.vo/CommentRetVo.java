package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.bo.Comment;
import cn.edu.xmu.otherservice.model.vo.CustomerVo;
import cn.edu.xmu.ooad.model.VoObject;
import lombok.Data;
import org.apache.dubbo.registry.client.metadata.SpringCloudMetadataServiceURLBuilder;

import java.time.LocalDateTime;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/20 8:47
 */
@Data
public class CommentRetVo implements VoObject {
    Long id;
    CustomerRetVo customerRetVo;
    Long goodsSkuId;
    Byte type;
    String content;
    Byte state;
    LocalDateTime gmtCreate;
    LocalDateTime gmtModified;

    public CommentRetVo(Comment comment){
        this.id=comment.getId();
        ////this.customerRetVo=comment.getCustomerRetVo();
        this.goodsSkuId=comment.getGoodsSkuId();
        this.type=comment.getType();
        this.content=comment.getContent();
       // this.state=comment.getState().getCode().byteValue();
//        this.gmtCreate=comment.getGmtCreate();
//        this.gmtModified=comment.getGmtModified();
    }

    @Override
    public Object createVo(){return null;}

    @Override
    public Object createSimpleVo(){return null;}

}
