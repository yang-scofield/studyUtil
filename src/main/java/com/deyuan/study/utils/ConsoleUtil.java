package com.deyuan.study.utils;


import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

/**
 * @Author yangdeyuan
 * @Date 2018/7/21  11:21
 * @description:
 */
public class ConsoleUtil {

    private final static String baseUrl = "http://online.nwpunec.net";
    private final static String loginPageUrl = "http://online.nwpunec.net/ELearningWebPlatform/Student/Login";
    private final static String loginUrl = "http://online.nwpunec.net/ELearningWebPlatform/Login";
    private final static String loginOut = "http://online.nwpunec.net/ELearningWebPlatform/Logout";
    private final static String courseListUrl = "";
    private final static String currentCourseDetailUrl = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/CurrentCourseDetail";//studentId,_
    private final static String courseStudyUrl = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/CourseStudy?courseId=%s";//courseId

    private final static String postList = "http://online.nwpunec.net/" +
            "ELearningWebForum/Student/PostList?courseId=%s";//论坛页

    private final static String createPost = "http://online.nwpunec.net/ELearningWebForum/Student/CreatePost";//发帖

    private final static String getMaterials = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/GetMaterials?courseId=%s&_=%s";//章节测试内容


    private final static String chapterTests = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/GetMaterials?courseId=%s&id=%s&_=%s";//章节测试全部链接

    private final static String quizPaper = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/QuizPaper?courseId=%s&quizId=%s&studentId=%s";//章节测试题页面

    private final static String quizSubmit = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/QuizPaper?courseId=%s&quizId=%s&action=submit";//测试题提交

    private final static String beginTimeCountUrl = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/StartTimeCount?studentId=%s&courseId=%s&count=%s";

