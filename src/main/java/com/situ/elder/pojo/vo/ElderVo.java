package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.Elder;

import com.situ.elder.pojo.entity.Tag;
import lombok.Data;

import java.util.List;


@Data
public class ElderVo extends Elder {
    private List<Tag> tags;
}
