package com.ppj.test.jdbc;

import com.ppj.test.service.User;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:47
 * Description:
 */
public class JdbcAgentTest {

    public static void main(String[] args) throws SQLException {
        User user = new User();
        Connection conn = DriverManager
                .getConnection(
                        "jdbc:mysql://10.1.4.31:3307/test",
                        "zcw_db_user", "PZ4tEcNVrLhcPxUt");
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
    }
}
