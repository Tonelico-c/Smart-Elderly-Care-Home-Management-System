package com.situ.elder.pojo.query;

import lombok.Data;

@Data
public class ElderLeaveQuery {
    private String elderId;
    private String elderName;
    private Integer status;
    private Integer page;
    private Integer limit;
}
