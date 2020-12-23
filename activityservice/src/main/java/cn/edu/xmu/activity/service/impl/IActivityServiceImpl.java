package cn.edu.xmu.activity.service.impl;


import cn.edu.xmu.activity.dao.FlashSaleDao;
import cn.edu.xmu.activity.model.po.FlashSaleItemPo;
import cn.edu.xmu.activity.service.FlashSaleService;
import cn.edu.xmu.goodsservice.client.IActivityService;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * @author Feiyan Liu
 * @date Created at 2020/12/14 21:55
 */

@DubboService(version = "2.1.0")
public class IActivityServiceImpl implements IActivityService {
    @Autowired
    FlashSaleService flashSaleService;
    @Override
    public Long getFlashSalePriceBySkuId(Long id) {
        TimeSegmentVo timeSegmentVo = flashSaleService.returnCurrentTimeSegmentVo();
        if (timeSegmentVo != null) {
            FlashSaleItemPo flashSaleItemPo = flashSaleService.getFlashSaleItemPoByTimeSegmentAndGoodsSkuId(timeSegmentVo.getId(), id);
            if (flashSaleItemPo != null) {
                return flashSaleItemPo.getPrice();
            }
        }
        return null;
    }
}
