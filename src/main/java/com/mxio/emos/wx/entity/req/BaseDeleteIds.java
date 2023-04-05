package com.mxio.emos.wx.entity.req;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author mxio
 */
@Data
@ToString
public class BaseDeleteIds {

    /**
     * 删除请求实体的id集合
     */
    @NotEmpty(message = "非法参数")
    private List<Long> ids;

}