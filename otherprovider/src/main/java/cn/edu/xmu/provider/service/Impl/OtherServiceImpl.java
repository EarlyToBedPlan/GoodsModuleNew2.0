package cn.edu.xmu.provider.service.Impl;

import cn.edu.xmu.otherservice.client.OtherService;
import cn.edu.xmu.otherservice.model.vo.CustomerVo;
import cn.edu.xmu.otherservice.model.vo.TimeSegmentVo;
import cn.edu.xmu.provider.mapper.CustomerPoMapper;
import cn.edu.xmu.provider.mapper.TimeSegmentPoMapper;
import cn.edu.xmu.provider.model.po.CustomerPo;
import cn.edu.xmu.provider.model.po.TimeSegmentPo;
import cn.edu.xmu.provider.model.po.TimeSegmentPoExample;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService(version = "1.0.0",group = "other-service")
public class OtherServiceImpl implements OtherService {

    @Autowired
    CustomerPoMapper customerPoMapper;

    @Autowired
    TimeSegmentPoMapper timeSegmentPoMapper;


    @Override
    public CustomerVo getUserById(Long id) {
        CustomerVo customerVo = new CustomerVo();
        try {
            CustomerPo customerPo = customerPoMapper.selectByPrimaryKey(id);
            customerVo.setGender(customerPo.getGender());
            customerVo.setBeDeleted(customerPo.getBeDeleted());
            customerVo.setBirthday(customerPo.getBirthday());
            customerVo.setEmail(customerPo.getEmail());
            customerVo.setUserName(customerPo.getUserName());
            customerVo.setState(customerPo.getState());
            customerVo.setRealName(customerPo.getRealName());
            customerVo.setPoint(customerPo.getPoint());
            customerVo.setPassword(customerPo.getPassword());
            customerVo.setMobile(customerPo.getMobile());
            customerVo.setGmtCreate(customerPo.getGmtCreate());
            customerVo.setId(customerPo.getId());
            customerVo.setGmtModified(customerPo.getGmtModified());
        }catch (Exception e){
            customerVo = null;
        }
        return customerVo;
    }

    @Override
    public List<TimeSegmentVo> getAllTimeSegment() {
        TimeSegmentPoExample timeSegmentPoExample = new TimeSegmentPoExample();
        TimeSegmentPoExample.Criteria criteria = timeSegmentPoExample.createCriteria();
        criteria.andTypeEqualTo((byte) 1);
        List<TimeSegmentPo> timeSegmentPos = timeSegmentPoMapper.selectByExample(timeSegmentPoExample);
        List<TimeSegmentVo> timeSegmentVos = new ArrayList<>(timeSegmentPos.size());
        for(TimeSegmentPo timeSegmentPo:timeSegmentPos){
            TimeSegmentVo timeSegmentVo = new TimeSegmentVo();
            timeSegmentVo.setBeginTime(timeSegmentPo.getBeginTime());
            timeSegmentVo.setEndTime(timeSegmentPo.getEndTime());
            timeSegmentVo.setGmtCreate(timeSegmentPo.getGmtCreate());
            timeSegmentVo.setId(timeSegmentPo.getId());
            timeSegmentVo.setType(timeSegmentPo.getType());
            timeSegmentVos.add(timeSegmentVo);
        }
        return timeSegmentVos;

    }

    @Override
    public Boolean checkTimeSegmentById() {
        return null;
    }
}
