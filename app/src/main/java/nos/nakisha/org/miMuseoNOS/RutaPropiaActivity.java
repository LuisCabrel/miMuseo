package nos.nakisha.org.miMuseoNOS;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

public class RutaPropiaActivity extends AppCompatActivity {
    //NOTA urlCuadroPasado es más bien nombreCuadroPasado

    //vbles web
    public String miurl="http://museocarandnuria.es/cuadro1.html";
    public WebView navegador;

    //vble SQLite
    public BBDD_Helper manejadorBBDD;

    //rssi
    public BeaconManager manejadorDeBalizas;
    public BeaconRegion region;

    //diccionarios
    public HashMap<Integer,Double> mapX;
    public HashMap<Integer,Double> mapY;
    public HashMap<Integer,Double> mapZ;
    public HashMap<String,Double> mapXcuadro;
    public HashMap<String,Double> mapYcuadro;
    public HashMap<String,Double> mapZcuadro;

    public HashMap<Integer,Integer> diccionarioTagSala;
    public HashMap<String,Integer> diccionarioCuadroSala;
    public HashMap<String,String> diccionarioCuadroUrl;
    public HashMap<Integer,String> listaSalaCuadros;
    public HashMap<Integer,Double> diccionarioSalaAngulo;
    public HashMap<Integer,Double> diccionarioSalaOrigenXCoords;
    public HashMap<Integer,Double> diccionarioSalaOrigenYCoords;
    public int salaPresente;
    //public TextView salaTxt;
    public int[] candidatoSala;

    public int[] salaEncontrada;
    public double[] anguloEncontrado;
    public double[] posXencontrada;
    public double[] posYencontrada;
    public double[] posZencontrada;
    public double[] pem;
    public double distanciaAlCuadro;

    private double[] deltaPos;
    private int[] idBalizasEncontradas;
    public int[] rssiEncontrado;
    public float sigma;
    public float p;
    public float norteMuseo;

    //matrices Filtro Kalman:
    public int RSS0;
    public int numDimensionesModelo;
    public double[][] Phi;
    public double[][] Q;
    public double[][] Pkk;
    public double[][] Pkk1;
    public double[] xkk;
    public double[] xkk1;
    public double miVarPos;
    public int deltaT;

