package com.data.common.mongodb;

import com.data.common.config.MongoPoolAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({MongoPoolAutoConfiguration.class})
public @interface EnableMongoPool {

}
