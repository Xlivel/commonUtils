前置：
使用负载时需使用springsession
启动类上添加注解
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)