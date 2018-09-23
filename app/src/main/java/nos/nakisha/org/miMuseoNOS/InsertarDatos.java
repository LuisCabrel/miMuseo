package nos.nakisha.org.miMuseoNOS;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.os.Bundle;

public class InsertarDatos extends FragmentActivity {

    private FragmentTabHost tabHost;
    public static int ALTURA_TAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertar_datos);

        tabHost=(FragmentTabHost)findViewById(android.R.id.tabhost);
        tabHost.setup(this,getSupportFragmentManager(),android.R.id.tabcontent);
        //tabHost.addTab(tabHost.newTabSpec("tab_parametros").setIndicator("",getResources().getDrawable(R.drawable.ic_list_black_24dp)),Tabparametros.class,null);
        tabHost.addTab(tabHost.newTabSpec("tab_balizas").setIndicator("",getResources().getDrawable(R.drawable.ic_add_location_black_24dp)),Tabbalizas.class, null);
        tabHost.addTab(tabHost.newTabSpec("tab_cuadros").setIndicator("",getResources().getDrawable(R.drawable.ic_insert_photo_black_24dp)),Tabcuadros.class, null);

        //View vistaleng = LayoutInflater.from(MainActivity.this).inflate(R.layout.apoyolengueta,null);
        //tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(vistaleng),Tab4.class, null);
        //tabHost.setCurrentTab(1);//selecciona lengüeta 2 por defecto

        tabHost.setCurrentTab(0);
        ALTURA_TAB=tabHost.getHeight();


    }




}
