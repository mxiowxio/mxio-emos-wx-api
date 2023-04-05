package com.mxio.emos.wx.db.pojo;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 节假日表
 * @TableName tb_holidays
 */
@TableName(value = "tb_holidays")
@Data
public class TbHolidaysPo implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 日期
     */
    private Date date;

    private static final long serialVersionUID = 1L;
}