**1 建任务（将业务逻辑拆分成多个任务执行）**
List<Callable<String>> tasks = new ArrayList<>();
for (int i = 0; i < pageCount; i++) {
    List page = ThreadUtil.page(i, size, list);
    Callable<String> task = () -> {
        //业务逻辑方法
        logger.info("");
        return "";
    };
    tasks.add(task);
}

**2 执行任务集合（结果以参数形式传入方法）**
tasks //任务（List<Callable<String>>）
nThreads //线程数（int）
ThreadUtil.executeTasks(tasks,nThreads);

**3 其他方法**
//list分页方法
ThreadUtil.page(pageNo,pageSize,list);