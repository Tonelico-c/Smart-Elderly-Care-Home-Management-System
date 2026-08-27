package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.TagQuery;
import com.situ.elder.pojo.vo.ElderVo;

import java.util.List;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-26
 */
public interface ITagService extends IService<Tag> {

    IPage<Tag> list(TagQuery tagQuery);

//    List<ElderVo> selectRelatedElder(Long tagId);
}
