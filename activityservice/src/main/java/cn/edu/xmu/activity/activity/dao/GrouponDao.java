package cn.edu.xmu.activity.dao;


import cn.edu.xmu.activity.mapper.GrouponPoMapper;
import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.bo.Groupon;
import cn.edu.xmu.activity.model.po.*;
import cn.edu.xmu.activity.model.po.GrouponPo;
import cn.edu.xmu.activity.model.po.GrouponPoExample;
import cn.edu.xmu.activity.model.vo.NewGrouponVo;
import cn.edu.xmu.ooad.model.VoObject;
import cn.edu.xmu.ooad.util.ResponseCode;
import cn.edu.xmu.ooad.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author LJP_3424
 * @create 2020-12-01 9:59
 */
@Repository
public class GrouponDao implements InitializingBean {

    @Autowired
    private GrouponPoMapper grouponPoMapper;

    private static final Logger logger = LoggerFactory.getLogger(GrouponDao.class);


    /**
     * @param goodsSpuId
     * @param beginTime
     * @param endTime
     * @return
     */
    // 20201215: 增加了判断状态,只有上线才会发生冲突
    public ReturnObject<Boolean> checkGrouponInActivities(Long goodsSpuId, LocalDateTime beginTime, LocalDateTime endTime) {
        GrouponPoExample example = new GrouponPoExample();
        GrouponPoExample.Criteria criteria1 = example.createCriteria();
        criteria1.andEndTimeGreaterThan(beginTime);
        criteria1.andBeginTimeLessThan(endTime);
        criteria1.andGoodsSpuIdEqualTo(goodsSpuId);
        criteria1.andStateEqualTo(Groupon.State.ON.getCode());
        List<GrouponPo> grouponPos = null;
        try {
            // 这里使用select,实际上自己写count可以得到更高的效率
            grouponPos = grouponPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            logger.error("selectAllGroupon: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        //返回为空则不存在,返回false ,不为空说明查询到了
        if (grouponPos == null || grouponPos.size() == 0) {
            return new ReturnObject<>(false);
        } else {
            return new ReturnObject<>(true);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }


    /**
     * @Description: 分页筛选查询团购活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:19
     */
    public ReturnObject<PageInfo<GrouponPo>> selectAllGroupon(Long shopId, Byte timeline, Long spuId, Integer pageNum, Integer pageSize) {
        GrouponPoExample example = new GrouponPoExample();
        GrouponPoExample.Criteria criteria = example.createCriteria();
        LocalDateTime tomorrow;
        if (timeline != null) {
            switch (timeline) {
                case 0:
                    //
                    criteria.andBeginTimeGreaterThan(LocalDateTime.now());
                    break;
                case 1:
                    // 明天
                    tomorrow = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN).minusDays(-1);
                    criteria.andBeginTimeBetween(tomorrow, tomorrow.minusDays(-1));
                    break;
                case 2:
                    criteria.andBeginTimeLessThanOrEqualTo(LocalDateTime.now());
                    criteria.andEndTimeGreaterThan(LocalDateTime.now());
                    break;
                case 3:
                    criteria.andEndTimeLessThan(LocalDateTime.now());
                    break;
            }
        }
        if (shopId != null) criteria.andShopIdEqualTo(shopId);
        if (spuId != null) criteria.andGoodsSpuIdEqualTo(spuId);

        criteria.andStateNotEqualTo(Groupon.State.DELETE.getCode());
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        try {
            //不加限定条件查询所有
            List<GrouponPo> grouponPos = grouponPoMapper.selectByExample(example);
            return new ReturnObject(PageInfo.of(grouponPos));
        } catch (DataAccessException e) {
            logger.error("selectAllGroupon: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }

    private List<GrouponPo> selectByExample(GrouponPoExample example, Integer pageNum, Integer pageSize) {
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<GrouponPo> grouponPos = null;
        try {
            //不加限定条件查询所有
            grouponPos = grouponPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            logger.error("selectAllGroupon: DataAccessException:" + e.getMessage());
            //return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            //return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return grouponPos;
    }

    /**
     * @Description: 分页查询团购活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject<com.github.pagehelper.PageInfo < cn.edu.xmu.ooad.model.VoObject>>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:20
     */
    public ReturnObject<PageInfo<GrouponPo>> selectGroupon(Long shopId, Byte state, Long spuId, LocalDateTime beginTime, LocalDateTime endTime, Integer pageNum, Integer pageSize) {
        GrouponPoExample example = new GrouponPoExample();
        GrouponPoExample.Criteria criteria = example.createCriteria();
        criteria.andShopIdEqualTo(shopId);
        if (state != null) criteria.andStateEqualTo(state);
        if (spuId != null) criteria.andGoodsSpuIdEqualTo(spuId);
        criteria.andBeginTimeGreaterThanOrEqualTo(beginTime);
        criteria.andEndTimeLessThanOrEqualTo(endTime);
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        try {
            List<GrouponPo> grouponPos = grouponPoMapper.selectByExample(example);
            return new ReturnObject(PageInfo.of(grouponPos));
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
     * @Description: 根绝ID查询团购活动
     * @Param: No such property: code for class: Script1
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.util.List>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:21
     */
    public List<GrouponPo> getGrouponById(Long id, Byte state) {
        GrouponPoExample example = new GrouponPoExample();
        GrouponPoExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsSpuIdEqualTo(id);
        if (state != null) criteria.andStateEqualTo(state);
        logger.debug("find Groupon By Id: Id = " + id + ";state = " + state);
        List<GrouponPo> grouponPos = null;
        grouponPos = grouponPoMapper.selectByExample(example);
        return grouponPos;
    }

    /**
     * @Description: vo创建Po后插入
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.util.List>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:22
     */
    public ReturnObject<GrouponPo> creatNewGroupon(NewGrouponVo vo, Long shopId, Long id) {
        GrouponPo grouponPo = vo.createGrouponPo();
        grouponPo.setGoodsSpuId(id);
        grouponPo.setShopId(shopId);
        grouponPo.setGmtCreate(LocalDateTime.now());
        grouponPo.setState(Groupon.State.ON.getCode());
        try {
            int insert = grouponPoMapper.insert(grouponPo);
            if (insert == 0) {
                return new ReturnObject<>(null);
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        return new ReturnObject<>(grouponPo);
    }

    public ReturnObject<GrouponPo> getGrouponPoByGrouponId(Long id) {
        GrouponPo grouponPo = null;
        try {
            grouponPo = grouponPoMapper.selectByPrimaryKey(id);
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        return new ReturnObject<>(grouponPo);
    }


    /**
     * @Description: 修改团购活动信息
     * @return: cn.edu.xmu.ooad.util.ReturnObject<cn.edu.xmu.ooad.model.VoObject>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:22
     */
    public ReturnObject updateGroupon(NewGrouponVo grouponVo, Long id) {
        GrouponPo po = new GrouponPo();
        po.setId(id);
        po.setStrategy(grouponVo.getStrategy());
        po.setBeginTime(grouponVo.getBeginTime());
        po.setEndTime(grouponVo.getEndTime());
        po.setGmtModified(LocalDateTime.now());
        return updateValue(po);
    }

    /**
     * @Description:更改团购活动状态来逻辑删除团购活动
     * @return: cn.edu.xmu.ooad.util.ReturnObject<java.lang.Object>
     * @Author: LJP_3424
     * @Date: 2020/12/5 23:22
     */
    public ReturnObject changeGrouponState(Long id, Byte state) {

        // 这里采用部分更新,防止数据更改
        GrouponPo grouponPo = new GrouponPo();
        grouponPo.setId(id);
        grouponPo.setState(state);
        return updateValue(grouponPo);
    }

    /**
     * 删除和更新都用update,两者结构相同,数据处理好后直接调用这个即可
     */
    private ReturnObject updateValue(GrouponPo grouponPo) {
        try {
            int ret = grouponPoMapper.updateByPrimaryKeySelective(grouponPo);
            if (ret == 1) {
                return new ReturnObject(ResponseCode.OK);
            } else {
                return new ReturnObject(ResponseCode.RESOURCE_ID_NOTEXIST);
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ResponseCode.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }
}