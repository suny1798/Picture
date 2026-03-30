package com.suny.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suny.picture.model.dto.space.SpaceAddRequest;
import com.suny.picture.model.dto.space.SpaceQueryRequest;
import com.suny.picture.model.dto.spaceuser.SpaceUserAddRequest;
import com.suny.picture.model.dto.spaceuser.SpaceUserQueryRequest;
import com.suny.picture.model.entity.Space;
import com.suny.picture.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.SpaceUserVO;
import com.suny.picture.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author sun
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-03-29 13:28:59
 */
public interface SpaceUserService extends IService<SpaceUser> {


    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @param loginUser
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);


    /**
     * 根据空间查询请求参数构建查询条件包装器
     *
     * @param spaceUserQueryRequest 用户查询请求对象，包含查询条件
     * @return QueryWrapper<Space> 返回一个包含查询条件的MyBatis-Plus的QueryWrapper对象，用于构建数据库查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 空间成员信息脱敏 列表
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     * 空间成员信息脱敏 列表
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceuserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 校验空间
     *
     * @param spaceUser
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

}
