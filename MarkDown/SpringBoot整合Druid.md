## SpringBoot整合Druid

> `Druid`连接池是阿里巴巴开源的数据库连接池项目，后来贡献给`Apache`开源；
>
> `Druid`的作用是负责分配、管理和释放数据库连接，它允许应用程序重复使用一个现有的数据库连接，而不是再重新建立一个；
>
> `Druid`连接池内置强大的监控功能，其中的`StatFilter`功能，能采集非常完备的连接池执行信息，方便进行监控，而监控特性不影响性能。
>
> `Druid`连接池内置了一个监控页面，提供了非常完备的监控信息，可以快速诊断系统的瓶颈。

不过我觉得像我这种小项目并木使用的必要

实际上druid的配置与使用是很简单的，所以这里顺便写一些

主要就是两步

1. 添加相关依赖
2. 修改yaml配置文件对应信息
3. 运行测试

### 添加Druid依赖

```xml
<!--    druid    -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.5</version>
</dependency>
```

### 修改yaml配置

本来我用的是application.properties，这里顺便改用application.yaml算了

```yaml
server:
  port: 8888

spring:
  application:
    name: FunctionalLearning
  datasource:
    # 使用Druid连接池
    type: com.alibaba.druid.pool.DruidDataSource
    #8.0以上版本用com.mysql.cj.jdbc.Driver ，8.0以下版本用com.mysql.jdbc.Driver
    driver-class-name: com.mysql.cj.jdbc.Driver
    # 填写你数据库的url、登录名、密码和数据库名，如果出现报错，记得查看url是否有问题
    url: jdbc:mysql://localhost:3306/test01?useUnicode=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8&autoReconnect=true&useSSL=false&allowMultiQueries=true
    username: root
    password: root
    druid:
      # 连接池的配置信息
      # 初始化大小，最小，最大
      initial-size: 5
      min-idle: 5
      maxActive: 20
      # 配置获取连接等待超时的时间
      maxWait: 60000
      # 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
      timeBetweenEvictionRunsMillis: 60000
      # 配置一个连接在池中最小生存的时间，单位是毫秒
      minEvictableIdleTimeMillis: 300000
      validationQuery: SELECT 1
      testWhileIdle: true
      testOnBorrow: false
      testOnReturn: false
      # 打开PSCache，并且指定每个连接上PSCache的大小
      poolPreparedStatements: true
      maxPoolPreparedStatementPerConnectionSize: 20
      # 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
      filters: stat,wall,slf4j
      # 通过connectProperties属性来打开mergeSql功能；慢SQL记录
      connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
      # 配置DruidStatFilter
      web-stat-filter:
        enabled: true
        url-pattern: "/*"
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"
      # 配置DruidStatViewServlet
      stat-view-servlet:
        # IP白名单(没有配置或者为空，则允许所有访问)
        allow: 127.0.0.1
        # IP黑名单 (存在共同时，deny优先于allow)
        deny: 192.168.1.73
        # 禁用HTML页面上的“Reset All”功能
        reset-enable: false
        # druid登录的账户名
        login-username: admin
        # druid登录的密码
        login-password: admin

        # 不要写2个*号！要1个！
        url-pattern: "/druid/*"
        #这里配true才能访问监控页面！
        enabled: true
```

druid的相关配置基本上全部在这了，但是实际上哪里用得了这么多，看看就好了

### 运行测试

[http://localhost:8888/druid/index.html](http://localhost:8888/druid/index.html)
![啊](https://img-blog.csdnimg.cn/dfc5004670854e049f9813e3dfc4b283.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAQWxpdm9ydGg=,size_20,color_FFFFFF,t_70,g_se,x_16)

druid的配置基本上就在这了，但是实际需要交互的使用却主要是在上面这个网站完成，比如相关sql的监视，主要是确保后端到数据库这层的数据通信和数据安全，这个部分的使用还是另外找博客再去学习吧

---

我的项目demo系列都会同步到GitHub上，欢迎围观

https://github.com/Livorth/FunctionalLearning