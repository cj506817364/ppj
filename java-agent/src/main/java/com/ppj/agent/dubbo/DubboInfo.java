package com.ppj.agent.dubbo;

import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 15:33
 * Description:
 */
public class DubboInfo implements Serializable {

    // 区分事件
    private String traceId;
    private String eventId;

    private String interfaceName;
    private String methodName;

    private long begin;
    private long useTime;
    private String url;
    private Object[] params;

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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "DubboInfo{" +
                "traceId='" + traceId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", interfaceName='" + interfaceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", begin=" + begin +
                ", useTime=" + useTime +
                ", url='" + url + '\'' +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
