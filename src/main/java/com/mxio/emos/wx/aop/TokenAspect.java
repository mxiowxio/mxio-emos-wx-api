package com.mxio.emos.wx.aop;

import com.mxio.emos.wx.common.util.R;
import com.mxio.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mxio
 * <p>
 * 利用切面类向客户端返回新令牌
 */


/*声明切面类*/


/**
 * AOP切面类作用拦截所有web请求，讲token返回给客户端：
 * 即AOP切面类，拦截所有Web方法返回的R对象，然后在R对象里面添加新令牌
 * 判断是否刷新生成新的令牌：
 * 检查threadLocalToken中是否保存有令牌，把令牌保存到R对象中
 */
@Aspect
@Component
public class TokenAspect {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    /*controller里面的方法的web请求都切面*/
    /*范围*/

    /**
     * 拦截点
     */

    @Pointcut("execution(public * com.mxio.emos.wx.controller.*.*(..))")
    public void aspect() {

    }

    /*切面中执行的方法，判断token是否有新的令牌，执行操作，向客户端返回新令牌*/

    /**
     * 拦截点中执行的方法
     */

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        R r = (R) point.proceed();  // 方法执行结果
        String token = threadLocalToken.getToken();
        // 如果ThreadLocal中存在token，说明是更新的token
        if (token != null) {
            r.put("token", token);  // 往响应中放置token
            threadLocalToken.clear();
        }
        return r;
    }

}
