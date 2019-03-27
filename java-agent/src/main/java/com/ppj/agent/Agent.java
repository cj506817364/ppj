package com.ppj.agent;

import com.ppj.agent.dubbo.DubboConsumerAgent;
import com.ppj.agent.dubbo.DubboProviderAgent;
import com.ppj.agent.jdbc.JdbcAgent;
import com.ppj.agent.server.ServerAgent;
import com.ppj.agent.web.WebAgent;

import java.lang.instrument.Instrumentation;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-26 20:10
 * Description:
 */
public class Agent {

    private static void premain(String args, Instrumentation instrumentation){
        WebAgent.premain(args,instrumentation);
        ServerAgent.premain(args,instrumentation);
        DubboConsumerAgent.premain(args,instrumentation);
        DubboProviderAgent.premain(args,instrumentation);
        JdbcAgent.premain(args,instrumentation);
    }
}
