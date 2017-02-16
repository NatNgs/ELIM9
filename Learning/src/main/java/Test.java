import com.googlecode.fannj.Fann;

/**
 * Created by nathael on 25/01/17.
 */
public class Test {


    public static void main(String[] args) {
        Fann fann = new Fann( "fannFile" );
        float[] inputs = new float[]{ -1, 1 };
        float[] outputs = fann.run( inputs );
        fann.close();
    }
}
