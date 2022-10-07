package com.mxio.emos.wx.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author mxio
 */

@Data
@Component
public class SystemConstants {

    //签到开始时间
    public String attendanceStartTime;
    //签到时间
    public String attendanceTime;
    //签到结束时间
    public String attendanceEndTime;
    //签到结束开始时间
    public String closingStartTime;
    //签到结束时间
    public String closingTime;
    //签到结束结束时间
    public String closingEndTime;

}