    public int numBalizasEncontradas;
    public boolean banderaPos;
    public boolean banderaHilo;
    public boolean banderaTresBalizas;
    public boolean banderaFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_propia);

        manejadorBBDD = new BBDD_Helper(this);
        navegador=(WebView)findViewById(R.id.webkitRutaPropia);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        miurl="http://museocarandnuria.es/";
        if(depie()){
            navegador.getSettings().setJavaScriptEnabled(true);
            navegador.setWebChromeClient(new WebChromeClient());
            navegador.setWebViewClient(new WebViewClient());
            navegador.loadUrl(miurl);
        }
        else{
            //CAMBIO MAÑANERO
            navegador.getSettings().setJavaScriptEnabled(true);
            navegador.setWebChromeClient(new WebChromeClient());
            navegador.setWebViewClient(new WebViewClient());
            navegador.loadUrl(miurl);
        }

        banderaFAB=false;
        final FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fabRP);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                banderaFAB=true;
                //Toast.makeText(RutaPropiaActivity.this, "¡Pon un método!", Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(RutaPropiaActivity.this, "Información del cuadro más cercano", Toast.LENGTH_LONG).show();

        //////////////////////////filtro kalman onCreate//////////
        salaEncontrada=new int[12];
        anguloEncontrado=new double[12];
        posXencontrada=new double[12];
        posYencontrada=new double[12];
        posZencontrada=new double[12];

        deltaPos=new double[3];
        pem=new double[3];
        pem[0]=0d;
        pem[1]=0d;
        pem[2]=100d;
        rssiEncontrado=new int[12];
        idBalizasEncontradas=new int[12];
        distanciaAlCuadro=0;
        banderaPos=true;
        banderaHilo=true;
        numBalizasEncontradas=0;
        banderaTresBalizas=false;

        mapX=new HashMap<Integer, Double>();
        mapY=new HashMap<Integer, Double>();
        mapZ=new HashMap<Integer, Double>();
        mapXcuadro=new HashMap<String, Double>();
        mapYcuadro=new HashMap<String, Double>();
        mapZcuadro=new HashMap<String, Double>();

        diccionarioTagSala=new HashMap<Integer, Integer>();
        diccionarioCuadroSala=new HashMap<String, Integer>();
        listaSalaCuadros=new HashMap<Integer,String>();
        diccionarioCuadroUrl=new HashMap<String, String>();
        diccionarioSalaAngulo=new HashMap<Integer, Double>();
        diccionarioSalaOrigenXCoords=new HashMap<Integer,Double>();
        diccionarioSalaOrigenYCoords=new HashMap<Integer,Double>();
        candidatoSala=new int[3];//no confundas con salaEncontrada que tiene 4 posiciones
        salaPresente=20;
        ///////salaTxt=(TextView)findViewById(R.id.); NO ESTÁ!!!

        diccionarioSalas();
        verTodasBalizas();//diccionario de los mapX, mapY y mapZ

        /*Phi= new double[9][9];
        Q= new double[9][9];
        Rmatriz=new double[3][3];
        Pkk=new double[9][9];
        Pkk1=new double[9][9];
        H=new double[3][9];
        K=new double[9][3];
        xkk=new  double[9];
        xkk1=new double[9];*/
        miVarPos=10;

        matricesPreferencias(); //inicializo las matrices del Filtro de Kalman
        //inicializacionOtrasMatrices();

        manejadorDeBalizas = new BeaconManager(this);
        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        manejadorDeBalizas.setForegroundScanPeriod((long)deltaT,0);
        manejadorDeBalizas.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> list) {

                //IMPORTANTE: ahora supongo un num. max de balizas a encontrar: 12 en concreto
                if (!list.isEmpty()) {

                    if(list.size()==3){
                        Beacon baliza;
                        int i=0;
                        if(banderaPos) {
                            for (i = 0; i < 3; i++) {
                                if(i==0){
                                    numBalizasEncontradas=0;
                                }
                                baliza = list.get(i);
                                try {
                                    if (baliza.getMajor() == EntradaMuseo.majorMuseo) {
                                        if(numBalizasEncontradas<12) {

                                            if((mapX.get(baliza.getMinor())!=null)&&(mapY.get(baliza.getMinor())!=null)&&(mapZ.get(baliza.getMinor())!=null)){
                                                ubica(mapX.get(baliza.getMinor()), mapY.get(baliza.getMinor()), mapZ.get(baliza.getMinor()), baliza.getRssi(), baliza.getMinor(), numBalizasEncontradas);
                                                candidatoSala[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                salaEncontrada[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                anguloEncontrado[numBalizasEncontradas] = diccionarioSalaAngulo.get(salaEncontrada[numBalizasEncontradas]);

                                                numBalizasEncontradas++;
                                            }

                                        }
                                    }
                                } catch (Exception e) {
                                    //Toast.makeText(RutaPropiaActivity.this, "Balizas no registradas", Toast.LENGTH_SHORT).show();
                                }
                            }//fin for asignación
                        }//fin if(banderaPos)

                        if(banderaHilo){
                            if(numBalizasEncontradas==3) {
                                banderaPos = false;
                                banderaHilo = false;
                                banderaTresBalizas = true;
                                verificaSala();
                                Inversake hiloInversake = new Inversake();
                                hiloInversake.start();
                            }
                        }

                    }


                    if(list.size()>=4){
                        //RECUERDA BLOQUEAR EN -119!!!
                        Beacon baliza;
                        int i=0;
                        if(banderaPos) {
                            int tamanyo=list.size();
                            for (i = 0; i < tamanyo; i++) {
                                if(i==0){
                                    numBalizasEncontradas=0;
                                }
                                baliza = list.get(i);

                                try {
                                    if (baliza.getMajor() == EntradaMuseo.majorMuseo) {
                                        if(numBalizasEncontradas<12) {

                                            if((mapX.get(baliza.getMinor())!=null)&&(mapY.get(baliza.getMinor())!=null)&&(mapZ.get(baliza.getMinor())!=null)){
                                                ubica(mapX.get(baliza.getMinor()), mapY.get(baliza.getMinor()), mapZ.get(baliza.getMinor()), baliza.getRssi(), baliza.getMinor(), numBalizasEncontradas);
                                                if (i < 3) {
                                                    candidatoSala[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                }
                                                salaEncontrada[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                anguloEncontrado[numBalizasEncontradas] = diccionarioSalaAngulo.get(salaEncontrada[numBalizasEncontradas]);

                                                numBalizasEncontradas++;
                                            }

                                        }
                                    }

                                } catch (Exception e) {
                                    //Toast.makeText(RutaPropiaActivity.this, "Balizas no registradas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        if(banderaHilo){
                            if(numBalizasEncontradas>=4) {
                                banderaPos = false;
                                banderaHilo = false;
                                banderaTresBalizas = false;
                                verificaSala();
                                MinimosCuadradoskd hiloMMCkd = new MinimosCuadradoskd(numBalizasEncontradas);
                                hiloMMCkd.start();
                            }
                        }

                    }//fin if(list.size()>=4)

                }
                else{
                    //si lista vacía y no se ven balizas no envío informacion a la BD
                }
            }
        });




    }//fin del onCreate

    @Override
    protected void onResume() {
        super.onResume();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            navegador.onResume();
        }
        SystemRequirementsChecker.checkWithDefaultDialogs(RutaPropiaActivity.this);
        manejadorDeBalizas.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                manejadorDeBalizas.startRanging(region);
            }
        });

    }//fin del onResume()

    @Override
    protected void onPause() {
        super.onPause();

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
            navegador.onPause();
        }
        manejadorDeBalizas.stopRanging(region);
    }//fin del onPause()



    @Override
    public void onSaveInstanceState(Bundle estado) {
        //definimos estado
        estado.putString("miurlantesdelgiro",miurl);
        //lo guardamos
        super.onSaveInstanceState(estado);

    }
    @Override
    public  void onRestoreInstanceState(Bundle estado){

        //recuperamos información almacenada
        super.onRestoreInstanceState(estado);
        //la utilizamos:
        miurl=estado.getString("miurlantesdelgiro");
        navegador.loadUrl(miurl);

    }

    public boolean depie(){

        int orientationdevice = getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }

