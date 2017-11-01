### netty-common包使用说明


#### base properties内容及配置介绍

    新建properties配置文件，文件名可随意。启动时加载此配置文件地址至配置环境
    如: 
    System.setProperty("base.config.path","/config/base.properties");
    注: base properties配置文件不会定时更新
    调用
    Config.getBase().getInt("test.int")
    数据库调用
    DbConfig.get().get("test.init")
    
    
##### 配置:
```properties
#数据库配置

#数据库是否定时加载至内存 默认false
deploy.server.config=true

#数据库定时加载时间间隔 单位:秒
db.loadconfig.intervaltime=300

#查询sql 必须按照此格式引用别名，property_name  property_value
db.query.sql=select name property_name,value property_value from table;

#数据库连接信息
db.connection=null
db.password=null
db.username=null

#备份文件信息
back.file.path=/config/
back.file.name=db-back.properties

```


#### load properties内容及配置介绍

    新建properties配置文件，文件名可随意。启动时加载此配置文件地址至配置环境
    如: 
    System.setProperty("load.config.path","/config/load.properties");
    注: load properties会定时内步配置文件到内存
    
    此properties配置主要用于配置业务调用配置荐
    调用
    Config.get().getInt("test.int")