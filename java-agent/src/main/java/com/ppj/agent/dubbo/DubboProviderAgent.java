package com.ppj.agent.dubbo;

import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.support.wrapper.MockClusterInvoker;
import com.ppj.agent.web.TraceSession;
import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:45
 * Description:
 */
public class DubboProviderAgent {

    public static void premain(String args, Instrumentation instrumentation) {

        System.out.println("拦截dubbo provider");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                // 如果是server方法
                if (!"com/alibaba/dubbo/rpc/filter/ClassLoaderFilter".equals(className)) {
                    return null;
                }

                try {
                    return buildMonitorClass(loader, className.replace("/", "."));
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
                            "   Object t = com.ppj.agent.dubbo.DubboProviderAgent.begin($args,$0);" +
                            "   try {" +
                            "       return " + newName + "($$); " +
                            "   } finally {" +
                            "       com.ppj.agent.dubbo.DubboProviderAgent.end(t);" +
                            "   }" +
                            "}");
        }
        ctClass.addMethod(copyMethod);
        return ctClass.toBytecode();
    }

    public static Object begin(Object[] args, Object invoker) {
        Invoker in = (Invoker) args[0];
        RpcInvocation rpcInvocation = (RpcInvocation) args[1];
        String traceId = rpcInvocation.getAttachment("_traceId");
        String parentId = rpcInvocation.getAttachment("_parentId");
        System.out.println("provider接收 traceId= " + traceId + " parentId= " + parentId);
        TraceSession session = new TraceSession(traceId,parentId);
        return null;
    }

    public static void end(Object object) {
        TraceSession.close();
    }


}
