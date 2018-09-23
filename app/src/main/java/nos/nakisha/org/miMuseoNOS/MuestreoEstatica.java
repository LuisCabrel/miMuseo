package nos.nakisha.org.miMuseoNOS;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

/**
 * Created by nakis on 09/01/2018.
 */

public class MuestreoEstatica extends FragmentActivity {

    private FragmentTabHost tabHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.muestreo_estatica);

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        tabHost.addTab(tabHost.newTabSpec("tab_muestreo_est").setIndicator("", getResources().getDrawable(R.drawable.ic_fingerprint_black_24dp)), TabMuestreoEst.class, null);
        //tabHost.addTab(tabHost.newTabSpec("tab_mapa_est").setIndicator("", getResources().getDrawable(R.drawable.ic_explore_black_24dp)), TabMapaEst.class, null);


        tabHost.setCurrentTab(0);


    }
}
