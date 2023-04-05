package com.mxio.emos.wx.controller.form;

import com.mxio.emos.wx.entity.req.VerifyGroups;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author mxio
 */
@Data
@ToString
public class UserDTO implements Serializable {

    /**
     * 用户ID
     */
    @Min(value = 1L, message = "用户ID不能小于1", groups = {VerifyGroups.Update.class})
    @NotNull(message = "用户ID不能为空", groups = {VerifyGroups.Update.class})
    @Null(message = "无法指定用户ID", groups = {VerifyGroups.Insert.class})
    private Integer id;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "用户密码不能为空", groups = {VerifyGroups.Insert.class})
    private String password;

    /**
     * 用户姓名
     */
    @NotBlank(message = "用户姓名不能为空", groups = {VerifyGroups.Insert.class})
    private String name;

    /**
     * 用户邮箱
     */
    @NotBlank(message = "用户邮箱不能为空", groups = {VerifyGroups.Insert.class})
    @Email(message = "邮箱格式错误", groups = {VerifyGroups.Insert.class})
    private String email;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空", groups = {VerifyGroups.Insert.class})
    private String tel;

    /**
     * 用户性别
     */
    private Object sex;

    /**
     * 头像地址
     */
    private String photo;

    /**
     * 是否可用（1正常 0停用）
     */
    private Boolean status;

    /**
     * 用户角色ID列表
     */
    @NotEmpty(message = "角色不能为空")
    private List<Long> roleIds;

}
