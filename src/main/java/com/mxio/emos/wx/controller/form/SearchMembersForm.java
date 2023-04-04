package com.mxio.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author mxio
 */
@Data
@ApiModel
public class SearchMembersForm {
    @NotBlank
    private String members;
}
