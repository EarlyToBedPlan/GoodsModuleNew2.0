package cn.edu.xmu.activity.service;


import cn.edu.xmu.activity.dao.GrouponDao;
import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.po.GrouponPo;
import cn.edu.xmu.activity.model.po.GrouponPo;
import cn.edu.xmu.activity.model.vo.NewGrouponVo;
import cn.edu.xmu.goodsservice.client.IGoodsService;
import cn.edu.xmu.goodsservice.model.bo.GoodsSimpleSpu;
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
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-02 01:26
 */
@Service
public class GrouponService {
    private Logger logger = LoggerFactory.getLogger(GrouponService.class);

    @Autowired
    GrouponDao grouponDao;

    @DubboReference(check = false, version = "2.2.0", group = "goods-service")
    IGoodsService goodsService;

    /**
     * 分页查询所有团购活动
     *
     * @param pageNum  页数
     * @param pageSize 每页大小
     * @return ReturnObject<PageInfo < VoObject>> 分页返回团购信息
     * @author LJP_3424
     */
    public ReturnObject<PageInfo<VoObject>> selectAllGroupon(Long shopId, Byte timeline, Long spuId, Integer pageNum, Integer pageSize) {
        ReturnObject<PageInfo<GrouponPo>> returnGrouponPoPage = grouponDao.selectAllGroupon(shopId, timeline, spuId, pageNum, pageSize);
        if (returnGrouponPoPage.getCode().equals(ResponseCode.OK) == false) {
            return new ReturnObject<>(returnGrouponPoPage.getCode(), returnGrouponPoPage.getErrmsg());
        }

        PageInfo<GrouponPo> grouponPosPageInfo = returnGrouponPoPage.getData();
        PageHelper.startPage(pageNum, pageSize);
        List<VoObject> voObjects = new ArrayList<>(grouponPosPageInfo.getSize());
        for (GrouponPo grouponPo : grouponPosPageInfo.getList()) {
            // 目前暂时关闭 dubbo,后续连接上后再取消
            // GoodsSpu goodsSpu = goodsService.getSpuById(grouponPo.getGoodsSpuId());
            // ShopSimple shopSimple = goodsService.getSimpleShopById(grouponPo.getShopId());
            GoodsSimpleSpu goodsSpu = goodsService.getSimpleSpuById(grouponPo.getGoodsSpuId());
            Long shopIdReturn = goodsService.getShopIdBySpuId(goodsSpu.getId());
            ShopSimple shopSimple = goodsService.getSimpleShopById(shopIdReturn);
            VoObject voObject = new Groupon(grouponPo, goodsSpu, shopSimple);
            voObjects.add(voObject);
        }

        PageInfo<VoObject> of = PageInfo.of(voObjects);
        of.setPages(grouponPosPageInfo.getPages());
        of.setTotal(grouponPosPageInfo.getTotal());
        of.setPageSize(grouponPosPageInfo.getPageSize());
        of.setTotal(grouponPosPageInfo.getTotal());
        of.setPageNum(grouponPosPageInfo.getPageNum());
        of.setSize(grouponPosPageInfo.getSize());
        return new ReturnObject<PageInfo<VoObject>>(of);
    }

    public boolean checkGrouponInActivities(Long goodsSpuId, LocalDateTime beginTime, LocalDateTime endTime) {
        Boolean result = grouponDao.checkGrouponInActivities(goodsSpuId, beginTime, endTime).getData();
        if (result == null) {
            return false;
        }
        return result;
    }

