
package cn.edu.xmu.goods.dao;

import cn.edu.xmu.goods.mapper.ShopPoMapper;
import cn.edu.xmu.goods.model.bo.Shop;
import cn.edu.xmu.goods.model.po.ShopPo;
import cn.edu.xmu.goods.model.po.ShopPoExample;
import cn.edu.xmu.ooad.util.ResponseCode;

import cn.edu.xmu.ooad.util.ReturnObject;

import com.alibaba.nacos.api.common.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * @author Ruzhen Chang
 */
@Repository
public class ShopDao implements InitializingBean{
    @Autowired
    private ShopPoMapper shopPoMapper;

    private  static  final Logger logger = LoggerFactory.getLogger(ShopDao.class);

    @Value("${shopservice.initialization}")
    private Boolean initialization;

    @Override
    public void afterPropertiesSet() {

    }
    /**
     * 由店铺id获得店铺
     * @author Ruzhen Chang
     */
    public ShopPo getShopById(Long shopId){
        ShopPo po=new ShopPo();
        try{
            po = shopPoMapper.selectByPrimaryKey(shopId);
        }catch (DataAccessException e){
            StringBuilder message=new StringBuilder().append("getShopById:").append(e.getMessage());
            logger.debug(message.toString());
        }
        return po;

    }

    /**
    * @Description: IDlist得到shoplist
    * @Param: [lst]
    * @return: java.util.List<cn.edu.xmu.goods.model.po.ShopPo>
    * @Author: Yancheng Lai
    * @Date: 2020/12/16 17:01
    */
    public List<ShopPo> getShopPoListByIdList(List<Long> lst){
        ShopPoExample example = new ShopPoExample();
        ShopPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(lst);
        List<ShopPo> spo = shopPoMapper.selectByExample(example);
        return spo;
    }

    /**
     * @Description 检查店铺名是否重复
     * @author Ruzhen Chang
     */
    public Boolean checkShopName(String shopName){
        ShopPoExample example = new ShopPoExample();
        ShopPoExample.Criteria criteria= example.createCriteria();
        criteria.andStateNotEqualTo((byte) Shop.State.FAILED.getCode());
        if(shopName!=null) {
            criteria.andNameEqualTo(shopName);
        }
        try{
            List<ShopPo> shopPos=shopPoMapper.selectByExample(example);
            return shopPos.isEmpty();
        } catch (Exception e){
            logger.error("发生严重服务器内部错误："+e.getMessage());
            return null;
        }
    }



    /**
     * 店家修改店铺信息
     * @author Ruzhen Chang
     */
    public ReturnObject<Shop> updateShop(Shop shop){
        ShopPo shopPo= new ShopPo();
        shopPo.setId(shop.getId());
        ShopPo shopPo1 = shopPoMapper.selectByPrimaryKey(shop.getId());
        if(shop.getShopName()==null){
            return new ReturnObject<>(ResponseCode.FIELD_NOTVALID,String.format("名字为空 id:" + shop.getId()));
        }
        if(shopPo1.getState()==Shop.State.FAILED.getCode()){
            return new ReturnObject<>(ResponseCode.SHOP_STATENOTALLOW,String.format("状态不允许"));
        }
        if(shop.getShopName().matches("\\s+")){
            return new ReturnObject<>(ResponseCode.FIELD_NOTVALID);
        }
        if(!checkShopName(shop.getShopName())) {
            return new ReturnObject<>(ResponseCode.FIELD_NOTVALID);
        }

        shopPo.setName(shop.getShopName());
        shopPo.setGmtModified(shop.getGmtModified());
        ReturnObject<Shop> returnObject=null;
        try {
            int ret = shopPoMapper.updateByPrimaryKeySelective(shopPo);
            if (ret == 0) {
                logger.debug("updateShop: update fail. shopId: " + shop.getId());
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("updateShop: update shop success : " + shop.toString());
                returnObject = new ReturnObject();
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;

    }


    /**
     * 关闭店铺
     * @author Ruzhen Chang
     */
    public ReturnObject closeShop(Long shopId){
        ReturnObject returnObject = null;
        ShopPo shopPo = getShopById(shopId);
        try {
            int ret;
            if(shopPo.getState()==Shop.State.UNAUDITED.getCode()) {
                ret = shopPoMapper.deleteByPrimaryKey(shopId);
            }
            else{
                shopPo.setState((byte) Shop.State.DELETE.getCode());
                ret = shopPoMapper.updateByPrimaryKeySelective(shopPo);
            }
            if (ret == 0) {
                logger.debug("closeShop fail. shopId: " + shopId);
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                logger.debug("closeShop success. shopId: " + shopId);
                returnObject = new ReturnObject(ResponseCode.OK);
            }
        } catch (Exception e) {
            logger.error("发生了严重的服务器内部错误：" + e.getMessage());
        }
        return returnObject;
    }



    public ReturnObject changeShopState(Shop shop){
        ShopPo shopPo=new ShopPo();
        shopPo.setId(shop.getId());
        shopPo.setState(shop.getState());
        ReturnObject returnObject=new ReturnObject();
        try{
            int ret=shopPoMapper.updateByPrimaryKeySelective(shopPo);
            if(ret==0){
                logger.debug("changeShopState: change failed. shop id:"+shopPo.getId());
                returnObject = new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            else {
                logger.debug("changeShopState:change success:" + shop.toString());
                returnObject=new ReturnObject();
            }
        }
        catch (Exception e){
            logger.error("发生了严重的服务器内部错误: " +e.getMessage());
        }
        return returnObject;
    }

    /**
     * @Description 新增店铺
     * @author Ruzhen Chang
     */
    public ShopPo insertShop(Shop shop) {
        ShopPo shopPo = shop.createPo();
        try {
            shopPoMapper.insert(shopPo);
            //插入成功
            logger.debug("insertShop: insert shop = " + shopPo.toString());
        } catch (Exception e) {
            logger.error("严重错误：" + e.getMessage());
        }
        return shopPo;
    }



}
