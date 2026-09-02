package com.situ.elder.pojo.query;

import lombok.Data;

@Data
public class CheckInRecordQuery {
    private String elderName;
    private Long buildingId;
    private Long roomId;
    private Integer status;
    private Integer page;
    private Integer limit;
}
