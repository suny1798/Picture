package com.suny.picture.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.mapper.SpaceMapper;
import com.suny.picture.model.dto.space.analyze.*;
import com.suny.picture.model.entity.Picture;
import com.suny.picture.model.entity.Space;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.space.analyze.SpaceCategoryAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceTagAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceUsageAnalyzeResponse;
import com.suny.picture.model.vo.space.analyze.SpaceUserAnalyzeResponse;
import com.suny.picture.service.PictureService;
import com.suny.picture.service.SpaceAnalyzeService;
import com.suny.picture.service.SpaceService;
import com.suny.picture.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sun
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-03-23 18:28:40
 */
@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private PictureService pictureService;

    /**
     * 空间分析
     *
     * @param spaceUsageAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        // 校验参数
        // 校验全空间或公共空间需要从 Picture 表分析，个人空间需要从 Space 表分析
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // 查询全空间或公共空间分析
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");
            fillAnalyzeQueryWrapper(spaceUsageAnalyzeRequest, queryWrapper);
            List<Object> objects = pictureService.getBaseMapper().selectObjs(queryWrapper);
            long usedsize = objects.stream().mapToLong(obj -> (Long) obj).sum();
            long usedCount = objects.size();
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            //公共图库  无数量 容量 比例限制
            response.setUsedSize(usedsize);
            response.setUsedCount(usedCount);
            response.setMaxSize(null);
            response.setSizeUsageRatio(null);
            response.setMaxCount(null);
            response.setCountUsageRatio(null);
            return response;
        } else {
            // 查询个人空间分析
            Space space = spaceService.getById(spaceUsageAnalyzeRequest.getSpaceId());
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            //校验权限
            checkSpaceAnalyzeAuth(spaceUsageAnalyzeRequest, loginUser);
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            //公共图库  无数量 容量 比例限制
            response.setUsedSize(space.getTotalSize());
            response.setUsedCount(space.getTotalCount());
            response.setMaxSize(space.getMaxSize());
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);
            response.setMaxCount(space.getMaxCount());
            response.setCountUsageRatio(countUsageRatio);
            return response;
        }
    }

    /**
     * 获取空间类别分析结果的方法
     *
     * @param spaceCategoryAnalyzeRequest 空间类别分析请求参数对象，包含分析所需的条件信息
     * @param loginUser                   当前登录用户信息，用于权限验证等操作
     * @return SpaceCategoryAnalyzeResponse 空间类别分析响应对象，包含分析结果数据
     * @Override 表示重写父类的同名方法
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 校验权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);
        // 查询空间类别分析结果
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);
        // mybatis-plus 分组查询
        queryWrapper.select("category", "count(*) as count", "sum(picSize) as totalSize").groupBy("category");

        List<SpaceCategoryAnalyzeResponse> collect = pictureService.getBaseMapper().selectMaps(queryWrapper).stream().map(result -> {
            String category = result.get("category") != null ? result.get("category").toString() : "未分类";
            Long count = ((Number) result.get("count")).longValue();
            Long totalSize = ((Number) result.get("totalSize")).longValue();
            return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 检查权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);

        // 查询所有符合条件的标签
        queryWrapper.select("tags");
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().filter(ObjUtil::isNotNull).map(Object::toString).collect(Collectors.toList());

        // 合并所有标签并统计使用次数
        Map<String, Long> tagCountMap = tagsJsonList.stream().flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream()).collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        // 转换为响应对象，按使用次数降序排序
        return tagCountMap.entrySet().stream().sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // 降序排列
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 检查权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        // 查询所有符合条件的图片大小
        queryWrapper.select("picSize");
        List<Long> picSizes = pictureService.getBaseMapper().selectObjs(queryWrapper).stream().map(size -> ((Number) size).longValue()).collect(Collectors.toList());

        // 定义分段范围，注意使用有序 Map
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        sizeRanges.put("<500KB", picSizes.stream().filter(size -> size < 500 * 1024).count());
        sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1024 * 1024).count());
        sizeRanges.put("1MB-10MB", picSizes.stream().filter(size -> size >= 1024 * 1024 && size < 10 * 1024 * 1024).count());
        sizeRanges.put(">10MB", picSizes.stream().filter(size -> size >= 10 * 1024 * 1024).count());

        // 转换为响应对象
        return sizeRanges.entrySet().stream().map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    /**
     * 校验空间分析权限
     *
     * @param spaceAnalyzeRequest
     * @param loginUSer
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUSer) {
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();

        if (queryAll || queryPublic) {
            // 查询所有或者查询公开空间，校验管理员
            ThrowUtils.throwIf(!userService.isAdmin(loginUSer), ErrorCode.NO_AUTH_ERROR);
        } else {
            // 查询个人空间，校验用户本人和管理员
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null, ErrorCode.PARAMS_ERROR);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 仅本人或管理员可编辑
            spaceService.checkSpaceAuth(space, loginUSer);
        }
    }

    /**
     * 用户行为分析
     *
     * @param spaceUserAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 检查权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);

        // 构造查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

        // 分析维度：每日、每周、每月
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            case "month":
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        // 分组和排序
        queryWrapper.groupBy("period").orderByAsc("period");

        // 查询结果并转换
        List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return queryResult.stream().map(result -> {
            String period = result.get("period").toString();
            Long count = ((Number) result.get("count")).longValue();
            return new SpaceUserAnalyzeResponse(period, count);
        }).collect(Collectors.toList());
    }

    /**
     * 查看空间使用排行
     * @param spaceRankAnalyzeRequest
     * @param loginUser
     * @return
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 仅管理员可查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");
        // 构造查询条件
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("LIMIT " + spaceRankAnalyzeRequest.getTopN()); // 取前 N 名
        // 查询结果
        return spaceService.list(queryWrapper);
    }


    /**
     * 填充分析查询条件
     *
     * @param spaceAnalyzeRequest
     * @param queryWrapper
     */
    private void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        //全空间分析
        if (queryAll) {
            return;
        }
        //查询公开空间
        if (queryPublic) {
            queryWrapper.isNull("spaceId");
            return;
        }
        //查询特定空间
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }


}




