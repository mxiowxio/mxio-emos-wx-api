package com.mxio.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mxio
 * <p>
 * 用来拦截xss攻击的 Wrapper
 * <p>
 * 使用了装饰器模式
 * 装饰器封装了厂商的Request实现类
 * 只需要覆盖Wrapper类的方法，就能做到覆盖厂商请求对象里方法
 * 类似于手机的手机套，手机本身没有改变，但加上了保护套之后，功能变多了
 * <p>
 * 所以避免XSS攻击最有效的办法就是对用户输入的数据进行转义，然后存储到数据库里面。
 * 等到视图层渲染HTML页面的时候。转义后的文字是不会被当做JavaScript执行的，这就可以抵御XSS攻击
 */

/**
 * 定义请求包装类
 * 我们平时写Web项目遇到的HttpServletRequest，它其实是个接口。如果我们想要重新定义请求类，扩展这个接口是最不应该的。
 * 因为HttpServletRequest接口中抽象方法太多了，我们逐一实现起来太耗费时间。
 * 所以我们应该挑选一个简单一点的自定义请求类的方式。那就是继承HttpServletRequestwrapper 父类。
 * <p>
 * JavaEE只是一个标准，具体的实现由各家应用服务器厂商来完成。比如说Tomcat在实现Servlet规范的时候，
 * 就自定义了HttpServletRequest接口的实现类。同时JavaEE规范还定义了HttpServletRequestwrapper，这
 * 个类是请求类的包装类，用上了装饰器模式。
 * <p>
 * 不得不说这里用到的设计模式真的非常棒，无论各家应用服务器厂商怎么去实现HttpServletRequest接口，
 * 用户想要自定义请求，只需要继承HttpServletRequestWrapper，对应覆盖某个方法即可，
 * 然后把请求传入请求包装类，装饰器模式就会替代请求对象中对应的某个方法。
 * <p>
 * 用户的代码和服务器厂商的代码完全解耦，我们不用关心HttpServletRequest接口是怎么实现的，
 * 借助于包装类我们可以随意修改请求中的方法。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StrUtil.hasEmpty(value)) {
                    value = HtmlUtil.filter(value);
                }
                values[i] = value;
            }
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters = super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameters != null) {
            for (String key : parameters.keySet()) {
                String[] values = parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (!StrUtil.hasEmpty(value)) {
                        value = HtmlUtil.filter(value);
                    }
                    values[i] = value;
                }
                map.put(key, values);
            }
        }
        return map;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (!StrUtil.hasEmpty(value)) {
            value = HtmlUtil.filter(value);
        }
        return value;
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();
        while (line != null) {
            body.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();
        in.close();
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            Object val = map.get(key);
            if (val instanceof String) {
                if (!StrUtil.hasEmpty(val.toString())) {
                    result.put(key, HtmlUtil.filter(val.toString()));
                }
            } else {
                result.put(key, val);
            }
        }
        String json = JSONUtil.toJsonStr(result);
        ByteArrayInputStream bain = new ByteArrayInputStream(json.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bain.read();
            }
        };
    }
}
