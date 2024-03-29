package com.mxio.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.mxio.emos.wx.common.util.R;
import com.mxio.emos.wx.config.shiro.JwtUtil;
//import com.mxio.emos.wx.config.tencent.TLSSigAPIv2;
import com.mxio.emos.wx.controller.form.*;
import com.mxio.emos.wx.db.pojo.TbDeptPo;
import com.mxio.emos.wx.db.pojo.TbUserPo;
import com.mxio.emos.wx.entity.req.BaseDeleteIds;
import com.mxio.emos.wx.entity.req.VerifyGroups;
import com.mxio.emos.wx.entity.resp.PageResult;
import com.mxio.emos.wx.exception.EmosException;
import com.mxio.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author mxio
 */

@Slf4j
@RestController
@RequestMapping("user")
@Api("用户模块Web接口")
public class UserController {

    /**
     * 调用业务层代码
     */
    @Autowired
    private UserService userService;

    /**
     * 用户注册成功后，向客户端返回，jwt生成令牌字符串
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 将令牌不仅要缓存到客户端，还要到redis里面
     */
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    /**
     * Valid，验证传入的数据，
     *
     * @param form
     * @return
     * @RequestBody 主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)
     * 可以是对象，可以是数据 根据传进来的form表单的数据，生成token，并且根据id查找权限，
     * 将权限和token返回给客户端，将token存入redis中
     */
    @PostMapping("register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm form) {
        // 根据service层的方法registerUser注册用户，返回用户id
        int id = userService.registerUser(form.getRegisterCode(), form.getCode(), form.getNickname(), form.getPhoto());
        // 根据主键值生成token字符串
        String token = jwtUtil.createToken(id);
        // 不仅向客户端返回token字符串（认证了），还要返回权限（给权限）
        Set<String> permsSet = userService.searchUserPermissions(id);
        // 向redis缓存token
        saveCacheToken(token, id);
        // 向客户端返回数据，返回注册的信息、令牌字符串、权限
        return R.ok("用户注册成功").put("token", token).put("permission", permsSet);
    }

    /**
     * 向redie缓存token
     */
    private void saveCacheToken(String token, int userId) {
        // 保存数据，key是token，value是id，刷新令牌，以后可以根据对应的id刷新令牌，可以根据userId重新生令牌
        /*
          set(K key, V value, long timeout, TimeUnit unit) 设置变量值的过期时间。
         */
        redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
    }

    /**
     * 用户登陆功能
     *
     * @param form
     * @return
     */
    @PostMapping("login")
    @ApiOperation("登陆系统")
    public R login(@Valid @RequestBody LoginForm form) {
        // 根据code，service-dao-xml中获取到对应的id，登录

        int id = userService.login(form.getCode());

        // 根据用户的id生成token，令牌
        String token = jwtUtil.createToken(id);
        // 保留token 以及对应 id
        saveCacheToken(token, id);
        // 根据id搜索用户的权限，不重复set
        Set<String> permsSet = userService.searchUserPermissions(id);
        TbUserPo userInfo = userService.getUserInfo(id);

        //根据opid查询用户信息
        //判断是否有工号
        //有则正常返回，没有则返回错误，提升该人员非公司员工
        // 返回
        // .put("userInfo",userInfo)
        return R.ok("登陆成功").put("token", token).put("permission", permsSet).put("userInfo", userInfo);
    }

    @PostMapping("/login-to-admin")
    @ApiOperation("登陆后台系统")
    public R loginToAdmin(@Valid @RequestBody LoginToAdminDTO loginToAdminDTO) {

        Integer id = userService.loginToAdmin(loginToAdminDTO);

        // 根据用户的id生成token，令牌
        String token = jwtUtil.createToken(id);
        // 保留token 以及对应 id
        saveCacheToken(token, id);
        // 根据id搜索用户的权限，不重复set
        Set<String> permsSet = userService.searchUserPermissions(id);
        TbUserPo userInfo = userService.getUserInfo(id);

        //根据opid查询用户信息
        //判断是否有工号
        //有则正常返回，没有则返回错误，提升该人员非公司员工
        // 返回
        // .put("userInfo",userInfo)
        HashMap<String, Object> res = new HashMap<>(16);
        res.put("token", token);
        res.put("permission", permsSet);
        res.put("userInfo", userInfo);
        return R.ok("登陆成功").put("data", res);
    }

    @GetMapping("/dept-tree")
    @ApiOperation("获取部门树")
    public R listDeptTree() {
        List<TbDeptPo> deptList = userService.listDeptTree();
        return R.ok().put("data", deptList);
    }

    @ApiOperation(value = "分页查询用户列表")
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public R listUsers(UserQueryReq query) {
        PageResult result = userService.listUsers(query);
        return R.ok().put("data", result);
    }

    @ApiOperation(value = "更新用户状态")
    @PutMapping("/change-state")
    public R changeUserState(@RequestBody @Valid ChangeUserStateDTO params) {
        userService.changeUserState(params);
        return R.ok();
    }

    @ApiOperation(value = "删除（单/多个）用户")
    @DeleteMapping
    public R deleteUsers(@RequestBody @Valid BaseDeleteIds ids) {
        userService.deleteUsers(ids.getIds());
        return R.ok();
    }

    @ApiOperation(value = "更新用户")
    @PutMapping
    public R updateUser(@RequestBody @Validated(value = {VerifyGroups.Update.class}) UserDTO user) {
        userService.updateUser(user);
        return R.ok();
    }


    /**
     * 根据OPENId查询用户信息
     *
     * @param form
     * @return
     */
    @PostMapping("getUserInfo")
    @ApiOperation("获取用户信息")
    public R getUserInfo(@Valid @RequestBody LoginForm form) {
        // 根据code，service-dao-xml中获取到对应的id，登录
        int id = userService.login(form.getCode());
        // 根据用户的id生成token，令牌
        String token = jwtUtil.createToken(id);
        // 保留token 以及对应 id
        saveCacheToken(token, id);
        // 根据id搜索用户的权限，不重复set
        Set<String> permsSet = userService.searchUserPermissions(id);

        //根据opid查询用户信息
        //判断是否有工号
        //有则正常返回，没有则返回错误，提升该人员非公司员工
        // 返回
        return R.ok("登陆成功").put("token", token).put("permission", permsSet);
    }


    @GetMapping("searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token) {
        // 将令牌中的userId取出
        int userId = jwtUtil.getUserId(token);
        // 调用接口查询数据并接收
        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result", map);
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT", "EMPLOYEE:SELECT"}, logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form) {
        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());
        return R.ok().put("result", list);
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form){
        if(!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        List param=JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList list=userService.searchMembers(param);
        return R.ok().put("result",list);
    }

    @PostMapping("logout")
    @ApiOperation("注销登录")
    public R logout() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout(); // Shiro认证信息清除
        return R.ok();
    }
}
