package com.data.common.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class CastUtil {

    public static <T> T toBean(Document document, Class<T> clazz)
            throws InvocationTargetException, IllegalAccessException, InstantiationException {
        T obj = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();// 获取所有属性
        Method[] methods = clazz.getMethods();// 获取所有的方法
        /*
         * 查找所有的属性，并通过属性名和数据库字段名通过相等映射
         */
        for (Field field : fields) {
            String fieldName = field.getName();
            Object bson = null;
            if ("id".equals(fieldName)) {
                bson = document.get("_id");
            } else {
                bson = document.get(fieldName);
            }
            if (null == bson) {
                continue;
            } else if (bson instanceof Document) {// 如果字段是文档了递归调用
                bson = toBean((Document) bson, field.getType());
            } else if (bson instanceof MongoCollection) {// 如果字段是文档集了调用colTOList方法
                bson = colToList(bson, field);
            }
            List<Method> setMethods = getMethodsByStart(methods, "set");
            for (Method setMethod : setMethods) {// 为对象赋值
                String methodName = setMethod.getName();
                if (methodName.startsWith("set") && equalFieldAndSet(fieldName, methodName)) {
                    if (setMethod.getParameters()[0].getType() == Set.class) {
                        setMethod.invoke(obj, new HashSet(Collections.singletonList(bson)));
                    } else {
                        setMethod.invoke(obj, bson);
                    }
                    break;
                }
            }
        }
        return obj;
    }

    private static List<Method> getMethodsByStart(Method[] methods, String set) {
        List<Method> methodsByStart = new ArrayList<>();
        Arrays.stream(methods).forEach(obj -> {
            if (obj.getName().startsWith(set)) {
                methodsByStart.add(obj);
            }
        });
        return methodsByStart;
    }

    private static List<Object> colToList(Object bson, Field field)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ParameterizedType pt = (ParameterizedType) field.getGenericType();// 获取列表的类型
        List<Object> objs = new ArrayList<>();
        @SuppressWarnings("unchecked")
        MongoCollection<Document> cols = (MongoCollection<Document>) bson;
        for (Document child : cols.find()) {
            @SuppressWarnings("rawtypes")
            Class clz = (Class) pt.getActualTypeArguments()[0];// 获取元素类型
            @SuppressWarnings("unchecked")
            Object obj = toBean(child, clz);
            objs.add(obj);
        }
        return objs;
    }

    /*
     * 比较setter方法和属性相等
     */
    private static boolean equalFieldAndSet(String field, String name) {
        return name.toLowerCase().matches("set" + field.toLowerCase());
    }

    /*
     * 比较getter方法和属性相等
     */
    private static boolean equalFieldAndGet(String field, String name) {
        return name.toLowerCase().matches("get" + field.toLowerCase());
    }
}
