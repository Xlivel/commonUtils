默认配置
-基础配置
spring.data.sql.{beanId}.driver-class-name=com.mysql.jdbc.Driver
spring.data.sql.{beanId}.url=jdbc:mysql://10.0.147.145:4406/opensys?characterEncoding=UTF-8&useSSL=true
spring.data.sql.{beanId}.username=root
spring.data.sql.{beanId}.password=123456
spring.data.sql.{beanId}.type=com.alibaba.druid.pool.DruidDataSource
spring.data.sql.{beanId}.filters=stat,wall,config
spring.data.sql.{beanId}.connection-properties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000;

-详细配置
spring.data.sql.{beanId}.type=com.alibaba.druid.pool.DruidDataSource
spring.data.sql.{beanId}.initial-size=10
spring.data.sql.{beanId}.min-idle=5
spring.data.sql.{beanId}.max-active=50
spring.data.sql.{beanId}.max-wait=60000
spring.data.sql.{beanId}.time-between-eviction-runs-millis=60000
spring.data.sql.{beanId}.min-evictable-idle-time-millis=300000
spring.data.sql.{beanId}.validation-query=SELECT 1 FROM DUAL
spring.data.sql.{beanId}.test-while-idle=false
spring.data.sql.{beanId}.test-on-return=false
spring.data.sql.{beanId}.test-on-borrow=false
spring.data.sql.{beanId}.default-ReadOnly=false
常用的插件有： 
监控统计用的filter:stat日志用的filter:log4j防御sql注入的filter:wall
spring.data.sql.{beanId}.filters=stat,wall,log4j,config
spring.data.sql.{beanId}.connection-properties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000


打印sql
<!--log4jdbc -->
<logger name="jdbc.sqltiming" />
<logger name="jdbc.resultset" />

<!--打印SQL-->
<logger name="java.sql.Connection" level="DEBUG" />
<logger name="java.sql.Statement" level="DEBUG" />
<logger name="java.sql.PreparedStatement" level="DEBUG" />




基础连接池配置，主要是配置数据库的账户密码，还有连接池的参数
spring.datasource.url=jdbc:mysql://数据库的IP:3306/数据库名?characterEncoding=utf-8&useSSL=false&useUnicode=true
spring.datasource.username=账户
spring.datasource.password=密码
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
# 连接池指定 springboot2.02版本默认使用HikariCP 此处要替换成Druid
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
 
## 初始化连接池的连接数量 大小，最小，最大
spring.datasource.druid.initialSize=5
spring.datasource.druid.minIdle=5
spring.datasource.druid.maxActive=20
## 配置获取连接等待超时的时间
spring.datasource.druid.maxWait=60000
# 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
spring.datasource.druid.timeBetweenEvictionRunsMillis=60000
# 配置一个连接在池中最小生存的时间，单位是毫秒
spring.datasource.druid.minEvictableIdleTimeMillis=300000
spring.datasource.druid.validationQuery=SELECT 1 FROM DUAL
spring.datasource.druid.testWhileIdle=true
spring.datasource.druid.testOnBorrow=false
spring.datasource.druid.testOnReturn=false
# 是否缓存preparedStatement，也就是PSCache  官方建议MySQL下建议关闭   个人建议如果想用SQL防火墙 建议打开
spring.datasource.druid.poolPreparedStatements=true
spring.datasource.druid.maxPoolPreparedStatementPerConnectionSize=20
# 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
spring.datasource.druid.filters=stat,wall,log4j
# 通过connectProperties属性来打开mergeSql功能；慢SQL记录
spring.datasource.druid.connectionProperties=druid.stat.mergeSql=true;druid.stat.slowSqlMillis=5000
# ！！！请勿配置timeBetweenLogStatsMillis 会定时输出日志 并导致统计的sql清零
#spring.datasource.druid.timeBetweenLogStatsMillis=20000



基础监控配置（主要是配置监控的身份验证信息）
# WebStatFilter配置，说明请参考Druid Wiki，配置_配置WebStatFilter
#是否启用StatFilter默认值true
spring.datasource.druid.web-stat-filter.enabled=true
##spring.datasource.druid.web-stat-filter.url-pattern=
spring.datasource.druid.web-stat-filter.exclusions=*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*
# StatViewServlet配置，说明请参考Druid Wiki，配置_StatViewServlet配置
#是否启用StatViewServlet默认值true
spring.datasource.druid.stat-view-servlet.enabled=true
spring.datasource.druid.stat-view-servlet.url-pattern=/druid/*
spring.datasource.druid.stat-view-servlet.reset-enable=false
spring.datasource.druid.stat-view-servlet.login-username=admin
spring.datasource.druid.stat-view-servlet.login-password=123456

springboot启动类：
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

druid状态接口
@RestController
public class DruidStatController {
    @GetMapping("/druid/stat")
    public Object druidStat() {
        // DruidStatManagerFacade#getDataSourceStatDataList 该方法可以获取所有数据源的监控数据，除此之外 DruidStatManagerFacade 还提供了一些其他方法，你可以按需选择使用。
        List<Map<String, Object>> dataSourceStatDataList = DruidStatManagerFacade.getInstance().getDataSourceStatDataList();
        return dataSourceStatDataList;
    }
}

sql相关：
