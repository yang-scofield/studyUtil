package com.deyuan.study.utils;



import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;

public class SignUtil {

    private static Logger logger = LoggerFactory.getLogger(SignUtil.class);

    /**
     * 生成签名
     * @param appKey
     * @param appSecret
     * @param map
     * @return
     */
    public static String sign(String appKey,String appSecret,String timeStamp,Map<String,String> map) {
        String param="";
        try {
            Collection<String> keyset = map.keySet();
            ArrayList<String> list = new ArrayList<String>(keyset);
            Collections.sort(list);
            param="appKey="+appKey;
            param+="&timeStamp="+timeStamp;
            for (int i = 0; i < list.size(); i++) {
                //param += "&" + URLEncoder.encode(list.get(i), "utf-8") + "=" + URLEncoder.encode(map.get(list.get(i)),"utf-8");
                param += "&" + list.get(i) + "=" + map.get(list.get(i));
            }
            param+="&"+"appSecret="+appSecret;
            logger.debug(param);
            String sign = DigestUtils.sha256Hex(param).toUpperCase();
            return sign;
        }catch(Exception e) {
            logger.error("create sign error",e);
        }

        return "";
    }

    /**
     * 验证签名
     * @param appKey
     * @param appSecret
     * @param paramSign
     * @param map
     * @return
     */
    public static boolean verify(String appKey,String appSecret,String paramSign, String timeStamp,Map<String,String> map) {
        String param="";
        if(StringUtils.isEmpty(appKey)||StringUtils.isEmpty(appSecret)||StringUtils.isEmpty(paramSign)||null==map) {
            logger.error("param can't null");
            return false;
        }
        try {
            Collection<String> keyset = map.keySet();
            ArrayList<String> list = new ArrayList<String>(keyset);
            Collections.sort(list);
            param="appKey="+appKey;
            param+="&timeStamp="+timeStamp;
            for (int i = 0; i < list.size(); i++) {
                param += "&" + list.get(i) + "=" + map.get(list.get(i));
            }
            param+="&"+"appSecret="+appSecret;
            logger.debug(param);
            String sign = DigestUtils.sha256Hex(param).toUpperCase();
            logger.info("============sign is :"+sign);
            if(paramSign.equals(sign)) {
                return true;
            }
        }catch(Exception e) {
            logger.error("verify sign error",e);
            return false;
        }
        return false;
    }

    public static void main(String[] args ) throws UnsupportedEncodingException {
        Map<String,String> map=new HashMap<String,String>();
        String appSecret="appSecret";
        String appKey="appKey";
        String timeStamp="1524321888650";
        map.put("name", "deyuan");
        String sign=sign(appKey,appSecret,timeStamp,map);
        System.out.println("sign:"+sign);
        System.out.println(verify(appKey,appSecret,sign,timeStamp,map));
        System.out.println(Instant.now().toEpochMilli()-1513565412849L);
    }
}
