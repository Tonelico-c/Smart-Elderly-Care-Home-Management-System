package com.situ.elder.service.impl;

import com.situ.elder.mapper.BedMapper;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.vo.BedVO;
import com.situ.elder.service.IBedService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 床位表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed> implements IBedService {

    @Autowired
    private BedMapper bedMapper;

    @Override
    public List<BedVO> listByRoom(Long roomId) {
        return bedMapper.listByRoom(roomId);
    }
}
