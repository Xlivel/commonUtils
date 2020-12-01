package com.data.common.dataSourcePool;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;

@Configuration
public class DataSourceInit implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {
    private final List<DataSourceProperties> pools = new ArrayList<>();
    private final List<String> dataSourceNames = new ArrayList<>();
    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        int index = 0;
        for (DataSourceProperties properties : pools) {
            boolean primary = false;
            if (index == 0) {
                primary = true;
                index++;
            }
            try {
                DataSource dataSource = buildDataSource(properties);
                registryTransactionTemplate(registry, primary, properties, transactionManager(dataSource));
                registryDataSourceTemplate(registry, primary, properties, dataSource);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void registryTransactionTemplate(BeanDefinitionRegistry registry, boolean primary,
                                             DataSourceProperties properties, DataSourceTransactionManager transactionManager) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(TransactionTemplate.class);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        abd.getConstructorArgumentValues().addGenericArgumentValue(transactionManager);
        abd.setPrimary(primary);
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, properties.getDataSourceLastName() + "Transaction");
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private void registryDataSourceTemplate(BeanDefinitionRegistry registry, boolean primary, DataSourceProperties properties,
                                            DataSource springDataSource) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(JdbcTemplate.class);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        abd.getConstructorArgumentValues().addGenericArgumentValue(springDataSource);
        abd.setPrimary(primary);
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, properties.getDataSourceLastName());
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    private static DataSource buildDataSource(DataSourceProperties pro) throws Exception {
        Properties properties = new Properties();
        properties.put("url", pro.getUrl());
        properties.put("driverClassName", pro.getDriver());
        properties.put("username", pro.getUsername());
        properties.put("password", pro.getPassword());
        properties.put("initialSize", String.valueOf(pro.getInitialSize()));
        properties.put("minIdle", String.valueOf(pro.getMinIdle()));
        properties.put("maxActive", String.valueOf(pro.getMaxActive()));
        properties.put("maxWait", String.valueOf(pro.getMaxWait()));
        properties.put("timeBetweenEvictionRunsMillis", String.valueOf(pro.getTimeBetweenEvictionRunsMillis()));
        properties.put("minEvictableIdleTimeMillis", String.valueOf(pro.getMinEvictableIdleTimeMillis()));
        properties.put("validationQuery", pro.getValidationQuery());
        properties.put("testWhileIdle", String.valueOf(pro.getTestWhileIdle()));
        properties.put("testOnBorrow", String.valueOf(pro.getTestOnBorrow()));
        properties.put("testOnReturn", String.valueOf(pro.getTestOnReturn()));
        properties.put("filters", pro.getFilters());
        properties.put("defaultReadOnly", String.valueOf(pro.getDefaultReadOnly()));

        /*properties.put("defaultAutoCommit", false);
        properties.put("defaultTransactionIsolation", pro.getFilters());
        properties.put("defaultCatalog", "");
        properties.put("maxIdle", pro.getFilters());
        properties.put("numTestsPerEvictionRun", 1);
        properties.put("phyTimeoutMillis", pro.getFilters());
        properties.put("validationQueryTimeout", pro.getFilters());
        properties.put("accessToUnderlyingConnectionAllowed", pro.getFilters());
        properties.put("removeAbandoned", pro.getFilters());
        properties.put("removeAbandonedTimeout", pro.getFilters());
        properties.put("logAbandoned", pro.getFilters());
        properties.put("poolPreparedStatements", pro.getFilters());
        properties.put("exceptionSorter", pro.getFilters());
        properties.put("exception-sorter-class-name", pro.getFilters());
        properties.put("init", pro.getFilters());*/
        /*Properties properties1 = getProperties(pro.getConnectionProperties());
        if (properties1 != null) {
            properties.put("connectionProperties", properties1);
        }*/
        properties.put("connectionProperties", pro.getConnectionProperties());
        return DruidDataSourceFactory.createDataSource(properties);
    }

