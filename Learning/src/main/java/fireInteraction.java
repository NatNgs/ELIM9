import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by Michael on 17/02/2017.
 */
public class fireInteraction {

    Firebase fb = new Firebase("https://elim9-76267.firebaseio.com");

    Firebase usersRef = fb.child("users");
    Firebase resultsRef = fb.child("results");

    public fireInteraction(){

        usersRef.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot snap) {
                System.out.println(snap.toString());
            }

            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    public void pushResult(String UserID, String deviceID, String yolo){
        resultsRef.child(UserID).child(deviceID).setValue(yolo);
    }
}
