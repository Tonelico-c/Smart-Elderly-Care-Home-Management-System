package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.pojo.query.ElderQuery;
import com.situ.elder.service.IElderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * 老人表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-25
 */
@Service
public class ElderServiceImpl extends ServiceImpl<ElderMapper, Elder> implements IElderService {

    @Autowired
    private ElderMapper elderMapper;

    @Override
    public IPage<Elder> list(ElderQuery elderQuery) {
        IPage<Elder> page = new Page<>(elderQuery.getPage(), elderQuery.getLimit());
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(elderQuery.getName()),Elder::getName, elderQuery.getName())
                .like(!ObjectUtils.isEmpty(elderQuery.getPhone()),Elder::getPhone, elderQuery.getPhone())
                .between(!ObjectUtils.isEmpty(elderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(elderQuery.getEndCreateTime()), Elder::getCreateTime, elderQuery.getBeginCreateTime(), elderQuery.getEndCreateTime());
        return elderMapper.selectPage(page, wrapper);
    }
}
