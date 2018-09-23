package nos.nakisha.org.miMuseoNOS;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by nakis on 14/06/2018.
 */

public class PreferenciasVisitanteActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new PreferenciasVisitanteFragment()).commit();
    }
}
