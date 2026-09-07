package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.exception.ServiceException;
import com.situ.elder.mapper.RoomMapper;
import com.situ.elder.pojo.entity.Bed;
import com.situ.elder.pojo.entity.Room;
import com.situ.elder.pojo.query.RoomQuery;
import com.situ.elder.pojo.vo.RoomVO;
import com.situ.elder.service.IBedService;
import com.situ.elder.service.IRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private IBedService bedService;

    @Override
    public IPage<RoomVO> list(RoomQuery roomQuery) {
        IPage<RoomVO> page = new Page<>(roomQuery.getPage(), roomQuery.getLimit());
        return roomMapper.list(page, roomQuery);
    }

    /**
     * 新增房间，并按床位数量自动生成对应床位（编号如 1号床，默认空闲、费用0，可在床位管理中调整）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRoom(Room room) {
        // 校验房型与床位数量匹配：单人间只能1个、双人间只能2个、多人间不能低于3个
        Integer roomType = room.getRoomType();
        Integer bedCount = room.getBedCount();
        if (roomType != null && bedCount != null) {
            boolean valid = (roomType == 1 && bedCount == 1)
                    || (roomType == 2 && bedCount == 2)
                    || (roomType == 3 && bedCount >= 3);
            if (!valid) {
                throw new ServiceException("床位数量与房型不符：单人间只能1个、双人间只能2个、多人间不能低于3个");
            }
        }
        // 保存房间，拿到自增 id
        this.save(room);
        // 按床位数量批量生成床位
        List<Bed> beds = new ArrayList<>();
        for (int i = 1; i <= bedCount; i++) {
            Bed bed = new Bed();
            bed.setRoomId(room.getId());
            bed.setBedNo(i + "号床");
            bed.setStatus(0);
            bed.setPrice(BigDecimal.ZERO);
            beds.add(bed);
        }
        if (!beds.isEmpty()) {
            bedService.saveBatch(beds);
        }
    }
}
