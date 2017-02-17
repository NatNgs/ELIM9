package fr.unice.polytech.elim.elim9;

/**
 * Created by nathael on 17/02/17.
 */
public class ServerMain {
    private final RandomForest rf = new RandomForest();
    private final RestFireClient rfc = new RestFireClient();

    public ServerMain() {
        while(true) {

        }
    }


    public static void main(String[] args) {
        new ServerMain();
    }
}
