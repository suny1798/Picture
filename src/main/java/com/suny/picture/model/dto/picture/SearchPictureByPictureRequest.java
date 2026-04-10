package com.suny.picture.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 搜索图片请求
 *
 * @author suny
 * @date 2024/6/17 17:28
 */
@Data
public class SearchPictureByPictureRequest implements Serializable {

    /**
     * 图片 id
     */
    private Long pictureId;

    private static final long serialVersionUID = 1L;
}
