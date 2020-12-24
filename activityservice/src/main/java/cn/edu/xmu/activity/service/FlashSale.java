package cn.edu.xmu.activity.model.bo;

import cn.edu.xmu.activity.model.po.FlashSalePo;
import cn.edu.xmu.activity.model.vo.FlashSaleVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LJP_3424
 * @create 2020-12-03 21:27
 */
public class FlashSale implements VoObject {

    public void setState(Byte state) {
        this.state = state;
    }

    public enum State {
        OFF(0, "已下线"),
        ON(1, "已上线"),
        DELETE(2, "已删除");

        private static final Map<Integer, State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (FlashSale.State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        State(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static FlashSale.State getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public byte getCode() {
            return (byte) code;
        }

        public String getDescription() {
            return description;
        }
    }
    
    private Long id;

    private LocalDateTime flashDate;

    private TimeSegmentVo timeSeq;

    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    private Byte state;

    public FlashSale(FlashSalePo po, TimeSegmentVo timeSegmentPo) {
        id = po.getId();
        flashDate = po.getFlashDate();
        gmtCreated = po.getGmtCreate();
        gmtModified = po.getGmtModified();
        timeSeq = timeSegmentPo;
        state = po.getState();
    }

    @Override
    public VoObject createVo() {
        FlashSaleVo flashSaleVo = new FlashSaleVo();
        flashSaleVo.setFlashDate(this.flashDate);
        flashSaleVo.setGmtCreated(this.gmtCreated);
        flashSaleVo.setGmtModified(this.gmtModified);
        flashSaleVo.setTimeSeq(this.timeSeq);
        flashSaleVo.setId(this.id);
        flashSaleVo.setState(this.state);
        return flashSaleVo;
    }

    @Override
    public VoObject createSimpleVo() {
        return null;
    }
}
