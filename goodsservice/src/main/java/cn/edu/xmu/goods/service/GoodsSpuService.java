package cn.edu.xmu.goods.service;

import cn.edu.xmu.goods.dao.*;
import cn.edu.xmu.goods.model.bo.*;
import cn.edu.xmu.goods.model.po.GoodsSkuPo;
import cn.edu.xmu.goods.model.po.GoodsSpuPo;
import cn.edu.xmu.goods.model.po.ShopPo;
import cn.edu.xmu.goods.model.vo.*;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ImgHelper;
import cn.edu.xmu.ooad.util.JacksonUtil;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import cn.edu.xmu.orderservice.client.OrderService;
import cn.edu.xmu.orderservice.model.bo.FreightModel;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SPU接口的实现类
 * @author Yancheng Lai
 * createdBy Yancheng Lai 2020/12/2 19:03
 * modifiedBy Yancheng Lai 19:03
 **/
@Service
public class GoodsSpuService{
    private Logger logger = LoggerFactory.getLogger(GoodsSpuService.class);
    @Autowired
    ShopService shopService;

    @Autowired
    GoodsSpuDao goodsSpuDao;

    @Autowired
    GoodsCategoryService categoryService;

    @Autowired
    BrandDao brandDao;

    @Autowired
    GoodsSkuDao goodsSkuDao;

    @Autowired
    ShopDao shopDao;

    @Autowired
    GoodsCategoryDao goodsCategoryDao;

    @Autowired
    GoodsSkuService goodsSkuService;

    @DubboReference(check = false)
    OrderService orderService;

    //@Autowired
    //ShopService shopService;

    @Value("${goodsservice.dav.username}")
    private String davUsername;

    @Value("${goodsservice.dav.password}")
    private String davPassword;

    @Value("${goodsservice.dav.baseUrl}")
    private String baseUrl;
    /**
    * @Description: 查询 Spu 以 Id
    * @Param: [id]
    * @return: ReturnObject<VoObject> Spu
    * @Author: Yancheng Lai
    * @Date: 2020/12/2 19:07
    */
    
