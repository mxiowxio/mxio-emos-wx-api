package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbMeetingPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbMeetingPo
 */
public interface TbMeetingMapper {

    public int insertMeeting(TbMeetingPo entity);

    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    public boolean searchMeetingMembersInSameDept(String uuid);

    public int updateMeetingInstanceId(HashMap map);

    public HashMap searchMeetingById(int id);

    public ArrayList<HashMap> searchMeetingMembers(int id);

    public int updateMeetingInfo(HashMap param);

    public int deleteMeetingById(int id);

    public List<String> searchUserMeetingInMonth(HashMap param);

}




