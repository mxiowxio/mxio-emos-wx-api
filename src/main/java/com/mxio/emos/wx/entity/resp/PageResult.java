package com.mxio.emos.wx.entity.resp;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author mxio
 */
@Data
public class PageResult {

    /**
     * 当前页数
     */
    private long current;

    /**
     * 每页记录数
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 列表数据
     */
    private List<?> records;

    /**
     * 灵活添加
     */
    private Map<String, Object> data;

    /**
     * 分页
     *
     * @param records 列表数据
     * @param current 当前页数
     * @param size    每页记录数
     * @param total   总记录数
     */
    public PageResult(List<?> records, long current, long size, long total) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
        this.pages = (long) Math.ceil((double) total / size);
    }

    /**
     * 分页
     *
     * @param records 列表数据
     * @param current 当前页数
     * @param size    每页记录数
     * @param total   总记录数
     * @param data    扩展数据
     */
    public PageResult(List<?> records, long current, long size, long total, Map<String, Object> data) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
        this.data = data;
        this.pages = (long) Math.ceil((double) total / size);
    }

    /**
     * 分页
     *
     * @param page Mybatis plus 分页插件对象
     */
    public PageResult(IPage<?> page) {
        this.records = page.getRecords();
        this.current = page.getCurrent();
        this.size = page.getSize();
        this.total = page.getTotal();
        this.pages = page.getPages();
    }
}
