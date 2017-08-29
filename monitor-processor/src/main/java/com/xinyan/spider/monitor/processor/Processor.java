package com.xinyan.spider.monitor.processor;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.xinyan.spider.monitor.Dao.MonitorInfoDaoImpl;
import com.xinyan.spider.monitor.common.utils.*;
import com.xinyan.spider.monitor.model.MonitorInfo;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

/**
 * @Author：WenqiangPu
 * @Description
 * @Date：Created in 14:25 2017/8/1
 * @Modified By：
 */
@Component
@Scope("prototype")
public class Processor {

    private String hostName;
    private String body;
    private String username = "manage";//写死12aa
    private String baseUrl = "https://192.168.161.+hostName";
    private WebClient webClient;
    HashMap<String, String> header = new HashMap();
    @Autowired
    private MonitorInfoDaoImpl monitorInfoDao;
    protected static Logger logger = LoggerFactory.getLogger(Processor.class);

    public void setHostName(String hostName) {
        this.baseUrl = "https://192.168.161."+hostName;
        this.hostName = hostName;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void login() {
        try{
            logger.info("[{}]主机登录中...",hostName);
            String url = "https://192.168.161."+hostName+"/v3/index.html";//主机url
            webClient = initWebClient(webClient);
            //创建Client客户端
            Page page = getPage(webClient, url, HttpMethod.GET, null, null);
            header.put("Referer", url);
            header.put("datatype", "xml");
            String bodyStr = "/api/login/"+this.body;
            String wurl = baseUrl+"/v3/api/";//登录url
            int tryAgain =0;
            String response;
            do{
                Page resultPage = getPage(webClient, wurl, HttpMethod.POST, null, header, bodyStr);
                response = resultPage.getWebResponse().getContentAsString();
                if (StringUtils.contains(response, "success<")) {
                    break;
                }
                tryAgain++;
                logger.info("第[{}]次[{}]主机登录失败...正在重新请求登录",tryAgain,hostName);
            }while(tryAgain< Constants.TRYAGAIN);
            if (StringUtils.contains(response, "success<")) {
                logger.info("[{}]主机登录成功...",hostName);
                monitor(response,url);
            }else{
                logger.error("[{}]主机登录失败...",hostName);
                return;
            }
        }catch (Exception e){
            logger.error("[{}]主机登录出现异常...",hostName);
        }finally {
            webClient.close();
        }

    }

    /**
     * @Author: WenqiangPu
     * @Description:
     * @param response
     * @param url
     * @return:
     * @Date: 15:47 2017/8/1
     */
    public void monitor(String response,String url) {

        String sessionKey = RegexUtils.matchValue("name=\"response\">(.*?)</PROPERTY>", response);
        Cookie cookie = new Cookie(baseUrl, "wbiusername", username);
        webClient.getCookieManager().addCookie(cookie);
        cookie = new Cookie(baseUrl, "wbisessionkey", sessionKey);
        webClient.getCookieManager().addCookie(cookie);
        header.clear();
        header.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729)");
        header.put("Referer", url);
        header.put("Connection", "Keep-Alive");
        header.put("sessionkey", sessionKey);
        while (true) {
            logger.info("[{}]主机获取服务器信息中...",hostName);
            String urlHost = baseUrl+"/v3/api/show/host-port-statistics?_=1501505153069";
            Page page = getPage(webClient, urlHost, HttpMethod.GET, null, header);
            String resource = page.getWebResponse().getContentAsString();//解析
            Boolean flag = parse(resource);
            if(!flag){//爬取失败
                logger.error("[{}]主机获取服务器信息失败...",hostName);
                break;
            }
            try {
                logger.info("[{}]主机等待获取下一时间服务器信息...",hostName);
                Thread.sleep(60 * 1000);
            } catch (Exception e) {
            }
        }

    }
    public boolean parse(String resource){
        try {
            if (StringUtils.contains(resource, "Success")) {//成功抓取到
                MonitorInfo monitorInfo = new MonitorInfo();
                monitorInfo.setStartTime(RegexUtils.matchValue("Start Sample Time\">(.*?)</PROPERTY>", resource));
                monitorInfo.setEndTime(RegexUtils.matchValue("Stop Sample Time\">(.*?)</PROPERTY>", resource));
                String iops = getIOPS(resource);
                String Byte = getBytes(resource);
                monitorInfo.setHostName(hostName);
                monitorInfo.setIops(iops);
                monitorInfo.setBytes(Byte + "MB/s");
                logger.info("[{}]主机获取服务器信息成功...", hostName);
                monitorInfoDao.add(monitorInfo);
            } else {//未抓取到，重新登录
                logger.info("[{}]主机获取服务器信息失败...", hostName);
                return false;
            }
        }catch (Exception e){
            logger.error("[{}]主机数据入库失败...", hostName);
        }
        return true;

    }