    private final static String continueTimeCountUrl = "http://online.nwpunec.net/" +
            "ELearningWebPlatform/Student/ContinueTimeCount?studentId=%s&courseId=%s&count=%s";


    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public static Map<String, Object> login(String username, String password) throws Exception {
        String page = HttpClientUtils.httpGet(loginPageUrl);
        Map<String, String> params = new HashMap();
        params.put("userNameTxt", username);
        params.put("passwordTxt", password);
        params.put("saveUserInfo", "0");
        params.put("accountType", "1");
        Map<String, Object> ret = HttpClientUtils.httpPostAndRedirection(loginUrl, params);

        if(null==ret||ret.isEmpty()){
            System.out.println("用戶：" + username + " 登录失败");
            return null;
        }
        if (JSON.parseObject(ret.get("Content").toString()).getIntValue("hr") != 0) {
            System.out.println("用戶：" + username + " 登录失败");
            return null;
        }
        Header[] headers = (Header[]) ret.get("Set-Cookie");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].getValue().contains("Student=")) {
                String stuInfo = headers[i].getValue().substring("Student=".length());
                stuInfo = URLDecoder.decode(stuInfo, "UTF-8");
                stuInfo = stuInfo.substring(0, stuInfo.lastIndexOf("; Path=/"));
                ret.put("student", stuInfo);
            }
            headers[i] = new BasicHeader("Cookie", headers[i].getValue());
        }
        return ret;
    }


    /**
     * 登录课程学习20次
     *
     * @param userName
     * @param password
     * @throws Exception
     */
    public static void study20(String userName, String password) throws Exception {
        for (int i = 0; i < 22; i++) {
            try{
                Map<String, Object> ret = login(userName, password);
                Header[] headers = (Header[]) ret.get("Set-Cookie");
                JSONObject stuInfo = JSON.parseObject(ret.get("student").toString());
                String studentId = String.valueOf(stuInfo.getIntValue("id"));
                Map<String, String> courseMap = getCourseList(studentId, headers);//获得课程ID和 课程名
                for (String courseId : courseMap.keySet()) {
                    getCourses(courseId, headers);   //登录课程学习
                }
                logout(headers);
            }catch (Exception e){
                System.out.println(e);
            }

        }
    }


    /**
     * 课程详情页
     * @param studentId
     * @param headers
     * @return
     */
    public static String currentCourseDetail(String studentId, Header[] headers) {
        String url = currentCourseDetailUrl + "?studentId=" + studentId + "&_=" + LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) + "000";
        return HttpClientUtils.httpGet(url, headers);
    }

    /**
     * 获取课程id
     *
     * @param
     * @return
     */
    public static Map<String, String> getCourseList(String studentId, Header[] headers) {
        try{
            String sourcePage = currentCourseDetail(studentId, headers);
            Document document = Jsoup.parse(sourcePage);
            Elements courses = document.getElementsByClass("courseId");
            Map<String, String> coursesINfo = new HashMap<String, String>();
            for (Element element : courses) {
                Element nextElement = element.nextElementSibling();
                coursesINfo.put(element.text(), nextElement.text());
            }
            return coursesINfo;
        }catch (Exception e){
            return new HashMap<String,String>();
        }

    }

    /**
     * 获取学生统计分数
     * @param studentId
     * @param headers
     * @return
     */
    public static Map<String, Map<String,Float>> getCourseScore(String studentId, Header[] headers) {
        try{
            String sourcePage = currentCourseDetail(studentId, headers);
            Document document = Jsoup.parse(sourcePage);
            Elements courses = document.getElementsByClass("courseId");
            Map<String, Map<String,Float>> coursesScore = new HashMap<String, Map<String,Float>>();
            for (Element element : courses) {
                Map<String,Float> map=new HashMap<String, Float>();
                Element trElement = element.parent();

                Elements  login_scoreElements=trElement.getElementsByClass("login_score");
                if(null!=login_scoreElements&&!login_scoreElements.isEmpty()){
                       map.put("login_score",Float.valueOf(login_scoreElements.get(0).text()));
                }else {
                    map.put("login_score",0F);
                }

                Elements study_timeElements=trElement.getElementsByClass("study_time_score");
                if(null!=study_timeElements&&!login_scoreElements.isEmpty()){
                    map.put("study_time_score",Float.valueOf(study_timeElements.get(0).text()));

                }else {
                    map.put("study_time_score",0F);
                }

                Elements quiz_scoreElements=trElement.getElementsByClass("quiz_score");
                if(null!=quiz_scoreElements&&!quiz_scoreElements.isEmpty()){
                    map.put("quiz_score",Float.valueOf(quiz_scoreElements.get(0).text()));

                }else {
                    map.put("quiz_score",0F);
                }

                Elements forum_scoreElements=trElement.getElementsByClass("forum_score");
                if(null!=forum_scoreElements&&!forum_scoreElements.isEmpty()){
                    map.put("forum_score",Float.valueOf(forum_scoreElements.get(0).text()));

                }else {
                    map.put("forum_score",0F);
                }


                coursesScore.put(element.text(), map);
            }
            return coursesScore;
        }catch (Exception e){
            System.out.println("获取学生统计分数失败:"+e);
            return null;
        }

    }

    /**
     * 获取每个课程学习的章节的视频链接，登录课程学习
     */
    public static List<String> getCourses(String courseId, Header[] headers) {
        List<String> courseMaterials = new ArrayList<String>();
        String url = String.format(courseStudyUrl, courseId);
        String html = HttpClientUtils.httpGet(url, headers);
        return new ArrayList<String>();
/*        if(null==html){
            return new ArrayList<String>();
        }

        Document document = Jsoup.parse(html);
        Element div = document.getElementById("courseMaterials");
        Elements as = div.getElementsByClass("font16");
        for (Element a : as) {
            courseMaterials.add(a.attr("url"));
        }

        return courseMaterials;*/
    }

    /**
     * 开始计时
     *
     * @param studentId
     * @param courseId
     * @param headers
     * @throws Exception
     */
    public static Map<String, Object> beginTimeCount(String studentId, String courseId, String count, Header[] headers) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        String url = String.format(beginTimeCountUrl, studentId, courseId, count);
        Map<String, String> map = new HashMap<String, String>();
        try{
            result = HttpClientUtils.httpPost(url, headers, map);
        }catch (Exception e){
            System.out.println(e);
        }

        return result;

    }

    /**
     * 继续计时
     *
     * @param studentId
     * @param courseId
     * @param headers
     * @throws Exception
     */
    public static void continueTimeCount(String studentId, String courseId, String count, Header[] headers) throws Exception {
        String url = String.format(continueTimeCountUrl, studentId, courseId, count);
        Map<String, String> map = new HashMap<String, String>();
        try{
            Map<String, Object> result = HttpClientUtils.httpPost(url, headers, map);
        }catch (Exception e){
            System.out.println(e);
        }


    }


    /**
     * 课程学习
     *
     * @param studentId
     * @param courseMap
     * @param headers
     * @throws Exception
     */
    public static void studyTenMinute(String studentId, Map<String, String> courseMap,Map<String,Map<String,Float>> studentScore, Header[] headers) throws Exception {
        Map<String, List<Header[]>> courseHeader = new HashMap<String, List<Header[]>>();
        for (String courseId : courseMap.keySet()) {

            if(studentScore.get(courseId).get("study_time_score")<12){
                //System.out.println("student:"+studentId+" 开始课程学习 "+"course:"+courseId);
                List<Header[]> subCourseHeader = new ArrayList<Header[]>();
                for (int count = 1; count < 8; count++) {
                    try{
                        Map<String, Object> beginResult = beginTimeCount(studentId, courseId, "1", headers);
                        Header[] beginCountHeader = (Header[]) beginResult.get("Set-Cookie");
                        for (int i = 0; i < beginCountHeader.length; i++) {
                            beginCountHeader[i] = new BasicHeader("Cookie", beginCountHeader[i].getValue());
                        }
                        subCourseHeader.add(beginCountHeader);
                    }catch (Exception e){
                        System.out.println(e);
                    }


                }
                courseHeader.put(courseId, subCourseHeader);
            }

        }

        for (int i = 0; i < 6; i++) {

            boolean jumpStudy=true;
            for(String cId:courseHeader.keySet()){
                if(studentScore.get(cId).get("study_time_score")<12){
                    jumpStudy=false;
                    break;
                }
            }
            if(jumpStudy){
                break;
            }

            Thread.sleep(1000 * 60*2);
            for (String courseId : courseHeader.keySet()) {
                if(studentScore.get(courseId).get("study_time_score")<12){
                    for (Header[] trackerHeader : courseHeader.get(courseId)) {
                        try{
                            Header[] copeHeader = headers.clone();
                            copeHeader = Arrays.copyOf(copeHeader, copeHeader.length + trackerHeader.length);
                            System.arraycopy(trackerHeader, 0, copeHeader, copeHeader.length - 1, trackerHeader.length);
                            continueTimeCount(studentId, courseId, "1", copeHeader);
                        }catch (Exception e){
                            System.out.println(e);
                        }
                    }
                    //System.out.println("student:"+studentId+" 继续课程学习 "+"course:"+courseId);
                }

            }
        }
    }


    /**
     * 获取章节测试id
     *
     * @param courseId
     * @param headers
     */
    public static String GetMaterials(String courseId, Header[] headers) {
        String url = String.format(getMaterials, courseId, LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) + "000");
        String result = HttpClientUtils.httpGet(url, headers);
        JSONArray chapterTest = JSON.parseArray(result);
        String id = "";
        for (int i = 0; i < chapterTest.size(); i++) {
            JSONObject subChapter = chapterTest.getJSONObject(i);
            if ("章节测试".equals(subChapter.getString("text"))) {
                id = subChapter.getString("id");
                break;
            }
        }
        return id;
    }

    /**
     * 获取课程测试题id列表
     *
     * @param courseId
     * @param headers
     * @return
     * @throws Exception
     */
    public static List<String> chapterTestUrlList(String courseId, Header[] headers) throws Exception {
        ArrayList<String> quizIds = new ArrayList<String>();
        try{
            String id = GetMaterials(courseId, headers);
            if (StringUtils.isEmpty(id)) {
                System.err.println("获取id失败");
                return quizIds;
            }
            String url = String.format(chapterTests, courseId, id, LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) + "000");
            String result = HttpClientUtils.httpGet(url, headers);
            JSONArray testArray = JSON.parseArray(result);
            for (int i = 0; i < testArray.size(); i++) {
                JSONObject obj = testArray.getJSONObject(i);
                String text = obj.getString("text");
                String quizId = text.substring(text.indexOf("quizId=") + 7, text.lastIndexOf("'>"));
                quizIds.add(quizId);
            }
        }catch ( Exception e){
            return  new ArrayList<String>();
        }

        return quizIds;

    }


    /**
     * 获得问题id
     *
     * @param studentId
     * @param courseId
     * @param quizId
     * @param headers
     * @return
     */
    public static Map<String, String> quizQuestionIds(String studentId, String courseId, String quizId, Header[] headers) {

        try{
            String url = String.format(quizPaper, courseId, quizId, studentId);
            String html = HttpClientUtils.httpGet(url, headers);
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByAttributeValueEnding("name", "quizQuestionId");
            Map<String, String> questions = new HashMap<String, String>();
            for (Element element : elements) {
                questions.put(element.attr("name"), element.attr("value"));
            }
            return questions;
        }catch (Exception e){
            System.out.println("获取测试题ID失败");
            //e.printStackTrace();
            return new HashMap<String,String>();
        }

    }

    /**
     * 测试题作答
     *
     * @param studentId
     * @param courseId
     * @param headers
     * @throws Exception
     */
    public static void quizSubmit(String studentId, String courseId, Header[] headers) throws Exception {
        List<String> quizIds = chapterTestUrlList(courseId, headers);

            for (String quizId : quizIds) {
                try{
                Map<String, String> ids = quizQuestionIds(studentId, courseId, quizId, headers);
                Map<String, String> param = new HashMap<String, String>();
                param.put("studentId", studentId);
                for(int questionCount=0;questionCount<ids.size();questionCount++){
                    String paramAnswer=String.format("studentQuestions[%s].answer",String.valueOf(questionCount));
                    String paramId=String.format("studentQuestions[%s].id",String.valueOf(questionCount));
                    param.put(paramAnswer,"A");
                    param.put(paramId,"0");
                }

                if (ids.isEmpty()) continue;
                param.putAll(ids);
                String url = String.format(quizSubmit, courseId, quizId);

                    Map<String, Object> result = HttpClientUtils.httpPost(url, headers, param);
                    for(int j=0;j<3;j++){
                        if(null!=result&&(!"0".equals(JSON.parseObject(result.get("Content").toString()).getString("hr")))){
                            result=HttpClientUtils.httpPost(url, headers, param);
                        }else{
                            break;
                        }
                    }
                }catch (Exception e){
                    System.err.println("答题请求错误");
                    //e.printStackTrace();
                }
            }

    }


    /**
     * 发帖
     *
     * @param courseId
     * @throws Exception
     */
    public static void createPost(String courseId, String courseName, Header[] headers) throws Exception {
        Map<String, String> param = new HashMap<String, String>();
        String titleTxt = courseName;
        String contentTxt = "<p>" + courseName + "</p>";
        param.put("courseId", courseId);
        param.put("titleTxt", titleTxt);
        param.put("contentTxt", contentTxt);
        for (int i = 0; i < 10; i++) {
            try{
                HttpClientUtils.httpPost(createPost, headers, param);
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }



    public static void logout(Header[] headers) {
        HttpClientUtils.httpGet(loginOut, headers);
    }

    /**
     * 学习入口
     *
     * @param userName
     * @param password
     * @throws Exception
     */
    public static void studyBegin(String userName, String password) {
        Header[] headers=null;
        Map<String,Map<String,Float>> afterStudyStudentScore=new HashMap<String,Map<String,Float>>();
        try {
            System.out.println("登录课程20次");
            study20(userName, password);
            System.out.println("结束登录课程20次");
            Map<String, Object> ret =new HashMap<String,Object>();
            try{
                ret= login(userName, password);
            }catch(Exception e){
                System.out.println("用户："+userName+" 登录失败");
                return;
            }
            if(null==ret||ret.isEmpty()){
                System.out.println("用户："+userName+" 登录失败");
                return;
            }

            headers= (Header[]) ret.get("Set-Cookie");
            JSONObject stuInfo = JSON.parseObject(ret.get("student").toString());
            String studentId = String.valueOf(stuInfo.getIntValue("id"));
            Map<String, String> courseMap = getCourseList(studentId, headers);//获得课程ID和 课程名
            Map<String,Map<String,Float>> studentScore=getCourseScore(studentId,headers);//获得学习分数
            if(null==studentScore||studentScore.isEmpty()){
                return;
            }
            System.out.println("用户："+userName+" 开始发帖");
            for (String courseId : courseMap.keySet()) {
                if(studentScore.get(courseId).get("forum_score")<4){
                    createPost(courseId, courseMap.get(courseId), headers); //发帖
                }
            }
            System.out.println("用户："+userName+" 发帖结束");

            System.out.println("用户："+userName+" 开始测试题");
            for(String courseId:courseMap.keySet()){
                if(studentScore.get(courseId).get("quiz_score")<8) {
                    quizSubmit(studentId, courseId, headers);       //测试题
                }
            }
            System.out.println("用户："+userName+" 测试题结束");

            System.out.println("用户："+userName+" 开始课程学习");
            studyTenMinute(studentId, courseMap,studentScore, headers);
            System.out.println("用户："+userName+" 课程学习结束");
            afterStudyStudentScore=getCourseScore(studentId,headers);

        } catch (Exception e) {
            System.out.println("用户：" + userName + " 学习失败"+"分数："+afterStudyStudentScore);
            //e.printStackTrace();
            return;
        }finally {
            if(null!=headers){
                logout(headers);
            }
        }
        System.out.println("用户：" + userName + " 学习成功"+"分数："+afterStudyStudentScore);

    }


    public static boolean checkScore(String userName,String password){
        Header[] headers=null;
        System.out.println(userName+"开始检查");
        try{
            Map<String, Object> ret =new HashMap<String,Object>();
            try{
                ret= login(userName, password);
            }catch(Exception e){
                System.out.println("用户："+userName+" 登录失败");
                return false;
            }
            if(null==ret||ret.isEmpty()){
                System.out.println("用户："+userName+" 登录失败");
                return false;
            }
            headers= (Header[]) ret.get("Set-Cookie");
            JSONObject stuInfo = JSON.parseObject(ret.get("student").toString());
            String studentId = String.valueOf(stuInfo.getIntValue("id"));
            Map<String, String> courseMap = getCourseList(studentId, headers);//获得课程ID和 课程名
            Map<String,Map<String,Float>> studentScore=getCourseScore(studentId,headers);//获得学习分数
            for (String courseId : courseMap.keySet()) {
                if (studentScore.get(courseId).get("forum_score") < 4) {
                    return false;
                }
                if (studentScore.get(courseId).get("quiz_score") < 8) {
                    return false;
                }
                if (studentScore.get(courseId).get("study_time_score") < 12) {
                    return false;
                }
                if (studentScore.get(courseId).get("login_score") < 6) {
                    return false;
                }
            }
        }catch (Exception e){
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        String userName = "140803203014";
        String password = "330105198211202527";
        studyBegin( userName,  password);

    }

}
