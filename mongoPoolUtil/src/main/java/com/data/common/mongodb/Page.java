package com.data.common.mongodb;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
    private static final long serialVersionUID = 5760097915453738435L;  
    public static final int DEFAULT_PAGE_SIZE = 10;  
    /** 
     * 姣忛〉鏄剧ず涓暟 
     */  
    private int pageSize;  
    /** 
     * 褰撳墠椤垫暟 
     */  
    private int currentPage;  
    /** 
     * 鎬婚〉鏁? 
     */  
    private int totalPage;  
    /** 
     * 鎬昏褰曟暟 
     */  
    private int totalCount;  
    /** 
     * 缁撴灉鍒楄〃 
     */  
    private List<T> rows;  
      
    public Page(){  
         this.currentPage = 1;  
         this.pageSize = DEFAULT_PAGE_SIZE;  
    }  
    public Page(int currentPage,int pageSize){  
        this.currentPage=currentPage<=0?1:currentPage;  
        this.pageSize=pageSize<=0?1:pageSize;  
    }  
    public int getPageSize() {  
        return pageSize;  
    }  
    public void setPageSize(int pageSize) {  
        this.pageSize = pageSize;  
    }  
    public int getCurrentPage() {  
        return currentPage;  
    }  
    public void setCurrentPage(int currentPage) {  
        this.currentPage = currentPage;  
    }  
    public int getTotalPage() {  
        return totalPage;  
    }  
    public void setTotalPage(int totalPage) {  
        this.totalPage = totalPage;  
    }  
    public int getTotalCount() {  
        return totalCount;  
    }  
    public void setTotalCount(int totalCount) {  
        this.totalCount = totalCount;  
    }  
  
    /** 
     * 璁剧疆缁撴灉 鍙婃?婚〉鏁? 
     * @param list 
     */  
     public void build(List<T> rows) {    
            this.setRows(rows);    
            int count =  this.getTotalCount();    
            int divisor = count / this.getPageSize();    
            int remainder = count % this.getPageSize();    
            this.setTotalPage(remainder == 0 ? divisor == 0 ? 1 : divisor : divisor + 1);    
        }  
    public List<T> getRows() {  
        return rows;  
    }  
    public void setRows(List<T> rows) {  
        this.rows = rows;  
    }    
}  