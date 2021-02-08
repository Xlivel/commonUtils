package com.data.common.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.ServerSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopeMetadataResolver;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * @author wj
 */
@Component
public class MongoPoolInit implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private final Map<String, MongoClient> pools = new HashMap<>();

    private final ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        int index = 0;
        for (String key : pools.keySet()) {
            SimpleMongoClientDbFactory mongoDbFactory =
                    new SimpleMongoClientDbFactory(pools.get(key), key);
            MappingMongoConverter converter = buildConverter(mongoDbFactory, false);
            boolean primary = false;
            if (index == 0) {
                primary = true;
                index++;
            }
            if (key != null && "".equals(key)) {
                registry(registry, primary, mongoDbFactory, converter, MongoTemplate.class, key);
            } else {
                registry(registry, primary, mongoDbFactory, converter, GridFSBucket.class, key);
                registry(registry, primary, mongoDbFactory, converter, GridFsTemplate.class, key);
            }
        }
    }

    private void registry(BeanDefinitionRegistry registry, boolean primary, SimpleMongoClientDbFactory mongoDbFactory,
                          MappingMongoConverter converter, Class<?> beanClass, String beanName) {
        AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
        ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
        abd.setScope(scopeMetadata.getScopeName());
        abd.getConstructorArgumentValues().addGenericArgumentValue(mongoDbFactory);
        abd.getConstructorArgumentValues().addGenericArgumentValue(converter);
//        abd.getConstructorArgumentValues().addGenericArgumentValue(beanName);
        abd.setPrimary(primary);
        AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
        BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
        BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
    }

    private MappingMongoConverter buildConverter(SimpleMongoClientDbFactory mongoDbFactory, boolean showClass) {
        MappingMongoConverter converter = new MappingMongoConverter(
                new DefaultDbRefResolver(mongoDbFactory),
                new MongoMappingContext());
        if (!showClass) {
            converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        }
        return converter;
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 初始化配置信息到对象的映射
        Map map = Binder.get(environment).bind("spring.data.mongodb", Map.class).get();
        Set<String> mongoTemplateNames = new TreeSet<>();
        Set keys = map.keySet();
        for (Object key : keys) {
            String mongoTemplateName = String.valueOf(key).split("\\.")[0];
            mongoTemplateNames.add(mongoTemplateName);
        }
        if (keys.size() > 1) {
            for (String name : mongoTemplateNames) {
                MongoPoolProperties pro = new MongoPoolProperties();
                buildProperties((Map) map.get(name), name, pro);
                MongoClient mongoClient = genMongoClient(formatStringValue(map, PoolAttributeTag.CONFIG, ""), pro);
                pools.put(name, mongoClient);
            }
        } else {
            for (String name : mongoTemplateNames) {
                String connectionStr = formatStringValue((Map) map.get(name), PoolAttributeTag.CONFIG, "");
                ConnectionString connectionString = buildConnectionString(connectionStr);
                MongoClient mongoClient = genMongoClient(connectionString);
                pools.put(name, mongoClient);
            }

        }
    }

    private ConnectionString buildConnectionString(String connectionStr) {
        return new ConnectionString(connectionStr);
    }

    private MongoClient genMongoClient(ConnectionString connectionString) {
        return MongoClients.create(connectionString);
    }

    private MongoClient genMongoClient(String connectionString, MongoPoolProperties properties) {

        MongoClientSettings.Builder builder = MongoClientSettings.builder()
                .applyConnectionString(buildConnectionString(connectionString))
                .readPreference(ReadPreference.secondaryPreferred())
                /*.autoEncryptionSettings()
                .compressorList()
                .applicationName()
                .commandListenerList()
                .addCommandListener()
                .streamFactoryFactory()*/
                .codecRegistry(genCodecRegistry(properties))
                .credential(genMongoCredential(properties))
                .readConcern(genReadConcern(properties))
                .retryReads(properties.isRetryReads())
                .retryWrites(properties.isRetryWrites())
                .writeConcern(genWriteConcern(properties))
                .applyToSslSettings(genSslSettings(properties))
                .applyToServerSettings(genServerSettings(properties))
                .applyToConnectionPoolSettings(genConnectionPoolSettings(properties))
//                .applyToClusterSettings()
                .applyToSocketSettings(genSocketSettings(properties));
        MongoClientSettings build = builder.build();
        return MongoClients.create(build);
    }

    private Block<ConnectionPoolSettings.Builder> genConnectionPoolSettings(MongoPoolProperties properties) {
        return o -> {
            o.maxSize(properties.getReadTimeout());
            o.minSize(properties.getReadTimeout());
            o.maxWaitQueueSize(properties.getReadTimeout());
            o.maxWaitTime(properties.getReadTimeout(), TimeUnit.SECONDS);
            o.maxConnectionLifeTime(properties.getReadTimeout(), TimeUnit.SECONDS);
            o.maxConnectionIdleTime(properties.getReadTimeout(), TimeUnit.SECONDS);
            o.maintenanceInitialDelay(properties.getReadTimeout(), TimeUnit.SECONDS);
            o.maintenanceFrequency(properties.getReadTimeout(), TimeUnit.SECONDS);
        };
    }

    private Block<SocketSettings.Builder> genSocketSettings(MongoPoolProperties properties) {
        return o -> {
            o.readTimeout(properties.getReadTimeout(), TimeUnit.SECONDS);
            o.connectTimeout(properties.getConnectTimeout(), TimeUnit.SECONDS);
            o.receiveBufferSize(properties.getReceiveBufferSize());
            o.sendBufferSize(properties.getSendBufferSize());
        };
    }

    private CodecRegistry genCodecRegistry(MongoPoolProperties properties) {
        return new CodecRegistry() {
            @Override
            public <T> Codec<T> get(Class<T> aClass) {
                return null;
            }
        };
    }

    private ReadConcern genReadConcern(MongoPoolProperties properties) {
        ReadConcernLevel type = ReadConcernLevel.valueOf(properties.getReadConcernLevel());
        switch (type) {
            case LOCAL:
                return new ReadConcern(ReadConcernLevel.LOCAL);
            case MAJORITY:
                return new ReadConcern(ReadConcernLevel.MAJORITY);
            case LINEARIZABLE:
                return new ReadConcern(ReadConcernLevel.LINEARIZABLE);
            case SNAPSHOT:
                return new ReadConcern(ReadConcernLevel.SNAPSHOT);
            case AVAILABLE:
                return new ReadConcern(ReadConcernLevel.AVAILABLE);
            default:
                return null;
        }
    }

    private WriteConcern genWriteConcern(MongoPoolProperties properties) {
        return WriteConcern.valueOf(properties.getWriteConcern());
    }

    private Block<ServerSettings.Builder> genServerSettings(MongoPoolProperties properties) {
        return o -> {
            ServerSettings.builder()
                    .heartbeatFrequency(properties.getHeartbeatFrequency(), TimeUnit.SECONDS)
                    .minHeartbeatFrequency(properties.getMinHeartbeatFrequency(), TimeUnit.SECONDS);
        };
    }

    private Block<SslSettings.Builder> genSslSettings(MongoPoolProperties properties) {
        return o -> {
            o = SslSettings.builder()
//                .context(new SSLContext(SSLContextSpi))
                    .enabled(properties.isSslEnabled())
                    .invalidHostNameAllowed(properties.isSslInvalidHostNameAllowed());
        };
    }

    private MongoCredential genMongoCredential(MongoPoolProperties properties) {
        AuthenticationMechanism type = AuthenticationMechanism.valueOf(properties.getAuthenticationMechanism());
        switch (type) {
            case GSSAPI:
                return MongoCredential.createGSSAPICredential(properties.getUsername());
            case PLAIN:
                return MongoCredential.createPlainCredential(properties.getUsername(), properties.getAuthenticationDatabase(),
                        properties.getPassword());
            case MONGODB_X509:
                return MongoCredential.createMongoX509Credential(properties.getUsername());
            case MONGODB_CR:
                return MongoCredential.createMongoCRCredential(properties.getUsername(), properties.getAuthenticationDatabase(),
                        properties.getPassword());
            case SCRAM_SHA_256:
                return MongoCredential.createScramSha256Credential(properties.getUsername(), properties.getAuthenticationDatabase(),
                        properties.getPassword());
            default:
                return MongoCredential.createCredential(properties.getUsername(), properties.getAuthenticationDatabase(),
                        properties.getPassword());
        }
    }

    private void buildProperties(Map<String, Object> map, String name, MongoPoolProperties pro) {
        pro.setUri(formatStringValue(map, PoolAttributeTag.CONFIG, "mongodb://admin:123456@127.0.0.1:27017/admin"));
//        pro.setUri(formatStringValue(map, PoolAttributeTag.URI, ""));
        pro.setShowClass(formatBoolValue(map, PoolAttributeTag.SHOW_CLASS, false));
        pro.setMongoTemplateName(name);
        pro.setGridFsTemplateName(formatStringValue(map, PoolAttributeTag.GRID_FS_TEMPLATE_NAME, name + "GridFsTemplate"));
        pro.setGridFSBucketName(formatStringValue(map, PoolAttributeTag.GRID_FS_BUCKET_NAME, name + "GridFSBucket"));
/*        pro.setHost(formatStringValue(map, PoolAttributeTag.HOST, "localhost"));
        pro.setPort(formatIntValue(map, PoolAttributeTag.PORT, 27017));
        pro.setDatabase(formatStringValue(map, PoolAttributeTag.DATABASE, "test"));
        pro.setAuthenticationDatabase(formatStringValue(map, PoolAttributeTag.AUTH_DATABASE, "admin"));
        pro.setGridFsDatabase(formatStringValue(map, PoolAttributeTag.GRIDFS_DATABASE, "test"));
        pro.setUsername(formatStringValue(map, PoolAttributeTag.USERNAME, null));
        pro.setPassword(formatChatValue(map));*/
        pro.setApplicationName(formatStringValue(map, PoolAttributeTag.APPLICATIONNAME, null));

        pro.setMinConnectionsPerHost(formatIntValue(map, PoolAttributeTag.MIN_CONN_PERHOST, 0));
        pro.setMaxConnectionsPerHost(formatIntValue(map, PoolAttributeTag.MAX_CONN_PERHOST, 100));
        pro.setThreadsAllowedToBlockForConnectionMultiplier(formatIntValue(map, PoolAttributeTag.THREADS_ALLOWED_TO_BLOCK_FOR_CONN_MULTIPLIER, 5));
        pro.setServerSelectionTimeout(formatIntValue(map, PoolAttributeTag.SERVER_SELECTION_TIMEOUT, 1000 * 30));
        pro.setMaxWaitTime(formatIntValue(map, PoolAttributeTag.MAX_WAIT_TIME, 1000 * 60 * 2));
        pro.setMaxConnectionIdleTime(formatIntValue(map, PoolAttributeTag.MAX_CONN_IDLE_TIME, 0));
        pro.setMaxConnectionLifeTime(formatIntValue(map, PoolAttributeTag.MAX_CONN_LIFE_TIME, 0));
        pro.setConnectTimeout(formatIntValue(map, PoolAttributeTag.CONN_TIMEOUT, 1000 * 10));
        pro.setSocketTimeout(formatIntValue(map, PoolAttributeTag.SOCKET_TIMEOUT, 0));

        pro.setSocketKeepAlive(formatBoolValue(map, PoolAttributeTag.SOCKET_KEEP_ALIVE, false));

        pro.setSslEnabled(formatBoolValue(map, PoolAttributeTag.SSL_ENABLED, false));
        pro.setSslInvalidHostNameAllowed(formatBoolValue(map, PoolAttributeTag.SSL_INVALID_HOSTNAME_ALLOWED, false));

        pro.setAlwaysUseMBeans(formatBoolValue(map, PoolAttributeTag.ALWAYS_USE_MBEANS, false));

        pro.setHeartbeatFrequency(formatIntValue(map, PoolAttributeTag.HEARTBEAT_FREQUENCY, 10000));
        pro.setMinHeartbeatFrequency(formatIntValue(map, PoolAttributeTag.MIN_HEARTBEAT_FREQUENCY, 500));
        pro.setHeartbeatConnectTimeout(formatIntValue(map, PoolAttributeTag.HEARTBEAT_CONN_TIMEOUT, 20000));
        pro.setHeartbeatSocketTimeout(formatIntValue(map, PoolAttributeTag.HEARTBEAT_SOCKET_TIMEOUT, 20000));
        pro.setLocalThreshold(formatIntValue(map, PoolAttributeTag.LOCAL_THRESHOLD, 15));
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

    private char[] formatChatValue(Map<String, Object> map) {
        if (map.containsKey(PoolAttributeTag.PASSWORD)) {
            return map.get(PoolAttributeTag.PASSWORD).toString().toCharArray();
        }
        return new char[0];
    }
}
