package com.ppj.agent;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.*;
import java.security.ProtectionDomain;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:45
 * Description:
 */
public class PpjAgent {

    public static void premain(String args, Instrumentation instrumentation){

        new PpjServer().sayHello();

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                if (!"com/ppj/agent/PpjServer".equals(className)) {
                    return null;
                }
                InputStream input = PpjAgent.class.getResourceAsStream("/PpjServer.class");
                try {
                    return IOUtils.readFully(input, -1, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });

        InputStream input = PpjAgent.class.getResourceAsStream("/PpjServer.class");
        try {
            byte[] bytes = IOUtils.readFully(input, -1, false);
            try {
                instrumentation.redefineClasses(new ClassDefinition(PpjServer.class,bytes));
            } catch (ClassNotFoundException | UnmodifiableClassException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
