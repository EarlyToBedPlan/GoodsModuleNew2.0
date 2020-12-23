package cn.edu.xmu.goods.service.impl;


import cn.edu.xmu.goods.dao.GoodsSkuDao;
import cn.edu.xmu.goods.dao.GoodsSpuDao;
import cn.edu.xmu.goods.dao.ShopDao;
import cn.edu.xmu.goods.model.po.GoodsSkuPo;
import cn.edu.xmu.goods.model.po.GoodsSpuPo;
import cn.edu.xmu.goods.model.po.ShopPo;
import cn.edu.xmu.goods.service.GoodsSkuService;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.bo.GoodsSimpleSpu;
import cn.edu.xmu.goodsservice.model.bo.GoodsSku;
import cn.edu.xmu.goodsservice.model.bo.ShopSimple;
import cn.edu.xmu.goodsservice.model.vo.GoodsSkuSimpleRetVo;
import cn.edu.xmu.goodsservice.model.vo.ShopVo;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "2.2.0")
public class IGoodsServiceImpl implements IGoodsService {

    @Autowired
    GoodsSkuDao goodsSkuDao;

    @Autowired
    GoodsSpuDao goodsSpuDao;

    @Autowired
    ShopDao shopDao;

    @Autowired
    GoodsSkuService goodsSkuService;

    @Override
    public Long getShopIdBySpuId(Long id) {
        GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(id).getData();
        if(goodsSpuPo == null){
            return null;
        }
        return goodsSpuPo.getShopId();
    }

    @Transactional
    public GoodsSku getSkuById(Long id){
        ReturnObject<GoodsSkuPo> goodsSkuReturnObject = goodsSkuDao.getSkuById(id);
        GoodsSkuPo goodsSkuPo = goodsSkuReturnObject.getData();
        if(goodsSkuPo == null){
            return null;
        }
        GoodsSku goodsSku = new GoodsSku();
        goodsSku.setStatecode(goodsSkuPo.getState());
        goodsSku.setDetail(goodsSkuPo.getDetail());
        goodsSku.setDisabled(goodsSkuPo.getDisabled());
        goodsSku.setId(goodsSkuPo.getId());
        goodsSku.setImageUrl(goodsSkuPo.getImageUrl());
        goodsSku.setName(goodsSkuPo.getName());
        goodsSku.setGoodsSpuId(goodsSkuPo.getGoodsSpuId());
        goodsSku.setSkuSn(goodsSkuPo.getSkuSn());
        goodsSku.setOriginalPrice(goodsSkuPo.getOriginalPrice());
        goodsSku.setConfiguration(goodsSkuPo.getConfiguration());
        goodsSku.setWeight(goodsSkuPo.getWeight());
        goodsSku.setInventory(goodsSkuPo.getInventory());
        goodsSku.setGmtCreate(goodsSkuPo.getGmtCreate());
        goodsSku.setGmtModified(goodsSkuPo.getGmtModified());
        return goodsSku;
    }

    @Override
    public ShopSimple getSimpleShopById(Long id) {
        ShopPo shopPo = shopDao.getShopById(id);
        if(shopPo == null){
            return null;
        }
        ShopSimple shopSimple = new ShopSimple();
        shopSimple.setId(shopPo.getId());
        shopSimple.setShopName(shopPo.getName());
        return shopSimple;
    }

    @Override
    public ShopVo getShopById(Long id) {
        ShopPo shopPo = shopDao.getShopById(id);
        if(shopPo == null){
            return null;
        }
        ShopVo shopSimple = new ShopVo();
        shopSimple.setId(shopPo.getId());
        shopSimple.setShopName(shopPo.getName());
        return shopSimple;
    }

    @Override
    public GoodsSimpleSpu getSimpleSpuById(Long id) {
        GoodsSpuPo data = goodsSpuDao.getGoodsSpuPoById(id).getData();
        if(data == null){
            return null;
        }
        GoodsSimpleSpu goodsSimpleSpu = new GoodsSimpleSpu();
        goodsSimpleSpu.setDisabled(data.getDisabled());
        goodsSimpleSpu.setGmtCreated(data.getGmtCreate());
        goodsSimpleSpu.setGmtModified(data.getGmtModified());
        goodsSimpleSpu.setGoodsSn(data.getGoodsSn());
        goodsSimpleSpu.setId(data.getId());
        goodsSimpleSpu.setImageUrl(data.getImageUrl());
        goodsSimpleSpu.setName(data.getName());
        return goodsSimpleSpu;
    }

    @Override
    public List<ShopVo> getShopVoBySkuIdList(List<Long> ids) {
        List<ShopVo> shopVos= new ArrayList<>();
        List<Long> lst = goodsSkuDao.getShopIdListBySkuIdList(ids);
        List<ShopPo> shopPos = shopDao.getShopPoListByIdList(lst);
        for(ShopPo shopPo : shopPos){
            ShopVo shopVo = new ShopVo();
            shopVo.setShopName(shopPo.getName());
            shopVo.setGmtModified(shopPo.getGmtModified());
            shopVo.setGmtCreate(shopPo.getGmtCreate());
            shopVo.setState(shopPo.getState());
            shopVo.setId(shopPo.getId());
            shopVos.add(shopVo);
        }
        return shopVos;
    }

    @Override
    public List<GoodsSkuSimpleRetVo> getGoodsSkuListBySkuIdList(List<Long> skuIds){

        List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getGoodsSkuPoListBySkuIdList(skuIds);

        if(goodsSkuPos == null){
            return null;
        }
        List<GoodsSkuSimpleRetVo> goodsSkuSimpleRetVos = new ArrayList<>();
        for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
            GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSkuSimpleRetVo();
            goodsSkuSimpleRetVo.setName(goodsSkuPo.getName());
            goodsSkuSimpleRetVo.setDisabled(goodsSkuPo.getDisabled());
            goodsSkuSimpleRetVo.setId(goodsSkuPo.getId());
            goodsSkuSimpleRetVo.setImageUrl(goodsSkuPo.getImageUrl());
            goodsSkuSimpleRetVo.setInventory(goodsSkuPo.getInventory());
            goodsSkuSimpleRetVo.setSkuSn(goodsSkuPo.getSkuSn());
            goodsSkuSimpleRetVo.setOriginalPrice(goodsSkuPo.getOriginalPrice());
            goodsSkuSimpleRetVo.setPrice(goodsSkuService.getPriceById(goodsSkuPo.getId()).intValue());
            goodsSkuSimpleRetVos.add(goodsSkuSimpleRetVo);
        }

        return goodsSkuSimpleRetVos;

    }
}
