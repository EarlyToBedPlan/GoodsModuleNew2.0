package cn.edu.xmu.goods.dao;

import cn.edu.xmu.goods.mapper.BrandPoMapper;
import cn.edu.xmu.goods.mapper.GoodsCategoryPoMapper;
import cn.edu.xmu.goods.mapper.GoodsSkuPoMapper;
import cn.edu.xmu.goods.mapper.GoodsSpuPoMapper;
import cn.edu.xmu.goods.model.bo.GoodsSpu;
import cn.edu.xmu.goods.model.po.*;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * SPU Dao
 * @author Yancheng Lai
 * createdBy Yancheng Lai 2020/12/1 19:55
 * modifiedBy Yancheng Lai 19:55
 **/

@Repository
public class GoodsSpuDao {

    private static final Logger logger = LoggerFactory.getLogger(GoodsSpuDao.class);

    @Autowired
    private GoodsSkuPoMapper goodsSkuPoMapper;

    @Autowired
    private GoodsSpuPoMapper goodsSpuPoMapper;

    @Autowired
    private BrandPoMapper brandPoMapper;

    @Autowired
    private GoodsCategoryPoMapper goodsCategoryPoMapper;

    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    /**
    * @Description:  查询GoodsSpuPo以id
    * @Param: [id]
    * @return: GoodsSpu
    * @Author: Yancheng Lai
    * @Date: 2020/12/1 22:18
    */
    public ReturnObject<GoodsSpuPo> getGoodsSpuPoById(Long id) {
        //以后改成selectbyprimarykey

        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        List<GoodsSpuPo> goodsSpuPos = null;
        try {
            goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            StringBuilder message = new StringBuilder().append("getGoodsSpuPoById: ").append(e.getMessage());
            logger.error(message.toString());
        }
        if (null == goodsSpuPos || goodsSpuPos.isEmpty()) {
            return new ReturnObject<>();
        } else {
                return new ReturnObject<>(goodsSpuPos.get(0));
            }
        }
    /**
    * @Description: 新增SPU
    * @Param: [goodsSpu]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 19:50
    */
    public boolean checkSpuDisabled(Long spuId){
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
        if(goodsSpuPo.getDisabled() == 0 ){
            return false;
        }
        return true;
    }

