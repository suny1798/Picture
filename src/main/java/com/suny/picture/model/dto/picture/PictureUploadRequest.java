package com.suny.picture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;

    /**
     * 图片URL
     */
    private String fileUrl;

    /**
     * 空间ID
     */
    private Long spaceId;

    /**
     * 图片名称
     */
    private String picName;

    /**
     * 类型
     */
    private String category;


    private static final long serialVersionUID = -2959956171459685708L;
}
