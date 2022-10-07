package com.mxio.emos.wx.config.shiro;


//import com.mxio.emos.wx.db.pojo.TbUser;
//import com.mxio.emos.wx.service.UserService;

import com.mxio.emos.wx.db.pojo.TbUserPo;
import com.mxio.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author mxio
 */

@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权（验证权限时调用）
     *
     * @param collection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        //在认证的过程中，传入三个参数
        //SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(user,accessToken,getName());
        //用户信息，token，realm的名字
        //所以在授权的时候就可以直接getPrimaryPrincipal，获得对象
        TbUserPo user = (TbUserPo) collection.getPrimaryPrincipal();
        //根据对象获取id
        int userId = user.getId();
        //根据userId获取权限
        Set<String> permsSet = userService.searchUserPermissions(userId);
        //将权限返回给info
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        // todo 查询用户的权限列表
        // todo 把权限列表添加到info对象中
        return info;
    }

    /**
     * 认证（验证登录时调用）
     *
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //从头部获取令牌字符串
        String accessToken = (String) token.getPrincipal();
        //通过令牌获取id
        int userId = jwtUtil.getUserId(accessToken);
        TbUserPo user = userService.searchById(userId);
        if (user == null) {
            throw new LockedAccountException("账号已被锁定，请联系管理员~");
        }
        // todo 从令牌中获取userId，然后检测该账号是否被冻结
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, accessToken, getName());
        // todo 往info对象中添加用户信息，token等字符串
        return info;
    }
}