    //
    /**
     * @Author: WenqiangPu
     * @Description: 抓取IOPS
     * @param resource
     * @return:
     * @Date: 20:28 2017/7/31
     */
    public String getIOPS(String resource){
        String[] IOPSList = RegexUtils.matchMutiValue("display-name=\"IOPS\">(\\d+)</PROPERTY>",resource);
        int sumIOPS = 0;
        for(String i:IOPSList){
            sumIOPS+=Integer.parseInt(i);
        }
        return String.valueOf(sumIOPS);
    }
    /**
     * @Author: WenqiangPu
     * @Description: 抓取流量
     * @param resource
     * @return:
     * @Date: 17:25 2017/8/1
     */
    public String getBytes(String resource){
        String[] IOPSList = RegexUtils.matchMutiValue("<PROPERTY name=\"bytes-per-second-numeric\" .*?\">(\\d+)</PROPERTY>",resource);
        int sumBytes = 0;
        for(String i:IOPSList){
            sumBytes+=Integer.parseInt(i);
        }
        return String.valueOf(Math.round(Double.parseDouble(String.valueOf(sumBytes/100000))/10));
    }


    private WebClient initWebClient(WebClient webClient) {
        if (null == webClient) {
            webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        }
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setActiveXNative(false);
        webClient.getOptions().setAppletEnabled(false);
        webClient.getOptions().setCssEnabled(false);// 禁用css支持
        webClient.getOptions().setThrowExceptionOnScriptError(false);// js运行错误时，是否抛出异常
        webClient.getOptions().setJavaScriptEnabled(false); //禁用JS
        webClient.getOptions().setTimeout(120000);
        return webClient;
    }