    /**
     * 分页查询所有团购(包括下线的)
     *
     * @param pageNum  页数
     * @param pageSize 每页大小
     * @return ReturnObject<PageInfo < VoObject>> 分页返回团购信息
     * @author LJP_3424
     */
    public ReturnObject<PageInfo<VoObject>> selectGroupon(Long shopId, Byte state, Long spuId, LocalDateTime beginTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        ReturnObject<PageInfo<GrouponPo>> returnGrouponPoPage = grouponDao.selectGroupon(shopId, state, spuId, beginTime, endTime, pageNum, pageSize);
        if (!returnGrouponPoPage.getCode().equals(ResponseCode.OK)) {
            return new ReturnObject<>(returnGrouponPoPage.getCode(), returnGrouponPoPage.getErrmsg());
        }

        PageInfo<GrouponPo> grouponPosPageInfo = returnGrouponPoPage.getData();
        PageHelper.startPage(pageNum, pageSize);
        List<VoObject> voObjects = new ArrayList<>(grouponPosPageInfo.getSize());
        for (GrouponPo grouponPo : grouponPosPageInfo.getList()) {
            GoodsSimpleSpu goodsSimpleSpu =goodsService.getSimpleSpuById(grouponPo.getGoodsSpuId());
            ShopSimple shopSimple = goodsService.getSimpleShopById(grouponPo.getShopId());
            VoObject voObject = new Groupon(grouponPo, goodsSimpleSpu, shopSimple);
            voObjects.add(voObject);
        }

        PageInfo<VoObject> of = PageInfo.of(voObjects);
        of.setPages(grouponPosPageInfo.getPages());
        of.setTotal(grouponPosPageInfo.getTotal());
        of.setPageSize(grouponPosPageInfo.getPageSize());
        of.setTotal(grouponPosPageInfo.getTotal());
        of.setPageNum(grouponPosPageInfo.getPageNum());
        of.setSize(grouponPosPageInfo.getSize());
        return new ReturnObject<PageInfo<VoObject>>(of);
    }

