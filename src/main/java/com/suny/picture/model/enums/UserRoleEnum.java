package com.suny.picture.model.enums;


import cn.hutool.core.util.ObjUtil;
import lombok.Getter;


/**
 * @program: sunpicture
 * @description: 用户角色枚举类
 */
@Getter
public enum UserRoleEnum {

    ADMIN("管理员", "admin"),
    USER("普通用户", "user"),
    FVIP("临时会员", "fvip"),
    SVIP("永久会员", "svip");


    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据传入的值获取对应的枚举实例
     *
     * @param value 枚举中存储的值
     * @return 匹配到的枚举实例，如果没有匹配则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        // 检查传入的值是否为空
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        // 遍历枚举的所有实例
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            // 比较当前枚举实例的值与传入的值是否相等
            if (roleEnum.getValue().equals(value)) {
                // 如果匹配，则返回该枚举实例
                return roleEnum;
            }
        }
        // 如果没有匹配的枚举实例，则返回null
        return null;
    }
}
