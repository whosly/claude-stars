本项目使用了 `org.codehaus.mojo#exec-maven-plugin` 插件，并且在启动入口处进行了参数校验。因此与本地 Runner/Debug 启动有一些差别。

## 启动方式
### Maven 面板运行

在 maven 面板中，找到client和server子项目，分别执行 package 即可。

### Maven 面板调试
打开 Maven 工具窗口 ，右侧边栏点击 Maven 图标 → 展开项目 → `Plugins` → `exec` 插件

定位插件目标：
* 调试 Java 类：双击 exec:java 目标
* 调试系统命令：双击 exec:exec 目标

右键菜单调试： 
* 在目标上右键， 选择 Debug 'exec:java'


## exec-maven-plugin
exec-maven-plugin 是一个用于在 Maven 构建过程中执行外部程序或 Java 代码的插件。它提供两种主要目标：exec:exec（执行系统命令）和 exec:java（执行 Java 类）

* 一、执行 Java 类 (exec:java)

```
1. 命令行直接运行： mvn exec:java -Dexec.mainClass="com.example.Main" -Dexec.args="arg1 arg2"
2. 在 pom.xml 中配置

<configuration>
    <mainClass>com.example.Main</mainClass>
    <arguments>
        <argument>arg1</argument>
        <argument>arg2</argument>
    </arguments>
    <systemProperties>
        <property>
            <key>env</key>
            <value>prod</value>
        </property>
    </systemProperties>
</configuration>

3. 绑定到 Maven 生命周期
<executions>
    <execution>
        <id>run-main</id>
        <phase>package</phase>
        <goals>
            <goal>java</goal>
        </goals>
    </execution>
</executions>
```

* 二、执行系统命令 (exec:exec)
此处略

