package com.data.common.logUtil;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Aspect 表示是一个切面
 * @Pointcut 表示切入点，可以是表达式，也可以是注解，我这里用的是表达式
 * @AfterReturning 和@After不同的是，表示在得到返回内容后切入，
 */
@Aspect
@Component
public class LogAop {

    ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    //这个表达式的意思是，HelloLogCotroller这个controller下所有的方法都会切入
    @Pointcut("execution(* com..controller.*.*(..)) || execution(* com..resources.*.*(..)) || execution(* com..rest.*.*(..))")
    public void setLogger() {
    }

    @Before("setLogger()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        startTime.set(System.currentTimeMillis());
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
//        logger.info("-请求url:{}，-请求类型:{}，-IP:{}", request.getRequestURL().toString(), request.getMethod(), request.getRemoteAddr());
//        logger.info("接口包路径 : {}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());

        if (StringUtils.equalsIgnoreCase("get", request.getMethod())) {
            logger.info("get:-请求url:{}，-请求类型:{}，-IP:{} ,-请求参数 : {}", request.getRequestURL().toString(), request.getMethod(), request.getRemoteAddr(), request.getQueryString());
        } else if (StringUtils.equalsIgnoreCase("post", request.getMethod())) {
            for (Object object : joinPoint.getArgs()) {
                if (object instanceof MultipartFile
                        || object instanceof HttpServletRequest
                        || object instanceof HttpServletResponse) {
                    continue;
                }
                logger.info("post:-请求url:{}，-请求类型:{}，-IP:{} ，-请求参数:{}", request.getRequestURL().toString(), request.getMethod(), request.getRemoteAddr(), objectMapper.writeValueAsString(object));
            }
        }
    }

    @AfterReturning(returning = "ret", pointcut = "setLogger()")
    public void doAfterReturning(ResponseEntity ret) throws Throwable {
        // 处理完请求，返回内容
        logger.info("-RESPONSE status: {}，SPEND TIME : {}， -RESPONSE body: {}", ret.getStatusCodeValue(),
                (System.currentTimeMillis() - startTime.get()) + "ms", JSONObject.toJSONString(ret.getBody()));
    }
}