package com.mxio.emos.wx.service;

import org.apache.shiro.crypto.hash.Hash;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author mxio
 */
public interface CheckinService {

    public String validCanCheckIn(int userId, String date);

    public void checkin(HashMap param);

//    public void createFaceModel(int userId, String path);

//    public String searchDeptName(int deptId);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);



}
