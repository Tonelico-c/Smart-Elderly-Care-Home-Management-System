package com.situ.elder.pojo.query;

import lombok.Data;

@Data
public class RoomQuery {
    private String roomNo;
    private Long buildingId;
    private Integer roomType;
    private Integer status;
    private Integer page;
    private Integer limit;
}