    private Properties getProperties(String connectionProperties) {
        Properties properties = new Properties();
        if (StringUtils.isNotBlank(connectionProperties)) {
            String[] pro = connectionProperties.split(";");
            Arrays.stream(pro).forEach(obj -> {
                if (StringUtils.isNotBlank(obj)) {
                    String[] kv = obj.split("=");
                    properties.put(kv[0], kv[1]);
                }
            });
            return properties;
        }
        return null;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public void setEnvironment(Environment environment) {
        // 初始化配置信息到对象的映射
        Map<String, Object> map = Binder.get(environment).bind("spring.data.sql", Map.class).get();

        map.keySet().forEach(obj -> {
            String dataSourceName = obj.split("\\.")[0];
            if (!dataSourceNames.contains(dataSourceName)) {
                dataSourceNames.add(dataSourceName);
            }
        });

        for (String name : dataSourceNames) {
            DataSourceProperties pro = new DataSourceProperties();
            buildProperties((Map) map.get(name), name, pro);
            pools.add(pro);
        }
    }

    private void buildProperties(Map<String, Object> map, String name, DataSourceProperties pro) {
        pro.setUsername(formatStringValue(map, DataSourceTag.USERNAME, "root"));
        pro.setPassword(formatStringValue(map, DataSourceTag.PASSWORD, "root"));
        pro.setDataSourceType(formatStringValue(map, DataSourceTag.TYPE, "com.alibaba.druid.pool.DruidDataSource"));
        String driver = formatStringValue(map, DataSourceTag.DRIVER_CLASS_NAME, "com.mysql.jdbc.Driver");
        pro.setDriver(driver);
        pro.setUrl(formatStringValue(map, DataSourceTag.URL, "jdbc:mysql://localhost:3306/test"));
        pro.setInitialSize(formatIntValue(map, DataSourceTag.INITIAL_SIZE, 10));
        pro.setMinIdle(formatIntValue(map, DataSourceTag.MIN_IDLE, 10));
        pro.setMaxActive(formatIntValue(map, DataSourceTag.MAX_ACTIVE, 50));
        pro.setMaxWait(formatIntValue(map, DataSourceTag.MAX_WAIT, 60000));
        pro.setTimeBetweenEvictionRunsMillis(formatIntValue(map, DataSourceTag.TIME_BETWEEN_EVICTION_RUNS_MILLIS, 60000));
        pro.setMinEvictableIdleTimeMillis(formatIntValue(map, DataSourceTag.MIN_EVICTABLE_IDLE_TIME_MILLIS, 300000));
        pro.setValidationQuery(formatStringValue(map, DataSourceTag.VALIDATION_QUERY, ValidationQuery.getValidationQuery(driver)));
        pro.setTestOnBorrow(formatBoolValue(map, DataSourceTag.TEST_ON_BORROW, false));
        pro.setTestOnReturn(formatBoolValue(map, DataSourceTag.TEST_ON_RETURN, false));
        pro.setTestWhileIdle(formatBoolValue(map, DataSourceTag.test_while_idle, true));
        pro.setFilters(formatStringValue(map, DataSourceTag.FILTERS, "stat,wall"));
        pro.setConnectionProperties(formatStringValue(map, DataSourceTag.CONNECTION_PROPERTIES, null));
        pro.setDefaultReadOnly(formatBoolValue(map, DataSourceTag.DEFAULT_READONLY, false));
        pro.setDataSourceLastName(name);
    }

    private String formatStringValue(Map<String, Object> map, String key, String defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key).toString();
        }
        return defaultValue;
    }

    private int formatIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map.containsKey(key)) {
            return Integer.parseInt(map.get(key).toString());
        }
        return defaultValue;
    }

    private boolean formatBoolValue(Map<String, Object> map, String key, boolean defaultValue) {
        if (map.containsKey(key)) {
            return Boolean.parseBoolean(map.get(key).toString());
        }
        return defaultValue;
    }

    private char[] formatChatValue(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key).toString().toCharArray();
        }
        return new char[0];
    }
}