    public ReturnObject<GoodsSpu> insertGoodsSpu(GoodsSpu goodsSpu) {
        GoodsSpuPo goodsSpuPo = goodsSpu.getGoodsSpuPo();
        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andSpecEqualTo(goodsSpu.getSpec());
        criteria.andNameEqualTo(goodsSpu.getName());
        criteria.andDetailEqualTo(goodsSpu.getDetail());
        ReturnObject<GoodsSpu> retObj = null;
        try{
            goodsSpuPo.setGmtModified(LocalDateTime.now());
            goodsSpuPo.setGmtCreate(LocalDateTime.now());
            goodsSpuPo.setGoodsSn("spu-"+randomUUID());
            int ret = goodsSpuPoMapper.insertSelective(goodsSpuPo);
            if (ret == 0) {
                retObj = new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST, String.format("Insert false：" + goodsSpuPo.getName()));
            } else {
                List<GoodsSpuPo> retSpu = goodsSpuPoMapper.selectByExample(example);
                retObj = new ReturnObject<>(new GoodsSpu(retSpu.get(0)));
            }
        }
        catch (DataAccessException e) {
            retObj = new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("Database Eoor: %s", e.getMessage()));
        }
        return retObj;
    }
    /**
    * @Description: 用skuid查询SPU
    * @Param: [id]
    * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.goods.model.po.GoodsSpuPo>
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 19:50
    */
    public ReturnObject<GoodsSpuPo> getGoodsSpuPoBySkuId(Long id) {

        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        List<GoodsSpuPo> goodsSpuPos = null;
        try {
            goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            StringBuilder message = new StringBuilder().append("getGoodsSpuPoById: ").append(e.getMessage());
            logger.error(message.toString());
        }
        if (null == goodsSpuPos ) {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        } else {
            return new ReturnObject<>(goodsSpuPos.get(0));
        }
    }
    /**
    * @Description: 查询SPUId是否在商店内
    * @Param: [shopId, spuId]
    * @return: boolean
    * @Author: Yancheng Lai
    * @Date: 2020/12/3 19:55
    */
    public boolean checkSpuIdInShop(Long shopId, Long spuId) {
        if(shopId == 0){
            return true;
        }
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
        if (goodsSpuPo == null) {
            return false;
        }
        if (goodsSpuPo.getShopId() != shopId){
            return false;
        }
        return true;
    }

    public ReturnObject<VoObject> updateSpu(GoodsSpu goodsSpu){
        GoodsSpuPo po = goodsSpuPoMapper.selectByPrimaryKey(goodsSpu.getId());
        if(po == null || po.getDisabled() == 1)
        {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(goodsSpu.getShopId() != po.getShopId()){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        goodsSpu.setGmtModified(LocalDateTime.now());
        goodsSpu.setDisabled((byte)0);
        int res =  goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpu.getGoodsSpuPo());
        return new ReturnObject<VoObject>();
    }


    public ReturnObject<VoObject> revokeSpu(Long shopId, Long id){
        GoodsSpuPoExample goodsSpuPoExample =  new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = goodsSpuPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(id);
        //shopid或spuid不存在
        if ( goodsSpuPo == null || goodsSpuPo.getDisabled() == 1) {
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(checkSpuIdInShop(shopId,id)==false){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        try {
            //int state = goodsSkuPoMapper.deleteByPrimaryKey(id);
            goodsSpuPo.setDisabled((byte)1);
            goodsSpuPo.setGmtModified(LocalDateTime.now());
            int state = goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
            if (state == 0){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }

        } catch (DataAccessException e) {
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Database Exception：%s", e.getMessage()));
        } catch (Exception e) {
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Unknown Exception：%s", e.getMessage()));
        }
        return new ReturnObject<>();
    }

    /**
    * @Description:  商品插入品牌
    * @Param: [shopId, spuId, id]
    * @return: ReturnObject<VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 14:36
    */

    public ReturnObject<VoObject>insertGoodsBrand(Long shopId,Long spuId,Long id)
    {
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
            BrandPo brandPo = brandPoMapper.selectByPrimaryKey(id);
            if(brandPo == null || goodsSpuPo == null  ||goodsSpuPo.getDisabled()!= 0){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(checkSpuIdInShop(shopId,spuId)==false){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            goodsSpuPo.setBrandId(id);
            goodsSpuPo.setGmtModified(LocalDateTime.now());
            goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
            return new ReturnObject<>();

    }

    /**
    * @Description: 商品删除品牌
    * @Param: [shopId, spuId, id]
    * @return: ReturnObject<VoObject>
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 14:36
    */

    public ReturnObject<VoObject>deleteGoodsBrand(Long shopId,Long spuId,Long id)
    {
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
            BrandPo brandPo = brandPoMapper.selectByPrimaryKey(id);
            if(brandPo == null || goodsSpuPo == null||goodsSpuPo.getBrandId() != id||goodsSpuPo.getDisabled()!= 0){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
            if(checkSpuIdInShop(shopId,spuId)==false){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
            }
            goodsSpuPo.setBrandId(0l);
            goodsSpuPo.setGmtModified(LocalDateTime.now());
            goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
            return new ReturnObject<>();

    }
    /** 
    * @Description: 改变运费模板 
    * @Param: [FreightId, defaultFreightId] 
    * @return: ReturnObject 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 21:10
    */
    public ReturnObject changeGoodsFreightWeight(Long FreightId, Long defaultFreightId){
        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andFreightIdEqualTo(FreightId);

        try{
            List<GoodsSpuPo> goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
            if (goodsSpuPos == null) {
                //not found
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                try {
                    for (GoodsSpuPo goodsSpuPo : goodsSpuPos){
                        goodsSpuPo.setFreightId(defaultFreightId);
                        goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
                    }
                } catch (DataAccessException e) {
                    //database error
                    return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                            String.format("Database Exception: %s", e.getMessage()));
                } catch (Exception e) {
                    return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                            String.format("Unknown Exception: %s", e.getMessage()));
                }
            }
        }
        catch (DataAccessException e) {
            //database error
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Database Exception: %s", e.getMessage()));
        } catch (Exception e) {
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Unknown Exception: %s", e.getMessage()));
        }


        return new ReturnObject<>(ResponseCode.OK);
    }

    /** 
    * @Description: 种类清空 
    * @Param: [CategoryId, defaultId] 
    * @return: ReturnObject 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 21:10
    */
    public ReturnObject setCategoryIdDefault(Long CategoryId, Long defaultId){
        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(CategoryId);

        try{
            List<GoodsSpuPo> goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
            if (goodsSpuPos == null || goodsSpuPos.size()==0) {
                //not found
                //return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
                return new ReturnObject<>(ResponseCode.OK);
            } else {
                try {
                    for (GoodsSpuPo goodsSpuPo : goodsSpuPos){
                        goodsSpuPo.setCategoryId(defaultId);
                        goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
                    }
                } catch (DataAccessException e) {
                    //database error
                    return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                            String.format("Database Exception: %s", e.getMessage()));
                } catch (Exception e) {
                    return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                            String.format("Unknown Exception: %s", e.getMessage()));
                }
            }
        }
        catch (DataAccessException e) {
            //database error
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Database Exception: %s", e.getMessage()));
        } catch (Exception e) {
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Unknown Exception: %s", e.getMessage()));
        }


        return new ReturnObject<>(ResponseCode.OK);
    }
    /** 
    * @Description:  种类id查商品SPU
    * @Param: [id] 
    * @return: ReturnObject<List<GoodsSpuPo>> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 21:09
    */
    public ReturnObject<List<GoodsSpuPo>> getGoodsSpuPosByCategoryId(Long id){
        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(id);
        List<GoodsSpuPo> goodsSpuPos = null;
        try{
            goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
            if( goodsSpuPos == null){
                return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
            } else {
                return new ReturnObject<>(goodsSpuPos);
            }
        } catch (DataAccessException e) {
            //database error
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Database Exception: %s", e.getMessage()));
        } catch (Exception e) {
            return new ReturnObject(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("Unknown Exception: %s", e.getMessage()));
        }
    }
    /** 
    * @Description: 新增商品种类 
    * @Param: [shopId, spuId, id] 
    * @return: ReturnObject<VoObject> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 21:09
    */
    public ReturnObject<VoObject>insertGoodsCategory(Long shopId,Long spuId,Long id)
    {
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
        GoodsCategoryPo goodsCategoryPo = goodsCategoryPoMapper.selectByPrimaryKey(id);
        if(goodsCategoryPo == null || goodsSpuPo == null||goodsSpuPo.getDisabled()!= 0){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(checkSpuIdInShop(shopId,spuId)==false){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        goodsSpuPo.setCategoryId(id);
        goodsSpuPo.setGmtModified(LocalDateTime.now());
        goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
        return new ReturnObject<>();
    }
    /** 
    * @Description: 删除商品种类 
    * @Param: [shopId, spuId, id] 
    * @return: ReturnObject<VoObject> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/11 21:09
    */
    public ReturnObject<VoObject>deleteGoodsCategory(Long shopId,Long spuId,Long id)
    {
        GoodsSpuPo goodsSpuPo = goodsSpuPoMapper.selectByPrimaryKey(spuId);
        GoodsCategoryPo goodsCategoryPo = goodsCategoryPoMapper.selectByPrimaryKey(id);
        if(goodsCategoryPo == null || goodsSpuPo == null || goodsSpuPo.getDisabled()==1|| goodsSpuPo.getCategoryId()!=id||goodsSpuPo.getDisabled()!= 0){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(checkSpuIdInShop(shopId,spuId)==false){
            return new ReturnObject<>(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        goodsSpuPo.setCategoryId(0l);
        goodsSpuPo.setGmtModified(LocalDateTime.now());
        goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
        return new ReturnObject<>();

    }
    /** 
    * @Description: shop里面的spu置disabled=1返回list 
    * @Param: [shopId] 
    * @return: java.util.List<java.lang.Long> 
    * @Author: Yancheng Lai
    * @Date: 2020/12/12 22:33
    */

    public List<Long> setAllSpuDisabledByShopId(Long shopId){
        GoodsSpuPoExample example = new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = example.createCriteria();
        criteria.andShopIdEqualTo(shopId);
        List<Long> ret = new ArrayList<>();
        List<GoodsSpuPo> goodsSpuPos = goodsSpuPoMapper.selectByExample(example);
        if(goodsSpuPos != null && goodsSpuPos.size()>0){
            for(GoodsSpuPo goodsSpuPo: goodsSpuPos){
                if(goodsSpuPo.getDisabled()==0){
                    goodsSpuPo.setDisabled((byte)1);
                    goodsSpuPoMapper.updateByPrimaryKeySelective(goodsSpuPo);
                    ret.add(goodsSpuPo.getId());
                }
            }
        }
        return ret;
    }
    /**
    * @Description: 查询spu
    * @Param: [shopId, spuId, spuSn]
    * @return: java.util.List<java.lang.Long>
    * @Author: Yancheng Lai
    * @Date: 2020/12/18 16:55
    */
    public List<Long> queryOnSpu(Long shopId,Long spuId,String spuSn){
        GoodsSpuPoExample goodsSpuPoExample =  new GoodsSpuPoExample();
        GoodsSpuPoExample.Criteria criteria = goodsSpuPoExample.createCriteria();
        if(shopId != -1){
          criteria.andShopIdEqualTo(shopId);
        }
        if(spuId != -1){
            criteria.andIdEqualTo(spuId);
        }
        if(!spuSn.contentEquals("default")){
            criteria.andGoodsSnEqualTo(spuSn);
        }
        List<GoodsSpuPo> goodsSpuPos = goodsSpuPoMapper.selectByExample(goodsSpuPoExample);
        List<Long> res = new ArrayList<>();
        for(GoodsSpuPo goodsSpuPo : goodsSpuPos){
            res.add(goodsSpuPo.getId());
        }
        return res;
    }
}
