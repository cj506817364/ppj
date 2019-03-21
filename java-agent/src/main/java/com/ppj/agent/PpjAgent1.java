package com.ppj.agent;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
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
public class PpjAgent1 {

    public static void premain(String args, Instrumentation instrumentation){

        PpjServer ppjServer = new PpjServer();
        ppjServer.sayHello("ppj","is handsome boy");

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                if (!"com/ppj/agent/PpjServer".equals(className)) {
                    return null;
                }
                InputStream input = PpjAgent1.class.getResourceAsStream("/PpjServer.class");
                try {
                    return IOUtils.readFully(input, -1, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        },true);

        try {
            // 清空已经装载过的类
            instrumentation.retransformClasses(PpjServer.class);
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
//        InputStream input = PpjAgent.class.getResourceAsStream("/PpjServer.class");
//        try {
//            byte[] bytes = IOUtils.readFully(input, -1, false);
//            try {
//                instrumentation.redefineClasses(new ClassDefinition(PpjServer.class,bytes));
//                ppjServer.sayHello();
//            } catch (ClassNotFoundException | UnmodifiableClassException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    }
}
