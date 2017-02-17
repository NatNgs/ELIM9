import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by nathael on 17/02/17.
 */
public class RandomForestTest {


    @Test
    public void testLearning() {
        RandomForest l = new RandomForest();

        Map<String, Serializable> dataMap1 = new HashMap<>();
        dataMap1.put("att1", 1);
        dataMap1.put("att2", 2);
        dataMap1.put("att3", 3);
        l.feedSet("Class1", dataMap1);

        Map<String, Serializable> dataMap2 = new HashMap<>();
        dataMap2.put("att1", 4);
        dataMap2.put("att2", 5);
        dataMap2.put("att3", 6);
        l.feedSet("Class1", dataMap2);

        Map<String, Serializable> dataMap3 = new HashMap<>();
        dataMap3.put("att1", 9);
        dataMap3.put("att2", 3);
        dataMap3.put("att3", 1);
        l.feedSet("Class2", dataMap3);

        Map<String, Serializable> dataMap4 = new HashMap<>();
        dataMap4.put("att1", 3);
        dataMap4.put("att2", 2);
        dataMap4.put("att3", 1);
        l.feedSet("Class2", dataMap4);

        Map<String, Serializable> dataMap5 = new HashMap<>();
        dataMap5.put("att1", 1);
        dataMap5.put("att2", 3);
        dataMap5.put("att3", 9);
        l.feedSet("Class1", dataMap5);

        l.learn();
        assertEquals(l.trySet(dataMap1), "Class1");
        assertEquals(l.trySet(dataMap2), "Class1");
        assertEquals(l.trySet(dataMap3), "Class2");
        assertEquals(l.trySet(dataMap4), "Class2");
        assertEquals(l.trySet(dataMap5), "Class1");
    }

}
