# Calcite 模块

基于 Apache Calcite 构建的查询解析和优化项目，涉及 SQL 解析器生成、查询优化规则、自定义语法扩展等数据库相关功能。

## 模块介绍

### 41.load-parser-jj
获取 Calcite 源码中的 Parser.jj 文件，使用 Maven 插件 maven-dependency-plugin 直接从 Calcite 源码包中进行拷贝。

### 42.parser-jj-generator
根据 parser-jj 模板文件生成 parser-jj 代码文件。

### 43.parser-generator
将 parser-jj-generator 模块中生成的 Parser.jj 代码文件生成 Parser Java代码 (路径 target\generated-sources\fmpp\javacc)，copy至此项目中。

### 44.auto-generator
根据 parser-jj 模板文件生成 Parser Java代码(不需要对Parser.jj进行定制化修改)。如果不需要对Parser.jj进行定制化修改，那么可以通过连续运行两个插件，根据 parser-jj 模板生成 Parser Java代码。

### 45.new-grammar
使用 FreeMarker 模版插件根据 config.fmpp 生成 parser.jj 文件，最后使用 JavaCC 编译插件生成最终的解析器代码。新增自定义语法的例子工程：
  * CREATE MATERIALIZED VIEW [ IF NOT EXISTS ] view_name AS query
  * JACKY JOB 'query'

### 46.calcite-schema
多种数据源加载的示例，自定义语法 submit job as query 的示例。

### 47.calcite-rule
基于 avacita 实现各种数据库jdbc查询的例子。

### 48.avacita
基于 avacita 实现各种数据库jdbc查询的例子，使用 avatica 1.26.0, calcite 1.39.0 实现的jdbc驱动的连接和查询，并在server端实现查询sql的改写将指定字段进行改写，实现脱敏处理。

## 功能特性

### 解析器生成
- 从 Calcite 源码获取 Parser.jj 文件
- 使用 FreeMarker 模板生成自定义解析器
- 通过 JavaCC 编译插件生成最终的解析器代码

### 自定义语法扩展
- 支持 CREATE MATERIALIZED VIEW 语法
- 支持自定义 JACKY JOB 语法
- 可扩展的语法解析框架

### 数据源支持
- 多种数据源加载示例
- JDBC 连接支持
- 数据库查询优化

### SQL 脱敏处理
- 基于规则的 SQL 字段脱敏
- 支持多种脱敏策略（全掩码、左掩码、右掩码、部分掩码、哈希、正则等）
- SQL 改写实现脱敏处理
- 支持复杂 SQL 语句（子查询、CTE、UNION 等）

## 使用说明

进入相应的目录，使用 Maven 命令进行构建：

```bash
# 构建特定模块
cd 45.new-grammar
mvn clean initialize
mvn package
mvn generate-resources
```

## 相关资源

- [Apache Calcite 官方文档](https://calcite.apache.org/docs/)
- [Apache Avatica 官方文档](https://calcite.apache.org/avatica/docs/)