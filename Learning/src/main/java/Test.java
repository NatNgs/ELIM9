import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by nathael on 25/01/17.
 */
public class Test {
    private static final String MAIL = "nathael.nogues@hotmail.fr";
    private static final String PASSWORD = "root42";
    private final Firebase baseRoot;

    public Test() throws InterruptedException {
        this.baseRoot = new Firebase("https://elim9-76267.firebaseio.com").getRoot();
        System.out.println("Authenticating...");

        /*baseRoot.authAnonymously(new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Test.this.onAuthenticated();
                synchronized (baseRoot) {
                    baseRoot.notify();
                }
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                System.err.println("Cannot authenticate");
                firebaseError.toException().printStackTrace();
                synchronized (baseRoot) {
                    baseRoot.notify();
                }
            }
        });*/

        synchronized (baseRoot) {
            baseRoot.wait(5000);
        }
        baseRoot.authAnonymously(null);
    }

    public static void main(String[] args) throws InterruptedException {
        new Test();
    }

    private void onAuthenticated() {
        Firebase usersRef = baseRoot.child("users");

        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("Child added:" + dataSnapshot.toString()+", "+s);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("Child changed:" + dataSnapshot.toString()+", "+s);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("Child removed:" + dataSnapshot.toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                System.out.println("Child moved:" + dataSnapshot.toString()+", "+s);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.err.println("Error: " + firebaseError.toString());
                firebaseError.toException().printStackTrace();
            }
        });
    }


    /*private void learn() throws IOException {
        List<ClassifierInstance> irisDataset = loadDataset();
        final RandomDecisionForest randomForest = new RandomDecisionForestBuilder<>(new DecisionTreeBuilder<>()
                // The default isn't desirable here because this dataset has so few attributes
                .attributeIgnoringStrategy(new IgnoreAttributesWithConstantProbability(0.2)))
                .buildPredictiveModel(irisDataset);

        AttributesMap attributes = new AttributesMap();
        attributes.put("sepal-length", 5.84);
        attributes.put("sepal-width", 3.05);
        attributes.put("petal-length", 3.76);
        attributes.put("petal-width", 1.20);
        System.out.println("Prediction: " + randomForest.predict(attributes));
        for (ClassifierInstance instance : irisDataset) {
            System.out.println("classification: " + randomForest.getClassificationByMaxProb(instance.getAttributes()));
        }
    }


    public static  List<ClassifierInstance> loadDataset() throws IOException {


        final List<ClassifierInstance> instances = new LinkedList<>();

        final BufferedReader br = new BufferedReader(new InputStreamReader((new GZIPInputStream(BenchmarkTest.class.getResourceAsStream("iris.data.gz")))));


        String[] headings = new String[]{"sepal-length", "sepal-width", "petal-length", "petal-width"};

        String line = br.readLine();
        while (line != null) {
            String[] splitLine = line.split(",");

            AttributesMap attributes = AttributesMap.newHashMap();
            for (int x = 0; x < splitLine.length - 1; x++) {
                attributes.put(headings[x], Double.valueOf((String)splitLine[x]));
            }
            instances.add(new ClassifierInstance(attributes, splitLine[splitLine.length - 1]));
            line = br.readLine();
        }

        return instances;
    }*/
}
