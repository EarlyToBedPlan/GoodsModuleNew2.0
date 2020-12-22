package cn.edu.xmu.provider.service.Impl;

import cn.edu.xmu.otherservice.client.OtherService;
import cn.edu.xmu.otherservice.model.po.CustomerPo;
import cn.edu.xmu.otherservice.model.po.TimeSegmentPo;
import cn.edu.xmu.otherservice.model.vo.CustomerVo;
import cn.edu.xmu.provider.mapper.CustomerPoMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@DubboService(version = "2.4.0")
public class OtherServiceImpl implements OtherService {

    @Autowired
    CustomerPoMapper customerPoMapper;



    @Override
    public CustomerVo getUserById(Long id) {
        CustomerVo customerVo = null;
        try {
            CustomerPo customerPo = customerPoMapper.selectByPrimaryKey(id);
            customerVo = new CustomerVo(customerPo);
        }catch (Exception e){
            customerVo = null;
        }
        return customerVo;
    }

    @Override
    public List<TimeSegmentPo> getAllTimeSegment() {
        return null;
    }

    @Override
    public Boolean checkTimeSegmentById() {
        return null;
    }
}
