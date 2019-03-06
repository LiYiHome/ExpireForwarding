import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by liyi
 */
public class ExpareForwarding {

    @Test
    public void testCode() {
        Map<Long, CopyOnWriteArraySet<String>> cslm = new ConcurrentSkipListMap<Long, CopyOnWriteArraySet<String>>();
        CopyOnWriteArraySet<String> uidSet = new CopyOnWriteArraySet<String>();
        uidSet.add("1508");
        uidSet.add("1509");
        cslm.put(1551321000L, uidSet);
        System.out.println(uidSet.contains("1508"));

    }

}
