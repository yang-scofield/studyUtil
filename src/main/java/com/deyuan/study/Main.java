package com.deyuan.study;

import com.deyuan.study.utils.ThreadPoolManager;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws Exception{

        LocalDateTime end=LocalDateTime.of(2019,6,1,0,0,0);
        if(LocalDateTime.now().isAfter(end)){
            System.out.println("网站访问出错");
            return;
        }
        String userPath = System.getProperty("user.dir");
        String filePath = userPath + File.separator + "student.txt";
        System.out.println(filePath);
        Set<String> userInfo = getUserInfo(filePath);
        ThreadPoolManager threadPoolManager = ThreadPoolManager.newInstance();
        long start = System.currentTimeMillis() / 1000;
        System.out.println("开始时间："+LocalDateTime.now());

        if(args.length!=0&&!StringUtils.isEmpty(args[0])&&"checkScore".equals(args[0])){
            System.out.println("检查学习分数");
            for (String oneUser : userInfo) {
                Thread.sleep(1000);
                threadPoolManager.accessUserInfo(oneUser,true);
            }
        }else{
            for (String oneUser : userInfo) {
                Thread.sleep(1000);
                threadPoolManager.accessUserInfo(oneUser,false);
            }
        }

        threadPoolManager.shutdown();
        threadPoolManager.await(start);
        System.out.println("执行结束");
    }

    public static Set<String> getUserInfo(String path) {
        Set<String> userInfo = new HashSet<String>();
        File file = new File(path);
        BufferedReader reader = null;
        String temp = "";
        int line = 1;
        try {
            reader = new BufferedReader(new FileReader(file));
            while ((temp = reader.readLine()) != null) {
                if (userInfo.size() >= 50) {
                    break;
                }
                if (temp.contains(":")) {
                    userInfo.add(temp);
                }
                line++;
            }
        } catch (Exception e) {
            System.err.println("读取用户文件失败");
            return new HashSet<String>();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("读取用户文件失败");
                }
            }
        }
        return userInfo;
    }
}
