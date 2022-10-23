package com.mxio.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mxio
 * <p>
 * /**
 * * 我们定义OAuth2Filter类拦截所有的HTTP请求，
 * *
 * *
 * * 一方面它会把请求中的Token字符串提取出来，封装成对象交给Shiro框架;
 * * 另一方面，它会检查Token的有效性。如果Token过期，那么会生成新的Token，分别存储在ThreadLocalToken和Redis中。
 * *
 * *
 * * 之所以要把新令牌保存到ThreadLocalToken里面，是因为要向AOP切面类传递这个新令牌。
 * * 虽然OAuth2Filter中有doFilterInternal()方法，我们可以得到响应并且写入新令牌。
 * * 但是这样非常麻烦，首先我们要通过IO流读取响应中的数据，然后还要把数据解析成JSON对象，最后再放入这个新令牌。
 * * 这也是为什么ThreadLocalToken的媒介作用，
 */

@Component
@Scope("prototype") //多例，否则数据出现问题单例对象，只会创建一个对象，加上注解，多例对象
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    /**
     * 考察的一个知识点，从xml文件中获取属性文件的属性值
     */
    @Value(("${emos.jwt.cache-expire}"))
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * redis对象，对redis对象读写操作，把redis传入threadLocalToken和redis
     */
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 拦截请求之后，用于把令牌字符串封装成令牌对象
     *
     * @param request
     * @param response
     * @return
     * @throws Exception 方法覆盖，createToken方法，请求中获取令牌字符串，封装成对象，以后交给shiro对象使用，去验证授权
     *                   createToken从请求中获取令牌字符串，然后封装成令牌对象OAuth2Token，交给shiro框架去处理
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        // 获取请求token
        String token = getRequestToken(req);
        //如果获取到的token中是空值的而且是空字符串，则返回null即可结束，否则继续
        if (StrUtil.isBlank(token)) {
            return null;
        }   //👆从抽象请求中获取令牌字符串，👇然后将字符串交给OAuth2Token方法，OAuth2Token会把令牌字符串封装成对象。
        return new OAuth2Token(token);
    }

    /**
     * 拦截请求，判断请求是否需要被 Shiro 处理
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        // Ajax提交application/json数据的时候，会先发出来Option请求
        // 这里要放行Option请求，不需要 Shiro 处理
        if (req.getMethod().equals(RequestMethod.OPTIONS.name())) {
            return true;
        }
        // 除了Option请求之外，所以请求都要被 Shiro 处理
        return false;
    }

    /**
     * 该方法用于处理所有应该被 Shiro 处理的请求
     *
     * @param request
     * @param response
     * @return
     * @throws Exception onAccessDenied 方法
     *                   设置响应的字符集，和响应的请求头。setHeader方法用来设置跨域请求。
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        //分别强制转换请求   和   响应
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 设置相应字符集和类型
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        // 设置允许跨域   这两行是允许跨域的意思，允许跨域请求，前后端分离项目
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        // 用之前一般都先清空内容，来刷新令牌
        threadLocalToken.clear();

        /**然后验证令牌是否过期。
         如果验证出现问题，就会抛出异常。
         通过捕获异常，就知道是令牌有问题，还是令牌过期了。
         JWTDecodeException 是内容异常。

         通过redisTemplate的hasKey查询Redis是否存在令牌。
         如果存在令牌，就删除老令牌，重新生成一个令牌，给客户端。
         executeLogin方法，让shiro执行realm类。
         */

        // 从请求头里获得 token 字符串
        String token = getRequestToken(req);
        //判断token是否是空字符串
        if (StrUtil.isBlank(token)) {
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌！");
            return false;
        }

        try {
            // 验证 token 是否有效
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {
            // 判定 Redis 中缓存的令牌是否过期
            // 如果存在，则说明客户端保存的令牌已过期，服务的的令牌未过期，进行令牌的刷新
            if (redisTemplate.hasKey(token)) {
                // 删除老令牌
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                // 生成新的令牌
                token = jwtUtil.createToken(userId);
                redisTemplate.opsForValue().set(token, userId + "", cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);
            } else {  // 客户端和服务端的令牌均过期，需要用户重新登陆
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期！");
                return false;
            }
        } catch (Exception e) {    // 客户端提交的令牌错误！
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌！");
            return false;
        }
        // 让 Shiro 间接的执行 Realm 类
        boolean bool = executeLogin(request, response);
        return bool;
    }

    /**
     * 判定用户是否登陆或登陆失败
     *
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 设置相应字符集和类型
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        // 设置允许跨域
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            resp.getWriter().print(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    /**
     * 从请求中获取里面的token字符串
     */
    private String getRequestToken(HttpServletRequest request) {
        //从请求头里面获取token
        String token = request.getHeader("token");
        //如果请求头里面没有token，就从请求体中获取token
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("toekn");
        }
        return token;
    }
}
