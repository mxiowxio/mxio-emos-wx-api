package com.mxio.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author mxio
 */
@Data
@ApiModel
public class searchMyMeetingListByPageForm {

    @NotNull
    @Min(1)
    private Integer page;
    @NotNull
    @Range(min = 1, max = 40)
    private Integer length;
}
