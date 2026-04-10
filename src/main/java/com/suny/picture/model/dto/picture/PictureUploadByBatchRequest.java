package com.suny.picture.model.dto.picture;

import lombok.Data;

/**
 * 批量抓取
 */
@Data
public class PictureUploadByBatchRequest {  
  
    /**  
     * 搜索词  
     */  
    private String searchText;  
  
    /**  
     * 抓取数量  
     */  
    private Integer count = 10;

    /**
     * 名称前缀
     */
    private String namePrefix;

    /**
     * 类型
     */
    private String category;
}
