package com.mxio.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.mxio.emos.wx.db.pojo.TbMeetingPo;
import com.mxio.emos.wx.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MxioEmosWxApiApplicationTests {

    @Autowired
    private MeetingService meetingService;

    @Test
    void contextLoads() {
    }

    @Test
    void test1() {
        int a = 26000;
        int b = 750;
        int sum = 0;
        for (int i = 0; i < 16; i++) {
            a = a + b;
            sum += a;
        }
        System.out.println(sum);
    }

    @Test
    void createMeetingData() {
        for (int i = 1; i <= 100; i++) {
            TbMeetingPo meeting = new TbMeetingPo();
            // 设置主键的值
            meeting.setId((long) i);
            // 生成UUID字符串并赋值
            meeting.setUuid(IdUtil.simpleUUID());
            // 生成会议标题
            meeting.setTitle("测试会议" + i);
            // 创建此会议的人
            meeting.setCreatorId(6L);
            // 开会日期
            meeting.setDate(DateUtil.today());
            // 会议地点
            meeting.setPlace("线下会议室");
            // 会议开始时间
            meeting.setStart("08:30");
            // 会议结束时间
            meeting.setEnd("10:30");
            // 会议类型
            meeting.setType((short) 1);
            // 参会人id
            meeting.setMembers("[6,15]");
            // 会议描述内容
            meeting.setDesc("组内周会");
            // 生成工作流的uuid
            meeting.setInstanceId(IdUtil.simpleUUID());
            // 会议状态
            meeting.setStatus((short) 3);
            meetingService.insertMeeting(meeting);
        }
    }

}
