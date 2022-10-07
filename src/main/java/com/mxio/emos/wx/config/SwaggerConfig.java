package com.mxio.emos.wx.config;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mxio
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * 创建docket
     * @return
     */
    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("Emos在线办公系统");
        ApiInfo info = builder.build();
        docket.apiInfo(info);

        // 设置哪些类的哪些方法添加到Swagger里
        /*包，包里面描述有ApiOperation的才可以调用*/
        ApiSelectorBuilder selectorBuilder = docket.select();
        selectorBuilder.paths(PathSelectors.any());
        selectorBuilder.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class));
        docket = selectorBuilder.build();

        // 让Swagger支持JWT
        /*swagger，知道，请求头，令牌参数*/
        /*
         * 下面的语句是开启对JWT的支持，当用户用Swagger调用受JWT认证保护的方法，
         * 必须要先提交参数（例如令牌）
         */
        //存储用户必须提交的参数
        //规定用户需要输入什么参数
        ApiKey apiKey = new ApiKey("token", "token", "header");
        List<ApiKey> apiKeyList = new ArrayList<>();
        apiKeyList.add(apiKey);
        docket.securitySchemes(apiKeyList);


        /*认证对象，作用域，令牌，不断封装，swagger中使用jwt，而不是springboot中jwt*/
        //如果用户JWT认证通过，则在Swagger中全局有效


        // 设定令牌作用域
        AuthorizationScope scope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] scopes = {scope};
        SecurityReference reference = new SecurityReference("token", scopes);
        List refList = new ArrayList();
        refList.add(reference);
        SecurityContext context = SecurityContext.builder().securityReferences(refList).build();
        List cxtList = new ArrayList();
        cxtList.add(context);
        docket.securityContexts(cxtList);
        docket.securityContexts(cxtList);


        return docket;
    }


}
