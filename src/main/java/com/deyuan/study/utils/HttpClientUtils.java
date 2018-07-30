package com.deyuan.study.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

/**
 * @Author yangdeyuan
 * @Date 2018/7/21  11:23
 * @description:
 */
public class HttpClientUtils {



    public static CloseableHttpClient createHttpsClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();
        builder.setSSLContext(sslContext);
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);

        connMgr.setMaxTotal(100);
        connMgr.setDefaultMaxPerRoute(20);
        builder.setConnectionManager( connMgr);
        CloseableHttpClient client = builder.build();
        return client;
    }


    public final static String httpGet(String url) throws Exception {
        HttpGet httpMethod = new HttpGet(url);
        return (String)httpExecute(httpMethod,false,10).get("Content");
    }

    public final static String  httpGet(String url, Header[] headers) {

        HttpGet httpMethod = new HttpGet(url);
        httpMethod.setHeaders(headers);
        return (String)httpExecute(httpMethod,false,10).get("Content");
    }

    public final static String httpPost(String url,  Map<String, String>  params) throws Exception {
        HttpPost httpMethod = new HttpPost(url);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (Entry<String, String> en: params.entrySet()){
            if (en.getKey()!=null && en.getValue()!=null) {
                list.add(new BasicNameValuePair(en.getKey(),en.getValue()));
            }
        }

        if(list.size() > 0){
            try {
                UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(list,"utf-8");
                httpMethod.setEntity(reqEntity);
            } catch (UnsupportedEncodingException e) {
                //e.printStackTrace();
                System.out.println(e);
            }

        }

        return (String)httpExecute(httpMethod,false,10).get("Content");
    }


    public final static Map<String,Object> httpPost(String url,  Header[] headers,Map<String, String>  params) throws Exception {
        HttpPost httpMethod = new HttpPost(url);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (Entry<String, String> en: params.entrySet()){
            if (en.getKey()!=null && en.getValue()!=null) {
                list.add(new BasicNameValuePair(en.getKey(),en.getValue()));
            }
        }
        if(list.size() > 0){
            try {
                UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(list,"utf-8");
                httpMethod.setEntity(reqEntity);
            } catch (UnsupportedEncodingException e) {
                System.out.println(e);
            }
        }
        //cookie header
        if (headers!=null && headers.length>0) {
            httpMethod.setHeaders(headers);
        }

        return httpExecute(httpMethod,false,10);
    }

    /**
     * 重定向
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public final static Map<String,Object> httpPostAndRedirection(String url,  Map<String, String>  params) throws Exception {
        HttpPost httpMethod = new HttpPost(url);
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        for (Entry<String, String> en: params.entrySet()){
            if (en.getKey()!=null && en.getValue()!=null) {
                list.add(new BasicNameValuePair(en.getKey(),en.getValue()));
            }
        }

        if(list.size() > 0){
            UrlEncodedFormEntity reqEntity = new UrlEncodedFormEntity(list,"utf-8");
            httpMethod.setEntity(reqEntity);
        }

        Map<String,Object> ret = httpExecute(httpMethod,true,10);

        if (ret.containsKey("Location")) {
            String location = (String)ret.get("Location");
            HttpGet httpGet = new HttpGet(location);
            httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
            ret = httpExecute(httpGet,true,10);
        }
        return ret;
    }

    public final static String httpPostForJson(String url,  Header[] headers , String  params) throws Exception {
        HttpPost httpMethod = new HttpPost(url);
        StringEntity  reqEntity = new StringEntity (params,"utf-8");
        reqEntity.setContentEncoding("UTF-8");
        reqEntity.setContentType("application/json");
        httpMethod.setEntity(reqEntity);
        //cookie header
        if (headers!=null && headers.length>0) {
            httpMethod.setHeaders(headers);
        }
        return (String)httpExecute(httpMethod,false,10).get("Content");
    }


    public final static String httpDelete(String url, Header[] headers) throws Exception {
        HttpDelete httpMethod = new HttpDelete(url);
        httpMethod.setHeaders(headers);
        return (String)httpExecute(httpMethod,false,10).get("Content");
    }


    public final static Map<String,Object> httpExecute(HttpRequestBase httpMethod, boolean redirection,int times) {
        CloseableHttpClient  httpClient =  HttpClients.createDefault();

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(10000)
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        httpMethod.setConfig(defaultRequestConfig);
        CloseableHttpResponse response = null;
        Map<String,Object> ret = new HashMap<String,Object>();
        try {

            response = httpClient.execute(httpMethod);
            HttpEntity respEntity = response.getEntity();
            Header[] headerFor302=null;
            if (response.getStatusLine().getStatusCode() == 302 && redirection==true) {
                headerFor302=response.getHeaders("Set-Cookie");
                String location = response.getHeaders("Location")[0].getValue();
                ret.put("Location", location);
                ret.put("Content", EntityUtils.toString(respEntity, "utf-8"));
                ret.put("Set-Cookie", headerFor302);
                return ret;
            } else if (response.getStatusLine().getStatusCode() >= 400) {
                throw new Exception("HTTP Request is not success, Response code is " + response.getStatusLine().getStatusCode());
            }

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                Header[] headers=response.getHeaders("Set-Cookie");
                for (int i=0;i<headers.length;i++) {
                    headers[i] = new BasicHeader("Cookie",headers[i].getValue());
                }
                ret.put("Set-Cookie", response.getHeaders("Set-Cookie"));
                ret.put("Content", EntityUtils.toString(respEntity, "utf-8"));
                EntityUtils.consume(respEntity);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //System.out.println("重新请求:"+times);
            try{
                Thread.sleep(2000);
            }catch (Exception e1){
                System.out.println("睡眠失败");
            }
            if(times>0){
                int secondTimes=--times;
                httpExecute(httpMethod,false,secondTimes);
            }
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if(null!=httpClient){
                    httpClient.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        return ret;


    }


    public static void main(String[] args) throws Exception {
//		Map map = new HashMap();
//		map.put("username", "admin");
//		map.put("password", "admin");
//		map.put("format", "json");
//		httpGet("http://127.0.0.1:8080/console/login?password=admin&format=json&username=admin");
//		httpPost("http://127.0.0.1:8080/console/login", map);

    }
}
