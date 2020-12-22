package cn.edu.xmu.provider.service.Impl;

import cn.edu.xmu.otherservice.client.OtherService;
import cn.edu.xmu.otherservice.model.po.CustomerPo;
import cn.edu.xmu.otherservice.model.po.TimeSegmentPo;
import cn.edu.xmu.otherservice.model.po.TimeSegmentPoExample;
import cn.edu.xmu.otherservice.model.vo.CustomerVo;
import cn.edu.xmu.provider.mapper.CustomerPoMapper;
import cn.edu.xmu.provider.mapper.TimeSegmentPoMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "2.4.0")
public class OtherServiceImpl implements OtherService {

    @Autowired
    CustomerPoMapper customerPoMapper;

    @Autowired
    TimeSegmentPoMapper timeSegmentPoMapper;


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
        TimeSegmentPoExample timeSegmentPoExample = new TimeSegmentPoExample();
        TimeSegmentPoExample.Criteria criteria = timeSegmentPoExample.createCriteria();
        criteria.andTypeEqualTo((byte) 1);
        List<TimeSegmentPo> timeSegmentPos = timeSegmentPoMapper.selectByExample(timeSegmentPoExample);
        List<TimeSegmentPo> timeSegmentPosReturn = new ArrayList<>(timeSegmentPos.size());
        return timeSegmentPosReturn;


    }

    @Override
    public Boolean checkTimeSegmentById() {
        return null;
    }
}
