package com.situ.elder.pojo.query;

import java.util.Date;
import lombok.Data;

@Data
public class ExamAppointmentQuery {
    private Long elderId;
    private Long packageId;
    private Integer status;
    private Date beginCreateTime;
    private Date endCreateTime;
    private Integer page;
    private Integer limit;
}
