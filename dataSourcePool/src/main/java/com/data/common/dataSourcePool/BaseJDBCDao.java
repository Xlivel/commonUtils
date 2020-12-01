package com.data.common.dataSourcePool;

import com.data.common.SpringContextUtil;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@DependsOn(value = {"springContextUtil"})
public abstract class BaseJDBCDao {

    protected JdbcTemplate jdbcTemplate;
    protected TransactionTemplate transactionTemplate;

    {
        JdbcDataSource annotation = getClass().getAnnotation(JdbcDataSource.class);
        if (annotation != null) {
            String value = annotation.value();
            jdbcTemplate = (JdbcTemplate) SpringContextUtil.getBean(value);
            transactionTemplate = (TransactionTemplate) SpringContextUtil.getBean(value + "Transaction");
        }
    }
}
