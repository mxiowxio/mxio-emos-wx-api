package com.mxio.emos.wx.service;

import com.mxio.emos.wx.db.pojo.TbMeetingPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author mxio
 */
public interface MeetingService {

    public void insertMeeting(TbMeetingPo entity);

    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    public HashMap searchMeetingById(int id);

    public void updateMeetingInfo(HashMap param);

    public void deleteMeetingById(int id);

    public List<String> searchUserMeetingInMonth(HashMap param);
}
