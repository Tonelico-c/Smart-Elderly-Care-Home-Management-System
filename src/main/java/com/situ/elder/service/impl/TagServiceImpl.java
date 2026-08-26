package com.situ.elder.service.impl;

import com.situ.elder.pojo.entity.Tag;
import com.situ.elder.mapper.TagMapper;
import com.situ.elder.service.ITagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
