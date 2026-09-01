package com.situ.elder.pojo.query;

import lombok.Data;

import java.util.Date;

@Data
public class BuildingQuery {
    private String buildingName;
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
