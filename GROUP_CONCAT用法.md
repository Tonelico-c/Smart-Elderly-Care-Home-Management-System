# MySQL 聚合函数 GROUP_CONCAT() 用法详解

> `GROUP_CONCAT()` 是 MySQL 的聚合函数，作用是：**把分组后同一组内的多行某个字段的值，拼接成一个字符串**。
> 普通聚合函数（SUM、COUNT）是把多行"压缩成一个数"，它则是把多行"压缩成一个串"。

## 一、基本语法

```sql
GROUP_CONCAT([DISTINCT] 要拼接的字段
             [ORDER BY 排序字段 ASC/DESC]
             [SEPARATOR '分隔符'])
```

三个可选部分都写在括号里，顺序固定：**去重 -> 排序 -> 分隔符**。

## 二、逐个演示

以下面数据为例（`elder_tag` 关联表 + `tag` 标签表联查结果）：

| elder_id | name |
|----------| ------ |
| 1        | 慢病   |
| 1        | 自理   |
| 1        | 高血压  |
| 2        | 自理   |

### 1. 最简形式（默认逗号分隔）

```sql
SELECT elder_id, GROUP_CONCAT(name) FROM ... GROUP BY elder_id;
-- 1 | 慢病,自理,高血压
-- 2 | 自理
```

### 2. SEPARATOR 自定义分隔符

```sql
GROUP_CONCAT(name SEPARATOR '|')
-- 1 | 慢病|自理|高血压
```

默认分隔符就是 `','`，所以 `SEPARATOR ','` 可以省略。
想用空串拼接（无分隔符）写 `SEPARATOR ''`。

### 3. ORDER BY 控制拼接顺序

```sql
GROUP_CONCAT(name ORDER BY name SEPARATOR ',')
-- 1 | 慢病,自理,高血压   （按名字排序，不按表里的出现顺序）
```

作用：让查询输出的顺序**稳定可控**。
例如按 `ORDER BY t.id` 排序，同一个老人每次查询输出的标签顺序都一致，前端展示不会乱跳。

### 4. DISTINCT 去重

```sql
GROUP_CONCAT(DISTINCT name ORDER BY name)
```

老人挂了两个同名标签时只拼一次。

## 三、三个必须知道的坑

### 1. NULL 处理

- 组内**全部**值为 NULL 时，结果为 NULL（不是空字符串）；
- 组内只要有一个非 NULL，NULL 值会被直接**跳过**不参与拼接。

```java
// Java 端处理全 NULL 的情况
vo.setTagNames(vo.getTagNamesStr() == null ? List.of() : List.of(vo.getTagNamesStr().split(",")));
```

### 2. 默认长度上限 1024 字节

由系统变量 `group_concat_max_len` 控制，拼接结果超长会**被静默截断**（不报错）。
标签数量多、或拼接长文本时必须调大：

```sql
-- 当前会话生效
SET SESSION group_concat_max_len = 10240;

-- 全局生效（需重启或配合 SET GLOBAL）
SET GLOBAL group_concat_max_len = 10240;

-- 或写入 my.ini / my.cnf 的 [mysqld] 段
-- group_concat_max_len = 10240
```

### 3. 必须配合分组使用

- 配合 `GROUP BY`：每组出一行，行内带该组全部值的拼接串；
- 没写 `GROUP BY`：整张表当成一组，把**所有行**的值拼成一个字符串输出。

## 四、项目实战示例

老人管理模块中，一条 SQL 同时查出老人信息和全部标签名（避免 Java 里循环查库）：

```sql
SELECT e.*, GROUP_CONCAT(t.name ORDER BY t.id SEPARATOR ',') AS tag_names
FROM elder AS e
LEFT JOIN elder_tag AS et ON e.id = et.elder_id
LEFT JOIN tag AS t ON et.tag_id = t.id AND t.deleted = 0
WHERE e.deleted = 0
GROUP BY e.id
```

要点说明：

| 写法 | 含义 |
| ---- | ---- |
| `GROUP BY e.id` | 每个老人一组，一人一行 |
| `ORDER BY t.id` | 标签按 id 排序，输出顺序稳定 |
| `SEPARATOR ','` | 逗号分隔，映射到 VO 的 `tagNamesStr` 字段 |
| `t.deleted = 0` 写在 ON 里 | 只过滤已删除标签，但**不影响老人行数**（写 WHERE 会把无有效标签的老人整个滤掉，退化为 INNER JOIN） |
| `e.deleted = 0` 写在 WHERE 里 | 直接过滤已删除的老人（主表条件放 WHERE 是正确的） |

等价的 Java 写法（项目中被替代的旧实现）：

```java
// 查出老人所有标签，按 id 排序，String.join(",", 名字列表)
```

**已知缺陷**：标签名本身含逗号时，Java 端 `split(",")` 无法区分"分隔符"还是"名字的一部分"。
数据可控（业务上标签名不允许逗号）就没问题；否则应改用 JSON 数组或子查询单独查标签。
