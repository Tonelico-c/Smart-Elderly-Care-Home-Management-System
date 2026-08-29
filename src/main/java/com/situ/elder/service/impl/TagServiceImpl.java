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

}
