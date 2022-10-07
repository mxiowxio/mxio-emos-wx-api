package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbCheckinPo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbCheckinPo
 */
public interface TbCheckinMapper {

    public Integer haveCheckin(HashMap param);

    public void insert(TbCheckinPo checkin);

    public HashMap searchTodayCheckin(int userId);

    public long searchCheckinDays(int userId);

    public ArrayList<HashMap> searchWeekCheckin(HashMap param);


}




