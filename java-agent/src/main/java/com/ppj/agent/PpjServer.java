package com.ppj.agent;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-21 11:46
 * Description:
 */
public class PpjServer {

    public Integer sayHello(String name,String msg){
        System.out.println("hello java-agent v2.0");
        append(name,msg);
        return 0;
    }

    public String append(String name,String msg){
        return name + " " + msg;
    }

    public Object getInt(){
        return 1;
    }

}
