package com.situ.elder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.situ.elder.pojo.entity.Elder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.situ.elder.pojo.query.ElderQuery;
import com.situ.elder.pojo.vo.ElderInfoVO;
import com.situ.elder.pojo.vo.ElderVo;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * <p>
 * 老人表 服务类
 * </p>
 *
 * @author Gao
 * @since 2026-08-25
 */
public interface IElderService extends IService<Elder> {

    IPage<ElderVo> list(ElderQuery elderQuery);

    Map<String, Object> selectAssignedTag(Long elderId);

    void assignTag(Long elderId, Long[] tagIds);

    void exportExcel(HttpServletResponse response);

    ElderInfoVO getElderInfo(Long elderId);
}
