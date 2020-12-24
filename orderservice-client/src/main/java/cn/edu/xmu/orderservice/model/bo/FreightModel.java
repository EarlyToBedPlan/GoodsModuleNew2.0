package cn.edu.xmu.orderservice.model.bo;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class FreightModel {
    /**
     * 构造函数
     */
    public FreightModel(){}

    /**
     * 后台运费模板状态
     */
    //无相关状态码

    /**
     * 运费模板类型
     */
    public enum FreightType {
        WEIGHT(0, "以重量计算"),
        PIECE(1, "以数量计算");

        private static final Map<Integer, FreightType> freightTypeMap;

        //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
        static {
            freightTypeMap = new HashMap();
            for (FreightType enum1 : values()) {
                freightTypeMap.put(enum1.code, enum1);
            }
        }

        private int code;

        private String description;

        FreightType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static FreightType getTypeByCode(Integer code) {
            return freightTypeMap.get(code);
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
    private Long id;
    private Long shop_id;
    private String name;
    private Byte type;
    private String default_Model;
    private Integer unit;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModify;


}
