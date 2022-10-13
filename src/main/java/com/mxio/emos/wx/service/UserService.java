package com.mxio.emos.wx.service;

import com.mxio.emos.wx.db.pojo.TbUserPo;

import java.util.HashMap;
import java.util.Set;

/**
 * @author mxio
 */
public interface UserService {

    public int registerUser(String registerCode,String code,String nickname,String photo);

    public Set<String> searchUserPermissions(int userId);

    public Integer login(String code);

    public TbUserPo getUserInfo(int userId);

    public TbUserPo searchById(int userId);

    public String searchUserHiredate(int userId);

    public HashMap searchUserSummary(int userId);

}
