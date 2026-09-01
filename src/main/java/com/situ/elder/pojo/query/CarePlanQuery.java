package com.situ.elder.pojo.query;

import java.util.Date;
import lombok.Data;

@Data
public class CarePlanQuery {
    private String name;
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
