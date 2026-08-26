package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Elder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.ElderQuery;

/**
 * <p>
 * 老人表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-25
 */
public interface IElderService extends IService<Elder> {

    IPage<Elder> list(ElderQuery elderQuery);
}
