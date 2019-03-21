package com.ppj.agent;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:45
 * Description:
 */
public class PpjAgent {

    public static void premain(String args, Instrumentation instrumentation) throws UnmodifiableClassException {

//        PpjServer server = new PpjServer();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                // 如果是server方法
                if(!className.toLowerCase().endsWith("server")){
                    return null;
                }

                try {
                    return buildMonitorClass(className.replace("/","."));
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        },true);

        // 新方法不能重置与重定义
//        instrumentation.retransformClasses(PpjServer.class);

    }
    private static byte[] buildMonitorClass(String className) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        CtClass ctClass = pool.get(className);
        if(ctClass == null){
            System.out.println(className + " is null");
            throw new RuntimeException("class no find: " + className);
        }
        CtMethod[] methods = ctClass.getMethods();
        for (CtMethod ctMethod : methods) {
            if(canModify(ctMethod)){
                String methodName = ctMethod.getName();
                String newName = methodName + "$agent";
                ctMethod.setName(newName);
                CtMethod copyMethod = CtNewMethod.copy(ctMethod, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
                copyMethod.setName(methodName);
                copyMethod.setBody(
                        "{" +
                        "   com.ppj.agent.TraceInfo t = com.ppj.agent.TraceInfo.begin($args);" +
                        "   try {" +
                        "       return "+newName+"($$); " +
                        "   } finally {" +
                        "       com.ppj.agent.TraceInfo.end(t);" +
                        "   }" +
                        "}");
                ctClass.addMethod(copyMethod);
            }
        }

        return ctClass.toBytecode();
    }



    public static boolean canModify(CtMethod ctMethod){
        int modifiers = ctMethod.getModifiers();
        boolean isAbs = Modifier.isAbstract(modifiers);
        boolean isNative = Modifier.isNative(modifiers);
        boolean isFinal = Modifier.isFinal(modifiers);
        return !isAbs && !isNative && !isFinal;
    }



}
