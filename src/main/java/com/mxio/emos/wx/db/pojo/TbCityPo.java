package com.mxio.emos.wx.db.pojo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 疫情城市列表
 * @TableName tb_city
 */
@TableName(value = "tb_city")
@Data
public class TbCityPo implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 城市名称
     */
    private String city;

    /**
     * 拼音简称
     */
    private String code;

    private static final long serialVersionUID = 1L;
}