package com.data.common.mongodb;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;


public interface BaseMongoDao<T> {
    /** 
     * 保存
     */  
    T save(T entity);

    /** 
     * 根据id查询对象。
     */  
    T findById(String id);  
  
    /** 
     * 閫氳繃ID鑾峰彇璁板綍,骞朵笖鎸囧畾浜嗛泦鍚堝悕(琛ㄧ殑鎰忔??) 
     */  
    T findById(String id, String collectionName);

    /**
     * 根据id查询，修复版
     * @param id
     * @return
     */
    T findByIdV1(String id);

    /**
     * 根据id查询，修复版
     * @param id
     * @return
     */
    T findByIdV1(String id,String collection);
  
    /** 
     * 鑾峰緱鎵?鏈夎绫诲瀷璁板綍 
     */  
    List<T> findAll();  
  
    /** 
     * 鑾峰緱鎵?鏈夎绫诲瀷璁板綍,骞朵笖鎸囧畾浜嗛泦鍚堝悕(琛ㄧ殑鎰忔??) 
     */  
    List<T> findAll(String collectionName);  
  
    /** 
     * 鏍规嵁鏉′欢鏌ヨ 
     */  
    List<T> find(Query query);
  
    /** 
     * 鏍规嵁鏉′欢鏌ヨ涓?涓? 
     */  
    T findOne(Query query);
  
    /** 
     * 鍒嗛〉鏌ヨ 
     */  
    Page<T> findPage(Page<T> page, Query query);
  
    /** 
     * 鏍规嵁鏉′欢 鑾峰緱鎬绘暟 
     */  
    long count(Query query);
  
    /** 
     * 鏍规嵁鏉′欢 鏇存柊 
     */
    UpdateResult update(Query query, Update update);
  
    /** 
     * 鏇存柊绗﹀悎鏉′欢骞秙ort涔嬪悗鐨勭涓?涓枃妗? 骞惰繑鍥炴洿鏂板悗鐨勬枃妗? 
     */  
    T updateOne(Query query, Update update);
  
    /** 
     * 鏍规嵁浼犲叆瀹炰綋ID鏇存柊 
     */
    UpdateResult update(T entity);
  
    /** 
     * 鏍规嵁鏉′欢 鍒犻櫎 
     *  
     * @param query 
     */  
    void remove(Query query);
}  
