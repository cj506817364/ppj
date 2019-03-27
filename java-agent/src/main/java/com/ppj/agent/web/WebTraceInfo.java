package com.ppj.agent.web;

import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 15:33
 * Description:
 */
public class WebTraceInfo {

    // 区分事件
    private String traceId;
    private String eventId;

    private long begin;
    private String url;
    private Map<String,String[]> params;
    private Cookie[] cookies;
    private String handler;
    private Long useTime;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String[]> getParams() {
        return params;
    }

    public void setParams(Map<String, String[]> params) {
        this.params = params;
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public void setCookies(Cookie[] cookies) {
        this.cookies = cookies;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Long getUseTime() {
        return useTime;
    }

    public void setUseTime(Long useTime) {
        this.useTime = useTime;
    }

    @Override
    public String toString() {
        return "WebTraceInfo{" +
                "traceId='" + traceId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", begin=" + begin +
                ", url='" + url + '\'' +
                ", params=" + params +
                ", cookies=" + Arrays.toString(cookies) +
                ", handler='" + handler + '\'' +
                ", useTime=" + useTime +
                '}';
    }
}
