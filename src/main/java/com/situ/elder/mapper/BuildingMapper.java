package com.situ.elder.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Building;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.situ.elder.pojo.query.BuildingQuery;
import com.situ.elder.pojo.vo.BuildingVO;

/**
 * <p>
 * 楼栋表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface BuildingMapper extends BaseMapper<Building> {

    IPage<BuildingVO> list(IPage<BuildingVO> page, BuildingQuery buildingQuery);

    /**
     * 顶部统计：房间总数、床位总数、入住总人数
     */
    BuildingVO stats();
}
