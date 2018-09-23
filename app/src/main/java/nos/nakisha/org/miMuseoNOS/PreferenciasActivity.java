package nos.nakisha.org.miMuseoNOS;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by nakis on 13/12/2017.
 */

public class PreferenciasActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new PreferenciasFragment()).commit();
    }
}


