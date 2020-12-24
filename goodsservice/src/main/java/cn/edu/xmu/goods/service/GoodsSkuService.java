package cn.edu.xmu.goods.service;

import cn.edu.xmu.goods.dao.FloatPriceDao;
import cn.edu.xmu.goods.dao.GoodsSkuDao;
import cn.edu.xmu.goods.dao.GoodsSpuDao;
import cn.edu.xmu.goods.dao.ShopDao;
import cn.edu.xmu.goods.model.bo.FloatPrice;
import cn.edu.xmu.goods.model.bo.GoodsSku;
import cn.edu.xmu.goods.model.bo.GoodsSpu;
import cn.edu.xmu.goods.model.bo.Shop;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.goods.model.vo.*;
import cn.edu.xmu.goodsservice.client.IActivityService;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ImgHelper;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.github.pagehelper.PageInfo;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SKU Service接口
 * @author Yancheng Lai
 * createdBy Yancheng Lai 2020/12/2 19:54
 * modifiedBy Yancheng Lai 19:54
 **/
@Service
public class GoodsSkuService {

    public static final Logger logger = LoggerFactory.getLogger(GoodsSkuService.class);
    @Autowired
    GoodsSkuDao goodsSkuDao;

    @Autowired
    GoodsSpuDao goodsSpuDao;

    @Autowired
    GoodsCategoryService goodsCategoryService;

    @Autowired
    FloatPriceDao floatPriceDao;

    @Autowired
    GoodsSpuService goodsSpuService;

    @Autowired
    ShopService shopService;

    @DubboReference(version = "2.7.8",group = "activity-service",check = false)
    IActivityService iActivityService;

    @Value("${goodsservice.dav.username}")
    private String davUsername;

    @Value("${goodsservice.dav.password}")
    private String davPassword;

    @Value("${goodsservice.dav.baseUrl}")
    private String baseUrl;

    @Autowired
    ShopDao shopDao;

    /** 
    * @Description: 分页获取sku信息 
    * @Param: [shopId, skuSn, spuId, spuSn, page, pagesize] 
    * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo<cn.edu.xmu.ooad.model.VoObject>> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/2 22:06
    */
    
