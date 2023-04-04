package com.mxio.emos.wx.service;

import com.mxio.emos.wx.db.pojo.TbUserPo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author mxio
 */
public interface UserService {

    /**
     * 用户注册
     */
    public int registerUser(String registerCode,String code,String nickname,String photo);

    /**
     * 查询用户权限
     */
    public Set<String> searchUserPermissions(int userId);

    /**
     * 用户登录
     */
    public Integer login(String code);

    /**
     * 获取用户信息
     */
    public TbUserPo getUserInfo(int userId);

    /**
     * 根据id查询用户
     */
    public TbUserPo searchById(int userId);

    /**
     * 查询员工入职日期
     */
    public String searchUserHiredate(int userId);

    /**
     * 查询员工摘要信息
     */
    public HashMap searchUserSummary(int userId);

    /**
     * 查询员工列表，按照部门分组排列
     */
    public ArrayList<HashMap> searchUserGroupByDept(String keyword);

    public ArrayList<HashMap> searchMembers(List param);

}
