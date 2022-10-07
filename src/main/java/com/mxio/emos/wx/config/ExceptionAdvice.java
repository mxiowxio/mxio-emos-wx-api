package com.mxio.emos.wx.config;

import com.mxio.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author mxio
 */

@Slf4j
/**
 * ControllerAdvice，捕获各种异常，后端是reastful风格，则加上rest，形成ControllerAdvice
 */
@RestControllerAdvice
public class ExceptionAdvice {
    /*方法返回的字符串是个消息，返回的字符串就是错误消息，是要写到响应里面的*/
    @ResponseBody
    /*相应的状态码，500*/
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)   //500, "Internal Server Error
    /**
     * 捕获异常Exception.class，全局捕获异常Exception的子类
     */
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        log.error("执行异常", e);
        /**
         * 后端数据验证失败异常，发送的数据有误
         */
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e;
            /*获得异常消息。获取某些字段不能通过的部分，获取具体消息，精简后的原因，不是直接getmessage()*/
            // 将错误信息返回给前台
            return exception.getBindingResult().getFieldError().getDefaultMessage();
            /**
             * EmosException异常，自定diy的异常情况,则精简异常内容
             */
        } else if (e instanceof EmosException) {
            EmosException exception = (EmosException) e;
            return exception.getMsg();
            /**
             * 未授权，权限异常，发送的请求是错误的
             */
        } else if (e instanceof UnauthorizedException) {
            return "你不具备相关权限";
            /**
             * 通后端异常，后端验证失败，普通类型的java异常
             */
        } else {
            return "后端执行异常";
        }
    }

}
