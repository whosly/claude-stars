#  📚 项目模块

## calcite jj 介绍
calcite parser代码生成逻辑

![code-generate-process](../dream/docs/calcite/calcite-parser-code-generate-process.png)

### [41.load-parser-jj](./41.load-parser-jj/)
获取 Calcite 源码中的 Parser.jj 文件

使用 Maven 插件 maven-dependency-plugin 直接从 Calcite 源码包中进行拷贝。

[README.md](./41.load-parser-jj/README.md)

### [42.parser-jj-generator](./42.parser-jj-generator/)
根据 parser-jj 模板文件生成 parser-jj 代码文件.

[README.md](./42.parser-jj-generator/README.md)


### [43.parser-generator](./43.parser-generator/)

将 parser-jj-generator 模块中生成的 Parser.jj 代码文件生成 Parser Java代码 (路径 target\generated-sources\fmpp\javacc)
copy至此项目中。

[README.md](./43.parser-generator/README.md)


### [44.auto-generator](./44.auto-generator/)
根据 parser-jj 模板文件生成 Parser Java代码(不需要对Parser.jj进行定制化修改)。

如果不需要对Parser.jj进行定制化修改，那么可以通过连续运行两个插件， 根据 parser-jj 模板生成 Parser Java代码。

[README.md](./44.auto-generator/README.md)

### [45.new-grammar](./45.new-grammar/)
使用 FreeMarker 模版插件根据 config.fmpp 生成 parser.jj 文件，最后使用 JavaCC 编译插件生成最终的解析器代码。

[README.md](./45.new-grammar/README.md)

新增自定义语法的例子工程

  * CREATE MATERIALIZED VIEW [ IF NOT EXISTS ] view_name AS query
  * JACKY JOB 'query'

### [46.calcite-schema](./46.calcite-schema/)
  * 多种数据源加载的示例
  * 自定义语法 submit job as query 的示例

多种数据源加载的示例。

[README.md](./46.calcite-schema/README.md)


### [47.calcite-rule](./47.calcite-rule/)
  * 基于 avacita 实现各种数据库jdbc查询的例子

### [48.avacita](./48.avacita/)
  * 基于 avacita 实现各种数据库jdbc查询的例子

[README.md](./48.avacita/README.md)
