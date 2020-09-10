package com.blacklist.blacklist.common;

import com.blacklist.blacklist.dao.BlacklistDao;
import com.blacklist.blacklist.entry.Blacklist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class BlacklistInterceptor implements HandlerInterceptor {

    /**
     * 访问次数上线
     */
    private static final int MAX_TIMES = 100;

    /**
     * 过期时间 单位：秒
     */
    private static final int EXPIRATION = 60;

    @Autowired
    BlacklistDao blackListDao;

    @Autowired
    CacheUtil cacheUtil;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        String ip = this.getIpAddr(httpServletRequest);
        String url = this.resolveRequestPath(httpServletRequest);

        //先查看数据库有没有黑名单
        Blacklist blackList = this.blackListDao.getBlacklistByIp(ip);
        if (blackList == null) {
            //查看缓存有没有访问记录
            Integer times = this.cacheUtil.get(ip, url);
            if (times != null) {
                //如果此ip对某个url访问在一分钟内达到100次则加入黑名单
                if (times >= MAX_TIMES) {
                    Calendar calendar = Calendar.getInstance();
                    Date creatTime = calendar.getTime();
                    Blacklist newBlacklist = new Blacklist();
                    newBlacklist.setIp(ip);
                    newBlacklist.setCreatTime(creatTime);
                    this.blackListDao.insert(newBlacklist);
                    //黑名单页面
                    modelAndView.setViewName("/errorpage/error.html");
                }
                //如果此ip还没访问过此url或者没达到访问上限，则插入缓存
                this.cacheUtil.put(ip, url, 60);
            }
        } else {
            //黑名单页面
            modelAndView.setViewName("/errorpage/error.html");
        }
    }

    //在整个请求结束之后被调用
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    /**
     * 获取用户真实IP
     *
     * @param request
     */
    public String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x - forwarded - for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {

            ip = request.getHeader("Proxy - Client - IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL - Proxy - Client - IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 解析请求路径
     *
     * @param request
     * @return
     */
    public String resolveRequestPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String requestPath = requestUri.substring(contextPath.length());

        HashMap pathVariables = (HashMap) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null && !pathVariables.isEmpty()) {
            for (Map.Entry entry : (Set<Map.Entry>) pathVariables.entrySet()) {
                try {
                    String varValue = URLEncoder.encode((String) entry.getValue(), "UTF-8");
                    String varName = "{" + entry.getKey() + "}";
                    requestPath = requestPath.replaceFirst(varValue, varName);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return requestPath;
    }
}
