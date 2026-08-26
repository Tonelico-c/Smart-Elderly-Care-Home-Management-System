package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ElderTagMapper;
import com.situ.elder.mapper.TagMapper;
import com.situ.elder.pojo.entity.Elder;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.pojo.entity.ElderTag;
import com.situ.elder.pojo.entity.Tag;
import com.situ.elder.pojo.query.ElderQuery;
import com.situ.elder.service.IElderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ElderTagMapper elderTagMapper;

    @Override
    public IPage<Elder> list(ElderQuery elderQuery) {
        IPage<Elder> page = new Page<>(elderQuery.getPage(), elderQuery.getLimit());
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(elderQuery.getName()),Elder::getName, elderQuery.getName())
                .like(!ObjectUtils.isEmpty(elderQuery.getPhone()),Elder::getPhone, elderQuery.getPhone())
                .between(!ObjectUtils.isEmpty(elderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(elderQuery.getEndCreateTime()), Elder::getCreateTime, elderQuery.getBeginCreateTime(), elderQuery.getEndCreateTime());
        return elderMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, Object> selectAssignedTag(Long elderId) {
        // 获取所有标签
        List<Tag> tagList = tagMapper.selectList(null);

        LambdaQueryWrapper<ElderTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderTag::getElderId, elderId);
        // 获取已分配的标签
        List<Long> assignedTagIdList = elderTagMapper.selectList(wrapper).stream()
                .map(ElderTag::getTagId).toList();
        Map<String, Object> map = new HashMap<>();
        map.put("tagList", tagList);
        map.put("assignedTagIdList", assignedTagIdList);
        return map;
    }

    @Override
    public void assignTag(Long elderId, Long[] tagIds) {
        // 删除老人已分配的标签
        LambdaQueryWrapper<ElderTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderTag::getElderId, elderId);
        elderTagMapper.delete(wrapper);
        // 添加新的标签
        for(Long tagId : tagIds){
            ElderTag eldertag = new ElderTag();
            eldertag.setElderId(elderId);
            eldertag.setTagId(tagId);
            elderTagMapper.insert(eldertag);
        }
    }
}
