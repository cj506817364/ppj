package com.ppj.agent.server;

import com.ppj.agent.utils.WildcardMatcher;
import javassist.*;
import javassist.bytecode.AccessFlag;

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
public class ServerAgent {

    public static void premain(String args, Instrumentation instrumentation) {

        System.out.println("拦截 server");
        // 确定采集目标
        // 通配符
        //com.ppj.agent.demo.*Server&com.ppj.agent.demo.*Service
        args=args==null||args.trim().equals("")?"com.ppj.agent.demo.*Server":args;
        final WildcardMatcher matcher = new WildcardMatcher(args);

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if (className == null || loader == null) {
                    return null;
                }
                if (!matcher.matches(className.replaceAll("/", "."))) {
                    return null;
                }
                try {
                    return buildMonitorClass(className.replace("/", "."));
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, true);

        // 新方法不能重置与重定义
//        instrumentation.retransformClasses(PpjServer.class);

    }

    private static byte[] buildMonitorClass(String className) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        CtClass ctClass = pool.get(className);
        if (ctClass == null) {
            System.out.println(className + " is null");
            throw new RuntimeException("class no find: " + className);
        }
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod ctMethod : methods) {
            if (canModify(ctMethod)) {

                String methodName = ctMethod.getName();
                String newName = methodName + "$agent";
                ctMethod.setName(newName);
                CtMethod copyMethod = CtNewMethod.copy(ctMethod, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
                copyMethod.setName(methodName);
                // 原方法 改为私有方法 否则在dubbo进行二次转换出现异常
//                ctMethod.setModifiers(AccessFlag.setPrivate(ctMethod.getModifiers()));
                if (ctMethod.getReturnType().getName().equals(CtClass.voidType.getName())) {
                    copyMethod.setBody(
                            "{" +
                                    "   com.ppj.agent.server.ServerTraceInfo t = com.ppj.agent.server.ServerTraceInfo.begin($args);" +
                                    "   try {" +
                                    newName + "($$); " +
                                    "   } finally {" +
                                    "       com.ppj.agent.server.ServerTraceInfo.end(t);" +
                                    "   }" +
                                    "}");
                } else {
                    copyMethod.setBody(
                            "{" +
                                    "   com.ppj.agent.server.ServerTraceInfo t = com.ppj.agent.server.ServerTraceInfo.begin($args);" +
                                    "   try {" +
                                    "       return " + newName + "($$); " +
                                    "   } finally {" +
                                    "       com.ppj.agent.server.ServerTraceInfo.end(t);" +
                                    "   }" +
                                    "}");
                }

                ctClass.addMethod(copyMethod);
            }
        }

        return ctClass.toBytecode();
    }


    public static boolean canModify(CtMethod ctMethod) {
        int modifiers = ctMethod.getModifiers();
        boolean isAbs = Modifier.isAbstract(modifiers);
        boolean isNative = Modifier.isNative(modifiers);
        boolean isFinal = Modifier.isFinal(modifiers);
        boolean isPublic = Modifier.isPublic(modifiers);
        return !isAbs && !isNative && !isFinal && isPublic ;
    }


}
