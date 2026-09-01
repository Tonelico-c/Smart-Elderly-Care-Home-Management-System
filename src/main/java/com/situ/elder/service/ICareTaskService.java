package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.CareTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.CareTaskQuery;

/**
 * <p>
 * 护理任务与打卡记录表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-09-01
 */
public interface ICareTaskService extends IService<CareTask> {

    IPage<CareTask> list(CareTaskQuery careTaskQuery);
}
