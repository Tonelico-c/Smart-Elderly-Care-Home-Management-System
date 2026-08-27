package com.situ.elder.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Elder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.situ.elder.pojo.query.ElderQuery;
import com.situ.elder.pojo.vo.ElderVo;

/**
 * <p>
 * 老人表 Mapper 接口
 * </p>
 *
 * @author Gao
 * @since 2026-08-25
 */
public interface ElderMapper extends BaseMapper<Elder> {

    IPage<ElderVo> list(IPage<ElderVo> voPage, ElderQuery elderQuery);
}
