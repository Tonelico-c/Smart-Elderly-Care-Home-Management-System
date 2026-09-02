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
import com.situ.elder.pojo.vo.ElderExcelVO;
import com.situ.elder.pojo.vo.ElderInfoVO;
import com.situ.elder.pojo.vo.ElderVo;
import com.situ.elder.service.IElderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.situ.elder.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;
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

    /**
     * 分页 + 多条件查询老人列表
     * <p>
     * 实现逻辑：
     * 1. 根据前端传入的分页参数（page、limit）构造分页对象；
     * 2. 构造 LambdaQueryWrapper，按姓名、电话模糊匹配，按创建时间区间过滤
     *    （条件为空时自动跳过，避免拼出无效 SQL）；
     * 3. 调用 Mapper 的自定义联表查询，一次拿到分页数据及每个老人的标签名拼接串；
     * 4. 遍历当前页记录，把逗号分隔的标签名串拆成 List，方便前端直接渲染标签。
     * <p>
     * 下方注释掉的代码是旧实现：先查 Elder 分页，再在内存中分批查标签表组装，
     * 已被 Mapper 中 GROUP_CONCAT 联表方案替代（后者只需一条 SQL）。
     *
     * @param elderQuery 分页及查询条件（姓名、电话、创建时间区间）
     * @return 老人信息分页结果，含每人的标签名集合
     */
    @Override
    public IPage<ElderVo> list(ElderQuery elderQuery) {
        // 构造分页对象：当前页码 + 每页条数
        IPage<ElderVo> page = new Page<>(elderQuery.getPage(), elderQuery.getLimit());

        // 构造查询条件：like 条件只在参数非空时生效，between 需要起止时间都传了才拼接
        LambdaQueryWrapper<Elder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(elderQuery.getName()),Elder::getName, elderQuery.getName())
                .like(!ObjectUtils.isEmpty(elderQuery.getPhone()),Elder::getPhone, elderQuery.getPhone())
                .between(!ObjectUtils.isEmpty(elderQuery.getBeginCreateTime()) && !ObjectUtils.isEmpty(elderQuery.getEndCreateTime()), Elder::getCreateTime, elderQuery.getBeginCreateTime(), elderQuery.getEndCreateTime());

        // 联表分页查询：Mapper 中已把老人信息和其标签名（GROUP_CONCAT 拼接）一起查出
       IPage<ElderVo> elderVoPage = elderMapper.list(page,elderQuery);
        // 把每条记录中逗号分隔的标签名串（如 "慢病,自理"）拆成 List，供前端展示
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

    /**
     * 查询指定老人的"分配标签"回显数据
     * <p>
     * 实现逻辑：
     * 1. 查出系统中所有标签（供前端渲染全量标签复选框）；
     * 2. 按 elderId 查 elder_tag 关联表，取出该老人已绑定的标签 id 集合；
     * 3. 两者放入 Map 一起返回：tagList = 全部标签，assignedTagIdList = 已选中项。
     *
     * @param elderId 老人 id
     * @return 全部标签列表 + 该老人已分配的标签 id 列表
     */
    @Override
    public Map<String, Object> selectAssignedTag(Long elderId) {
        // 查出全部标签（selectList 传 null 表示无条件查全表）
        List<Tag> tagList = tagMapper.selectList(null);

        // 按 elderId 过滤 elder_tag 关联表，只取 tagId 组成集合
        LambdaQueryWrapper<ElderTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderTag::getElderId, elderId);
        List<Long> assignedTagIdList = elderTagMapper.selectList(wrapper).stream()
                .map(ElderTag::getTagId).toList();
        Map<String, Object> map = new HashMap<>();
        map.put("tagList", tagList);
        map.put("assignedTagIdList", assignedTagIdList);
        return map;
    }

    /**
     * 重新分配老人的标签（全量覆盖式）
     * <p>
     * 实现逻辑：先删除该老人在 elder_tag 表中的所有旧关联，
     * 再把前端传来的新标签 id 逐条插入，等价于"先删后增"的全量更新。
     * 注：未加事务注解，中途失败可能产生部分插入，必要时可加 @Transactional。
     *
     * @param elderId 老人 id
     * @param tagIds  新的标签 id 数组
     */
    @Override
    public void assignTag(Long elderId, Long[] tagIds) {
        // 1. 删除该老人已有的全部标签关联
        LambdaQueryWrapper<ElderTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderTag::getElderId, elderId);
        elderTagMapper.delete(wrapper);
        // 2. 逐条插入新选择的标签关联
        for(Long tagId : tagIds){
            ElderTag eldertag = new ElderTag();
            eldertag.setElderId(elderId);
            eldertag.setTagId(tagId);
            elderTagMapper.insert(eldertag);
        }
    }

    /**
     * 导出全部老人信息为 Excel 并写入响应流
     * <p>
     * 实现逻辑：
     * 1. 一次性查出所有老人（含标签名拼接串）；
     * 2. ElderVo 转 ElderExcelVO（字段名相同的自动拷贝），
     *    同时把数字状态码翻译成中文备注（如 0→禁用、4→入住中），
     *    便于阅读导出文件，而非面对裸数字；
     * 3. 调用工具类通过 EasyExcel 写出，文件名为"老人信息表"。
     *
     * @param response HTTP 响应，Excel 文件直接写回给浏览器下载
     */
    @Override
    public void exportExcel(HttpServletResponse response) {
        // 查询所有老人的展示数据（含标签名串）
        List<ElderVo> elderVoList = elderMapper.selectElderVoList();

        // VO 转换 + 状态码翻译
        List<ElderExcelVO> elderExcelVOList = elderVoList.stream()
                        .map(elderVo -> {
                            ElderExcelVO elderExcelVO = new ElderExcelVO();
                            // 同名属性自动拷贝（id、name、birthday 等）
                            BeanUtils.copyProperties(elderVo, elderExcelVO);
                            // 状态码 -> 中文说明，导出后更易读
                            switch (elderVo.getStatus()){
                                case 0:
                                    elderExcelVO.setStatusRemark("禁用");
                                    break;
                                case 1:
                                    elderExcelVO.setStatusRemark("启用");
                                    break;
                                case 2:
                                    elderExcelVO.setStatusRemark("请假");
                                    break;
                                case 3:
                                    elderExcelVO.setStatusRemark("退住中");
                                    break;
                                case 4:
                                    elderExcelVO.setStatusRemark("入住中");
                                    break;
                                case 5:
                                    elderExcelVO.setStatusRemark("已退住");
                                    break;
                            }
                            return elderExcelVO;
                        }).toList();

        // EasyExcel 写出并下载
        ExcelUtil.exportExcel(response,elderExcelVOList,ElderExcelVO.class,"老人信息表");
    }

    @Override
    public ElderInfoVO getElderInfo(Long elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new RuntimeException("老人不存在");
        }
        ElderInfoVO elderInfoVO = new ElderInfoVO();
        BeanUtils.copyProperties(elder, elderInfoVO);
        if (elder.getBirthday() != null) {
            elderInfoVO.setBirthday(new SimpleDateFormat("yyyy-MM-dd").format(elder.getBirthday()));
            elderInfoVO.setAge(calcAge(elder.getBirthday()));
        }
        return elderInfoVO;
    }

    /**
     * 根据出生日期计算周岁年龄
     */
    private Integer calcAge(Date birthday) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthday);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        //生日还没到，年龄减1
        if (now.get(Calendar.MONTH) < birth.get(Calendar.MONTH)
                || (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }
        return age;
    }
}
