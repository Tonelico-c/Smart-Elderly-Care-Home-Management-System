package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.situ.elder.mapper.ElderMapper;
import com.situ.elder.mapper.ElderTagMapper;
import com.situ.elder.pojo.entity.ElderTag;
import com.situ.elder.pojo.entity.Tag;
import com.situ.elder.mapper.TagMapper;
import com.situ.elder.pojo.query.TagQuery;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 标签表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-08-26
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ElderTagMapper elderTagMapper;
    @Autowired
    private ElderMapper elderMapper;

    @Override
    public IPage<Tag> list(TagQuery tagQuery) {
        IPage<Tag> page = new Page<>(tagQuery.getPage(), tagQuery.getLimit());
        LambdaQueryWrapper<Tag> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(!ObjectUtils.isEmpty(tagQuery.getCode()),Tag::getCode, tagQuery.getCode());
        return tagMapper.selectPage(page, lambdaQueryWrapper);
    }

    /*@Override
    public List<ElderVo> selectRelatedElder(Long tagId) {
        // 1. 查出该标签关联的所有老人id
        List<Long> elderIds = elderTagMapper.selectList(
                new LambdaQueryWrapper<ElderTag>().eq(ElderTag::getTagId, tagId))
                .stream().map(ElderTag::getElderId).toList();

        if (elderIds.isEmpty()) {
            return List.of();
        }
        // 2. 一次批量查出所有老人并转成ElderVo（逻辑删除的会被自动过滤）
        List<ElderVo> elderVos = elderMapper.selectBatchIds(elderIds).stream().map(elder -> {
            ElderVo vo = new ElderVo();
            BeanUtils.copyProperties(elder, vo);
            return vo;
        }).toList();

        // 3. 批量查出这些老人的所有标签关联，并填充到每个老人的tags中
        List<ElderTag> elderTags = elderTagMapper.selectList(
                new LambdaQueryWrapper<ElderTag>().in(ElderTag::getElderId, elderIds));
        if (!elderTags.isEmpty()) {
            // tagId -> Tag
            Map<Long, Tag> tagMap = tagMapper.selectBatchIds(
                            elderTags.stream().map(ElderTag::getTagId).distinct().toList())
                    .stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
            // elderId -> 该老人的标签列表
            Map<Long, List<Tag>> elderTagMap = elderTags.stream().collect(
                    Collectors.groupingBy(ElderTag::getElderId,
                            Collectors.mapping(elderTag -> tagMap.get(elderTag.getTagId()),
                                    Collectors.toList())));
            for (ElderVo vo : elderVos) {
                List<Tag> tags = elderTagMap.getOrDefault(vo.getId(), List.of()).stream()
                        .filter(Objects::nonNull)
                        .toList();
                vo.setTags(tags);
            }
        }
        return elderVos;
    }*/
}
