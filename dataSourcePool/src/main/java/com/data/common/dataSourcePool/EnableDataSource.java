package com.data.common.dataSourcePool;

import com.data.common.config.DataSourceConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DataSourceConfig.class})
public @interface EnableDataSource {
}
