package com.situ.elder.pojo.query;

import java.util.Date;
import lombok.Data;

@Data
public class CareTaskQuery {
    private Long elderId;
    private Integer status;
    private Date beginPlanExecuteDate;
    private Date endPlanExecuteDate;
    private Integer page;
    private Integer limit;
    private Long userId;
}
