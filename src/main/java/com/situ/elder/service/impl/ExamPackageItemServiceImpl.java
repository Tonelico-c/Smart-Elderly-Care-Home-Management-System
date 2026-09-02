package com.situ.elder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.situ.elder.pojo.entity.ExamPackageItem;
import com.situ.elder.mapper.ExamPackageItemMapper;
import com.situ.elder.service.IExamPackageItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 体检套餐项目关联表 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-02
 */
@Service
public class ExamPackageItemServiceImpl extends ServiceImpl<ExamPackageItemMapper, ExamPackageItem> implements IExamPackageItemService {

    @Override
    public List<Long> selectAssignedItem(Long packageId) {
        List<ExamPackageItem> examPackageItemList = list(new QueryWrapper<ExamPackageItem>().eq("package_id", packageId));
        return examPackageItemList.stream().map(ExamPackageItem::getExamItemId).toList();
    }

    @Override
    public void assignItem(Long packageId, Long[] examItemIds) {
        // 删除该套餐原有的关联
        remove(new QueryWrapper<ExamPackageItem>().eq("package_id", packageId));
        // 插入新选择的关联,遍历examItemIds,将每个examItemId与packageId组合成ExamPackageItem对象并插入数据库
        if (examItemIds != null) {
            for (Long examItemId : examItemIds) {
                ExamPackageItem examPackageItem = new ExamPackageItem();
                examPackageItem.setPackageId(packageId);
                examPackageItem.setExamItemId(examItemId);
                save(examPackageItem);
            }
        }
    }
}
