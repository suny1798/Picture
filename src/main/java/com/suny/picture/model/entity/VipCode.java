package com.suny.picture.model.entity;

import lombok.Data;

@Data
public class VipCode {
    private String code;
    private Integer months;
    private Boolean used;
}