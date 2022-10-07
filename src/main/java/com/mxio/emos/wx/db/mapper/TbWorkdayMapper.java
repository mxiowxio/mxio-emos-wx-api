package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbWorkdayPo;
import io.swagger.models.auth.In;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbWorkdayPo
 */
public interface TbWorkdayMapper {

    public Integer searchTodayIsWorkday();

    public ArrayList<String> searchWorkdayInRange(HashMap param);

}




