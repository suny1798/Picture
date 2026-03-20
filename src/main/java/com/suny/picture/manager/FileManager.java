package com.suny.picture.manager;

import cn.hutool.Hutool;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.suny.picture.common.ResultUtils;
import com.suny.picture.config.CosClientConfig;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import com.suny.picture.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class FileManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;


    /**
     * 上传图片
     *
     * @param multipartFile    上传的文件
     * @param uploadFilePrefix 上传文件前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadFilePrefix) {
        //校验图片上传
        validPicture(multipartFile);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));

        String uploadPath = "/" + uploadFilePrefix + "/" + uploadFilename;
        //上传图片 并 解析结果
        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue());
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            //返回
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("文件上传到对象存储失败" + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 删除临时文件
     *
     * @param tempFile
     * @param filepath
     */
    private void deleteTempFile(File tempFile) {
        if (tempFile != null) {
            boolean delete = tempFile.delete();
            if (!delete) {
                log.error("临时文件删除失败");
            }
        }
    }

    /**
     * 校验图片上传
     *
     * @param multipartFile 文件
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        //校验文件大小
        long size = multipartFile.getSize();
        final long maxSize = 1024 * 1024;
        ThrowUtils.throwIf(size > maxSize * 2, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过 " + "2 MB");
        //检验文件后缀
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        //文件后缀集合
        final List<String> ALLOW_FILE_FORMAT = Arrays.asList("png", "jpg", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FILE_FORMAT.contains(suffix), ErrorCode.PARAMS_ERROR, "上传文件格式错误");
    }

    /**
     * 上传图片 通过URL
     *
     * @param multipartFile    上传的文件
     * @param uploadFilePrefix 上传文件前缀
     * @return
     */
    public UploadPictureResult uploadPictureByURL(String fileUrl, String uploadFilePrefix) {
        //校验图片上传
        validPicture(fileUrl);
        //图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));

        String uploadPath = "/" + uploadFilePrefix + "/" + uploadFilename;
        //上传图片 并 解析结果
        File tempFile = null;
        try {
            tempFile = File.createTempFile(uploadPath, null);
            HttpUtil.downloadFile(fileUrl, tempFile);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue());
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            //返回
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("文件上传到对象存储失败" + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 根据URL校验图片
     *
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        //校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "上传地址不能为空");
        // 校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传地址格式错误");
        }
        // 校验URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "上传地址格式错误");
        // 校验文件是否存在  发送HEAD请求
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            String type = httpResponse.header("Content-Type");
            if (StrUtil.isNotBlank(type)) {
                //检验文件后缀
                //文件后缀集合
                final List<String> ALLOW_FILE_FORMAT = Arrays.asList("image/png", "image/jpg", "image/jpeg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FILE_FORMAT.contains(type.toLowerCase()), ErrorCode.PARAMS_ERROR, "上传文件格式错误");
            }
            String len = httpResponse.header("Content-Length");
            if (StrUtil.isNotBlank(len)) {
                //校验文件大小
                try {
                    long parseLong = Long.parseLong(len);
                    final long size = 1024 * 1024;
                    ThrowUtils.throwIf(parseLong > size * 2, ErrorCode.PARAMS_ERROR, "上传文件大小不能超过 " + "2 MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传文件大小格式异常");
                }
            }

        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

    }

}
