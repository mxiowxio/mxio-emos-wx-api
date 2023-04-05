package com.mxio.emos.wx.entity.req;

import lombok.ToString;

import java.io.Serializable;

/**
 * @author mxio
 */
@ToString
public class BasePageQuery implements Serializable {

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页记录数
     */
    private Integer size = 10;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current <= 0 ? 1 : current;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size <= 0 ? 10 : size;
    }
}

