package com.mxio.emos.wx.db.mapper;

import com.mxio.emos.wx.db.pojo.TbFaceModelPo;

/**
 * @Entity com.mxio.emos.wx.db.pojo.TbFaceModelPo
 */
public interface TbFaceModelMapper {

    public String searchFaceModel(int userId);

    public void insert(TbFaceModelPo faceModel);

    public int deleteFaceModel(int userId);

}




