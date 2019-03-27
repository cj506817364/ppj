package com.ppj.agent.server;

import com.ppj.agent.demo.TraceInfo;
import com.ppj.agent.web.TraceSession;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 15:33
 * Description:
 */
public class ServerTraceInfo {

    private String traceId;
    private String eventId;

    long begin;
    Object[] args;

    public ServerTraceInfo(long begin, Object[] args) {
        this.begin = begin;
        this.args = args;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public static ServerTraceInfo begin(Object[] args) {
        ServerTraceInfo t = new ServerTraceInfo(System.nanoTime(), args);
        TraceSession currentSession = TraceSession.getCurrentSession();
        if (currentSession != null) {
            t.traceId = currentSession.getTraceId();
            t.eventId = currentSession.getParentId() + "." + currentSession.getNextEventId();
        }
        return t;
    }

    public static void end(Object param) {
        ServerTraceInfo traceInfo = (ServerTraceInfo) param;
        System.out.println(traceInfo);
    }

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

    @Override
    public String toString() {
        return "ServerTraceInfo{" +
                "traceId='" + traceId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", begin=" + begin +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
