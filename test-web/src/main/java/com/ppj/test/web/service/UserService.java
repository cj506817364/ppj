package com.ppj.test.web.service;/**
 * Created by Administrator on 2018/5/31.
 */

import com.ppj.test.web.bean.User;

/**
 * @author Tommy
 *         Created by Tommy on 2018/5/31
 **/
public interface UserService {
    User getUser(String userid, String name);
}
