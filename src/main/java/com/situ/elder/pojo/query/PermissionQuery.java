package com.situ.elder.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class PermissionQuery {
    private Long parentId;
    private String name;
    private String permissionValue;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
