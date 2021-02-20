package com.data.common.mongodb;

import com.data.common.SpringContextUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.result.UpdateResult;
import org.bson.conversions.Bson;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * @author wj
 */
@SuppressWarnings("unchecked")
@DependsOn(value = {"springContextUtil"})
public class BaseMongoDaoImpl<T> implements BaseMongoDao<T> {
    /**
     * spring mongodb�?集成操作类�??
     */
    protected MongoTemplate mongoTemplate;

    protected GridFSBucket gridFSBucket;

    protected GridFsTemplate gridFsTemplate;

    protected String collectionName;

    private final Class<T> tClass;

    /**
     * 注入mongodbTemplate
     *
     * @param mongoTemplate
     */
    protected void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    {
        MongoDbCol annotation = getClass().getAnnotation(MongoDbCol.class);
        if (annotation != null) {
            String value = annotation.value();
            if (value.endsWith("GridFsTemplate")) {
                gridFsTemplate = (GridFsTemplate) SpringContextUtil.getBean(value);
            }
            if (value.endsWith("GridFSBucket")) {
                gridFSBucket = (GridFSBucket) SpringContextUtil.getBean(value);
            }
            if (!value.endsWith("GridFsTemplate") && !value.endsWith("GridFSBucket")) {
                mongoTemplate = (MongoTemplate) SpringContextUtil.getBean(value);
            }
        }

        tClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Document document = tClass.getAnnotation(Document.class);
        if (document != null) {
            collectionName = document.collection();
        }
    }

    public T save(T entity) {
        mongoTemplate.insert(entity);
        return entity;
    }

    public T findById(String id) {
        return mongoTemplate.findById(id, this.getEntityClass());
    }

    public T findById(String id, String collectionName) {
        return mongoTemplate.findById(id, this.getEntityClass(), collectionName);
    }

    @Override
    public T findByIdV1(String id) {
        return findByIdV1(id, collectionName);
    }

    @Override
    public T findByIdV1(String id, String collection) {
        Bson searchBson = eq("_id", id);
        FindIterable<org.bson.Document> documents = mongoTemplate.getCollection(collection).find(searchBson);
        if (documents.first() != null) {
            try {
                return CastUtil.toBean(documents.first(), tClass);
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public List<T> findAll() {
        return mongoTemplate.findAll(this.getEntityClass());
    }

    public List<T> findAll(String collectionName) {
        return mongoTemplate.findAll(this.getEntityClass(), collectionName);
    }

    public List<T> find(Query query) {
        return mongoTemplate.find(query, this.getEntityClass());
    }

    public T findOne(Query query) {
        return mongoTemplate.findOne(query, this.getEntityClass());
    }

    public Page<T> findPage(Page<T> page, Query query) {
        //如果没有条件 则所有全�?  
        query = query == null ? new Query(Criteria.where("_id").exists(true)) : query;
        long count = this.count(query);
        // 总数  
        page.setTotalCount((int) count);
        int currentPage = page.getCurrentPage();
        int pageSize = page.getPageSize();
        query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        List<T> rows = this.find(query);
        page.build(rows);
        return page;
    }

    public long count(Query query) {
        return mongoTemplate.count(query, this.getEntityClass());
    }

    public UpdateResult update(Query query, Update update) {
        if (update == null) {
            return null;
        }
        return mongoTemplate.updateMulti(query, update, this.getEntityClass());
    }

    public T updateOne(Query query, Update update) {
        if (update == null) {
            return null;
        }
        return mongoTemplate.findAndModify(query, update, this.getEntityClass());
    }

    public UpdateResult update(T entity) {
        Field[] fields = this.getEntityClass().getDeclaredFields();
        if (fields.length <= 0) {
            return null;
        }
        Field idField = null;
        // 查找ID的field  
        for (Field field : fields) {
            if ("id".equals(field.getName().toLowerCase())) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            return null;
        }
        idField.setAccessible(true);
        String id = null;
        try {
            id = (String) idField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (id == null || "".equals(id.trim()))
            return null;
        // 根据ID更新  
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = ReflectionUtils.getUpdateObj(entity);
        if (update == null) {
            return null;
        }
        return mongoTemplate.updateFirst(query, update, getEntityClass());
    }

    public void remove(Query query) {
        mongoTemplate.remove(query, this.getEntityClass());
    }

    /**
     * 获得泛型�?
     */
    private Class<T> getEntityClass() {
        return ReflectionUtils.getSuperClassGenricType(getClass());
    }

    public UpdateResult upsert(T entity) throws Exception {


        Field[] fields = this.getEntityClass().getDeclaredFields();
        if (fields.length <= 0) {
            return null;
        }
        Field idField = null;
        // 查找ID的field  
        for (Field field : fields) {
            if ("id".equals(field.getName().toLowerCase())) {
                idField = field;
                break;
            }
        }
        if (idField == null) {
            return null;
        }
        idField.setAccessible(true);
        String id = null;
        try {
            id = (String) idField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (id == null || "".equals(id.trim()))
            return null;
        // 根据ID更新  
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = ReflectionUtils.getUpdateObj(entity);
        if (update == null) {
            return null;
        }
        return mongoTemplate.upsert(query, update, this.getEntityClass());
    }

}