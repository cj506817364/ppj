package com.ppj.agent.jdbc;

import com.ppj.agent.web.TraceSession;
import com.ppj.agent.web.WebTraceInfo;
import javassist.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: cj
 * Date: 2019-03-25 13:39
 * Description:
 */
public class JdbcAgent {

    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("拦截 jdbc");

        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(!"com/mysql/jdbc/NonRegisteringDriver".equals(className)){
                    return null;
                }

                // 代理HttpServlet
                try {
                    return buildMonitorClass(loader,className.replace("/","."));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }

    private static byte[] buildMonitorClass(ClassLoader loader, String name) throws NotFoundException, CannotCompileException, IOException {
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(name);
        CtMethod service = ctClass.getDeclaredMethod("connect");
        String methodName = service.getName();
        String newName = methodName + "$agent";
        service.setName(newName);
        CtMethod copyMethod = CtNewMethod.copy(service, ctClass, new ClassMap());// ClassMap 目前不清楚干嘛的
        copyMethod.setName(methodName);
        if (!service.getReturnType().getName().equals(CtClass.voidType.getName())) {
            copyMethod.setBody("{return com.ppj.agent.jdbc.JdbcAgent.proxyConnection(" + newName + "($$));}");
        }
        ctClass.addMethod(copyMethod);
        return ctClass.toBytecode();
    }

    private static Object begin(Connection connection, String sql) {
        JdbcStatistics jdbcStat = new JdbcStatistics();
        try {
            jdbcStat.jdbcUrl = connection.getMetaData().getURL();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        jdbcStat.begin = System.nanoTime();
        TraceSession session = TraceSession.getCurrentSession();
        if (session != null) {
            jdbcStat.traceId = session.getTraceId();
            jdbcStat.eventId = session.getParentId() + "." + session.getNextEventId();
        }
        jdbcStat.sql = sql;
        return jdbcStat;
    }

    private static void end(Object stat) {
        System.out.println(stat);
    }

    public static Connection proxyConnection(Connection conn) {
        return (Connection) Proxy.newProxyInstance(conn.getClass().getClassLoader(),
                new Class[]{Connection.class}, new ProxyConnection(conn));
    }

    private static PreparedStatement proxyStatement(PreparedStatement statement, Object stat) {
        return (PreparedStatement) Proxy.newProxyInstance(statement.getClass().getClassLoader(),
                new Class[]{PreparedStatement.class},new PreparedStatementHandler(statement,stat));
    }

    public static class ProxyConnection implements InvocationHandler {
        // 动态代理方法
        // 所有的connection 方法执行前都要经过该方法
        Connection target; //原来的那个链接对象

        public ProxyConnection(Connection target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isTargetMethod = "prepareStatement".equalsIgnoreCase(method.getName());
            Object stat = null;
            if (isTargetMethod) {
                // jdbc 执行事件开端
                stat = begin(target, (String) args[0]);
            }
            Object result = method.invoke(target, args);
            if (result instanceof PreparedStatement) {
                return proxyStatement((PreparedStatement) result, stat);
            }
            return result;
        }


    }
    /**
     * PreparedStatement 代理处理
     */
    public static class PreparedStatementHandler implements InvocationHandler {
        private final PreparedStatement statement;
        private final Object jdbcStat;

        public PreparedStatementHandler(PreparedStatement statement, Object jdbcStat) {
            this.statement = statement;
            this.jdbcStat = jdbcStat;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = null;
            try {
                result = method.invoke(statement, args);
            } catch (Throwable e) {
                throw e;
            } finally {
                if ("close".equals(method.getName())) {
                    end(jdbcStat);
                }
            }
            return result;
        }
    }

    // 实现 jdbc 数据采集器
    public static class JdbcStatistics implements Serializable {
        private String traceId;
        private String eventId;
        private Long useTime;
        public Long begin;// 时间戳
        // jdbc url
        public String jdbcUrl;
        // sql 语句
        public String sql;
        // 数据库名称
        public String databaseName;

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public Long getBegin() {
            return begin;
        }

        public void setBegin(Long begin) {
            this.begin = begin;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public void setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }

        public String getSql() {
            return sql;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public Long getUseTime() {
            return useTime;
        }

        public void setUseTime(Long useTime) {
            this.useTime = useTime;
        }

        @Override
        public String toString() {
            return "JdbcStatistics{" +
                    "traceId='" + traceId + '\'' +
                    ", eventId='" + eventId + '\'' +
                    ", useTime='" + useTime + '\'' +
                    ", begin=" + begin +
                    ", jdbcUrl='" + jdbcUrl + '\'' +
                    ", sql='" + sql + '\'' +
                    ", databaseName='" + databaseName + '\'' +
                    '}';
        }
    }

}
