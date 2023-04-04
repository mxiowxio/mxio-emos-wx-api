package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbDeptPo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbDeptPo
 */
public interface TbDeptMapper {

    public String searchDeptName(int deptId);

    public ArrayList<HashMap> searchDeptMembers(String keyword);

}




