package cn.edu.xmu.activity.service;

import cn.edu.xmu.activity.dao.PreSaleDao;
import cn.edu.xmu.activity.model.bo.PreSale;
import cn.edu.xmu.activity.model.po.PreSalePo;
import cn.edu.xmu.activity.model.vo.NewPreSaleVo;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.bo.GoodsSku;
import cn.edu.xmu.goodsservice.model.bo.ShopSimple;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-01 14:25
 */
@Service
public class PreSaleService {
    private Logger logger = LoggerFactory.getLogger(PreSaleService.class);

    @Autowired
    PreSaleDao preSaleDao;

    @DubboReference(check = false,version = "2.2.0",group = "goods-service")
    IGoodsService goodsService;


    @Transactional
    public ReturnObject<List> getPreSaleById(Long shopId, Long id, Byte state) {
        ReturnObject<List> returnObject = preSaleDao.getPreSaleBySpuId(shopId, id, state);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 存在错误则直接返回
            return new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg());
        }
        List<PreSalePo> preSalePos = returnObject.getData();
        List<PreSale> voObjects = new ArrayList<>(preSalePos.size());
        for (PreSalePo preSalePo : preSalePos) {
            GoodsSku goodsSku = goodsService.getSkuById(preSalePo.getGoodsSkuId());
            ShopSimple shopSimple = goodsService.getSimpleShopById(preSalePo.getShopId());
            PreSale preSale= new PreSale(preSalePo, goodsSku, shopSimple);
            voObjects.add(preSale);
        }
        return new ReturnObject<>(voObjects);
    }


    @Transactional
    public ReturnObject<PageInfo<VoObject>> selectAllPreSale(Long shopId, Byte timeline, Long skuId, Integer pageNum, Integer pageSize) {
        ReturnObject<PageInfo<PreSalePo>> returnPreSalePoPage = preSaleDao.selectAllPreSale(shopId, timeline, skuId, pageNum, pageSize);
        if (!returnPreSalePoPage.getCode().equals(ResponseCode.OK)) {
            return new ReturnObject<>(returnPreSalePoPage.getCode(), returnPreSalePoPage.getErrmsg());
        }

        PageInfo<PreSalePo> preSalePosPageInfo = returnPreSalePoPage.getData();
        PageHelper.startPage(pageNum, pageSize);
        List<VoObject> voObjects = new ArrayList<>(preSalePosPageInfo.getSize());
        for (PreSalePo preSalePo : preSalePosPageInfo.getList()) {
            GoodsSku goodsSku =goodsService.getSkuById(preSalePo.getGoodsSkuId());
            ShopSimple shopSimple = goodsService.getSimpleShopById(preSalePo.getShopId());
            VoObject voObject = new PreSale(preSalePo, goodsSku, shopSimple);
            voObjects.add(voObject);
        }

        PageInfo<VoObject> of = PageInfo.of(voObjects);
        of.setPages(preSalePosPageInfo.getPages());
        of.setTotal(preSalePosPageInfo.getTotal());
        of.setPageSize(preSalePosPageInfo.getPageSize());
        of.setTotal(preSalePosPageInfo.getTotal());
        of.setPageNum(preSalePosPageInfo.getPageNum());
        of.setSize(preSalePosPageInfo.getSize());
        return new ReturnObject<PageInfo<VoObject>>(of);
    }


    @Transactional
    public ReturnObject createNewPreSale(NewPreSaleVo vo, Long shopId, Long id) {
        // 检查商品skuId是否为真
        GoodsSku goodsSku = goodsService.getSkuById(id);
        if (goodsSku == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if (shopId.longValue() != goodsService.getShopIdBySpuId(goodsSku.getGoodsSpuId()).longValue()) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 检查商品在beginTime 和 endTime是否参与了活动
        if (preSaleDao.checkPreSaleInActivities(id, vo.getBeginTime(), vo.getEndTime()).getData() != false) {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        if(goodsSku.getInventory() < vo.getQuantity()){
            return new ReturnObject(ResponseCode.SKU_NOTENOUGH);
        }

        ReturnObject<PreSalePo> returnObject = preSaleDao.createNewPreSale(vo, shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 存在错误则直接返回
            return new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg());
        }
        PreSalePo newPreSalePo = returnObject.getData();
        // 如果插入成功,获取信息,GoodsSku 前面已经获取
        ShopSimple shopSimple = goodsService.getSimpleShopById(shopId);
        VoObject vo1 = new PreSale(newPreSalePo, goodsSku, shopSimple).createVo();
        return new ReturnObject<>(vo1);
    }


    @Transactional
    public ReturnObject updatePreSale(NewPreSaleVo newPreSaleVo, Long shopId, Long id) {
        ReturnObject<PreSalePo> returnObject = preSaleDao.getPreSalePoByPreSaleId(id);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 存在错误则直接返回
            return new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg());
        }
        PreSalePo preSalePo = returnObject.getData();
        if(preSalePo == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        if(preSalePo.getShopId().longValue() != shopId.longValue()){
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 确认状态:id存在性和权限以及是否下线
        ReturnObject confirmResult = confirmPreSaleId(preSalePo, shopId, PreSale.State.OFF.getCode());
        if (confirmResult.getCode() != ResponseCode.OK) {
            return confirmResult;
        }
        // 更新后直接返回结果即可
        return preSaleDao.updatePreSale(newPreSaleVo, id);
    }


    @Transactional
    public ReturnObject changePreSaleState(Long shopId, Long id, Byte state) {
        ReturnObject<PreSalePo> returnObject = preSaleDao.getPreSalePoByPreSaleId(id);
        // 存在错误,往上层传
        if (returnObject.getCode() != ResponseCode.OK) {
            return returnObject;
        }
        PreSalePo preSalePo = returnObject.getData();
        // 确认状态:id存在性和权限以及是否下线
        Byte expectState;
        if (state == PreSale.State.ON.getCode() || state == PreSale.State.DELETE.getCode()) {
            expectState = PreSale.State.OFF.getCode();
        } else {
            expectState = PreSale.State.ON.getCode();
        }
        ReturnObject confirmResult = confirmPreSaleId(preSalePo, shopId, expectState);
        if (confirmResult.getCode() != ResponseCode.OK) {
            return confirmResult;
        }
        // 状态相同,改不了,下线的无法再下线,正如上线的无法再上线
        if (returnObject.getData().getState() == state) {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }
        // 错误路径4,专门给上线状态用
        // 如果将要上线的时间段该商品存在其他活动,无法上线,时段冲突
        // 当发生错误时,为了保证不冲突,应该拒绝上线

        if (state == PreSale.State.ON.getCode()) {
            ReturnObject<Boolean> returnResult = preSaleDao.checkPreSaleInActivities(preSalePo.getGoodsSkuId(),
                    preSalePo.getBeginTime(), preSalePo.getEndTime());
            if (returnResult.getCode() != ResponseCode.OK) {
                return new ReturnObject(returnObject.getCode(), returnObject.getErrmsg());
            }
            if (returnResult.getData().equals(false)) {
                return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
            }
        }
        return preSaleDao.changePreSaleState(id, state);
    }



    // 可修改状态检测
    //
    public ReturnObject confirmPreSaleId(PreSalePo preSalePo, Long shopId, Byte expectState) {

        // 错误路径1,该id不存在
        if (preSalePo == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 错误路径2,不是自家活动
        if (preSalePo.getShopId().longValue() != shopId.longValue()) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }

        // 错误路径3,状态不允许,并且目前只需要下线就能修改,不需要管改的结果
        // 时段冲突也不需要考虑,因为在下线状态,考虑的事情,扔给上线吧
        // 参数校验方面 Vo检测未来 Controller检测开始大于结束
        if (preSalePo.getState() != expectState) {
            return new ReturnObject(ResponseCode.PRESALE_STATENOTALLOW);
        }

        // 校验成功,通过
        return new ReturnObject(ResponseCode.OK);
    }


    /****************************************/


}