package com.suny.picture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suny.picture.model.dto.picture.PictureQueryRequest;
import com.suny.picture.model.dto.picture.PictureReviewRequest;
import com.suny.picture.model.dto.picture.PictureUploadRequest;
import com.suny.picture.model.dto.user.UserQueryRequest;
import com.suny.picture.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suny.picture.model.entity.User;
import com.suny.picture.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author sun
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-19 16:11:52
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest,
                            User loginUser);


    /**
     * 根据图片查询请求参数构建查询条件包装器
     *
     * @param pictureQueryRequest 用户查询请求对象，包含查询条件
     * @return QueryWrapper<Picture> 返回一个包含查询条件的MyBatis-Plus的QueryWrapper对象，用于构建数据库查询条件
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 图片信息脱敏 单条
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @Param("pictureReviewRequest") PictureReviewRequest pictureReviewRequest,
     * @Param("loginuser") User loginuser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginuser);

    /**
     * 填充图片审核参数
     *
     * @param picture
     * @param loginuser
     */
    void fillReviewParams(Picture picture, User loginuser);
}
