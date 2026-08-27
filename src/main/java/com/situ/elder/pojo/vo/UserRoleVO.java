package com.situ.elder.pojo.vo;

import com.situ.elder.pojo.entity.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleVO {
    private List<Role> roleList;
    private List<Long> assignedRoleIdList;
}
