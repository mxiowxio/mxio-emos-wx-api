package com.mxio.emos.wx.db.pojo;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 签到表
 * @TableName tb_checkin
 */
@TableName(value = "tb_checkin")
@Data
public class TbCheckinPo implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 签到地址
     */
    private String address;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区划
     */
    private String district;

    /**
     * 考勤结果
     */
    private Byte status;

    /**
     * 风险等级
     */
    private Integer risk;

    /**
     * 签到日期
     */
    private String date;

    /**
     * 签到时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}