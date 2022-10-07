package com.mxio.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.mxio.emos.wx.config.SystemConstants;
import com.mxio.emos.wx.db.mapper.SysConfigMapper;
import com.mxio.emos.wx.db.pojo.SysConfigPo;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync    //开启异步线程池
@MapperScan("com.mxio.emos.wx.db.mapper")
public class MxioEmosWxApiApplication {

    @Autowired
    private SysConfigMapper sysConfigDao;

    @Autowired
    private SystemConstants constants;

    @Value("${emos.image-folder}")
    private String imageFolder;


    public static void main(String[] args) {
        SpringApplication.run(MxioEmosWxApiApplication.class, args);
    }


    /**
     * 6-7 缓存系统常量数据
     * 从对象中的数据，
     * springboot中的注解，springboot已启动就会直接执行，类似javaee中static
     * 因为这些常量信息跟考勤模块息息相关，所以我们要编写Java代码，
     * 在Spring Boot项目启动的时候，就去数据库读取这些常量信息
     * 然后缓存成Java对象，全局都可以使用。
     * Java对象就是constants，供全局使用
     * 将sysConfigs中获取的两个值，存到constants中，需要映射过去
     * <p>
     * 首先是为了在Spring Boot项目启动的时候，就去数据库读取这些常量信息
     * 就注解PostConstruct，交给springboot管理，一启动就运行，将数据库中的ParamKey，ParamValue
     * 都取出来，然后创建一个对象类，将数据插进到对象类中，对象类就可以给全局使用了！
     * 注意驼峰命名
     */
    @PostConstruct
    public void init() {
        List<SysConfigPo> list = sysConfigDao.selectAllParam();
        list.forEach(one -> {
            String key = one.getParamKey();
            key = StrUtil.toCamelCase(key);
            String value = one.getParamValue(); //数据库中的不是驼峰命名法，而映射到constants中的都是驼峰命名法，需要一一对应后进行映射
            try {
                Field field = constants.getClass().getDeclaredField(key);
                field.set(constants, value);    //根据key来寻找，value传进constants中
            } catch (Exception e) {
                log.error("执行异常！", e);
            }
        });
        new File(imageFolder).mkdir();
    }
}
