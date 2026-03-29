package com.suny.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suny.picture.model.dto.space.SpaceAddRequest;
import com.suny.picture.model.dto.space.SpaceQueryRequest;
import com.suny.picture.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author sun
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-03-23 18:28:40
*/
public interface SpaceService extends IService<Space> {


    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 根据空间查询请求参数构建查询条件包装器
     *
     * @param spaceQueryRequest 用户查询请求对象，包含查询条件
     * @return QueryWrapper<Space> 返回一个包含查询条件的MyBatis-Plus的QueryWrapper对象，用于构建数据库查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 空间信息脱敏 单条
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 分页获取空间封装
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验空间
     *
     * @param space
     */
    void validSpace(Space space, boolean add);

    /**
     * 填充空间信息
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 空间权限校验
     *
     * @param space
     * @param loginUser
     */
    void checkSpaceAuth(Space space, User loginUser);
}
