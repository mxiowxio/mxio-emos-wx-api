package com.mxio.emos.wx.service;

import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author mxio
 */
public interface CheckinService {

    /**
     * 查看用户今天是否可以签到
     */
    public String validCanCheckIn(int userId, String date);

    /**
     * 签到方法
     */
    public void checkin(HashMap param);

//    public void createFaceModel(int userId, String path);

//    public String searchDeptName(int deptId);

    /**
     * 查询员工当日考勤结果
     */
    public HashMap searchTodayCheckin(int userId);

    /**
     * 查询员工签到天数
     */
    public long searchCheckinDays(int userId);

    /**
     * 查询员工本周的考勤结果
     */
    public ArrayList<HashMap> searchWeekCheckin(HashMap param);

    /**
     * 查询员工本月的考勤结果
     */
    public ArrayList<HashMap> searchMonthCheckin(HashMap param);



}
