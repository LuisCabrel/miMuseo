package nos.nakisha.org.miMuseoNOS;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

/**
 * Created by nakis on 22/01/2018.
 */

public class GestionBBDD extends FragmentActivity {

    private FragmentTabHost tabHost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.muestreo_estatica);

        tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        tabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        //R.drawable.ic_list_black_24dp
        tabHost.addTab(tabHost.newTabSpec("tab_resumenbbdd").setIndicator("",getResources().getDrawable(R.drawable.ic_list_black_24dp)),Tabresumenbbdd.class, null);

        tabHost.addTab(tabHost.newTabSpec("tab_almacenamientodatos").setIndicator("",getResources().getDrawable(R.drawable.ic_swap_vertical_circle_black_24dp)),Tabalmacenamientodatos.class, null);

        tabHost.setCurrentTab(0);


    }
}
