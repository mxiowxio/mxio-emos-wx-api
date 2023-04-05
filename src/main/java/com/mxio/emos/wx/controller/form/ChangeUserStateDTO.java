package com.mxio.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author mxio
 */
@ApiModel(value = "更新用户状态 DTO")
@Data
@ToString
public class ChangeUserStateDTO {

    @ApiModelProperty(value = "用户ID")
    @NotNull(message = "参数不能为空")
    @Min(value = 1, message = "参数非法")
    private Integer id;

    @ApiModelProperty(value = "用户状态")
    @NotNull(message = "参数不能为空")
    private Boolean status;

}