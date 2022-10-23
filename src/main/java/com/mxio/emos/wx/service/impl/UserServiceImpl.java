package com.mxio.emos.wx.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mxio.emos.wx.db.mapper.TbUserMapper;
import com.mxio.emos.wx.db.pojo.TbUserPo;
import com.mxio.emos.wx.exception.EmosException;
import com.mxio.emos.wx.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author mxio
 */

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId;

    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserMapper userDao;

    /**
     * 通过传来的临时授权字符换code,从微信小程序中获取openid,发送的变量中有appid,appsecret,code,
     * 从微信小程序端获取到code临时字符串，从code临时字符串中获取openid的值。
     * appid,appsecret相当于账号密码
     * **code就是发送过去给微信,让微信返回openid回来
     * **
     */

    private String getOpenId(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session";
        HashMap map = new HashMap();
        /*定义发送的变量*/
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);

        log.info("appid:{}", appId);
        log.info("secret:{}", appSecret);
        log.info("js_code:{}", code);

        /*确定的，固定的*/
        map.put("grant_type", "authorization_code");

        /*利用hutool工具类中的HttpUtil发送url和map变量请求到微信中*/
        String response = HttpUtil.post(url, map);

        /*将获得的数据转化成json格式*/
        /*从string转换成json*/
        JSONObject json = JSONUtil.parseObj(response);

        System.out.println("json:" + json);

        /*从json格式中获取openid*/
        String openId = json.getStr("openid");

        System.out.println("openId:" + openId);

        /*判断openid*/
        if (openId == null || openId.length() == 0) {
            throw new RuntimeException("临时登陆凭证错误！");
        }
        return openId;
    }

    /**
     * registerCode，激活码；
     * code，临时授权字符串;
     * openid；
     * nickname，昵称；
     * photo，头像
     */
    @Override
    public int registerUser(String registerCode, String code, String nickname, String photo) {
        // 填入的注册码是超级管理员邀请码，判断数据库中有没有信息
        if (registerCode.equals("000000")) {
            // 查询超级管理员账号是否已经绑定
//            Boolean bool = userDao.haveRootUser();
//            if (!bool) {
            // 把当前用户绑定到root账户中
            String openId = getOpenId(code);
            // 把当前用户绑定到root账户中，通过
            HashMap param = new HashMap();
            // 如果不存在超级管理员账户，添加信息
            param.put("openId", openId);
            param.put("nickname", nickname);
            param.put("photo", photo);
            param.put("role", "[1, 2, 3, 4, 5, 6, 7, 8]");
            param.put("status", 1);
            param.put("createTime", new Date());
            param.put("root", true);
            userDao.insert(param);
            // 通过微信发来的openid，从数据库中查找id
            Integer id = userDao.searchIdByOpenId(openId);
            return id;
//            }

            // 数据库中存在，则抛出异常
            /*else {
                throw new EmosException("无法绑定超级管理员账号");
            }*/
        }

        // 填入的不是000000超级管理员激活码，则是普通用户的邀请码输入
        if (registerCode.equals("123456")) {
            // 普通员工注册
            // 把当前用户绑定到普通账户中
            String openId = getOpenId(code);
            // 把当前用户绑定到root账户中，通过
            HashMap param = new HashMap();
            // 如果不存在超级管理员账户，添加信息
            param.put("openId", openId);
            param.put("nickname", nickname);
            param.put("photo", photo);
            param.put("role", "[0]");
            param.put("status", 1);
            param.put("createTime", new Date());
            param.put("root", true);
            userDao.insert(param);
            // 通过微信发来的openid，从数据库中查找id
            Integer id = userDao.searchIdByOpenId(openId);
            return id;

        }
        return 0;
    }

    /**
     * 底层查询到的权限，返回Set<String>，set表表示不重复显示，String里面是权限具体信息
     */
    @Override
    public Set<String> searchUserPermissions(int userId) {
        System.out.println("userId:" + userId);
        Set<String> permissions = userDao.searchUserPermissions(userId);
        System.out.println("permissions:" + permissions);
        return permissions;
    }

    /**
     * 我们可以这样设计，用户在Emos登陆页面点击登陆按钮，
     * 然后小程序把临时授权字符串，提交给后端Java系统。
     * 后端Java系统拿着临时授权字符串换取到openid，
     * 我们查询用户表中是否存在这个openid，如果存在，
     * 意味着该用户是已注册用户，可以登录。如果不存在，说明该用户尚未注册，
     * 目前还不是我们的员工，所以禁止登录，
     */
    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if (id == null) {
            throw new EmosException("账户不存在");
        }

        // todo 从消息队列中接受消息。转移到消息表

        return id;
    }

    @Override
    public TbUserPo getUserInfo(int userId) {
        TbUserPo userInfo = userDao.getUserInfo(userId);
        return userInfo;
    }

    @Override
    public TbUserPo searchById(int userId) {
        TbUserPo user = userDao.searchById(userId);
        return user;
    }

    /**
     * 查询员工入职日期
     *
     * @param userId
     * @return
     */
    @Override
    public String searchUserHiredate(int userId) {
        // 定义一个变量保存入职日期
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap map = userDao.searchUserSummary(userId);
        return map;
    }
}
