package cn.edu.xmu.goods.model.vo;

import cn.edu.xmu.goods.model.bo.Shop;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商铺简单视图对象
 * @author Yancheng Lai
 * createdBy Yancheng Lai 2020/12/2 18:13
 * modifiedBy Yancheng Lai 18:13
 **/
@Data
@ApiModel(description = "商店对象")
public class ShopSimpleRetVo implements Serializable {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "名称")
    private String name;

    ShopSimpleRetVo(){}

    public ShopSimpleRetVo(Shop shop){
        this.id = shop.getId();
        this.name = shop.getShopName();
    }
}

