package com.mxio.emos.wx.controller.form;

import com.mxio.emos.wx.entity.req.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author mxio
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryReq extends BasePageQuery implements Serializable {

    /**
     *  用户名
     */
    private String username;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号码
     */
    private String tel;

    /**
     * 部门编号
     */
    private Integer deptId;

    /**
     * 状态
     */
    private Boolean status;

    /**
     * 起始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    private static final long serialVersionUID = 1L;

}
