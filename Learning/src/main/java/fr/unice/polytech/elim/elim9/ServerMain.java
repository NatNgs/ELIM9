package fr.unice.polytech.elim.elim9;

import com.google.gson.Gson;
import fr.unice.polytech.elim.elim9.firebasearchi.Device;
import fr.unice.polytech.elim.elim9.firebasearchi.Users;
import quickml.data.PredictionMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.lang.InterruptedException;

/**
 * Created by nathael on 17/02/17.
 */
public class ServerMain {
    /** 4 = there will be 4 datasnaphot used as test for 1 datasnapshot used as learning (80% tests, 20% learning)
     * 1 = there will be 1 datasnaphot used as test for 1 datasnapshot used as learning (50% tests, 50% learning)
     */
    private static final int ratioTestAndLearningSet = 2;
    private final RandomForest rf = new RandomForest();
    private final RestFireClient rfc = new RestFireClient();
    private Users users;

    public ServerMain() {
		rf.unfeedSet();
		users = new Users(rfc.getFromFire());

		// Learning...
		learningPhase();

		// Testing...
		testingPhase();
			
		while(true) {
			// Predicting...
			predictingPhase();
			
			try {
				Thread.sleep(20000);
			} catch(InterruptedException ignored) {
				break;
			}
		}
    }



    private void learningPhase() {
		System.out.println("\t--- LEARNING PHASE ---");
        Device.select(Device.VIEW_ONLY_FEED);

        for(int i=0; i<users.countDataSnapshot(); i++/*=1+ratioTestAndLearningSet*/) {
            Map<String, Serializable> snapshot = users.getDataSnapshot(i);

            Serializable feedClass = snapshot.get("feedClass").toString();
            snapshot.remove("feedClass");
            //System.out.println("Feeded as "+feedClass+": "+snapshot.toString());
            rf.feedSet(feedClass, snapshot);
        }

        rf.learn();
    }

    private void testingPhase() {
		System.out.println("\t--- TESTING PHASE ---");
        Device.select(Device.VIEW_ONLY_FEED);

        int goods = 0, bads = 0;
        for(int i=1; i<users.countDataSnapshot(); i++) {
            if(i%(1+ratioTestAndLearningSet) == 0)
                continue;

            Map<String, Serializable> snapshot = users.getDataSnapshot(i);

            Serializable feedClass = snapshot.get("feedClass").toString();
            snapshot.remove("feedClass");
            Serializable prediction = rf.trySet(snapshot);

            if(feedClass.equals(prediction)) {
                goods ++;
            } else {
                bads++;
            }
        }

        System.out.println("Testing phase: "+goods+" correct, "+bads+" incorrect"
                    +(goods+bads!=0?"("+(100*goods/(goods+bads))+"%)":""));
    }


    private void predictingPhase() {
		System.out.println("\t--- PREDICTING PHASE ---");
        Device.select(Device.VIEW_ONLY_LAST);

        for(int i=0; i<users.countDataSnapshot(); i++) {
            Map<String, Serializable> snapshot = users.getDataSnapshot(i);

            snapshot.remove("feedClass");
            PredictionMap prediction = rf.predictSet(snapshot);

            Map<Serializable, Serializable> value = new HashMap<>(prediction);
            value.put("appPct", 0.42);
            value.put("ramPct", 0.42);

            // TODO
            //System.out.println("Try to send:"+new Gson().toJson(value));
            for(Serializable key : value.keySet()) {
                rfc.postToFire2(users.getAddressAt(i)+"/"+key, value.get(key));
            }
        }

        rf.learn();
    }

    public static void main(String[] args) {
        new ServerMain();
    }
}
