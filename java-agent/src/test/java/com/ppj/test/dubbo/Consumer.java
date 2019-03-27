package com.ppj.test.dubbo;


import com.ppj.test.service.UserService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Consumer {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "dubbo-consumer.xml");
        context.start();
        UserService userService = context.getBean(UserService.class);
        while (true) {
            byte[] b = new byte[1024];
            int szie = System.in.read(b);
            String cmd = new String(b, 0, szie).trim();
            if (cmd.startsWith("send")) {
                try {
                    System.out.println(userService.getUser("1", "ppj"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}