/////////////////////////////////FILTRO KALMAN///////////////////////////

    public void ubica(double posX,double posY,double posZ,int rssi,int idBaliza,int indice){
        synchronized (posXencontrada){posXencontrada[indice]=posX;}
        synchronized (posYencontrada){posYencontrada[indice]=posY;}
        synchronized (posZencontrada){posZencontrada[indice]=posZ;}
        synchronized (rssiEncontrado){rssiEncontrado[indice]=rssi;}
        idBalizasEncontradas[indice]=idBaliza;
    }

    public void matricesPreferencias(){

        int mov; double stdProceso; int i,j,tipoRSS0;
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(RutaPropiaActivity.this);
        mov=Integer.parseInt(preferences.getString("movimiento",""+0));
        norteMuseo=(360f-Float.parseFloat(preferences.getString("norte",""+0)));
        numDimensionesModelo=(mov+1)*2;//3
        Phi= new double[numDimensionesModelo][numDimensionesModelo];
        Q= new double[numDimensionesModelo][numDimensionesModelo];
        Pkk=new double[numDimensionesModelo][numDimensionesModelo];
        Pkk1=new double[numDimensionesModelo][numDimensionesModelo];
        xkk=new  double[numDimensionesModelo]; xkk[0]=anchoCanvas/2d;xkk[1]=altoCanvas/2d;
        xkk1=new double[numDimensionesModelo];xkk1[0]=anchoCanvas/2d;xkk1[1]=altoCanvas/2d;
        stdProceso=Double.parseDouble(preferences.getString("ruidoVAR",""+0));
        deltaT=Integer.parseInt(preferences.getString("deltaT",""+250));
        tipoRSS0=Integer.parseInt(preferences.getString("matrizR",""+7));
        switch (tipoRSS0){
            case 11:
                RSS0=-80;
                break;
            case 12:
                RSS0=-82;
                break;
            case 13:
                RSS0=-84;
                break;
            default:
                RSS0=-77-tipoRSS0;
        }

        i=0;j=0;

        if(deltaT<100){
            deltaT=100;
        }


        switch (mov){
            case 0:
                //inicializo Phi y Q
                for(i=0;i<2;i++){
                    for (j=0;j<2;j++){
                        Phi[i][j]=0;
                        Q[i][j]=0;
                        if(i==j){
                            Phi[i][j]=1;
                            double q_acc = Math.pow(stdProceso, 2);//Math.pow(0.05, 2);//varianza ruido aceleración profesor: 0.05
                            Q[i][j] = Math.pow(deltaT/1000d, 2) * q_acc;
                            //voy a dividir deltaT entre 1000 pero antes sin estar, funcionaba bien
                        }
                    }
                }
                //inicializo gv, Pkk1, xkk, xkk1
                for(i=0;i<2;i++){
                    for(j=0;j<2;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;

            case 1:
                //inicializo Phi y Q
                for(i=0;i<4;i++){
                    for (j=0;j<4;j++){
                        Phi[i][j]=0;
                        Q[i][j]=0;
                        if(i==j){
                            Phi[i][j]=1;
                            if(i<2&&j<2){
                                Q[i][j]=(double)(0.25*Math.pow((double)(deltaT/1000d),4)*Math.pow(stdProceso,2));
                            }
                            if(i>1&&j>1){
                                Q[i][j]=(double)(Math.pow((double)(deltaT/1000d),2)*Math.pow(stdProceso,2));
                            }
                        }
                        if((i+2)==j){
                            Phi[i][j]=(double)(deltaT/1000d);//*(1d);
                            Q[i][j]=(double)(0.5*Math.pow((double) (deltaT/1000d),3)*Math.pow(stdProceso,2));
                        }
                        if(i==(j+2)){
                            Q[i][j]=(double)(0.5*Math.pow((double) (deltaT/1000d),3)*Math.pow(stdProceso,2));
                        }
                    }
                }
                //inicializo Pkk, Pkk1, xkk, xkk1
                for(i=0;i<4;i++){
                    for(j=0;j<4;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            if(i<2){
                                Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);
                            }
                            if(1<i&&i<4){
                                Pkk1[i][j]=1;
                            }
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;
            default:
                for(i=0;i<6;i++){
                    for (j=0;j<6;j++){
                        Phi[i][j]=0;
                        Q[i][j]=0;

                        if(i==j){
                            Phi[i][j]=1;
                            if(i<2){     // 0 1
                                Q[i][j]=Math.exp((double)(-8));//-8
                            }
                            if(1<i&&i<4){// 2 3
                                Q[i][j]=Math.exp((double)(-6));//-6
                            }
                            if(3<i){     // 4 5
                                Q[i][j]=Math.exp((double)(-4));//-4
                            }
                        }
                        if((i+2)==j){
                            Phi[i][j]=(double)(deltaT/1000d);//*(1d);
                        }
                        if((i+4)==j){
                            Phi[i][j]=Math.pow((double)(deltaT/1000d),2)*0.5;
                        }
                    }
                }
                //inicializo Pkk, Pkk1, xkk, xkk1
                for(i=0;i<6;i++){
                    for(j=0;j<6;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            if(i<2){
                                Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);
                            }
                            if(1<i&&i<4){
                                Pkk1[i][j]=1;
                            }
                            if(i>3){
                                Pkk1[i][j]=1;
                            }
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;
        }//fin switch mov


    }

    public void verTodasBalizas(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo };

        Cursor cursor = db.query(
                Estructura_BD.Balizas.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursor.moveToFirst();

        if(cursor.getCount()==0){
            //no hay balizas alacenadas
        }else{
            int numFilas=0;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO

                mapX.put(cursor.getInt(0),cursor.getDouble(1));
                mapY.put(cursor.getInt(0),cursor.getDouble(2));
                mapZ.put(cursor.getInt(0),(double)(-1)*cursor.getDouble(3));

                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();

    }

    public void buscadorDeCuadrosDeLaSala(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_6,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_7,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo,""+salaPresente };

        Cursor cursorCuadrosSala = db.query(
                Estructura_BD.Cuadros.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorCuadrosSala.moveToFirst();

        if(cursorCuadrosSala.getCount()==0){
            //no hay cuadros almacenados
            Toast.makeText(this, "ESTA SALA NO TIENE CUADROS", Toast.LENGTH_SHORT).show();
        }else{
            //EN LECTURA NO ASIGNO
            String nombre,url;int numFilas=0;
            try{
                mapXcuadro.clear();
                mapYcuadro.clear();
                mapYcuadro.clear();
                diccionarioCuadroSala.clear();
                listaSalaCuadros.clear();
                diccionarioCuadroUrl.clear();
            }catch (Exception e){
                Toast.makeText(RutaPropiaActivity.this, "No se puede hacer el clear", Toast.LENGTH_SHORT).show();
            }
            try{
                Toast.makeText(RutaPropiaActivity.this, "Hay "+cursorCuadrosSala.getCount()+" cuadros en la sala", Toast.LENGTH_SHORT).show();
                for(numFilas=0;numFilas<cursorCuadrosSala.getCount();numFilas++){
                    nombre=cursorCuadrosSala.getString(4);
                    url=cursorCuadrosSala.getString(5);
                    mapXcuadro.put(nombre,cursorCuadrosSala.getDouble(0));
                    mapYcuadro.put(nombre,cursorCuadrosSala.getDouble(1));
                    mapZcuadro.put(nombre,(double)(-1)*cursorCuadrosSala.getDouble(2));
                    diccionarioCuadroSala.put(nombre,cursorCuadrosSala.getInt(3));
                    listaSalaCuadros.put(numFilas,nombre);
                    diccionarioCuadroUrl.put(nombre,url);
                    cursorCuadrosSala.moveToNext();
                }
            }catch (Exception e){
                Toast.makeText(this, "No se pueden meter cuadros", Toast.LENGTH_SHORT).show();
            }
        }
        cursorCuadrosSala.close();
    }

    public void diccionarioSalas(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_6,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_7,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_4
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo};

        Cursor cursorSalas = db.query(
                Estructura_BD.Parametros.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorSalas.moveToFirst();
        if(cursorSalas.getCount()==0){
            Toast.makeText(RutaPropiaActivity.this,"Sin salas que incluir al diccionario",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            int numFilas=0;
            int sala;
            double ocx,ocy;
            double ang;
            for(numFilas=0;numFilas<cursorSalas.getCount();numFilas++){
                sala=cursorSalas.getInt(0);
                ocx=cursorSalas.getDouble(1);
                ocy=cursorSalas.getDouble(2);
                ang=360d-cursorSalas.getDouble(3);
                diccionarioBalizas(sala);
                diccionarioSalaOrigenXCoords.put(sala,ocx);
                diccionarioSalaOrigenYCoords.put(sala,ocy);
                diccionarioSalaAngulo.put(sala,ang);
                cursorSalas.moveToNext();
            }
        }
        cursorSalas.close();

    }

    public void diccionarioBalizas(int sala){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+ Estructura_BD.Balizas.NOMBRE_COLUMNA_6+" = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo,""+sala };

        Cursor cursorBalizas = db.query(
                Estructura_BD.Balizas.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorBalizas.moveToFirst();

        if(cursorBalizas.getCount()==0){
            //no hay balizas alacenadas
        }else{
            int numFilas=0;
            int idBaliza;
            for(numFilas=0;numFilas<cursorBalizas.getCount();numFilas++){
                idBaliza=cursorBalizas.getInt(0);
                diccionarioTagSala.put(idBaliza,sala);
                //Toast.makeText(this, "baliza "+idBaliza+" en sala "+sala, Toast.LENGTH_SHORT).show();
                cursorBalizas.moveToNext();
            }
            numFilas=0;
        }
        cursorBalizas.close();
    }


    public void leerParamentros(int sala){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.Parametros._ID,
                //Estructura_BD.Parametros.NOMBRE_COLUMNA_1,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_2,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_3
        };

// Filter results WHERE "title" = 'My Title'
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo,""+sala};

        Cursor cursor = db.query(
                Estructura_BD.Parametros.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursor.moveToFirst();
        if(cursor.getCount()==0){
            Toast.makeText(RutaPropiaActivity.this,"Sin parametros",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            anchoCanvas= cursor.getInt(0);
            altoCanvas= Integer.parseInt(cursor.getString(1));
        }
        cursor.close();

    }

    public void leerSigmaYP(int sala){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.SigmaTable._ID,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1,
                //Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " = ? AND "+ Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4 +" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo,""+sala};

        Cursor cursor = db.query(
                Estructura_BD.SigmaTable.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursor.moveToFirst();
        if(cursor.getCount()==0){
            Toast.makeText(RutaPropiaActivity.this,"sin sigma ni p",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            sigma= cursor.getFloat(0);
            p= cursor.getFloat(1);
        }
        cursor.close();

    }

    public void verificaSala(){
        int mismaSala, nuevaSala,j;
        mismaSala=0;nuevaSala=0;j=0;
        for(j=0;j<3;j++){
            if(salaPresente==candidatoSala[j]){
                mismaSala++;
            }else {
                nuevaSala++;
            }
        }
        if(mismaSala>=nuevaSala){//si son 3 distintos nos quedamos aquí
            //nada-->caso(***)
        }else{
            if(candidatoSala[0]==candidatoSala[1]){
                cambiarASala(candidatoSala[0]);
            }else{
                if(candidatoSala[0]==candidatoSala[2]){
                    cambiarASala(candidatoSala[0]);
                }else{
                    if(candidatoSala[1]==candidatoSala[2]){
                        cambiarASala(candidatoSala[1]);
                    }else{
                        //ninguno igual--> caso(***)
                    }
                }
            }
        }
    }

    public void cambiarASala(int sala){
        Toast.makeText(RutaPropiaActivity.this, " Usted se encuentra en la sala "+sala, Toast.LENGTH_SHORT).show();
        salaPresente=sala;
        //salaTxt.setText("Sala nº: "+sala);
        leerParamentros(sala);
        leerSigmaYP(sala);
        pem[0]=(double) anchoCanvas/2;
        pem[1]=(double) altoCanvas/2;
        pem[2]=100d;
        buscadorDeCuadrosDeLaSala();

    }

    class Inversake extends Thread{
        public Inversake(){}


        @Override public void run(){

            double[][] H = new double[3][numDimensionesModelo];
            double[][] Rmatriz = new double[3][3];
            double[][] K = new double[numDimensionesModelo][3];
            double ox3,oy3;
            double[][]PB=new double[3][3];
            double[] re=new double[3];
            double[] r=new double[3];
            double[][] A3=new double[3][3];
            double[][] A3inv=new double[3][3];
            double[] B3=new double[3];
            double[] distRmat=new double[3];
            double[][]H_Pkk1_Ht_R=new double[3][3];
            double[][]H_Pkk1_Ht_R_INV=new double[3][3];
            double[] restoPos=new double[3];
            double[][] eye=new double[numDimensionesModelo][numDimensionesModelo];
            int i=0;int j=0;
            boolean recalcularPos=true;
            int cotaRecalcularPos=0;
            boolean hayActuacionKalman=true;

            while (recalcularPos){
                recalcularPos=false;
                cotaRecalcularPos++;

                for(i=0;i<3;i++){
                    if(salaEncontrada[i]==salaPresente) {
                        PB[i][0] = posXencontrada[i]/100d;
                        PB[i][1] = posYencontrada[i]/100d;
                        PB[i][2] = posZencontrada[i];
                        re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2) + Math.pow(pem[2] - PB[i][2], 2));
                        //re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2));
                        A3[i][0] = (PB[i][0] - pem[0]) / re[i];
                        A3[i][1] = (PB[i][1] - pem[1]) / re[i];
                        A3[i][2] = (PB[i][2] - pem[2]) / re[i];
                        //convierto distancia en m a cm: (*100) NO
                        r[i] = Math.pow(10, ((RSS0 - rssiEncontrado[i]) / (10 * p)));// * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
                        B3[i] = r[i] - re[i];
                    }else {//otra sala
                        ox3=(double)(Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));
                        oy3=(double)(Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));

                        PB[i][0]=(double)( Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+ox3);
                        PB[i][0]=PB[i][0]/100d; //en metros
                        PB[i][1]=(double)( Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+oy3);
                        PB[i][1]=PB[i][1]/100d; //en metros
                        PB[i][2]=posZencontrada[i];
                        re[i]=Math.sqrt(Math.pow(pem[0]-PB[i][0],2)+Math.pow(pem[1]-PB[i][1],2)+Math.pow(pem[2]-PB[i][2],2));
                        //re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2));
                        A3[i][0]=(PB[i][0]-pem[0])/re[i];
                        A3[i][1]=(PB[i][1]-pem[1])/re[i];
                        A3[i][2]=(PB[i][2]-pem[2])/re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));//*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B3[i]=r[i]-re[i];
                    }
                }
                i=0;

                RealMatrix a = MatrixUtils.createRealMatrix(A3);

                try {

                    RealMatrix aInverse = new LUDecomposition(a).getSolver().getInverse();
                    //RealMatrix aInverse = new LUDecomposition((a.transpose()).multiply(a)).getSolver().getInverse();
                    //RealMatrix atainvat= aInverse.multiply(a.transpose());

                    for (i=0;i<2;i++){
                        for (j=0;j<2;j++){
                            A3inv[i][j]=aInverse.getEntry(i,j);
                            //A3inv[i][j]=atainvat.getEntry(i,j);
                        }
                    }
                    i=0;j=0;
                    for (j=0;j<2;j++){
                        deltaPos[j]=0;
                        for(i=0;i<2;i++){
                            deltaPos[j]=deltaPos[j]+ A3inv[j][i]*B3[i];
                        }
                        pem[j]=pem[j]-deltaPos[j];

                        //poner aqui los pasos de cuando hay nuevo valor generado
                        //Update Kalman
                        //hayActualizacionKalman=true;

                    }
                    if(deltaPos[0]>0.01){
                        recalcularPos=true;
                    }
                    if(deltaPos[1]>0.01){
                        recalcularPos=true;
                    }


                }catch (Exception e){
                    //poner aquí los pasos de cuando no hay un nuevo valor generado
                    //hayActualizacionKalman=false;
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }

            }//fin del while de TRILATERACIÓN

            if(true/*hayActualizacionKalman*/){

                //defino la nueva matriz H de Kalman integrado
                for(i=0;i<3;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        if(j<2) {
                            H[i][j] = (-1d)*(PB[i][j] - xkk1[j]) / ( Math.sqrt( Math.pow((PB[i][0] - xkk1[0]), 2) + Math.pow((PB[i][1] - xkk1[1]), 2) ));
                        }else{
                            H[i][j] =0;
                        }
                    }
                }
                //defino matriz R
                //R en kalman integrado NO depende de sigma de la posición que es 2.6*sigma de los rangos,
                //sino solo sigma de rangos, por lo que elimino el 2.6 que le precede
                //double acumulaDistRmat=0;
                double cteSigmaRango=sigma*Math.log(10)/(10*p);
                double minSigmaRango=2;//200;
                for(i=0;i<3;i++){
                    distRmat[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));
                }
                //acumulaDistRmat=(double)(acumulaDistRmat)/numBalizasHilo;
                double[] sigmaRangos=new double[3];//acumulaDistRmat*cteSigmaRango + minSigmaRango ;
                for(i=0;i<3;i++){
                    sigmaRangos[i]=distRmat[i]*cteSigmaRango + minSigmaRango;
                }
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        if(i==j){
                            Rmatriz[i][j]=Math.pow(sigmaRangos[i],2);
                        }else{
                            Rmatriz[i][j]=0;
                        }
                    }
                }
                //inicializo K por si acaso
                for(i=0;i<numDimensionesModelo;i++){
                    for(j=0;j<3;j++){
                        K[i][j]=0;
                    }
                }

                RealMatrix h = MatrixUtils.createRealMatrix(H);
                RealMatrix pkk1 = MatrixUtils.createRealMatrix(Pkk1);
                RealMatrix ht = h.transpose();
                RealMatrix hpkk1ht=((h.multiply(pkk1)).multiply(ht));
                RealMatrix pkk1ht=pkk1.multiply(ht);
                //FROZEN
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        H_Pkk1_Ht_R[i][j]=hpkk1ht.getEntry(i,j)+Rmatriz[i][j];
                    }
                }

                RealMatrix hpkk1htr = MatrixUtils.createRealMatrix(H_Pkk1_Ht_R);

                try{
                    RealMatrix hpkk1htrInverse = new LUDecomposition(hpkk1htr).getSolver().getInverse();

                    for (i=0;i<3;i++){
                        for (j=0;j<3;j++){
                            H_Pkk1_Ht_R_INV[i][j]=hpkk1htrInverse.getEntry(i,j);
                        }
                    }

                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<3;j++){
                            K[i][j]=(pkk1ht.multiply(hpkk1htrInverse)).getEntry(i,j);
                        }
                    }

                    RealMatrix k=MatrixUtils.createRealMatrix(K);

                    for(i=0;i<3;i++){
                        restoPos[i]=0;
                    }
                    for(i=0;i<3;i++){
                        //rangoNuevo-rango0
                        restoPos[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)))-(Math.sqrt(Math.pow((PB[i][0] - xkk1[0]), 2) + Math.pow((PB[i][1] - xkk1[1]), 2) ));
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=0;
                        for(j=0;j<3;j++){
                            xkk[i]=xkk[i]+K[i][j]*(restoPos[j]);
                        }
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=xkk[i]+xkk1[i];//valor estimado actualizado
                    }

                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            eye[i][j]=0;
                            if(i==j){
                                eye[i][j]=1;
                            }
                        }
                    }
                    RealMatrix eYe=MatrixUtils.createRealMatrix(eye);
                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            Pkk[i][j]=(eYe.subtract(k.multiply(h))).multiply(pkk1).getEntry(i,j);
                        }
                    }


                }catch (Exception e){
                    //si no saliera, hago el else
                    for (i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            xkk[i]=xkk1[i];
                            Pkk[i][j]=Pkk1[i][j];
                        }
                    }
                    //MATRIZ SINGULAR

                }

            }


            //PREDICCIÓN
            for (i=0;i<numDimensionesModelo;i++){
                xkk1[i]=0;
                for(j=0;j<numDimensionesModelo;j++){
                    xkk1[i]=xkk1[i]+Phi[i][j]*xkk[j];
                }

            }
            RealMatrix phi = MatrixUtils.createRealMatrix(Phi);
            RealMatrix pkk = MatrixUtils.createRealMatrix(Pkk);
            RealMatrix phipkk = phi.multiply(pkk);
            RealMatrix phit=phi.transpose();
            RealMatrix phipkkphit=phipkk.multiply(phit);

            for (i=0;i<numDimensionesModelo;i++){
                for(j=0;j<numDimensionesModelo;j++){
                    Pkk1[i][j]=phipkkphit.getEntry(i,j)+Q[i][j];
                }
            }

            //DISTANCIA AL CUADRO

            runOnUiThread(new Runnable(){
                @Override public void run(){
                    double miDistx,miDisty,miDistz,distAuxiliar;
                    int contador=0, contadorAuxiliar=0;
                    String nombreCuadroPasado;
                    distAuxiliar=1000000d;
                    try{
                        for(contador=0;contador<listaSalaCuadros.size();contador++){
                            nombreCuadroPasado=listaSalaCuadros.get(contador);
                            //solo se consideran los cuadros de la sala
                            distanciaAlCuadro=Math.sqrt(Math.pow(xkk[0]*100-mapXcuadro.get(nombreCuadroPasado),2)+Math.pow(xkk[1]*100-mapYcuadro.get(nombreCuadroPasado),2) );
                            if(contador==0){distAuxiliar=distanciaAlCuadro;
                                contadorAuxiliar=contador;
                                miurl=diccionarioCuadroUrl.get(listaSalaCuadros.get(contadorAuxiliar));
                            }
                            if(distanciaAlCuadro<distAuxiliar){
                                distAuxiliar=distanciaAlCuadro;
                                contadorAuxiliar=contador;
                                miurl=diccionarioCuadroUrl.get(listaSalaCuadros.get(contadorAuxiliar));
                                //Toast.makeText(RutaPropiaActivity.this, "dAux", Toast.LENGTH_SHORT).show();
                            }

                        }//fin de bucle for()
                        //Tras el bucle for, decidimos que la menor distancia está asociada al cuadro de nombre=listaSalaCuadros(contadorAuxiliar)
                        //por lo que sacamos la url de este cuadro y la ponemos en el webkit
                        if(banderaFAB){
                            navegador.loadUrl(miurl);
                        }

                    }catch (Exception e){
                        Toast.makeText(RutaPropiaActivity.this, "No puedo actualizar url", Toast.LENGTH_SHORT).show();
                        miurl="http://museocarandnuria.es/";
                        if(banderaFAB){
                            navegador.loadUrl(miurl);
                        }
                    }

                    banderaFAB=false;
                    banderaPos=true;
                    banderaHilo=true;

                }
            });


        }
    }

    class MinimosCuadradoskd extends Thread{
        private int numBalizasHilo;

        public  MinimosCuadradoskd(int numBalizasHilo){
            this.numBalizasHilo=numBalizasHilo;
        }

        @Override public void run(){


            Log.d("HILO MINIMOS CUADRADOS"," ¡entro!");
            //numBalizasHilo=4;
            double[][] H=new double[numBalizasHilo][numDimensionesModelo];
            double[][] Rmatriz=new double[numBalizasHilo][numBalizasHilo];
            double[][] K=new double[numDimensionesModelo][numBalizasHilo];
            double ox4,oy4;
            double[][]PB=new double[numBalizasHilo][3];
            double[]re=new double[numBalizasHilo];
            double[] r=new double[numBalizasHilo];
            double[][] A=new double[numBalizasHilo][3];
            double[] B=new double[numBalizasHilo];
            double[] distRmat=new double[numBalizasHilo];
            double[][]transitoMMC=new double[3][numBalizasHilo];
            double[][]H_Pkk1_Ht_R=new double[numBalizasHilo][numBalizasHilo];
            double[][]H_Pkk1_Ht_R_INV=new double[numBalizasHilo][numBalizasHilo];
            double[] restoPos=new double[numBalizasHilo];
            double[][] eye=new double[numDimensionesModelo][numDimensionesModelo];
            int i=0;int j=0;
            boolean recalcularPos=true;
            int cotaRecalcularPos=0;

            //PRIMERO: TRILATEARACIÓN: PASAMOS LAS MEDIDAS A RANGOS
            while (recalcularPos){
                recalcularPos=false;
                cotaRecalcularPos++;

                for(i=0;i<numBalizasHilo;i++){
                    if(salaEncontrada[i]==salaPresente) {
                        PB[i][0] = posXencontrada[i]/100d;
                        PB[i][1] = posYencontrada[i]/100d;
                        PB[i][2] = posZencontrada[i]/100d;
                        re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2) + Math.pow(pem[2] - PB[i][2], 2));
                        A[i][0] = (PB[i][0] - pem[0]) / re[i];
                        A[i][1] = (PB[i][1] - pem[1]) / re[i];
                        A[i][2] = (PB[i][2] - pem[2]) / re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i] = Math.pow(10, ((RSS0 - rssiEncontrado[i]) / (10 * p)));// * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
                        B[i] = r[i] - re[i];
                    }else {//hay que corregir la posición de la baliza
                        for(j=0;j<numBalizasHilo;j++){
                            Log.e("FALLO OTRA SALA","minor "+idBalizasEncontradas[j]+" "+rssiEncontrado[j]);
                        }
                        ox4=(double)(Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));
                        oy4=(double)(Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));

                        PB[i][0]=(double)( Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+ox4);
                        PB[i][0]=PB[i][0]/100d;
                        PB[i][1]=(double)( Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+oy4);
                        PB[i][1]=PB[i][1]/100d;
                        PB[i][2]=posZencontrada[i];
                        PB[i][2]=PB[i][2]/100d;

                        re[i]=Math.sqrt(Math.pow(pem[0]-PB[i][0],2)+Math.pow(pem[1]-PB[i][1],2)+Math.pow(pem[2]-PB[i][2],2));
                        A[i][0]=(PB[i][0]-pem[0])/re[i];
                        A[i][1]=(PB[i][1]-pem[1])/re[i];
                        A[i][2]=(PB[i][2]-pem[2])/re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));//*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B[i]=r[i]-re[i];
                    }

                }
                i=0;

                RealMatrix a = MatrixUtils.createRealMatrix(A);
                RealMatrix aTa=(a.transpose()).multiply(a);


                try {

                    RealMatrix aTaInverse = new LUDecomposition(aTa).getSolver().getInverse();
                    RealMatrix transitoRM=(aTaInverse).multiply(a.transpose());

                    for (i=0;i<3;i++){
                        for (j=0;j<numBalizasHilo;j++){
                            transitoMMC[i][j]=transitoRM.getEntry(i,j);
                        }
                    }
                    i=0;j=0;
                    for (j=0;j<3;j++){
                        deltaPos[j]=0;
                        for(i=0;i<numBalizasHilo;i++){
                            deltaPos[j]=deltaPos[j]+ transitoMMC[j][i]*B[i];
                        }
                        pem[j]=pem[j]-deltaPos[j];

                        //hayActualizacionKalman=true;
                    }
                    if(deltaPos[0]>0.01){
                        recalcularPos=true;
                    }
                    if(deltaPos[1]>0.01){
                        recalcularPos=true;
                    }


                }catch (Exception e){
                    //nada para trilateración
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }

            }//fin del while


            //sigo con Kalman
            if(true/*hayActualizacionKalman*/){
                //defino la nueva matriz H de Kalman integrado que es igual a la matriz A de antes
                for(i=0;i<numBalizasHilo;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        if(j<2) {
                            H[i][j] = (-1d)*(PB[i][j] - xkk1[j]) / (Math.sqrt(Math.pow((PB[i][0] - xkk1[0]), 2) + Math.pow((PB[i][1] - xkk1[1]), 2) ));
                        }else{
                            H[i][j] =0;
                        }
                    }
                }
                //defino matriz R
                //R en kalman integrado NO depende de sigma de la posición que es 2.6*sigma de los rangos,
                //sino solo sigma de rangos, por lo que elimino el 2.6 que le precede
                //double acumulaDistRmat=0;
                double cteSigmaRango=sigma*Math.log(10)/(10*p);
                double minSigmaRango=2;//200;
                for(i=0;i<numBalizasHilo;i++){
                    distRmat[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));
                }
                //acumulaDistRmat=(double)(acumulaDistRmat)/numBalizasHilo;
                double[] sigmaRangos=new double[numBalizasHilo];//acumulaDistRmat*cteSigmaRango + minSigmaRango ;
                for(i=0;i<numBalizasHilo;i++){
                    sigmaRangos[i]=distRmat[i]*cteSigmaRango + minSigmaRango;
                }
                for(i=0;i<numBalizasHilo;i++){
                    for(j=0;j<numBalizasHilo;j++){
                        if(i==j){
                            Rmatriz[i][j]=Math.pow(sigmaRangos[i],2);
                        }else{
                            Rmatriz[i][j]=0;
                        }
                    }
                }
                //inicializo K por si acaso
                for(i=0;i<numDimensionesModelo;i++){
                    for(j=0;j<numBalizasHilo;j++){
                        K[i][j]=0;
                    }
                }


                RealMatrix h = MatrixUtils.createRealMatrix(H);
                RealMatrix pkk1 = MatrixUtils.createRealMatrix(Pkk1);
                RealMatrix ht = h.transpose();
                RealMatrix hpkk1ht=((h.multiply(pkk1)).multiply(ht));
                RealMatrix pkk1ht=pkk1.multiply(ht);

                for(i=0;i<numBalizasHilo;i++){
                    for(j=0;j<numBalizasHilo;j++){
                        H_Pkk1_Ht_R[i][j]=hpkk1ht.getEntry(i,j)+Rmatriz[i][j];
                    }
                }

                RealMatrix hpkk1htr = MatrixUtils.createRealMatrix(H_Pkk1_Ht_R);

                try{
                    Log.d("HILO MINIMOS CUADRADOS"," PREPARO INVERSA DE (H*Pkk*H' + R)");
                    RealMatrix hpkk1htrInverse = new LUDecomposition(hpkk1htr).getSolver().getInverse();
                    Log.d("HILO MINIMOS CUADRADOS","¡conseguido!");
                    for (i=0;i<numBalizasHilo;i++){
                        for (j=0;j<numBalizasHilo;j++){
                            H_Pkk1_Ht_R_INV[i][j]=hpkk1htrInverse.getEntry(i,j);
                        }
                    }



                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numBalizasHilo;j++){
                            K[i][j]=(pkk1ht.multiply(hpkk1htrInverse)).getEntry(i,j);
                        }
                    }

                    RealMatrix k=MatrixUtils.createRealMatrix(K);

                    for(i=0;i<numBalizasHilo;i++){
                        restoPos[i]=0;
                    }
                    for(i=0;i<numBalizasHilo;i++){
                        //rangoNuevo-rango0
                        restoPos[i]=Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)))-(Math.sqrt( Math.pow((PB[i][0] - xkk1[0]), 2) + Math.pow((PB[i][1] - xkk1[1]), 2)  ));
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=0;
                        for(j=0;j<numBalizasHilo;j++){
                            xkk[i]=xkk[i]+K[i][j]*(restoPos[j]);
                        }

                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=xkk[i]+xkk1[i];
                    }
                    Log.d("HILO MINIMOS CUADRADOS"," HAY ACTUALIZACIÓN Y PREDICCIÓN (la anterior)");
                    Log.d("HILO MINIMOS CUADRADOS"," Kalman Integrado: ( "+xkk[0]+", "+xkk[1]+")");
                    //actualizo Pkk
                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            eye[i][j]=0;
                            if(i==j){
                                eye[i][j]=1;
                            }
                        }
                    }
                    RealMatrix eYe=MatrixUtils.createRealMatrix(eye);
                    for(i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            Pkk[i][j]=(eYe.subtract(k.multiply(h))).multiply(pkk1).getEntry(i,j);
                        }
                    }


                }catch (Exception e){
                    //si no saliera, hago el else
                    for (i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            xkk[i]=xkk1[i];
                            Pkk[i][j]=Pkk1[i][j];
                        }
                    }
                    Log.d("HILO MINIMOS CUADRADOS"," SOLO PREDICCIÓN");
                    Log.d("HILO MINIMOS CUADRADOS"," Kalman Integrado: ( "+xkk[0]+", "+xkk[1]+")");

                    //MATRIZ SINGULAR

                }

            }


            //PREDICCIÓN
            for (i=0;i<numDimensionesModelo;i++){
                xkk1[i]=0;
                for(j=0;j<numDimensionesModelo;j++) {
                    xkk1[i] = xkk1[i] + Phi[i][j] * xkk[j];
                }
            }

            Log.d("HILO MINIMOS CUADRADOS"," PREDICCIÓN: ( "+xkk[0]+", "+xkk[1]+")");


            RealMatrix phi = MatrixUtils.createRealMatrix(Phi);
            RealMatrix pkk = MatrixUtils.createRealMatrix(Pkk);
            RealMatrix phipkk = phi.multiply(pkk);
            RealMatrix phit=phi.transpose();
            RealMatrix phipkkphit=phipkk.multiply(phit);

            for (i=0;i<numDimensionesModelo;i++){
                for(j=0;j<numDimensionesModelo;j++){
                    Pkk1[i][j]=phipkkphit.getEntry(i,j)+Q[i][j];
                }
            }

            //DISTANCIA AL CUADRO

            runOnUiThread(new Runnable(){
                @Override public void run(){
                    double miDistx,miDisty,miDistz,distAuxiliar;
                    int contador=0, contadorAuxiliar=0;
                    String nombreCuadroPasado;
                    distAuxiliar=1000000d;
                    try {
                        for (contador = 0; contador < listaSalaCuadros.size(); contador++) {
                            nombreCuadroPasado = listaSalaCuadros.get(contador);

                            distanciaAlCuadro = Math.sqrt(Math.pow(xkk[0]*100 - mapXcuadro.get(nombreCuadroPasado), 2) + Math.pow(xkk[1]*100 - mapYcuadro.get(nombreCuadroPasado), 2) );
                            if(contador==0){
                                distAuxiliar=distanciaAlCuadro;
                                contadorAuxiliar = contador;
                                miurl = diccionarioCuadroUrl.get(listaSalaCuadros.get(contadorAuxiliar));
                            }
                            if (distanciaAlCuadro < distAuxiliar) {
                                distAuxiliar = distanciaAlCuadro;
                                contadorAuxiliar = contador;
                                miurl = diccionarioCuadroUrl.get(listaSalaCuadros.get(contadorAuxiliar));
                                //Toast.makeText(RutaPropiaActivity.this, "dAux", Toast.LENGTH_SHORT).show();
                            }


                        }//fin de bucle for()
                        //Tras el bucle for, decidimos que la menor distancia está asociada al cuadro de nombre=listaSalaCuadros(contadorAuxiliar)
                        //por lo que sacamos la url de este cuadro y la ponemos en el webkit
                        if(banderaFAB){
                            navegador.loadUrl(miurl);
                        }

                    }catch (Exception e){
                        Toast.makeText(RutaPropiaActivity.this, "No puedo actualizar url", Toast.LENGTH_SHORT).show();
                        miurl="http://museocarandnuria.es/";
                        if(banderaFAB){
                            navegador.loadUrl(miurl);
                        }
                    }

                    banderaFAB=false;
                    banderaPos=true;
                    banderaHilo=true;

                }
            });
        }
    }


}
