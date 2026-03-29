package com.suny.picture.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间类别分析请求类
 * 继承自SpaceAnalyzeRequest，用于空间类别分析相关的请求参数
 * 使用了Lombok的@EqualsAndHashCode和@Data注解简化代码
 */
@EqualsAndHashCode(callSuper = true) // 生成equals和hashCode方法时包含父类字段
@Data // 自动生成getter、setter、toString等方法
public class SpaceCategoryAnalyzeRequest extends SpaceAnalyzeRequest {

    // 空体类，没有额外字段，直接继承父类SpaceAnalyzeRequest的所有功能
}
