package com.ppj.agent.demo;

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
public class PpjAgent2 {

    public static void premain(String args, Instrumentation instrumentation) throws UnmodifiableClassException {

//        PpjServer server = new PpjServer();
        final String _className = "com/ppj/agent/PpjServer";
        final String method = "sayHello";


        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

                if(!_className.equals(className)){
                    return null;
                }

                try {
                    return buildMonitorClass(_className.replace("/","."),method);
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        },true);

        // 新方法不能重置与重定义
//        instrumentation.retransformClasses(PpjServer.class);

    }

    /**
     * 1. 拷贝一个新的方法
     * 2. 修改原方法名称
     * 3. 新方法中加入监听代码
     * // public Integer sayHello(String name,String msg){
     * //   long begin = System.nanoTime();
     * //       try {
     * //           return sayHello$agent(name,msg);
     * //       } finally {
     * //           System.out.println(System.nanoTime() - begin);
     * //       }
     * //   }
     * //
     * //
     * //   public Integer sayHello$agent(String name,String msg){
     * //       System.out.println("hello java-agent v2.0");
     * //       return 0;
     * //   }
     */
    private static byte[] buildMonitorClass(String className,String methodName) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        CtClass ctClass = pool.get(className);
        if(ctClass == null){
            System.out.println(className + " is null");
            throw new RuntimeException("class no find: " + className);
        }
        CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);

//        ctMethod.insertBefore("System.out.println(this==$0);");
//        ctMethod.insertBefore("System.out.println($1);");
//        ctMethod.insertBefore("System.out.println($2);");
        ctMethod.insertBefore("System.out.println(java.util.Arrays.toString($args));");
        ctMethod.insertBefore("System.out.println(append($$));");
//        ctMethod.insertBefore("System.out.println(java.util.Arrays.toString($sig));");
//        ctMethod.insertBefore("System.out.println($type);");
//        ctMethod.insertBefore("System.out.println($class);");
//        ctMethod.insertAfter("System.out.println(Integer a1 = ($w)3);");
//        ctMethod.insertAfter("System.out.println($_ = ($r)getInt());");
//        ctMethod.insertAfter("System.out.println(return $(w) 3);");

        String newName = methodName + "$agent";
        ctMethod.setName(newName);
        CtMethod copyMethod = CtNewMethod.copy(ctMethod, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
        copyMethod.setName(methodName);
        copyMethod.setBody("{long begin = System.nanoTime();try {return "+newName+"($$); } finally {System.out.println(System.nanoTime() - begin);}}");
        ctClass.addMethod(copyMethod);
        return ctClass.toBytecode();
    }

}
