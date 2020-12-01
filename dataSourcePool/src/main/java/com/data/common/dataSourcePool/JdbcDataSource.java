package com.data.common.dataSourcePool;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JdbcDataSource {
    String value();
}