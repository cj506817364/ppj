package com.ppj.test.web;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:47
 * Description:
 */
public class WebAgentTest {

    public static void main(String[] args) {
        int port = 8088;
        Server server = new Server(port);
        WebAppContext context = new WebAppContext();
        context.setContextPath("/");//context访问路径
        context.setResourceBase(WebAgentTest.class.getResource("/webapp/").getPath());
        context.setDescriptor(WebAgentTest.class.getResource("/webapp/WEB-INF/web.xml").getPath());
        server.setHandler(context);
        try {
            server.start();
            System.out.println("启动成功 端口号: " + port);
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
