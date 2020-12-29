package com.data.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * 多线程工具类
 *
 * @author wj
 */
public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);

    /**
     * @param list    处理集合
     * @param nThread 线程数
     * @param func    方法
     * @param <T>
     */
    public static <T> void startWithMultiThread(List<T> list, int nThread, Consumer<T> func) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (nThread <= 0) {
            return;
        }
        if (func == null) {
            return;
        }
        Semaphore semaphore = new Semaphore(nThread);
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        for (T obj : list) {
            try {
                semaphore.acquire();
                executorService.execute(() -> {
                    try {
                        func.accept(obj);
                        semaphore.release();
                    } catch (Exception e) {
                        logger.error("startWithMultiThread error: ", e);
                    }
                });
            } catch (InterruptedException e) {
                logger.error("startWithMultiThread InterruptedException error: ", e);
            }
        }
        executorService.shutdown();
    }

    /**
     * @param tasks    任务列表
     * @param nThreads 线程数
     */
    public static void executeTasks(List<Callable<String>> tasks, int nThreads) {
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<Future<String>> futures = null;
        try {
            futures = executorService.invokeAll(tasks);
            // 阻塞方法，当所有任务执行完毕，中断或超时时返回
        } catch (InterruptedException e1) {
            logger.error("ThreadUtil run error: ", e1);
        }
        assert futures != null;
        for (Future<String> future : futures) {
            try {
                logger.info(future.get());
            } catch (ExecutionException e) {
                logger.error("ThreadUtil 异常 error: ", e);
            } catch (CancellationException e) {
                logger.error("ThreadUtil 超时 error: ", e);
            } catch (InterruptedException e) {
                logger.error("ThreadUtil 中断 error: ", e);
            }
        }
        executorService.shutdown();
    }


    public static List page(int pageNo, int pageSize, List list) {
        List result = new ArrayList();
        if (list != null && list.size() > 0) {
            int allCount = list.size();
            int pageCount = (allCount + pageSize - 1) / pageSize;
            if (pageNo >= pageCount) {
                pageNo = pageCount;
            }
            int start = (pageNo - 1) * pageSize;
            int end = pageNo * pageSize;
            if (end >= allCount) {
                end = allCount;
            }
            for (int i = start; i < end; i++) {
                result.add(list.get(i));
            }
        }
        return result.size() > 0 ? result : null;
    }

    public <T> List<T> getDataList(List<T> list, int pageSize, int pageNo) {
        /**
         * 每页的起始索引
         */
        pageNo = (pageNo - 1) * pageSize;
        List<T> finalResult = new ArrayList<>();
        /**
         * 记录总数
         */
        int sum = list.size();
        if (pageNo + pageSize > sum) {
            finalResult.addAll(list.subList(pageNo, sum));
        } else {
            finalResult.addAll(list.subList(pageNo, pageNo + pageSize));
        }
        return finalResult;
    }

    public <T> List<List<T>> pageList(int pageSize, List<T> list) {
        List<List<T>> finalResult = new ArrayList<>();
        int count;
        int page = 1;
        do {
            count = 0;
            List<T> dataList = getDataList(list, pageSize, page);
            if (dataList != null && !dataList.isEmpty()) {
                finalResult.add(dataList);
                count = dataList.size();
            }
            page++;
        } while (count == pageSize);
        return finalResult;
    }

}
