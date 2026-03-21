package com.suny.picture.mapper;

import com.suny.picture.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author sun
 * @description 针对表【picture(图片)】的数据库操作Mapper
 * @createDate 2026-03-19 16:11:52
 * @Entity com.suny.picture.model.entity.Picture
 */
public interface PictureMapper extends BaseMapper<Picture> {

    
    @Select("SELECT DISTINCT category FROM picture WHERE category IS NOT NULL AND isDelete = 0")
    List<String> selectDistinctCategory();

    @Select("SELECT tags FROM picture WHERE tags IS NOT NULL AND isDelete = 0")
    List<String> selectAllTags();
}




