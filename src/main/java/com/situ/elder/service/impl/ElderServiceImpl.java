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
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.IElderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public IPage<ElderVo> list(ElderQuery elderQuery) {
        IPage<ElderVo> page = new Page<>(elderQuery.getPage(), elderQuery.getLimit());

        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(elderQuery.getName()),Elder::getName, elderQuery.getName())
                .like(!ObjectUtils.isEmpty(elderQuery.getPhone()),Elder::getPhone, elderQuery.getPhone())
                .between(!ObjectUtils.isEmpty(elderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(elderQuery.getEndCreateTime()), Elder::getCreateTime, elderQuery.getBeginCreateTime(), elderQuery.getEndCreateTime());

       IPage<ElderVo> elderVoPage = elderMapper.list(page,elderQuery);
        for(ElderVo elderVo : elderVoPage.getRecords()){
            elderVo.setTagNames(elderVo.getTagNamesStr() == null
                    ? List.of() : List.of(elderVo.getTagNamesStr().split(",")));
        }
       return elderVoPage;

        /*// 1. 用 Elder 做分页查询（selectPage 泛型上界要求 IPage<Elder>）
        IPage<Elder> elderPage = new Page<>(elderQuery.getPage(), elderQuery.getLimit());
        elderMapper.selectPage(elderPage, wrapper);
        // 2. Elder 转 ElderVo，组装成新的分页对象（把 total、页码等信息带过去）
        List<ElderVo> records = elderPage.getRecords().stream().map(elder -> {
            ElderVo vo = new ElderVo();
            BeanUtils.copyProperties(elder, vo);
            return vo;
        }).toList();
        Page<ElderVo> voPage = new Page<>(elderPage.getCurrent(), elderPage.getSize(), elderPage.getTotal());
        voPage.setRecords(records);
        if(!ObjectUtils.isEmpty(records)){
            // 收集当前页所有老人的id
            List<Long> elderIds = records.stream().map(ElderVo::getId).toList();
            // 一次查出这些老人在 elder_tag 表中的所有关联记录
            List<ElderTag> elderTagList = elderTagMapper.selectList(
                    new LambdaQueryWrapper<ElderTag>().in(ElderTag::getElderId, elderIds)
            );
            // 一次查出涉及的标签详情，转成 id -> Tag 的Map，避免内层循环再查库
            Map<Long, Tag> tagMap = elderTagList.isEmpty() ? Map.of()
                    : tagMapper.selectBatchIds(elderTagList.stream().map(ElderTag::getTagId).distinct().toList())
                    .stream()
                    .collect(Collectors.toMap(Tag::getId, tag -> tag));
            // 按 elderId 分组，填充到每条记录的 tags 字段
            Map<Long, List<ElderTag>> elderTagMap = elderTagList.stream()
                    .collect(Collectors.groupingBy(ElderTag::getElderId));
            for (ElderVo vo : records) {
                List<Tag> tags = elderTagMap.getOrDefault(vo.getId(), List.of()).stream()
                        .map(elderTag -> tagMap.get(elderTag.getTagId()))
                        .filter(Objects::nonNull)
                        .toList();
                vo.setTags(tags);
            }
        }

        return voPage;*/
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
