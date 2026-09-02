package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.ExamItem;
import com.situ.elder.pojo.query.ExamItemQuery;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 体检项目表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
public interface IExamItemService extends IService<ExamItem> {

    IPage<ExamItem> list(ExamItemQuery examItemQuery);
}
