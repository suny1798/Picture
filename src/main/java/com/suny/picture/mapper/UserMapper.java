package com.suny.picture.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.suny.picture.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author sun
 * @description 针对表【user(用户)】的数据库操作Mapper
 * @createDate 2026-03-18 10:31:59
 * @Entity com.suny.sunpicturebackend.model.entity.User
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




