package com.situ.elder.pojo.query;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ElderQuery {
    private String name;
    private String phone;
    private List<Long> tagIds;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
