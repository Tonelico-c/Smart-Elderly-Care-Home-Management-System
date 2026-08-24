package com.situ.elder.pojo.query;

import lombok.Data;

@Data
public class UserQuery {
    private String name;
    private String email;
    private Integer page;
    private Integer limit;
}