    @Transactional
    public ReturnObject<List> getGrouponBySpuId(Long id, Byte state) {
        try {
            List<GrouponPo> grouponPos = grouponDao.getGrouponById(id, state);
            List<VoObject> ret = new ArrayList<>(grouponPos.size());
            for (GrouponPo grouponPo : grouponPos) {
                VoObject voObject = new Groupon(grouponPo).createSimpleVo();
                ret.add(voObject);
            }
            return new ReturnObject<List>(ret);
        } catch (DataAccessException e) {
            logger.error("selectAllGroupon: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }


    /**
     * @Description: 向dao层传入vo来插入新活动
     * @Param: No such property: code for class: Script1
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:24
     */
    @Transactional
    public ReturnObject createNewGroupon(NewGrouponVo vo, Long shopId, Long id) {
        GoodsSimpleSpu goodsSimpleSpu = goodsService.getSimpleSpuById(id);
        // 错误路径1.1: id不存在
        if (goodsSimpleSpu == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 错误路径1.2:shop不存在
        ShopSimple shopSimple = goodsService.getSimpleShopById(shopId);
        if(shopSimple == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 错误路径2: 操作的id不是自家的
        if (goodsService.getShopIdBySpuId(id).longValue() != shopId.longValue()) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 错误路径3: 时间段内已经参与了其他活动
        if (checkGrouponInActivities(id, vo.getBeginTime(), vo.getEndTime())) {
            return new ReturnObject(ResponseCode.TIMESEG_CONFLICT);
        }


        // 插入操作
        ReturnObject<GrouponPo> returnObject = grouponDao.creatNewGroupon(vo, shopId, id);
        if (returnObject.getCode() != ResponseCode.OK) {
            // 存在错误则直接返回
            return new ReturnObject<>(returnObject.getCode(), returnObject.getErrmsg());
        }

        GrouponPo grouponPo = returnObject.getData();
        // 正常路径

        VoObject voObject = new Groupon(grouponPo, goodsSimpleSpu, shopSimple).createVo();
        return new ReturnObject(voObject);
    }



    /**
     * @Description: 传入VO更新团购活动信息
     * @Param: No such property: code for class: Script1
     * @return: cn.edu.xmu.ooad.util.ReturnObject
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:24
     */
    @Transactional
    public ReturnObject updateGroupon(NewGrouponVo newGrouponVo, Long shopId, Long id) {
        ReturnObject<GrouponPo> returnObject = grouponDao.getGrouponPoByGrouponId(id);
        // 存在错误,往上层传
        if (returnObject.getCode() == ResponseCode.INTERNAL_SERVER_ERR) {
            return returnObject;
        }
        GrouponPo grouponPo = returnObject.getData();
        if(grouponPo == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        ShopSimple simpleShopById = goodsService.getSimpleShopById(shopId);
        if(simpleShopById == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        // 商店 和id
        if(grouponPo.getShopId().longValue() != shopId.longValue()){
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        // 确认状态:id存在性和权限以及是否下线
        ReturnObject confirmResult = confirmGrouponId(grouponPo, shopId, Groupon.State.OFF.getCode());
        if (confirmResult.getCode() != ResponseCode.OK) {
            return confirmResult;
        }
        // 更新后直接返回结果即可
        return grouponDao.updateGroupon(newGrouponVo, id);
    }


    public ReturnObject goodsGetTest(){
        GoodsSku skuById = goodsService.getSkuById(280L);
        ShopSimple simpleShopById = goodsService.getSimpleShopById(290L);
        GoodsSimpleSpu simpleSpuById = goodsService.getSimpleSpuById(274L);
        return new ReturnObject(simpleShopById);
    }

    /**
     * @Description: 更改团购状态
     * @Param: No such property: code for class: Script1
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.lang.Object>
     * @Author: LJP_3424
     * @Date: 2020/12/5 1:06
     */
    @Transactional
    public ReturnObject changeGrouponState(Long shopId, Long id, Byte state) {
        ReturnObject<GrouponPo> returnObject = grouponDao.getGrouponPoByGrouponId(id);
        // 存在错误,往上层传
        if (returnObject.getCode() != ResponseCode.OK) {
            return returnObject;
        }
        GrouponPo grouponPo = returnObject.getData();
        if(grouponPo == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }
        ShopSimple simpleShopById = goodsService.getSimpleShopById(shopId);
        if(simpleShopById == null){
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 确认状态:id存在性和权限以及是否下线
        Byte expectState;
        if (state == Groupon.State.ON.getCode() || state == Groupon.State.DELETE.getCode()) {
            expectState = Groupon.State.OFF.getCode();
        } else {
            expectState = Groupon.State.ON.getCode();
        }
        // id 与商店
        if(grouponPo.getShopId().longValue() != shopId.longValue()){
            return new ReturnObject(ResponseCode.RESOURCE_ID_OUTSCOPE);
        }
        ReturnObject confirmResult = confirmGrouponId(grouponPo, shopId, expectState);
        if (confirmResult.getCode() != ResponseCode.OK) {
            return new ReturnObject(confirmResult.getCode(),confirmResult.getErrmsg());
        }

        // 状态相同,改不了,下线的无法再下线,正如上线的无法再上线
        if (returnObject.getData().getState() == state) {
            return new ReturnObject(ResponseCode.GROUPON_STATENOTALLOW);
        }
        // 错误路径4,专门给上线状态用
        // 如果将要上线的时间段该商品存在其他活动,无法上线,时段冲突
        if (state == Groupon.State.ON.getCode() && grouponDao.checkGrouponInActivities(grouponPo.getGoodsSpuId(), grouponPo.getBeginTime(), grouponPo.getEndTime()).getData() == true) {
            return new ReturnObject(ResponseCode.TIMESEG_CONFLICT);
        }
        return grouponDao.changeGrouponState(id, state);
    }


    private ReturnObject<PageInfo<VoObject>> changeListIntoPage(List<GrouponPo> grouponPos, Integer pageNum, Integer pageSize) {
        List<VoObject> ret = new ArrayList<>(grouponPos.size());
        for (GrouponPo grouponPo : grouponPos) {
            VoObject voObject = new Groupon(grouponPo).createSimpleVo();
            ret.add(voObject);
        }
        PageHelper.startPage(pageNum, pageSize);
        PageInfo<VoObject> of = PageInfo.of(ret);
        return new ReturnObject<PageInfo<VoObject>>(of);
    }


    /*************通用函数区域*************/

    // 可修改状体检测
    public ReturnObject confirmGrouponId(GrouponPo grouponPo, Long shopId, Byte expectState) {

        // 错误路径1,该id不存在
        if (grouponPo == null) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }

        // 错误路径3,状态不允许,并且目前只需要下线就能修改,不需要管改的结果
        // 时段冲突也不需要考虑,因为在下线状态,考虑的事情,扔给上线吧
        // 参数校验方面 Vo检测未来 Controller检测开始大于结束
        if (grouponPo.getState() != expectState) {
            return new ReturnObject(ResponseCode.GROUPON_STATENOTALLOW);
        }

        // 错误路径2,不是自家活动
        if (grouponPo.getShopId().longValue() != shopId.longValue()) {
            return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
        }


        // 校验成功,通过
        return new ReturnObject(ResponseCode.OK);
    }


/*************通用函数结束************/
}