    protected void close(WebClient webClient) {
        if (webClient != null) {
            webClient.close();
            webClient = null;
        }
    }

//    protected void loginout(WebClient webClient) {
//        String url = "https://hb.ac.10086.cn/logout";
//        Page page = getPage(webClient, url, HttpMethod.GET, null, null);
//    }
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params, Map<String, String> header) {
        return getPage(webClient, url, httpMethod, params, CharsetCode.UTF8, header);
    }

    /**
     * 获取页面
     *
     * @param webClient  访问客户端
     * @param url        访问地址
     * @param httpMethod 提交方法
     * @param params     访问参数
     * @param header     访问Header
     * @param bodyStr    Body参数
     * @return
     * @description
     * @author heliang
     * @create 2016年8月13日 上午9:48:59
     */
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params, Map<String, String> header, String bodyStr) {
        return getPage(webClient, url, httpMethod, params, CharsetCode.UTF8, header, bodyStr);
    }

    /**
     * 获取页面
     * <b>有重复尝试机制</b>
     *
     * @param webClient  访问客户端
     * @param url        访问地址
     * @param httpMethod 提交方法
     * @param params     访问参数
     * @param retryTimes 重复尝试次数
     * @param logFlag    日志标志
     * @param header     访问Header
     * @return
     * @description
     * @author heliang
     * @create 2016年8月13日 上午9:48:59
     */
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params,
                        int retryTimes, String logFlag, Map<String, String> header) {
        Page page;
        int rt = 1;
        while (rt <= retryTimes) {
            try {
                page = getPage(webClient, url, httpMethod, params, CharsetCode.UTF8, header);
                if (null != page) {
                    return page;
                } else {
                    rt++;
                }
            } catch (Exception e) {
                rt++;
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }

    /**
     * 获取页面
     * <b>有重复尝试机制</b>
     *
     * @param webClient   访问客户端
     * @param url         访问地址
     * @param httpMethod  提交方法
     * @param params      访问参数
     * @param retryTimes  重复尝试次数
     * @param logFlag     日志标志
     * @param header      访问Header
     * @param charsetCode 请求编码
     * @return
     * @description
     * @author york
     * @create 2016年9月23日 上午11:48:59
     */
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params,
                        int retryTimes, String logFlag, Map<String, String> header, CharsetCode charsetCode) {
        Page page;
        int rt = 1;
        while (rt <= retryTimes) {
            try {
                page = getPage(webClient, url, httpMethod, params, charsetCode, header);
                if (null != page) {
                    return page;
                } else {
                    rt++;
                }
            } catch (Exception e) {
                rt++;
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                }
            }
        }
        return null;
    }

    /**
     * 获取页面
     * <b>有重复尝试机制</b>
     *
     * @param webClient  访问客户端
     * @param url        访问地址
     * @param httpMethod 提交方法
     * @param params     访问参数
     * @param retryTimes 重复尝试次数
     * @param logFlag    日志标志
     * @param header     访问Header
     * @param bodyStr    Body参数
     * @return
     * @description
     * @author heliang
     * @create 2016年8月13日 上午9:48:59
     */
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params,
                        int retryTimes, String logFlag, Map<String, String> header, String bodyStr) {
        Page page;
        int rt = 1;
        while (rt <= retryTimes) {
            try {
                page = getPage(webClient, url, httpMethod, params, CharsetCode.UTF8, header, bodyStr);
                if (null != page) {
                    return page;
                } else {
                    rt++;
                }
            } catch (Exception e) {
                rt++;
            }
        }
        return null;
    }

    /**
     * 获取页面
     *
     * @param webClient  访问客户端
     * @param url        访问地址
     * @param httpMethod 提交方法
     * @param params     访问参数
     * @param header     访问Header
     * @return
     * @description
     * @author heliang
     * @create 2016年8月13日 上午9:48:59
     */
    public Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params, Map<String, String> header, CharsetCode charsetCode) {
        return getPage(webClient, url, httpMethod, params, charsetCode, header);
    }

    //--------------------------------------------------------
    //以下方法无须关注
    //--------------------------------------------------------

    private Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params,
                         CharsetCode charset, Map<String, String> header) {
        if (httpMethod == HttpMethod.GET) {
            return doGet(webClient, url, params, charset, header, "");
        } else {
            return doPost(webClient, url, params, charset, header, "");
        }
    }

    private Page getPage(WebClient webClient, String url, HttpMethod httpMethod, List<NameValuePair> params,
                         CharsetCode charset, Map<String, String> header, String bodyStr) {
        if (httpMethod == HttpMethod.GET) {
            return doGet(webClient, url, params, charset, header, bodyStr);
        } else {
            return doPost(webClient, url, params, charset, header, bodyStr);
        }
    }

    private Page doPost(WebClient webClient, String pageUrl, List<NameValuePair> reqParam,
                        CharsetCode charset, Map<String, String> header, String bodyStr) {
        try {
            URL url = new URL(pageUrl);
            WebRequest webRequest = new WebRequest(url, HttpMethod.POST);
            webRequest.setAdditionalHeader("Accept-Language", "zh-CN");
            if (charset == null) {
                charset = CharsetCode.UTF8;
            }
            webRequest.setCharset(charset.getCode());
            if (reqParam != null) {
                webRequest.setRequestParameters(reqParam);
            }
            if (null != header) {
                for (String key : header.keySet()) {
                    webRequest.setAdditionalHeader(key, header.get(key));
                }
            }
            if (StringUtils.isNotBlank(bodyStr)) {
                webRequest.setRequestBody(bodyStr);
            }
            return webClient.getPage(webRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Page doGet(WebClient webClient, String pageUrl, List<NameValuePair> reqParam,
                       CharsetCode charset, Map<String, String> header, String bodyStr) {
        try {
            URL url;
            if (CollectionUtil.isEmpty(reqParam)) {
                url = new URL(pageUrl);
            } else {
                url = new URL(pageUrl + "?" + EntityUtils.toString((HttpEntity) reqParam));
            }

            WebRequest webRequest = new WebRequest(url, HttpMethod.GET);
            if (null != header) {
                for (String key : header.keySet()) {
                    webRequest.setAdditionalHeader(key, header.get(key));
                }
            }
            if (charset == null) {
                charset = CharsetCode.UTF8;
            }
            webRequest.setCharset(charset.getCode());
            if (StringUtils.isNotBlank(bodyStr)) {
                webRequest.setRequestBody(bodyStr);
            }
            return webClient.getPage(webRequest);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
