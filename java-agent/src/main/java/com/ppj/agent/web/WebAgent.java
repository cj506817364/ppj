package com.ppj.agent.web;

import javassist.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-25 13:39
 * Description:
 */
public class WebAgent {

    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("拦截 servlet");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!"javax/servlet/http/HttpServlet".equals(className)){
                    return null;
                }

                // 代理HttpServlet
                try {
                    return buildMonitorClass(loader,className.replace("/","."));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }

    private static byte[] buildMonitorClass(ClassLoader loader, String name) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(name);
        CtMethod service = ctClass.getDeclaredMethod("service",
                pool.get(new String[]{"javax.servlet.http.HttpServletRequest","javax.servlet.http.HttpServletResponse"}));
        String methodName = service.getName();
        String newName = methodName + "$agent";
        service.setName(newName);
        CtMethod copyMethod = CtNewMethod.copy(service, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
        copyMethod.setName(methodName);
        if (service.getReturnType().getName().equals(CtClass.voidType.getName())) {
            copyMethod.setBody(
                    "{" +
                    "   Object t = com.ppj.agent.web.WebAgent.begin($args);" +
                    "   try {" +
                            newName + "($$); " +
                    "   } finally {" +
                    "       com.ppj.agent.web.WebAgent.end(t);" +
                    "   }" +
                    "}");
        }
        ctClass.addMethod(copyMethod);
        return ctClass.toBytecode();
    }

    public static Object begin(Object[] args){
        HttpServletRequest request = (HttpServletRequest) args[0];
        HttpServletResponse response = (HttpServletResponse) args[1];
        WebTraceInfo traceInfo = new WebTraceInfo();
        traceInfo.setParams(request.getParameterMap());
        traceInfo.setCookies(request.getCookies());
        traceInfo.setUrl(request.getRequestURI());
        traceInfo.setBegin(System.nanoTime());

        String traceId = UUID.randomUUID().toString().replace("-", "");
        TraceSession session = new TraceSession(traceId,"0");
        traceInfo.setTraceId(traceId);
        traceInfo.setEventId(session.getParentId() + "." + session.getNextEventId());
        return traceInfo;
    }

    public static void end(Object object){
        WebTraceInfo traceInfo = (WebTraceInfo) object;
        traceInfo.setUseTime(System.nanoTime() - traceInfo.getBegin());
        System.out.println(traceInfo);
        TraceSession.close();
    }

}
