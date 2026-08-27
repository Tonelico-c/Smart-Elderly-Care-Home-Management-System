# 老人管理：按标签（多选）搜索

## 原则
- 不改动任何已有代码行，只在必要位置**追加**新代码 / 新增字段。
- 复用现有 `GET /elders` 接口和 `ElderVo` 返回结构，搜索结果格式与原来完全一致（分页、tags 列展示都不变）。

## 匹配逻辑
「全部匹配」：老人必须**同时拥有**所有选中的标签才出现在结果中（任一标签未分配则不显示）。

## 后端改动（2 个文件，全部是追加）

### 1. `src/main/java/com/situ/elder/pojo/query/ElderQuery.java`
新增字段（不改动已有字段）：
```java
private List<Long> tagIds;   // import java.util.List
```

### 2. `src/main/java/com/situ/elder/service/impl/ElderServiceImpl.java` 的 `list` 方法
在 wrapper 构建之后、`selectPage` 之前**追加**一段标签过滤逻辑：
1. 若 `elderQuery.getTagIds()` 非空：
   - 查 `elder_tag` 表：`in(ElderTag::getTagId, tagIds)`；
   - 按 `elderId` 分组，统计每个老人命中的**去重标签数**；
   - 命中数 `== tagIds 去重后的数量` 的老人才算匹配（全部匹配）；
   - 若没有一个老人匹配，直接返回空的 `Page<ElderVo>`（total=0）；
   - 否则给 wrapper 追加 `.in(Elder::getId, matchedElderIds)`，走原有分页 + tags 组装流程。

这样原有分页、ElderVo 转换、标签填充逻辑全部原样复用，返回格式不变。

## 前端改动（1 个文件 `ui/ui-admin/src/views/Elder.vue`，全部是追加）

1. `import tagApi`，新增 `allTags` ref；页面加载时调用 `tagApi.list({page:1, limit:1000})` 拉取全部标签作为选项。
2. 新增 `selectedTagIds = ref([])` 保存多选结果。
3. `loadData` 内**追加一行**：`elderQuery.value.tagIds = selectedTagIds.value.join(',')`
   —— 与已有 `assignTag` 的传参风格一致（逗号拼接），Spring MVC 会自动把 `"1,2,3"` 转成 `List<Long>`，无需改 request.js 的参数序列化。
4. 搜索表单**追加**一个 `el-form-item`：
   ```html
   <el-form-item label="标签">
     <el-select v-model="selectedTagIds" multiple clearable collapse-tags
                placeholder="请选择标签" style="width: 220px">
       <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id"/>
     </el-select>
   </el-form-item>
   ```

## 效果
- 搜索栏多一个可多选的标签下拉框，和名字/电话/创建时间条件是 AND 关系，点「搜索」一起生效。
- 结果仍是原来的分页表格（含标签列），翻页时条件保留。

## 不需要改动的部分
- `ElderController`、`elder.js` API 层、`request.js`、分页组件、标签列展示 —— 全部复用现状。
