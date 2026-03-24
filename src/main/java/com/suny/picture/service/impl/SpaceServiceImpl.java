package com.suny.picture.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.model.dto.space.SpaceAddRequest;
import com.suny.picture.model.dto.space.SpaceQueryRequest;
import com.suny.picture.model.entity.Picture;
import com.suny.picture.model.entity.Space;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.enums.SpaceLevelEnum;
import com.suny.picture.model.vo.PictureVO;
import com.suny.picture.model.vo.SpaceVO;
import com.suny.picture.model.vo.UserVO;
import com.suny.picture.service.SpaceService;
import com.suny.picture.mapper.SpaceMapper;
import com.suny.picture.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sun
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-03-23 18:28:40
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;


    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param loginUser
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        //1. 填充默认参数
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (ObjUtil.isEmpty(space.getSpaceLevel())) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        //校验参数
        this.validSpace(space, true);
        // 权限校验
        Long userId = loginUser.getId();
        space.setUserId(userId);

        if (space.getSpaceLevel() != (SpaceLevelEnum.COMMON.getValue()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限创建指定级别的空间");
        }

        //设置权限 普通用户只能创建一个空间
        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                //判断是否已有空间 否则不能创建
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId).exists();
                // 以后空间，抛出异常
                if (exists) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "每个用户只能拥有一个私人空间");
                }
                //创建空间
                boolean save = this.save(space);
                if (!save) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建失败,数据库操作失败");
                }
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }

    /**
     * 查询
     *
     * @param spaceQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        // 需要拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 空间信息脱敏 单条
     *
     * @param space
     * @param request
     * @return
     */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    /**
     * 验证参数
     *
     * @param space
     * @param add   判断是否为创建或更新
     */
    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);

        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(spaceName), ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            ThrowUtils.throwIf(spaceLevel == null, ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        }
        //修改时校验
        ThrowUtils.throwIf(spaceName.length() > 20, ErrorCode.PARAMS_ERROR, "空间名称过长");
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "空间级别错误");

    }

    /**
     * 填充空间信息
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (enumByValue != null) {
            long maxCount = enumByValue.getMaxCount();
            long maxSize = enumByValue.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }
}




