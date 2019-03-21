package com.ppj.agent;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
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
public class PpjAgent {

    public static void premain(String args, Instrumentation instrumentation){

        System.out.println("premain");

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

    }
}
