package com.mxio.emos.wx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mxio.emos.wx.controller.form.ChangeUserStateDTO;
import com.mxio.emos.wx.controller.form.LoginToAdminDTO;
import com.mxio.emos.wx.controller.form.UserDTO;
import com.mxio.emos.wx.controller.form.UserQueryReq;
import com.mxio.emos.wx.db.pojo.TbDeptPo;
import com.mxio.emos.wx.db.pojo.TbUserPo;
import com.mxio.emos.wx.entity.resp.PageResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author mxio
 */
public interface UserService extends IService<TbUserPo> {

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

    /**
     * 登录至后台管理
     *
     * @param loginToAdminDTO 登录信息
     * @return 用户ID
     */
    Integer loginToAdmin(LoginToAdminDTO loginToAdminDTO);

    /**
     * 获取部门列表
     *
     * @return 部门列表
     */
    List<TbDeptPo> listDeptTree();

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 用户列表
     */
    PageResult listUsers(UserQueryReq query);


    /**
     * 更新用户状态
     *
     * @param params 信息
     */
    void changeUserState(ChangeUserStateDTO params);

    /**
     * 删除用户
     *
     * @param ids 用户id列表
     */
    void deleteUsers(List<Long> ids);

    /**
     * 更新用户
     *
     * @param user 用户信息
     */
    void updateUser(UserDTO user);
}
