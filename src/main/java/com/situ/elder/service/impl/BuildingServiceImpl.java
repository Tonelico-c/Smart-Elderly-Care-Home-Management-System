package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.BuildingMapper;
import com.situ.elder.pojo.entity.Building;
import com.situ.elder.pojo.query.BuildingQuery;
import com.situ.elder.pojo.vo.BuildingVO;
import com.situ.elder.service.IBuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 楼栋表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements IBuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public IPage<BuildingVO> list(BuildingQuery buildingQuery) {
        IPage<BuildingVO> page = new Page<>(buildingQuery.getPage(), buildingQuery.getLimit());
        return buildingMapper.list(page, buildingQuery);
    }

    @Override
    public Map<String, Object> stats() {
        // 楼栋数量
        long buildingCount = count();
        // 房间总数、床位总数、入住总人数、空闲床位数量（Mapper.xml 联表统计）
        BuildingVO statsVO = buildingMapper.stats();
        Map<String, Object> map = new HashMap<>();
        map.put("buildingCount", buildingCount);
        map.put("roomCount", statsVO == null ? 0 : statsVO.getRoomCount());
        map.put("bedCount", statsVO == null ? 0 : statsVO.getBedCount());
        map.put("occupiedCount", statsVO == null ? 0 : statsVO.getResidentCount());
        map.put("freeBedCount", statsVO == null ? 0 : statsVO.getFreeBedCount());
        return map;
    }
}
