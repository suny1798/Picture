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
}