    public ReturnObject<PageInfo<GoodsSkuSimpleRetVo>> findAllSkus(Long shopId, String skuSn, Long spuId, String spuSn, Integer page, Integer pagesize) {

        PageHelper.startPage(page, pagesize,true,true,null);

        //PageInfo<GoodsSkuSimpleRetVo> returnObject = goodsSkuDao.findAllGoodSkus(shopId, skuSn, spuId, spuSn, page, pagesize);

        List<GoodsSkuSimpleRetVo> goodsSkuSimpleRetVos = new ArrayList<>();

        //sku not null others null
        if( !skuSn.contentEquals("default") && shopId == -1 && spuId == -1 && spuSn.contentEquals("default")){
            List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getSkuPoBySkuSn(skuSn);
            for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
                GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSku(goodsSkuPo).createSimpleVo();
                goodsSkuSimpleRetVo.setPrice(getPriceById(goodsSkuPo.getId()));
                goodsSkuSimpleRetVos.add(goodsSkuSimpleRetVo);
            }
        } else if (!skuSn.contentEquals("default") && !( shopId == -1 && spuId == -1 && spuSn.contentEquals("default"))){
            // sku not null othsrs not null
            List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getSkuPoBySkuSn(skuSn);

            for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
                GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(goodsSkuPo.getId()).getData();
                boolean flag = true;
                if(shopId != -1 && shopId != goodsSpuPo.getShopId()){
                    flag = false;
                }
                if(spuId != -1 && spuId != goodsSpuPo.getId()){
                    flag = false;
                }
                if( !spuSn.contentEquals("default") && spuId != goodsSpuPo.getId()){
                    flag = false;
                }
                if(flag){
                    GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSku(goodsSkuPo).createSimpleVo();
                    goodsSkuSimpleRetVo.setPrice(getPriceById(goodsSkuPo.getId()));
                    goodsSkuSimpleRetVos.add(goodsSkuSimpleRetVo);
                }
            }
        } else if (skuSn.contentEquals("default")&& shopId == -1 && spuId == -1 && spuSn.contentEquals("default")){
            //all null
            List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getAllSkus();
            for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
                GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSku(goodsSkuPo).createSimpleVo();
                goodsSkuSimpleRetVo.setPrice(getPriceById(goodsSkuPo.getId()));
                goodsSkuSimpleRetVos.add(goodsSkuSimpleRetVo);
            }
        } else {
            List<Long> goodsSpuPoIds = goodsSpuDao.queryOnSpu(shopId,spuId,spuSn);
            if(goodsSpuPoIds == null || goodsSpuPoIds.size()==0){
                PageInfo<GoodsSkuSimpleRetVo> pageInfo = new PageInfo<>(goodsSkuSimpleRetVos);
                pageInfo.setPages(page);
                pageInfo.setPageSize(pagesize);
                return new ReturnObject<>(pageInfo);
            }
            List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getGoodsSkuPoListBySpuIdList(goodsSpuPoIds);
            for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
                GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSku(goodsSkuPo).createSimpleVo();
                goodsSkuSimpleRetVo.setPrice(getPriceById(goodsSkuPo.getId()));
                goodsSkuSimpleRetVos.add(goodsSkuSimpleRetVo);
            }
        }
        PageInfo<GoodsSkuSimpleRetVo> pageInfo = new PageInfo<>(goodsSkuSimpleRetVos);
        pageInfo.setPages(page);
        pageInfo.setPageSize(pagesize);
        return new ReturnObject<>(pageInfo);
    }
    /** 
    * @Description: 逻辑删除Sku，先检查，后删除
    * @Param: [shopId, id] 
    * @return: java.lang.Object 
    * @Author: Yancheng Lai
    * @Date: 2020/12/2 23:52
    */
    @Transactional
    public ReturnObject<VoObject> revokeSku(Long shopId,Long id){
            return goodsSkuDao.revokeSku(shopId,id);

    }

    /**
    * @Description: 新增GoodsSku
    * @Param: [goodsSku]
    * @return: cn.edu.xmu.ooad.util.ReturnObject
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 15:27
    */


    @Transactional
    public ReturnObject<GoodsSkuSimpleRetVo> insertGoodsSku(GoodsSku goodsSku, Long shopId, Long id) {

        logger.info("Service");

        ReturnObject<GoodsSkuSimpleRetVo> retObj = null;
        GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(id).getData();
        if(goodsSpuPo==null||goodsSpuPo.getDisabled()!=0){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        //查询spu是否属于该商铺
        if(goodsSpuDao.checkSpuIdInShop(shopId,id)==false){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        GoodsSpuPoExample goodsSpuPoExample = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = goodsSpuPoExample.createCriteria();

        goodsSku.setGoodsSpuId(id);
        retObj = goodsSkuDao.insertGoodsSku(goodsSku);
        logger.info("Service node 4");
        return retObj;
    }

    /**
    * @Description: 修改SKU
    * @Param: [vo, shopId, id]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 19:53
    */

    @Transactional
    
    public ReturnObject<VoObject> updateSku(GoodsSku vo,Long shopId,Long id){
        ReturnObject<VoObject> ret =  goodsSkuDao.updateSku(vo,shopId, id);
        return ret;
    }

    /**
    * @Description: 返回所有SKU状态
    * @Param: []
    * @return: ReturnObject<List<StateVo>>
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:24
    */
    
    @Transactional
    public ReturnObject<List<StateVo>>findSkuStates(){
        List<StateVo> lst = new ArrayList<StateVo>();

        for (GoodsSku.State e : GoodsSku.State.values()) {
            StateVo stateVo = new StateVo((byte)e.getCode(),e.getDescription());
            lst.add(stateVo);
        }

        return new ReturnObject<>( lst);
    }

    /**
    * @Description: 上传SKU图片
    * @Param: [id, shopId, multipartFile]
    * @return: ReturnObject
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:25
    */
    @Transactional
    
    public ReturnObject uploadSkuImg(Long id,Long shopId,MultipartFile multipartFile){
        GoodsSkuPo goodsSkuPo = goodsSkuDao.getSkuById(id).getData();
        ReturnObject<VoObject> goodsSkuReturnObject = new ReturnObject<>(new GoodsSku(goodsSkuPo));
        if(goodsSkuReturnObject.getCode() == ResponseCode.RESOURCE_ID_NOTEXIST) {
            return goodsSkuReturnObject;
        }
        GoodsSku goodsSku = (GoodsSku)goodsSkuReturnObject.getData();

        ReturnObject returnObject = new ReturnObject();
        try{
            returnObject = ImgHelper.remoteSaveImg(multipartFile,2,davUsername, davPassword,baseUrl);
            if(returnObject.getCode()!=ResponseCode.OK){
                return returnObject;
            }

            String oldFilename = goodsSku.getImageUrl();
            goodsSku.setImageUrl(returnObject.getData().toString());

            ReturnObject updateReturnObject = goodsSkuDao.updateSku(goodsSku,shopId,goodsSku.getId());
            if(updateReturnObject.getCode()==ResponseCode.FIELD_NOTVALID) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUsername, davPassword, baseUrl);
                return updateReturnObject;
            }
            if(oldFilename!=null) {
                ImgHelper.deleteRemoteImg(oldFilename, davUsername, davPassword,baseUrl);
            }
        }
        catch (IOException e){
            return new ReturnObject(ResponseCode.FILE_NO_WRITE_PERMISSION);
        }
        return new ReturnObject<>();

    }

    /**
    * @Description: 用Id得到GoodsSku
    * @Param: [id]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/6 16:36
    */
    public boolean checkSkuId(Long id)
    {
        ReturnObject<GoodsSkuPo> goodsSkuReturnObject = goodsSkuDao.getSkuById(id);
        GoodsSkuPo goodsSkuPo = goodsSkuReturnObject.getData();
        if(goodsSkuPo == null || goodsSkuPo.getDisabled()!=0){
            return false;
        }
        return true;
    }

    @Transactional
    public ReturnObject<GoodsSkuRetVo> getSkuById(Long id){
        logger.debug("service: get Sku by id: "+ id);
        ReturnObject<GoodsSkuPo> goodsSkuReturnObject = goodsSkuDao.getSkuById(id);
        GoodsSkuPo goodsSkuPo = goodsSkuReturnObject.getData();
        if(goodsSkuPo == null || goodsSkuPo.getDisabled()!=0){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSku goodsSku = new GoodsSku(goodsSkuPo);
        GoodsSkuRetVo retVo = goodsSku.createVo();
        //GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoBySkuId(id).getData();
        //GoodsSpu goodsSpu = new GoodsSpu(goodsSpuPo);
        ReturnObject<GoodsSpuRetVo> ret = goodsSpuService.findSpuById(goodsSkuPo.getGoodsSpuId());
        if(ret.getCode() == ResponseCode.RESOURCE_ID_NOTEXIST){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        GoodsSpuRetVo goodsSpuRetVo = ret.getData();
        retVo.setState((int)goodsSku.getStatecode());
        retVo.setGoodsSpu(goodsSpuRetVo);
        retVo.setPrice(getPriceById(id).intValue());
        if(retVo.getGoodsSpu() == null ){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        return new ReturnObject<GoodsSkuRetVo> (retVo);

    }
    /** 
    * @Description:  用SPU得到SKUid
    * @Param: [id] 
    * @return: java.util.List<VoObject> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:25
    */
    
    @Transactional
    public List<VoObject> getSkuBySpuId(Long id){
        List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getSkuBySpuId(id);
        List<VoObject> goodsSkus = new ArrayList<>();
        for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
            goodsSkus.add(new GoodsSku(goodsSkuPo));
        }
        return goodsSkus;
    }

    /** 
    * @Description:  用SPUid得到SKU的简单Vo
    * @Param: [id] 
    * @return: java.util.List<cn.edu.xmu.goods.model.vo.GoodsSkuSimpleRetVo> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:26
    */
    @Transactional
    
    public List<GoodsSkuSimpleRetVo> getSkuSimpleVoBySpuId(Long id){
        List<GoodsSkuPo> goodsSkuPos = goodsSkuDao.getSkuBySpuId(id);
        List<GoodsSkuSimpleRetVo> goodsSkuSimpleRetVos = new ArrayList<>();
        for(GoodsSkuPo goodsSkuPo : goodsSkuPos){
            goodsSkuSimpleRetVos.add(new GoodsSku(goodsSkuPo).createSimpleVo());
        }
        return goodsSkuSimpleRetVos;
    }

    /** 
    * @Description: 商品上架 
    * @Param: [shopId, id] 
    * @return: ReturnObject<VoObject> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:26
    */
    @Transactional
    
    public ReturnObject<VoObject> updateSkuOnShelves(Long shopId,Long id){
        return goodsSkuDao.updateSkuOnShelves(shopId,id);
    }

    /** 
    * @Description: 商品下架 
    * @Param: [shopId, id] 
    * @return: ReturnObject<VoObject> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:27
    */
    @Transactional
    
    public ReturnObject<VoObject> updateSkuOffShelves(Long shopId,Long id){
            return goodsSkuDao.updateSkuOffShelves(shopId,id);
    }

    /** 
    * @Description: 对对应的vo减少库存 先查后减 
    * @Param: [vo] 
    * @return: java.lang.Boolean 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 20:37
    */
    @Transactional
    public Boolean deductStock(List<OrderItemVo> vo) {
        for(OrderItemVo itemVo: vo){
            ReturnObject ret = goodsSkuDao.checkStock(itemVo.getSkuId(),itemVo.getQuantity());
            if(ret.getCode() == ResponseCode.OK){
                continue;
            } else {
                return false;
            }
        }
        for(OrderItemVo itemVo: vo){
            logger.info("s: deduck for "+itemVo.getSkuId());
            ReturnObject ret = goodsSkuDao.deductStock(itemVo.getSkuId(),itemVo.getQuantity());
            if(ret.getCode() == ResponseCode.OK){
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public InfoVo getWeightBySkuId(Long skuId) {
        ReturnObject<GoodsSkuPo> retobj= goodsSkuDao.getSkuById(skuId);
        ReturnObject<GoodsSpuPo> retspu= goodsSpuDao.getGoodsSpuPoBySkuId(skuId);
        InfoVo infoVo = new InfoVo();
        infoVo.setWeight(retobj.getData().getWeight().intValue());
        infoVo.setFreightModelId(retspu.getData().getFreightId());
            return infoVo;
    }

    @Transactional
    public Boolean changeGoodsFreightWeight(Long FreightId, Long defaultFreightId) {
        ReturnObject retobj= goodsSpuDao.changeGoodsFreightWeight(FreightId, defaultFreightId);
        if(retobj.getCode()== ResponseCode.OK){
            return true;
        }
        return false;
    }

    @Transactional
    public GoodsSku getGoodsSkuById(Long skuid) {
        GoodsSku goodsSku = new GoodsSku(goodsSkuDao.getSkuById(skuid).getData());
        return goodsSku;
    }

    @Transactional
    public Boolean checkSkuIdByShopId(Long shopId, Long skuId) {
        return goodsSkuDao.checkSkuIdInShop(shopId,skuId);
    }

    public List<Long> getSkuIdListByShopId(Long shopId) {
        return goodsSkuDao.getSkuIdListByShopId(shopId);

    }

    public ShopVo getShopBySkuId(Long skuId){
        GoodsSkuPo goodsSkuPo = goodsSkuDao.getSkuById(skuId).getData();
        Long spuId = goodsSkuPo.getGoodsSpuId();
        GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(spuId).getData();
        Long shopId = goodsSpuPo.getShopId();
        ShopPo po = shopDao.getShopById(shopId);
        ShopVo shopVo = new ShopVo(new Shop(po));
        return shopVo;
    }

    public List<GoodsSku> getSkuListByShopId(Long shopId) {
        return goodsSkuDao.getSkuListByShopId(shopId);
    }

    
    @Transactional
    public Long getPriceById(Long goodsSkuId) {
        Long result = null;
//        result = iActivityService.getFlashSalePriceBySkuId(goodsSkuId);
//        if(result != null){
//            return  result;
//        }
        ReturnObject<List<FloatPricePo>> listReturnObject = floatPriceDao.getFloatPriceBySkuId(goodsSkuId);
        List<FloatPricePo> target=listReturnObject.getData();
        if(target != null){
            for(FloatPricePo aFloatPrice : target){
                if(LocalDateTime.now().isAfter(aFloatPrice.getBeginTime())&&LocalDateTime.now().isBefore(aFloatPrice.getEndTime())){
                    Long item= aFloatPrice.getActivityPrice();
                    result = item;
                }
            }
        }

        if(result == null){
            return goodsSkuDao.getSkuById(goodsSkuId).getData().getOriginalPrice();
        }
        return result;
    }


    
    @Transactional
    public GoodsCartVo getCartByskuId(Long Sku) {
        GoodsCartVo vo = new GoodsCartVo();
        GoodsSkuPo goodsSkuPo = goodsSkuDao.getSkuById(Sku).getData();
        GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(goodsSkuPo.getGoodsSpuId()).getData();
        vo.setGoodsSkuId(Sku);
        vo.setSkuName(goodsSkuPo.getName());
        vo.setSpuName(goodsSpuPo.getName());
        //vo.setQuantity(goodsSkuPo.getInventory());
        vo.setPrice(getPriceById(Sku).intValue());
        vo.setGmtCreate(goodsSkuPo.getGmtCreate());
        vo.setGmtModified(goodsSkuPo.getGmtModified());
        return vo;
    }

    public ReturnObject<GoodsSkuPo> getSkuPoById(Long id){
        return goodsSkuDao.getSkuById(id);
    }


    public boolean checkSkuIdInShop(Long shopId, Long skuId) {
        return goodsSkuDao.checkSkuIdInShop(shopId,skuId);
    }

    
    public boolean checkSkuDisabled(Long skuId) {
        return goodsSkuDao.checkSkuDisabled(skuId);
    }
}
