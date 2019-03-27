package com.ppj.test.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by Tommy on 2018/3/8.
 */
public class UserServiceImpl implements UserService {

    public User getUser(String userid, String name) {
        System.out.println("获取用户信息:" + userid);
        try {
            return selectUser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User selectUser() throws Exception {
        User user = new User();
        Connection conn = DriverManager
                .getConnection(
                        "jdbc:mysql://47.100.63.227:31306/test",
                        "root", "Sy2557bln");
        PreparedStatement statment = conn
                .prepareStatement("select * from `user`");
        ResultSet r = statment.executeQuery();
        while (r.next()) {
            user.setUserid(r.getString("id"));
            user.setUserName(r.getString("name"));
            System.out.println(user);
        }
        statment.close();
        conn.close();
        return user;
    }
}
