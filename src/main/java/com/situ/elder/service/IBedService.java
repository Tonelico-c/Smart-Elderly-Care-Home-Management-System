package com.situ.elder.service;

import com.situ.elder.pojo.entity.Bed;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.vo.BedVO;

import java.util.List;

/**
 * <p>
 * 床位表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface IBedService extends IService<Bed> {

    List<BedVO> listByRoom(Long roomId);
}
