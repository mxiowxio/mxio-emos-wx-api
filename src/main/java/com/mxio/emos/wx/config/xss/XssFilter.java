package com.mxio.emos.wx.config.xss;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**设置过滤器
 *为了让刚刚定义的包装类生效，我们还要在com.example.emos.wx.config.xss中创建XssFilter过滤器。
 * 过滤器拦截所有请求，然后把请求传入包装类，这样包装类就能覆盖所有请求的参数方法。
 * 用户从请求中获得数据，全都经过转义。
 *
 * XssHttpServletRequestWrapper中是包装类，由XssFilter拦截器拦截所有请求，把里面的参数方法都给到XssHttpServletRequestWrapper
 * XssHttpServletRequestWrapper来覆盖里面的请求方法，用户从请求中获得数据，全都经过转义了，xss攻击失效。

 */

@WebFilter(urlPatterns = "/*")
public class XssFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // ServletRequest强转成HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // wrapper包装，定义包装request请求里面的new的所有内容
        XssHttpServletRequestWrapper wrapper = new XssHttpServletRequestWrapper(request);
        // void doFilter(ServletRequest var1, ServletResponse var2)，请求，响应
        // 开始拦截servletResponse响应中的所有内容， 交给XssHttpServletRequestWrapper去转义
        filterChain.doFilter(wrapper, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
