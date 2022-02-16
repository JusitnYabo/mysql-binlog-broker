# mysql-binlog-broker

#### 介绍
自定义的mysql-binlog日志读取分发框架

#### 软件架构
软件架构说明


#### 安装教程

1.  单数据源配置
    ```
    spring:
        datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306?characterEncoding=utf8&useSSL=false&serverTimezone=CTT
        username: root
        password: root
    redis:
        host: localhost
        port: 6379
    ```
2.  多数据源配置
    1. 配置文件
    ```
    spring:
    # 数据源配置
      datasource:
        ds1: #数据源1
          driver-class-name: com.mysql.jdbc.Driver # mysql的驱动你可以配置别的关系型数据库
          url: jdbc:mysql://ip:3306/db1 #数据源地址
          username: root # 用户名
          password: root # 密码
        ds2: # 数据源2
          driver-class-name: com.mysql.jdbc.Driver # mysql的驱动你可以配置别的关系型数据库
          url: jdbc:mysql://ip:3307/db2#数据源地址
          username: root # 用户名
          password: root # 密码
    ```
    2. config配置
    ```
      @Configuration
      public class DataSourceConfig {
    
      //主数据源配置 ds1数据源
      @Primary
      @Bean(name = "ds1DataSourceProperties")
      @ConfigurationProperties(prefix = "spring.datasource.ds1")
      public DataSourceProperties ds1DataSourceProperties() {
      return new DataSourceProperties();
      }
    
      //主数据源 ds1数据源
      @Primary
      @Bean(name = "ds1DataSource")
      public DataSource ds1DataSource(@Qualifier("ds1DataSourceProperties") DataSourceProperties dataSourceProperties) {
      return dataSourceProperties.initializeDataSourceBuilder().build();
      }
    
      //第二个ds2数据源配置
      @Bean(name = "ds2DataSourceProperties")
      @ConfigurationProperties(prefix = "spring.datasource.ds2")
      public DataSourceProperties ds2DataSourceProperties() {
      return new DataSourceProperties();
      }
    
      //第二个ds2数据源
      @Bean("ds2DataSource")
      public DataSource ds2DataSource(@Qualifier("ds2DataSourceProperties") DataSourceProperties dataSourceProperties) {
      return dataSourceProperties.initializeDataSourceBuilder().build();
      }
    
    }

    ```
        


3.  MetaEventListener 自定义实现事件监听类，启动项目实现数据分发

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
# mysql-binlog-broker
