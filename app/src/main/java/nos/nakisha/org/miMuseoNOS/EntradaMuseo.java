package nos.nakisha.org.miMuseoNOS;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.ar.core.ArCoreApk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_BALIZAS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_CUADROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_PARAMETROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_SIGMATABLE;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_BALIZAS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_CUADROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_PARAMETROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_SIGMATABLE;

public class EntradaMuseo extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public BBDD_Helper manejadorBBDD;

    public Vector<String> titulo;
    public Vector<String> descripcion;
    public Vector<Integer> imagOpcs;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdaptadorOpciones adaptadorOpciones;

    public BeaconManager manejadorDeBalizas;
    public BeaconRegion region;

    public static int majorMuseo;

    public static boolean unaVezEntradaMuseo,museoDetectadoEntradaMuseo;

    private ImageView mipmapLogoNavHeader;

    private boolean usarAR=false;

    public static String urlCompraTickets="http://museocarandnuria.es/error_html.html";

    static final int PICK_HELP_REQUEST = 1;
    public boolean banderaEmergencia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada_museo);

        titulo=new Vector<String>();
        descripcion=new Vector<String>();
        imagOpcs =new Vector<Integer>();
        iniciarElementosRecyclerView();

        banderaEmergencia=false;

        manejadorBBDD = new BBDD_Helper(EntradaMuseo.this);
        unaVezEntradaMuseo=false;museoDetectadoEntradaMuseo=false;

        recyclerView=(RecyclerView)findViewById(R.id.recyclerViewEntrada);
        adaptadorOpciones=new AdaptadorOpciones(this,titulo,imagOpcs,descripcion);
        recyclerView.setAdapter(adaptadorOpciones);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adaptadorOpciones.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int pos=recyclerView.getChildAdapterPosition(v);
                switch (pos){
                    case 0:
                        if(!unaVezEntradaMuseo){
                            borrarBBDD();
                            descargarParametros();
                            descargarBalizas();
                            descargarCuadros();
                            descargarSigmaYP();
                            descargarPreferencias();
                            descargarComprarTickets();
                            museoDetectadoEntradaMuseo=true;

                        }else{
                            Toast.makeText(EntradaMuseo.this, "Museo NO detectado. Vuelva a intentarlo", Toast.LENGTH_LONG).show();
                        }

                        break;
                    case 1:
                        if(museoDetectadoEntradaMuseo){
                            DialogFragment newFragment = new InfoVisitaGuiada();
                            newFragment.show(getSupportFragmentManager(), "vg");
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 2:
                        if(museoDetectadoEntradaMuseo){
                            DialogFragment newFragment = new InfoVisitaLibre();
                            newFragment.show(getSupportFragmentManager(), "vl");
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        if(museoDetectadoEntradaMuseo){
                            Intent intentoAG = new Intent(getApplicationContext(), SelectorDeMedia.class);
                            startActivity(intentoAG);
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 4:
                        if(museoDetectadoEntradaMuseo){

                            if(usarAR){
                                Intent intentoAR = new Intent(getApplicationContext(), RealidadAumentada.class);
                                startActivity(intentoAR);
                            }else{
                                Toast.makeText(EntradaMuseo.this, "Dispositivo NO compatible", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 5:
                        if(museoDetectadoEntradaMuseo){
                            Intent intentoPosicionar = new Intent(getApplicationContext(), KalmanDin.class);
                            intentoPosicionar.putExtra("desarrollador",0);
                            LoginActivity.mimajor=EntradaMuseo.majorMuseo;
                            startActivity(intentoPosicionar);
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 6:
                        if(museoDetectadoEntradaMuseo){
                            Intent intentoPedirAyuda = new Intent(getApplicationContext(), KalmanDin.class);
                            intentoPedirAyuda.putExtra("ayuda",1);
                            intentoPedirAyuda.putExtra("desarrollador",0);
                            LoginActivity.mimajor=EntradaMuseo.majorMuseo;
                            startActivityForResult(intentoPedirAyuda,PICK_HELP_REQUEST);
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                            //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
                            llamarEmergencias();
                        }
                        break;
                    case 7:
                        //comprar tickets
                        if(museoDetectadoEntradaMuseo){
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(urlCompraTickets));
                            startActivity(i);
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                        }
                        break;
                    default:
                        if(museoDetectadoEntradaMuseo){
                            lanzarPreferencias();
                        }else{
                            DialogFragment newFragment = new InfoCargarMuseo();
                            newFragment.show(getSupportFragmentManager(), "cm");
                        }
                        break;
                }

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        manejadorDeBalizas = new BeaconManager(this);
        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        unaVezEntradaMuseo=true;

        manejadorDeBalizas.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> list) {

                //IMPORTANTE: ahora supongo un num. max de balizas a encontrar: 12 en concreto
                if (!list.isEmpty()) {

                    //asi nos quedamos con el más fuerte
                    Beacon baliza=list.get(0);
                    majorMuseo=baliza.getMajor();

                    //si nos quereos quedar con la opción más repetida:
                   
                    int contadorBalizasEncontradas=0;
                    HashMap<Integer,Integer> majorDetectados,cantidadDetectadas;
                    majorDetectados=new HashMap<Integer,Integer>();
                    cantidadDetectadas=new HashMap<Integer,Integer>();

                    for(contadorBalizasEncontradas=0;contadorBalizasEncontradas<list.size();contadorBalizasEncontradas++){
                        if (contadorBalizasEncontradas==0){
                            //inicializo
                            majorDetectados.put(0,list.get(contadorBalizasEncontradas).getMajor());
                            cantidadDetectadas.put(0,1);

                        }else{
                            //ya inicializado
                            //si existe el nuevo valor encontrado, incremento la cantidad y si no, creo un nuevo índice
                           if(majorDetectados.containsValue(list.get(contadorBalizasEncontradas).getMajor())) {
                               //buscar la key del major encontrado
                               int keyCont=0;
                               for(keyCont=0;keyCont<majorDetectados.size();keyCont++){
                                   if( list.get(contadorBalizasEncontradas).getMajor() == majorDetectados.get(keyCont) ) break;
                               }
                               //sumar 1 a la cantidad anterior
                               int cantAnt=cantidadDetectadas.get(keyCont);
                               cantidadDetectadas.put(keyCont,cantAnt+1);

                           }else{
                               //inicializo a cant=1 el nuevo valor de major detectado
                               int tamanyoMajor=majorDetectados.size();
                               majorDetectados.put(tamanyoMajor,list.get(contadorBalizasEncontradas).getMajor());
                               cantidadDetectadas.put(tamanyoMajor,1);
                           }
                        }
                    }
                    //ya los tengo todos agrupados
                    //ahora decido cuál es el major de este museo
                    int frecMajor=0;
                    for(contadorBalizasEncontradas=0;contadorBalizasEncontradas<majorDetectados.size();contadorBalizasEncontradas++) {
                        if (contadorBalizasEncontradas == 0){
                            //inicializo
                            frecMajor = cantidadDetectadas.get(contadorBalizasEncontradas);
                            majorMuseo=majorDetectados.get(contadorBalizasEncontradas);
                        }else{
                            //tras inicializar
                            if(cantidadDetectadas.get(contadorBalizasEncontradas)>frecMajor){
                                frecMajor=cantidadDetectadas.get(contadorBalizasEncontradas);
                                majorMuseo=majorDetectados.get(contadorBalizasEncontradas);
                            }
                        }
                    }


                    Log.d("BALIZAS TOTALES: ",""+list.size());
                    Log.d("NUEVO MAJOR"," major: "+baliza.getMajor()+", minor: "+baliza.getMinor());


                    if(unaVezEntradaMuseo){
                        Toast.makeText(EntradaMuseo.this, "¡Museo Detectado!", Toast.LENGTH_SHORT).show();
                        unaVezEntradaMuseo=false;
                    }


                }
                else{
                    //si lista vacía y no se ven balizas no envío informacion a la BD
                }
            }
        });

        if(Build.VERSION.SDK_INT>=24)
        usarAR=disponibilidadAR();

    }//fin onCreate

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        manejadorDeBalizas.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                manejadorDeBalizas.startRanging(region);
            }
        });

        if(banderaEmergencia){
            banderaEmergencia=false;
            llamarEmergencias();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        manejadorDeBalizas.stopRanging(region);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void irWeb(View view){
        String url = "http://www.museocarandnuria.es";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_1) {
            if(museoDetectadoEntradaMuseo){
                DialogFragment newFragment = new InfoVisitaGuiada();
                newFragment.show(getSupportFragmentManager(), "vg");
            }else{
                DialogFragment newFragment = new InfoCargarMuseo();
                newFragment.show(getSupportFragmentManager(), "cm");
                //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
            }
        }
        else if (id == R.id.nav_2) {
            if(museoDetectadoEntradaMuseo){
                DialogFragment newFragment = new InfoVisitaLibre();
                newFragment.show(getSupportFragmentManager(), "vl");
            }else{
                DialogFragment newFragment = new InfoCargarMuseo();
                newFragment.show(getSupportFragmentManager(), "cm");
                //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
            }
        }else if(id == R.id.nav_3){
            if(museoDetectadoEntradaMuseo){
                Intent intentoAG = new Intent(getApplicationContext(), SelectorDeMedia.class);
                startActivity(intentoAG);
            }else{
                DialogFragment newFragment = new InfoCargarMuseo();
                newFragment.show(getSupportFragmentManager(), "cm");
                //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
            }
        } else if(id == R.id.nav_4){
            if(museoDetectadoEntradaMuseo){

                if(usarAR){
                    Intent intentoAR = new Intent(getApplicationContext(), RealidadAumentada.class);
                    startActivity(intentoAR);
                }else{
                    Toast.makeText(EntradaMuseo.this, "Dispositivo NO compatible", Toast.LENGTH_SHORT).show();
                }

            }else{
                DialogFragment newFragment = new InfoCargarMuseo();
                newFragment.show(getSupportFragmentManager(), "cm");
                //Toast.makeText(EntradaMuseo.this, "Cargue primero el contenido del museo", Toast.LENGTH_LONG).show();
            }
        }else if(id==R.id.nav_desarrollador){
            Intent intent_desarrollador=new Intent(EntradaMuseo.this, LoginActivity.class);

            mipmapLogoNavHeader=(ImageView)findViewById(R.id.imageViewHeader);

            ActivityOptionsCompat options= ActivityOptionsCompat.makeSceneTransitionAnimation(EntradaMuseo.this,
                    new Pair<View,String>( mipmapLogoNavHeader ,getString(R.string.transition_logo)));
            ActivityCompat.startActivity( EntradaMuseo.this,intent_desarrollador,options.toBundle());

        }else if(id==R.id.nav_informacionPpal){
            Intent intentInformacionPpal=new Intent(EntradaMuseo.this,InfoPrincipal.class);
            startActivity(intentInformacionPpal);
        }else if(id==R.id.nav_opcionesVisitante){
            if(museoDetectadoEntradaMuseo){
                lanzarPreferencias();
            }else{
                DialogFragment newFragment = new InfoCargarMuseo();
                newFragment.show(getSupportFragmentManager(), "cm");
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==PICK_HELP_REQUEST && resultCode==RESULT_OK){
            banderaEmergencia=true;
        }

    }


    ///////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    public void llamarEmergencias(){
        try{
            Log.d("cuadroDialogo","ANTES");
            DialogFragment newFragmentAyuda = new InfoAyuda();
            newFragmentAyuda.show(getSupportFragmentManager(), "pedirayuda");
            Log.d("cuadroDialogo","ANTES");
        }catch (Exception e){
            Log.d("cuadroDialogo","NO SE PUEDE MOSTRAR");
        }
    }

    @TargetApi(24)
    public boolean disponibilidadAR(){
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(getApplicationContext());
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    disponibilidadAR();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            return true;
            // indicator on the button.
        } else {
            // Unsupported or unknown.
            return false;
        }

    }

    public void iniciarElementosRecyclerView(){
        titulo.add(0,"Bienvenido");
        descripcion.add(0,"Cargue el contenido de este museo");
        imagOpcs.add(0,R.drawable.iconchecked);

        titulo.add(1,"Visita Guiada");
        descripcion.add(1,"Búsqueda a través de la visita guiada");
        imagOpcs.add(1,R.drawable.compass);

        titulo.add(2,"Visita Libre");
        descripcion.add(2,"Descubra las piezas más cercanas a usted");
        imagOpcs.add(2,R.drawable.pictures);

        titulo.add(3,"Audioguía");
        descripcion.add(3,"Explicación de las obras");
        imagOpcs.add(3,R.drawable.ic_headphones);

        titulo.add(4,"Realidad Aumentada");
        descripcion.add(4,"Interactúe con los contenidos ocultos");
        imagOpcs.add(4,R.drawable.ic_ar_p);

        titulo.add(5,"Mapa");
        descripcion.add(5,"Visualice su posición en el museo");
        imagOpcs.add(5,R.drawable.map);

        titulo.add(6,"Pedir Ayuda");
        descripcion.add(6,"Un empleado se acercará a su posición");
        imagOpcs.add(6,R.drawable.bell);

        titulo.add(7,"Comprar tickets");
        descripcion.add(7,"Adquirir entradas desde la web");
        imagOpcs.add(7,R.drawable.ic_invoice);

        titulo.add(8,"Configuración");
        descripcion.add(8,"Ajustar parámetros de posicionamiento");
        imagOpcs.add(8,R.drawable.settings);

    }

    protected void borrarBBDD(){
        SQLiteDatabase db=manejadorBBDD.getWritableDatabase() ;
        db.execSQL(SQL_DELETE_TABLA_PARAMETROS);
        db.execSQL(SQL_CREATE_TABLA_PARAMETROS);
        db.execSQL(SQL_DELETE_TABLA_BALIZAS);
        db.execSQL(SQL_CREATE_TABLA_BALIZAS);
        db.execSQL(SQL_DELETE_TABLA_CUADROS);
        db.execSQL(SQL_CREATE_TABLA_CUADROS);
        db.execSQL(SQL_DELETE_TABLA_SIGMATABLE);
        db.execSQL(SQL_CREATE_TABLA_SIGMATABLE);

        Toast.makeText(EntradaMuseo.this, "Cargando cuadros...", Toast.LENGTH_LONG).show();
        Toast.makeText(EntradaMuseo.this, "Listo", Toast.LENGTH_SHORT).show();
    }


    protected void descargarSigmaYP(){
        String url = "http://www.museocarandnuria.es/bajarSigma.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        String sigmaWeb;
                        int majorWeb;
                        String pWeb;
                        int salaWeb;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                sigmaWeb=respuestaJSONobjeto.getString("sigma");
                                majorWeb=respuestaJSONobjeto.getInt("major");
                                pWeb=respuestaJSONobjeto.getString("p");
                                salaWeb=respuestaJSONobjeto.getInt("sala");
                                guardarSigmaYP(sigmaWeb,""+majorWeb,pWeb,""+salaWeb);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(this).addColaPeticiones(postRequest);
    }

    protected void descargarCuadros(){
        String url = "http://www.museocarandnuria.es/bajarCuadros.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        int majorWeb;
                        String urlWeb;
                        String nombreWeb;
                        int posXWeb;
                        int posYWeb;
                        int posZWeb;
                        int salaWeb;
                        String mediaWeb;
                        String fotoWeb;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                majorWeb=respuestaJSONobjeto.getInt("major");
                                urlWeb=respuestaJSONobjeto.getString("url");
                                nombreWeb=respuestaJSONobjeto.getString("nombre");
                                posXWeb=respuestaJSONobjeto.getInt("posX");
                                posYWeb=respuestaJSONobjeto.getInt("posY");
                                posZWeb=respuestaJSONobjeto.getInt("posZ");
                                salaWeb=respuestaJSONobjeto.getInt("sala");
                                mediaWeb=respuestaJSONobjeto.getString("media");
                                fotoWeb=respuestaJSONobjeto.getString("foto");
                                guardarCuadros(""+majorWeb,urlWeb,nombreWeb,""+posXWeb,""+posYWeb,""+posZWeb,""+salaWeb,mediaWeb,fotoWeb);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(this).addColaPeticiones(postRequest);
    }

    protected void descargarBalizas(){
        String url = "http://www.museocarandnuria.es/bajarBalizas.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        int majorWeb;
                        int idEstimoteWeb;
                        int posXWeb;
                        int posYWeb;
                        int posZWeb;
                        int salaWeb;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                majorWeb=respuestaJSONobjeto.getInt("major");
                                idEstimoteWeb=respuestaJSONobjeto.getInt("idEstimote");
                                posXWeb=respuestaJSONobjeto.getInt("posX");
                                posYWeb=respuestaJSONobjeto.getInt("posY");
                                posZWeb=respuestaJSONobjeto.getInt("posZ");
                                salaWeb=respuestaJSONobjeto.getInt("sala");
                                guardarBalizas(""+majorWeb,""+idEstimoteWeb,""+posXWeb,""+posYWeb,""+posZWeb,""+salaWeb);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(EntradaMuseo.this).addColaPeticiones(postRequest);
    }


    protected void descargarParametros(){
        String url = "http://www.museocarandnuria.es/bajarParametros.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        int majorWeb;
                        int anchoWeb;
                        int largoWeb;
                        String anguloWeb;
                        int salaWeb;
                        int ocxWeb;
                        int ocyWeb;
                        String media;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                majorWeb=respuestaJSONobjeto.getInt("major");
                                anchoWeb=respuestaJSONobjeto.getInt("ancho");
                                largoWeb=respuestaJSONobjeto.getInt("largo");
                                anguloWeb=respuestaJSONobjeto.getString("angulo");
                                salaWeb=respuestaJSONobjeto.getInt("sala");
                                ocxWeb=respuestaJSONobjeto.getInt("ocx");
                                ocyWeb=respuestaJSONobjeto.getInt("ocy");
                                media=respuestaJSONobjeto.getString("media");
                                guardarParametros(""+majorWeb,""+anchoWeb,""+largoWeb,anguloWeb,""+salaWeb,""+ocxWeb,""+ocyWeb,media);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(EntradaMuseo.this).addColaPeticiones(postRequest);
    }

    protected void descargarComprarTickets(){
        String url = "http://www.museocarandnuria.es/bajarComprarTicketsAt.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                urlCompraTickets=respuestaJSONobjeto.getString("ticketsat");
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga de tickets", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };

        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(this).addColaPeticiones(postRequest);
    }

    public void guardarSigmaYP(final String sigma,final String major,final String p, final String sala){
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1, sigma);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2, major);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3, p);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4, sala);
        // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.SigmaTable.TABLE_NAME, null, cjto_valores_nuevos);
    }

    public void guardarParametros(final String major,final String ancho,final String largo, final String angCorreccion, final String sala, final String ocx, final String ocy, final String media){
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_1, major);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_2, ancho);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_3, largo);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_4, angCorreccion);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_5, sala);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_6, ocx);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_7, ocy);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_8, media);
        // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.Parametros.TABLE_NAME, null, cjto_valores_nuevos);
    }

    public void guardarBalizas(final String major,final String idEstimote,final String posX, final String posY, final String posZ, final String sala){
        //para recoger valores de la BBDD:
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_1, major);
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_2, idEstimote);
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_3, posX);
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_4, posY);
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_5, posZ);
        cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_6, sala);

        // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.Balizas.TABLE_NAME, null, cjto_valores_nuevos);
    }

    public void guardarCuadros(final String major,final String url, final String nombre, final String posX, final String posY, final String posZ, final String sala,final String media,final String foto){
        //para recoger valores de la BBDD:
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_1, major);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_2, url);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_3, nombre);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_4, posX);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_5, posY);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_6, posZ);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_7, sala);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_8, media);
        cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_9, foto);

        // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.Cuadros.TABLE_NAME, null, cjto_valores_nuevos);
    }


    public void guardarPreferencias(final String norte,final String ruido,final String dt, final String mov, final String r, final String segundos){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("norte",norte);
        editor.putString("ruidoVAR",ruido);
        editor.putString("deltaT",dt);
        editor.putString("movimiento",mov);
        editor.putString("matrizR",r);
        editor.putString("segundosGuardado",segundos);
        editor.apply();
    }

    protected void descargarPreferencias(){
        String url = "http://www.museocarandnuria.es/bajarPreferencias.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        String norte;//=respuestaJSONobjeto.getString("norte");
                        String ruido;//=respuestaJSONobjeto.getString("ruido");
                        String dt;//=respuestaJSONobjeto.getString("dt");
                        String mov;//=respuestaJSONobjeto.getString("mov");
                        String r;//=respuestaJSONobjeto.getString("r");
                        String segundos;//=respuestaJSONobjeto.getString("segundos");

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            norte=respuestaJSONobjeto.getString("norte");
                            ruido=respuestaJSONobjeto.getString("ruido");
                            dt=respuestaJSONobjeto.getString("dt");
                            mov=respuestaJSONobjeto.getString("mov");
                            r=respuestaJSONobjeto.getString("r");
                            segundos=respuestaJSONobjeto.getString("segundos");
                            guardarPreferencias(norte,ruido,dt,mov,r,segundos);


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(EntradaMuseo.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(EntradaMuseo.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+EntradaMuseo.majorMuseo );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(this).addColaPeticiones(postRequest);
    }

    public void lanzarPreferencias(){
        Intent intent_preferencias=new Intent(EntradaMuseo.this,PreferenciasVisitanteActivity.class);
        startActivity(intent_preferencias);
    }

}
