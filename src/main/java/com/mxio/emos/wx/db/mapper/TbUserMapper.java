package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbUserPo;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbUserPo
 */
public interface TbUserMapper {

    public Boolean haveRootUser();

    public int insert(HashMap param);

    public Integer searchIdByOpenId(String openId);

    public TbUserPo getUserInfo(int userId);

    public Set<String> searchUserPermissions(int userId);

    public TbUserPo searchById(int userId);

    public HashMap searchNameAndDept(int userId);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);

    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);

    public HashMap searchUserInfo(int userId);

    public int searchDeptManagerId(int id);

    public int searchGmId();
}




