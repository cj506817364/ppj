package com.ppj.test.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-26 21:07
 * Description:
 */
public class Bootstrap {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "dubbo-provider.xml");
        context.start();
        System.out.println("dubbo multicast 服务启动成功 ");
        System.in.read(); // press any key to exit
    }
}
