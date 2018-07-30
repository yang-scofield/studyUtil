package com.deyuan.study.utils;

import org.springframework.util.StringUtils;

/**
 * @Author yangdeyuan
 * @Date 2018/7/23  22:57
 * @description:
 */
public class ThreadAccess implements  Runnable{
    private String user;

    private boolean checkScore;

    public ThreadAccess() {
        super();
    }
    public ThreadAccess(String user,boolean checkScore) {
        this.user = user;
        this.checkScore=checkScore;
    }

    public boolean isCheckScore() {
        return checkScore;
    }

    public void setCheckScore(boolean checkScore) {
        this.checkScore = checkScore;
    }

    public String getMsg() {
        return user;
    }
    public void setMsg(String user) {
        this.user = user;
    }
    @Override
    public void run() {
        String[] userInfo=user.split(":");
        if(null==userInfo||userInfo.length!=2)return;
        String userName=userInfo[0];
        String password=userInfo[1];
        if(StringUtils.isEmpty(userName)||StringUtils.isEmpty(password)){
            System.out.println("用户名和密码不能为空");
            return;
        }else{

            if(checkScore){
                boolean checkResult=ConsoleUtil.checkScore(userName,password);
                if(!checkResult){
                    System.out.println(userName+":"+password+"  学习分数未满");
                }
            }else {
                System.out.println("用户："+userName+" 开始学习");
                try{

                    ConsoleUtil.studyBegin(userName,password);
                }catch (Exception e){
                    System.out.println("用户："+userName+" 学习失败");
                    return;
                }
                System.out.println("用户："+userName+" 学习完成");
            }

        }

    }
}
