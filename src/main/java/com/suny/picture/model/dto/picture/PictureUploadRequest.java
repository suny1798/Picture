package com.suny.picture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * 图片id
     */
    private Long id;

    private static final long serialVersionUID = -2959956171459685708L;
}
