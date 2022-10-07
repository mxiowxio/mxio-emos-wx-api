package com.mxio.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author mxio
 *
 * 实现注册超级管理员功能（Web层）
 * 接受移动端提交的注册请求、用表单来接受数据，封装数据
 * 创建RegisterForm表单
 * 会接受到注册码、临时授权字符串、昵称、头像
 * 注册码的输入需要符合正则表达式里面的0-9数字输入
 */

@Data
@ApiModel
public class RegisterForm {

    @NotBlank(message = "注册码不能为空")
    @Pattern(regexp = "^[0-9]{6}$", message = "注册码必须是6为数字")
    private String registerCode;

    @NotBlank(message = "微信临时授权不能为空")
    private String Code;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "头像不能为空")
    private String photo;

}
