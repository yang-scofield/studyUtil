package com.deyuan.study.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * @Author yangdeyuan
 * @Date 2018/7/23  22:37
 * @description:
 */
public class ThreadPoolManager {

    private static ThreadPoolManager threadPoolManager = new ThreadPoolManager();

    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 10;

    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 10;

    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 0;

    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 10;

    // 消息缓冲队列
    Queue<String> msgQueue = new LinkedList<String>();


    // 定长线程池
    final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);


    public static ThreadPoolManager newInstance() {
        return threadPoolManager;
    }

    private ThreadPoolManager() {
    }


    public void accessUserInfo(String UserInfo,boolean checkScore) {
        Runnable task = new ThreadAccess(UserInfo,checkScore);
        fixedThreadPool.execute(task);
    }

    /**
     * 线程池等待完成
     * @param start
     */
    public void await(long start) {
        try {
            // awaitTermination返回false即超时会继续循环，返回true即线程池中的线程执行完成主线程跳出循环往下执行，每隔10秒循环一次
            while (!fixedThreadPool.awaitTermination(10, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis() / 1000;
        System.out.println("学习时长：" + (end - start) + "s");
    }

    public void shutdown(){
        fixedThreadPool.shutdown();
    }



    public static void main(String[] args) {
        ThreadPoolManager tpm = ThreadPoolManager.newInstance();
        List<String> list = new ArrayList<String>();
        for (String message : list) {
            tpm.accessUserInfo(message,true);
        }

    }
}
