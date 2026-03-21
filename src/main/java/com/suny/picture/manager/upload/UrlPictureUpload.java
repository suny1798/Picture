package com.suny.picture.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        // ... 跟之前的校验逻辑保持一致
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

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 中提取文件名  
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录  
        HttpUtil.downloadFile(fileUrl, file);
    }
}
