/**
 * Created by nathael on 29/01/17.
 */
public class TestRandomForest {

    public static void main(String[] args) {
        RandomForest rf = new RandomForest();
        System.out.println("Feeding...");
        rf.feed(RandomForest.makeAttMap("Yolophone",3*60,60,48*60,30,420*1024L),true); // exemple de tel en bon état
        rf.feed(RandomForest.makeAttMap("Yolophone",60  ,60,2*60 ,45,300*1024L),false); // exemple de tel cassé
        rf.feed(RandomForest.makeAttMap("Yolophone",4*60,50,50*60,25,400*1024L),true); // exemple de tel en bon état
        rf.feed(RandomForest.makeAttMap("Yolophone",2*60,60,12*60,30,  2*1024L),false); // exemple de tel cassé

        System.out.println("Testing...");
        rf.test(RandomForest.makeAttMap("Yolophone",12    ,34,56   ,78,90*1024L)); // should be broken
        rf.test(RandomForest.makeAttMap("Yolophone",2.5*60,60,72*60,45,90*1024L)); // should be good
    }
}
;