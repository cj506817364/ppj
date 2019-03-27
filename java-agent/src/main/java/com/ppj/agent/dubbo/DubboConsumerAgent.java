package com.ppj.agent.dubbo;

import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker;
import com.ppj.agent.web.TraceSession;
import com.ppj.agent.web.WebTraceInfo;
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
 * Date: 2019-03-21 11:45
 * Description:
 */
public class DubboConsumerAgent {

    public static void premain(String args, Instrumentation instrumentation) {

        System.out.println("拦截dubbo consumer");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                // 如果是server方法
                if (!"com/alibaba/dubbo/rpc/cluster/support/wrapper/MockClusterInvoker".equals(className)) {
                    return null;
                }

                try {
                    return buildMonitorClass(loader,className.replace("/", "."));
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, true);

        // 新方法不能重置与重定义
//        instrumentation.retransformClasses(PpjServer.class);

    }

    private static byte[] buildMonitorClass(ClassLoader loader, String name) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(name);
        CtMethod service = ctClass.getDeclaredMethod("invoke");
        String methodName = service.getName();
        String newName = methodName + "$agent";
        service.setName(newName);
        CtMethod copyMethod = CtNewMethod.copy(service, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
        copyMethod.setName(methodName);
        if (!service.getReturnType().getName().equals(CtClass.voidType.getName())) {
            copyMethod.setBody(
                    "{" +
                            "   Object t = com.ppj.agent.dubbo.DubboConsumerAgent.begin($args,$0);" +
                            "   try {" +
                            "       return " + newName + "($$); " +
                            "   } finally {" +
                            "       com.ppj.agent.dubbo.DubboConsumerAgent.end(t);" +
                            "   }" +
                            "}");
        }
        ctClass.addMethod(copyMethod);
        return ctClass.toBytecode();
    }


    public static Object begin(Object[] args, Object invoker) {
        RpcInvocation invocation = (RpcInvocation) args[0];
        MockClusterInvoker mockClusterInvoker = (MockClusterInvoker) invoker;
        DubboInfo dubboInfo = new DubboInfo();
        dubboInfo.setBegin(System.nanoTime());
        dubboInfo.setParams(invocation.getArguments());
        dubboInfo.setMethodName(invocation.getMethodName());
        dubboInfo.setInterfaceName(mockClusterInvoker.getInterface().getName());
        dubboInfo.setUrl(mockClusterInvoker.getUrl().toFullString());

        TraceSession session = TraceSession.getCurrentSession();
        if(session != null){
            dubboInfo.setTraceId(session.getTraceId());
            dubboInfo.setEventId(session.getParentId() + "." + session.getNextEventId());

            invocation.setAttachment("_traceId",dubboInfo.getTraceId());
            invocation.setAttachment("_parentId",dubboInfo.getEventId());
        }

        return dubboInfo;
    }

    public static void end(Object object) {
        DubboInfo traceInfo = (DubboInfo) object;
        traceInfo.setUseTime(System.nanoTime() - traceInfo.getBegin());
        System.out.println(traceInfo);
    }


}
