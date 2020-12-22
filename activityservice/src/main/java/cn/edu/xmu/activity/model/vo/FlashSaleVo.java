package cn.edu.xmu.activity.model.vo;

import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author LJP_3424
 * @create 2020-12-03 17:56
 */
@Data
public class FlashSaleVo implements VoObject {

    private Byte state;

    private Long id;

    private LocalDateTime flashDate;

//    private Long timeSegId;
    private TimeSegmentVo timeSeq;
    private LocalDateTime gmtCreated;


    private LocalDateTime gmtModified;

    @Override
    public Object createVo() {
        return null;
    }

    @Override
    public Object createSimpleVo() {
        return null;
    }
}
