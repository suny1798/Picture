package com.suny.picture.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.suny.picture.annotation.AuthCheck;
import com.suny.picture.common.BaseResponse;
import com.suny.picture.common.DeleteRequest;
import com.suny.picture.common.ResultUtils;
import com.suny.picture.constant.UserConstant;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.manager.CosManager;
import com.suny.picture.mapper.PictureMapper;
import com.suny.picture.model.dto.picture.*;
import com.suny.picture.model.entity.Picture;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.enums.PictureReviewStatusEnum;
import com.suny.picture.model.vo.PictureTagCategory;
import com.suny.picture.model.vo.PictureVO;
import com.suny.picture.service.PictureService;
import com.suny.picture.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequestMapping("/picture")
@RestController
@Api(tags = "图片相关接口")
public class PictureController {

    @Resource
    private CosManager cosManager;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @Resource
    private PictureMapper pictureMapper;


    /**
     * 上传图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 请求
     * @param request              请求
     * @return 图片信息
     */
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    @ApiOperation(value = "上传图片 by 文件")
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //上传图片
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);

    }

    /**
     * 通过URL上传图片
     *
     * @param pictureUploadRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/url")
    @ApiOperation(value = "上传图片 bu URL")
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //上传图片
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);

    }


    /**
     * 删除图片
     *
     * @param deleteRequest 请求
     * @param request       请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除图片")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断数据是否存在
        Picture oldPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        //仅限本人和管理员可修改、删除
        ThrowUtils.throwIf(!(loginUser.getId().equals(oldPicture.getUserId()) || userService.isAdmin(loginUser)), ErrorCode.NO_AUTH_ERROR);
        //操作数据库
        boolean result = pictureService.removeById(deleteRequest.getId());
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(true);

    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新图片（仅管理员可用）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        //获取当前用户
        User loginUser = userService.getLoginUser(request);
        //数据校验
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        //填充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据 id 获取图片（仅管理员可用）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取图片（封装类 用户使用）")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取图片列表（仅管理员可用）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取图片列表（封装类 用户使用）")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //只查询通过审核的
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑图片（给用户使用）")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        //填充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

//    @GetMapping("/tag_category")
//    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
//        PictureTagCategory pictureTagCategory = new PictureTagCategory();
//        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
//        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
//        pictureTagCategory.setTagList(tagList);
//        pictureTagCategory.setCategoryList(categoryList);
//        return ResultUtils.success(pictureTagCategory);
//    }

    /**
     * 获取图片分类和标签
     * @return
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        // 1. 查询 category
        List<String> categoryList = pictureMapper.selectDistinctCategory();
        // 2. 查询 tags
        List<String> tagJsonList = pictureMapper.selectAllTags();
        // 3. 解析 tags
        Set<String> tagSet = new HashSet<>();
        for (String tagJson : tagJsonList) {
            if (StringUtils.isBlank(tagJson)) {
                continue;
            }
            try {
                List<String> tags = JSONUtil.toList(tagJson, String.class);
                tagSet.addAll(tags);
            } catch (Exception e) {
                // 防止脏数据
                log.warn("标签解析失败: {}", tagJson);
            }
        }
        // 4. 封装返回
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        pictureTagCategory.setCategoryList(categoryList);
        pictureTagCategory.setTagList(new ArrayList<>(tagSet));
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     */
    @PostMapping("/review")
    @ApiOperation(value = "审核图片（仅管理员可用）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null || pictureReviewRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量抓取并上传图片
     * @param pictureUploadByBatchRequest
     * @param request
     * @return
     */
    @PostMapping("/upload/batch")
    @ApiOperation(value = "批量抓取图片（仅管理员可用）")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }


}
