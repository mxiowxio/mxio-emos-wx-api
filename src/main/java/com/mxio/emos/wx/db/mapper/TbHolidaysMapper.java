package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbHolidaysPo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbHolidaysPo
 */
public interface TbHolidaysMapper {

    public Integer searchTodayIsHolidays();

    public ArrayList<String> searchHolidaysInRange(HashMap param);

}