    public ReturnObject<GoodsSpuRetVo> findSpuById(Long id) {
        ReturnObject<GoodsSpuRetVo> returnObject = null;

        GoodsSpuPo goodsSpuPo = goodsSpuDao.getGoodsSpuPoById(id).getData();
        if(goodsSpuPo != null && goodsSpuPo.getDisabled() ==0) {
            logger.info("findSpuById : " + returnObject);
            GoodsSpuRetVo goodsSpuRetVo = new GoodsSpuRetVo(new GoodsSpu(goodsSpuPo));
            //goodsSpuRetVo.setCategory(categoryService.findCategorySimpleVoById(goodsSpuPo.getCategoryId()).getData());
            ShopPo shopPo = shopDao.getShopById(goodsSpuPo.getShopId());
            if(shopPo != null){
                Shop shop = new Shop(shopPo);
                ShopVo shopSimpleVo = (ShopVo)shop.createVo();
                goodsSpuRetVo.setShop(shopSimpleVo);
            }

            Brand brand = brandDao.getBrandById(goodsSpuPo.getBrandId()).getData();
            if(brand != null){
                BrandSimpleRetVo brandSimpleRetVo = brand.createSimpleVo();
                if (brandSimpleRetVo == null){
                    logger.info("simplevo == null");
                }
                else logger.info("brand!=null");
                goodsSpuRetVo.setBrand(brandSimpleRetVo);
            } else {
                BrandSimpleRetVo brandSimpleRetVo = new BrandSimpleRetVo();
                brandSimpleRetVo.setId(0l);
                goodsSpuRetVo.setBrand(brandSimpleRetVo);
            }

            GoodsCategory goodsCategory = goodsCategoryDao.getCategoryById(goodsSpuPo.getCategoryId()).getData();
            if(goodsCategory != null ){
                GoodsCategorySimpleVo goodsCategorySimpleVo = goodsCategory.createSimpleVo();
                if (goodsCategorySimpleVo == null){
                    logger.info("simplevo == null");
                }
                else logger.info("brand!=null");
                goodsSpuRetVo.setCategory(goodsCategorySimpleVo);
            } else {
                GoodsCategorySimpleVo goodsCategorySimpleVo = new GoodsCategorySimpleVo();
                goodsCategorySimpleVo.setId(0l);
                goodsSpuRetVo.setCategory(goodsCategorySimpleVo);
            }

//            Long freightId = goodsSpuPo.getFreightId();
//            FreightModel freightModel = orderService.getFreightModelById(freightId);
//            if(freightModel != null){
//                goodsSpuRetVo.setFreightModel(freightModel);
//            }

            Spec s = new Spec();
            String specJson = goodsSpuPo.getSpec();
            if(specJson!=null && (!specJson.contentEquals("default")))
            {
                s.setName(JacksonUtil.parseObject(specJson,"name",String.class));
                s.setId(JacksonUtil.parseObject(specJson,"id",Long.class));
                List<SpecItems> l = JacksonUtil.parseObjectList(specJson,"specItems",SpecItems.class);
                s.setSpecItems(JacksonUtil.parseObjectList(specJson,"specItems",SpecItems.class));
                goodsSpuRetVo.setSpec(s);
            }


            List<GoodsSkuPo> Skus = goodsSkuDao.getSkuBySpuId(goodsSpuPo.getId());
            List<GoodsSkuSimpleRetVo> retSkus = new ArrayList<>();
            for( GoodsSkuPo goodsSkuPo : Skus) {
                GoodsSkuSimpleRetVo goodsSkuSimpleRetVo = new GoodsSkuSimpleRetVo(new GoodsSku(goodsSkuPo));
                goodsSkuSimpleRetVo.setPrice(goodsSkuService.getPriceById(goodsSkuPo.getId()));
                retSkus.add(goodsSkuSimpleRetVo);
            }
            goodsSpuRetVo.setSku(retSkus);
            goodsSpuRetVo.setId(goodsSpuPo.getId());
            goodsSpuRetVo.setGoodsSn(goodsSpuPo.getGoodsSn());
            goodsSpuRetVo.setDetail(goodsSpuPo.getDetail());
            goodsSpuRetVo.setImageUrl(goodsSpuPo.getImageUrl());
            if(goodsSpuPo.getDisabled()==(byte)1){
                goodsSpuRetVo.setDisabled(true);
            } else {
                goodsSpuRetVo.setDisabled(false);
            }
            goodsSpuRetVo.setGmtModified(goodsSpuPo.getGmtModified());
            goodsSpuRetVo.setGmtCreate(goodsSpuPo.getGmtCreate());
            returnObject = new ReturnObject<> (goodsSpuRetVo);
        } else {
            logger.info("findSpuById: Not Found");
            returnObject = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        return returnObject;

    }

    /**
    * @Description: 插入SPU
    * @Param: [goodsSpu, id]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 19:48
    */
    
    public ReturnObject<GoodsSpuRetVo> insertGoodsSpu(GoodsSpu goodsSpu, Long id) {

        ReturnObject<GoodsSpuRetVo> retObj = null;

        goodsSpu.setShopId(id);
        GoodsSpu spu = goodsSpuDao.insertGoodsSpu(goodsSpu).getData();
        GoodsSpuRetVo goodsSpuRetVo = spu.createVo();
        retObj = new ReturnObject<>(goodsSpuRetVo);
        return retObj;
    }

    /**
    * @Description: 更新Spu
    * @Param: [vo, shopId, id]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 20:36
    */

    @Transactional
    public ReturnObject<VoObject> updateSpu(GoodsSpu vo,Long shopId,Long id){
        if (goodsSpuDao.checkSpuIdInShop(shopId,id)) {
            vo.setShopId(shopId);
            vo.setId(id);
            return goodsSpuDao.updateSpu(vo);
        } else {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
    }

    @Transactional
    
    public ReturnObject<VoObject> removeSpuCategory(Long shopId,Long spuId, Long id) {
        return goodsSpuDao.deleteGoodsCategory(shopId,spuId,id);
    }

    @Transactional
    
    public ReturnObject<VoObject> addSpuCategory(Long shopId,Long spuId, Long id) {
        return goodsSpuDao.insertGoodsCategory(shopId,spuId,id);
    }

    
    @Transactional
    public ReturnObject<VoObject> revokeSpu( Long shopId, Long id){
        ReturnObject<VoObject> ret =  goodsSpuDao.revokeSpu(shopId, id);
        if(ret.getCode() == ResponseCode.OK){
            ReturnObject<VoObject> skuret = goodsSkuDao.setSpuNull(id);
                return skuret;
            }

        return ret;
    }


    
    @Transactional
    public ReturnObject uploadSpuImg(Long id, MultipartFile multipartFile){
        ReturnObject<GoodsSpu> goodsSpuReturnObject = new ReturnObject<>(new GoodsSpu(goodsSpuDao.getGoodsSpuPoById(id).getData()));

        if(goodsSpuReturnObject.getCode() == ResponseCode.RESOURCE_ID_NOTEXIST) {
            return goodsSpuReturnObject;
        }
        GoodsSpu goodsSpu = goodsSpuReturnObject.getData();

        ReturnObject returnObject = new ReturnObject();
        try{
            returnObject = ImgHelper.remoteSaveImg(multipartFile,2,davUsername, davPassword,baseUrl);
            if(returnObject.getCode()!=ResponseCode.OK){
                return returnObject;
            }

            String oldFilename = goodsSpu.getImageUrl();
            goodsSpu.setImageUrl(returnObject.getData().toString());

            ReturnObject updateReturnObject = goodsSpuDao.updateSpu(goodsSpu);
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



    
    public ReturnObject<VoObject> setCategoryDefault(Long id,Long defaultValue){
        return goodsSpuDao.setCategoryIdDefault(id,defaultValue);
    }

    public GoodsCartVo getCartByskuId(Long Sku) {
        return null;
    }

    
    public boolean checkSpuIdInShop(Long shopId, Long spuId) {
        return goodsSpuDao.checkSpuIdInShop(shopId,spuId);
    }

    
    public boolean checkSpuIdDisabled(Long spuId) {
        return goodsSpuDao.checkSpuDisabled(spuId);
    }

    
    @Transactional
    public ReturnObject setSkuDisabledByShopId(Long shopId){
        List<Long> spuIds = goodsSpuDao.setAllSpuDisabledByShopId(shopId);
        if(spuIds!=null && spuIds.size()!=0){
                goodsSkuDao.setSkuDisabledBySpuId(spuIds);
        }
        return new ReturnObject<>();
    }

    
    @Transactional
    public ReturnObject setAllSkuOnShelvesByShopId(Long shopId){
        return goodsSkuDao.setAllSkuOnShelvesByShopId(shopId);
    }

    
    @Transactional
    public ReturnObject setAllSkuOffShelvesByShopId(Long shopId){
        return goodsSkuDao.setAllSkuOffShelvesByShopId(shopId);
    }
}
