package com.ppj.agent;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 15:33
 * Description:
 */
public class TraceInfo {
    long begin;
    Object[] args;

    public TraceInfo(long begin, Object[] args) {
        this.begin = begin;
        this.args = args;
    }

    public TraceInfo() {
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

    public static TraceInfo begin(Object[] args) {
        return new TraceInfo(System.nanoTime(), args);
    }

    public static void end(Object param) {
        TraceInfo traceInfo = (TraceInfo) param;
        System.out.println("执行时间(纳秒): " + (System.nanoTime() - traceInfo.getBegin()));
        System.out.println("执行参数: " + Arrays.toString(traceInfo.getArgs()));
    }

}
