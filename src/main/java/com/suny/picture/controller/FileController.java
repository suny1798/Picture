package com.suny.picture.controller;

import cn.hutool.http.server.HttpServerResponse;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.suny.picture.annotation.AuthCheck;
import com.suny.picture.common.BaseResponse;
import com.suny.picture.common.ResultUtils;
import com.suny.picture.constant.UserConstant;
import com.suny.picture.exception.BusinessException;
import com.suny.picture.exception.ErrorCode;
import com.suny.picture.manager.CosManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Slf4j
@RequestMapping("/file")
@RestController
@Api(tags = "文件上传")
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    @ApiOperation(value = "上传文件")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file){
        String filename = file.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        File tempFile = null;
        try {
            tempFile = File.createTempFile(filepath, null);
            file.transferTo(tempFile);
            cosManager.putObject(filepath, tempFile);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            log.error("文件上传失败, 文件路径为" + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        }finally {
            if (tempFile != null) {
                boolean delete = tempFile.delete();
                if (!delete) {
                    log.error("临时文件删除失败, 文件路径为" + filepath);
                }
            }
        }
    }



    /**
     * 测试文件下载
     *
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download/")
    @ApiOperation(value = "下载文件")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            byte[] bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // 写入响应
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }




}
