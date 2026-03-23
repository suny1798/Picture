package com.suny.picture.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.suny.picture.config.CosClientConfig;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

@Component
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }


    /**
     * 上传并解析图片
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        //对图像进行处理
        PicOperations picOperations = new PicOperations();
        //1表示返回图片信息
        picOperations.setIsPicInfo(1);

        //规则列表
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        //图片压缩--webp
        String webpkey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule rule = new PicOperations.Rule();
        rule.setFileId(webpkey);
        rule.setBucket(cosClientConfig.getBucket());
        rule.setRule("imageMogr2/format/webp");
        ruleList.add(rule);
        //仅当图片大于100k的时候才生成缩略图
        if (file.length() > 100 * 1024) {
            //缩略图处理
            PicOperations.Rule thumbRule = new PicOperations.Rule();
            String thumbKey = FileUtil.mainName(key) + "_thumb." + FileUtil.getSuffix(key);
            thumbRule.setFileId(thumbKey);
            thumbRule.setBucket(cosClientConfig.getBucket());
            //缩放规则
            thumbRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 300, 300));
            ruleList.add(thumbRule);
        }
        //构造处理参数
        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }




}
