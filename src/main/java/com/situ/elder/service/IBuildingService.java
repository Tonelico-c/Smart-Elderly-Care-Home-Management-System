package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Building;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.BuildingQuery;
import com.situ.elder.pojo.vo.BuildingVO;

import java.util.Map;

/**
 * <p>
 * 楼栋表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface IBuildingService extends IService<Building> {

    IPage<BuildingVO> list(BuildingQuery buildingQuery);

    Map<String, Object> stats();
}
