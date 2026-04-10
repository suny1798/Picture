package com.suny.picture.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间分类分析响应类
 * 用于存储和传输图片分类分析后的结果数据
 * 实现了Serializable接口，支持序列化操作
 * 使用了Lombok的@Data、@AllArgsConstructor和@NoArgsConstructor注解，自动生成getter、setter、构造函数等方法
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     * 表示图片所属的分类名称，如"风景"、"人物"等
     */
    private String category;

    /**
     * 图片数量
     * 表示该分类下图片的总数量
     */
    private Long count;

    /**
     * 分类图片总大小
     * 表示该分类下所有图片的总大小，通常以字节为单位
     */
    private Long totalSize;

    /**
     * 序列化版本UID
     * 用于序列化和反序列化过程中验证版本一致性，确保对象在序列化后反序列化时类的定义没有发生不兼容的变化
     */
    private static final long serialVersionUID = 1L;
}
