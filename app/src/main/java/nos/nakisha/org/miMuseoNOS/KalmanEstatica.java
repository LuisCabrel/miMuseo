package nos.nakisha.org.miMuseoNOS;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 30/01/2018.
 */

public class KalmanEstatica extends AppCompatActivity implements View.OnClickListener  {

    public static float[] mis_pxMaek;
    public static int[] mis_dpMaek;

    //vbles fichero
    public boolean banderaGrabacion;
    private static String FICHERO;
    public long incrementoMin;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    //rssi
    public BeaconManager manejadorDeBalizas;
    public BeaconRegion region;

    //vbles view:
    public TextView textViewPosXKalmanEst;
    public TextView textViewPosYKalmanEst;
    public TextView textViewPosZKalmanEst;

    public Button limpiarMapaBtn;
    public Button guardarPosiciones;
    public Button cambiarPosicion;
    public Button incrementarPosicion;

    private PizarraKalmanEst pizarraKalmanEst;

    public HashMap<Integer,Double> mapX;
    public HashMap<Integer,Double> mapY;
    public HashMap<Integer,Double> mapZ;

    public HashMap<Integer,Integer> diccionarioTagSala;
    public HashMap<Integer,Double> diccionarioSalaAngulo;
    public HashMap<Integer,Double> diccionarioSalaOrigenXCoords;
    public HashMap<Integer,Double> diccionarioSalaOrigenYCoords;
    public int salaPresente;
    public TextView salaTxt;
    public int[] candidatoSala;

    public int[] salaEncontrada;
    public double[] anguloEncontrado;
    public double[] posXencontrada;
    public double[] posYencontrada;
    public double[] posZencontrada;
    public double[] pem;
    public double[] pemTrilat;

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
    public int segundos;
    public int deltaT;

    public int numBalizasEncontradas;
    public boolean banderaPos;
    public boolean banderaHilo;
    public boolean banderaTresBalizas;
    public boolean desarrollador=false;
    public boolean banderaCambiarPosic;
    public boolean banderaIncrementarPosic;
    public int contadorIncrementarPosic;
    public boolean pedirAyuda;
    public int contadorPedirAyuda;
    public LinearLayout linearLayoutAEsconder;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_kalman_est);
        Bundle extras=getIntent().getExtras();
        if(extras.getInt("desarrollador")==1){
            desarrollador=true;
        }else{
            desarrollador=false;
        };
        linearLayoutAEsconder=(LinearLayout)findViewById(R.id.linLytAEsconder);
        if(desarrollador){
            linearLayoutAEsconder.setVisibility(View.VISIBLE);
        }else{
            linearLayoutAEsconder.setVisibility(View.GONE);
        }
        if(extras.getInt("ayuda")==1){
            pedirAyuda=true;
            contadorPedirAyuda=0;
        }else{
            pedirAyuda=false;
            contadorPedirAyuda=3;
        };

        manejadorBBDD = new BBDD_Helper(this);

        mis_pxMaek=new float[2];
        mis_dpMaek=new int[2];
        medidas();

        salaEncontrada=new int[12]; ////////////////////////////////////////////////OJO PONGO 12 PERO NO QUEDA ELEGANTE
        anguloEncontrado=new double[12];
        posXencontrada=new double[12];
        posYencontrada=new double[12];
        posZencontrada=new double[12];

        deltaPos=new double[3];
        pem=new double[3];
        pem[0]=0d;
        pem[1]=0d;
        pem[2]=100d;
        pemTrilat=new double[3];
        pemTrilat[0]=0d;
        pemTrilat[1]=0d;
        pemTrilat[2]=100d;
        rssiEncontrado=new int[12];/////////////////////OJO PONGO 12 PERO NO QUEDA ELEGANTE
        idBalizasEncontradas=new int[12];
        banderaPos=true;
        banderaHilo=true;
        numBalizasEncontradas=0;
        banderaTresBalizas=false;
        banderaCambiarPosic=false;
        banderaIncrementarPosic=false;
        contadorIncrementarPosic=0;

        mapX=new HashMap<Integer, Double>();
        mapY=new HashMap<Integer, Double>();
        mapZ=new HashMap<Integer, Double>();

        diccionarioTagSala=new HashMap<Integer, Integer>();
        diccionarioSalaAngulo=new HashMap<Integer, Double>();
        diccionarioSalaOrigenXCoords=new HashMap<Integer,Double>();
        diccionarioSalaOrigenYCoords=new HashMap<Integer,Double>();
        candidatoSala=new int[3];//no confundas con salaEncontrada que tiene 4 posiciones
        salaPresente=20;
        salaTxt=(TextView)findViewById(R.id.salaKalmanEstTxtVw);

        diccionarioSalas();

        verTodasBalizas();

        //OJO miVarPos se inicializa después de saber altoCanvas
        miVarPos=1000;//esto significa que no tengo mucha idea de dónde estoy


        textViewPosXKalmanEst=(TextView) findViewById(R.id.posXKalmanEst);
        textViewPosYKalmanEst=(TextView)findViewById(R.id.posYKalmanEst);
        textViewPosZKalmanEst=(TextView)findViewById(R.id.posZKalmanEst);

        pizarraKalmanEst=(PizarraKalmanEst) findViewById(R.id.viewPizarraKalmanEst);

        limpiarMapaBtn=(Button)findViewById(R.id.limpiaKalmanEstBtn);
        limpiarMapaBtn.setOnClickListener(this);
        incrementoMin=0;
        //FICHERO= Environment.getExternalStorageDirectory()+"/milisegundosYdato.txt";
        banderaGrabacion=false;
        guardarPosiciones=(Button)findViewById(R.id.guardarKalmanEstBtn);
        guardarPosiciones.setOnClickListener(this);
        cambiarPosicion=(Button)findViewById(R.id.cambiarPosicionKalmanEstBtn);
        cambiarPosicion.setOnClickListener(this);
        incrementarPosicion=(Button)findViewById(R.id.incrementarPosicionKalmanEstBtn);
        incrementarPosicion.setOnClickListener(this);

        matricesPreferencias(); //inicializo las matrices del Filtro de Kalman
        inicializacionOtrasMatrices();

        if(segundos!=40){//para ejecutar ésto, antes debemos llamar a matricesPreferencias() --> OK
            cambiarPosicion.setVisibility(View.GONE);
            incrementarPosicion.setVisibility(View.GONE);
        }

        manejadorDeBalizas = new BeaconManager(this);
        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        manejadorDeBalizas.setForegroundScanPeriod( (long)deltaT,0); //importante que se haya llamado antes a matricesPreferencias(), para inicializar deltaT
        manejadorDeBalizas.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> list) {

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
                                    if (baliza.getMajor() == LoginActivity.mimajor) {
                                        if(numBalizasEncontradas<12) {

                                            if((mapX.get(baliza.getMinor())!=null)&&(mapY.get(baliza.getMinor())!=null)&&(mapZ.get(baliza.getMinor())!=null)){
                                                ubica(mapX.get(baliza.getMinor()), mapY.get(baliza.getMinor()), mapZ.get(baliza.getMinor()), baliza.getRssi(), baliza.getMinor(), numBalizasEncontradas);
                                                candidatoSala[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                salaEncontrada[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                anguloEncontrado[numBalizasEncontradas] = diccionarioSalaAngulo.get(salaEncontrada[numBalizasEncontradas]);

                                                numBalizasEncontradas++;//lo dejo al final para que de fallo antes de actualizar éste
                                            }

                                        }
                                    }else{
                                        //numBalizasEncontradas--;
                                    }
                                } catch (Exception e) {
                                    //numBalizasEncontradas--;
                                    //Toast.makeText(KalmanEstatica.this, "Se detectan balizas no registradas", Toast.LENGTH_SHORT).show();
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
                                    if (baliza.getMajor() == LoginActivity.mimajor) {
                                         if(numBalizasEncontradas<12) {

                                             if((mapX.get(baliza.getMinor())!=null)&&(mapY.get(baliza.getMinor())!=null)&&(mapZ.get(baliza.getMinor())!=null)){
                                                 ubica(mapX.get(baliza.getMinor()), mapY.get(baliza.getMinor()), mapZ.get(baliza.getMinor()), baliza.getRssi(), baliza.getMinor(), numBalizasEncontradas);
                                                 if (i < 3) {
                                                     candidatoSala[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                 }
                                                 salaEncontrada[numBalizasEncontradas] = diccionarioTagSala.get(baliza.getMinor());
                                                 anguloEncontrado[numBalizasEncontradas] = diccionarioSalaAngulo.get(salaEncontrada[numBalizasEncontradas]);

                                                 numBalizasEncontradas++;//lo dejo al final para que de fallo antes de actualizar éste
                                             }

                                         }
                                    }else{
                                        //numBalizasEncontradas--;
                                    }

                                } catch (Exception e) {
                                    //numBalizasEncontradas--;
                                    //Toast.makeText(KalmanEstatica.this, "Se detectan balizas no registradas", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }
                        if(banderaHilo){
                            if(numBalizasEncontradas>=4) {
                                banderaPos = false;
                                banderaHilo = false;
                                banderaTresBalizas = false;
                                verificaSala();
                                MinimosCuadradoske hiloMMCke = new MinimosCuadradoske( numBalizasEncontradas);
                                hiloMMCke.start();
                            }
                        }

                    }//fin if(list.size()>=4)

                }
                else{
                    //si lista vacía y no se ven balizas no hago nada
                }
            }
        });


    }//fin del onCreate()


    @Override
    public void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(KalmanEstatica.this);
        manejadorDeBalizas.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                manejadorDeBalizas.startRanging(region);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        manejadorDeBalizas.stopRanging(region);
    }
    @Override
    public void onStop(){
        banderaGrabacion=false;
        super.onStop();
    }

    public void ubica(double posX,double posY,double posZ,int rssi,int idBaliza,int indice){
        synchronized (posXencontrada){posXencontrada[indice]=posX;}
        synchronized (posYencontrada){posYencontrada[indice]=posY;}
        synchronized (posZencontrada){posZencontrada[indice]=posZ;}
        synchronized (rssiEncontrado){rssiEncontrado[indice]=rssi;}
        idBalizasEncontradas[indice]=idBaliza;
    }


    public void medidas(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        mis_pxMaek[0]=metrics.heightPixels;
        mis_pxMaek[1]=metrics.widthPixels;
        if(depie()){
            mis_dpMaek[0]=Math.round(metrics.heightPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMaek[1]=Math.round(metrics.widthPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
        else{
            mis_dpMaek[0]=Math.round(metrics.heightPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMaek[1]=Math.round(metrics.widthPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        }
    }

    public boolean depie(){
        int orientationdevice = getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }

    public void inicializacionOtrasMatrices(){
        int i,j;i=0;j=0;
        //H y K se definen en el hilo Kalman integrado

    }

    public void matricesPreferencias(){

        int mov; double stdProceso; int i,j,tipoRSS0;
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(KalmanEstatica.this);
        mov=Integer.parseInt(preferences.getString("movimiento",""+0));
        numDimensionesModelo=(mov+1)*3;
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

        segundos=Integer.parseInt(preferences.getString("segundosGuardado",""+1));
        norteMuseo=(360f-Float.parseFloat(preferences.getString("norte",""+0)));

        if(deltaT<100){
            deltaT=100;
        }

        switch (segundos){
            case 0:
                segundos=10;
                break;
            case 1:
                segundos=20;
                break;
            case 2:
                segundos=30;
                break;
            default:
                segundos=40;
                break;
        }
        i=0;j=0;

        switch (mov){
            case 0:
                Toast.makeText(KalmanEstatica.this, "Caso estática", Toast.LENGTH_SHORT).show();
                //inicializo Phi y Q
                for(i=0;i<3;i++){
                    for (j=0;j<3;j++){
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
                //inicializo Pkk, Pkk1, xkk, xkk1
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            if(i<3){
                                Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);//creo que habría que elevarlo al cuadrado
                            }
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;

            case 1:
                Toast.makeText(KalmanEstatica.this, "Caso velocidad cte", Toast.LENGTH_SHORT).show();
                //inicializo Phi y Q
                for(i=0;i<6;i++){
                    for (j=0;j<6;j++){
                        Phi[i][j]=0;
                        Q[i][j]=0;
                        if(i==j){
                            //if(i<6){
                            Phi[i][j]=1;
                            if(i<3&&j<3){ // 0 1  2
                                Q[i][j]=(double)(0.25*Math.pow((double)(deltaT/1000d),4)*Math.pow(stdProceso,2));
                            }
                            if(i>2&&j>2){ // 3 4 5
                                Q[i][j]=(double)(Math.pow((double)(deltaT/1000d),2)*Math.pow(stdProceso,2));
                            }
                            //}
                        }
                        if((i+3)==j){
                            //if(i<6){
                            Phi[i][j]=(double) (deltaT/1000d);//*(1d);
                            Q[i][j]=(double)(0.5*Math.pow((double) (deltaT/1000d),3)*Math.pow(stdProceso,2));
                            //}
                        }
                        if(i==(j+3)){
                            //if(i<6){
                            Q[i][j]=(double)(0.5*Math.pow((double) (deltaT/1000d),3)*Math.pow(stdProceso,2));
                            //}
                        }
                    }
                }
                //inicializo Pkk, Pkk1, xkk, xkk1
                for(i=0;i<6;i++){
                    for(j=0;j<6;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            if(i<3){
                                Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);
                            }
                            if(2<i&&i<6){
                                Pkk1[i][j]=(double) 0.1;
                            }
                            if(i>5){
                                Pkk1[i][j]=1;
                            }
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;
            default:
                Toast.makeText(KalmanEstatica.this, "Caso aceleración cte", Toast.LENGTH_SHORT).show();
                for(i=0;i<9;i++){
                    for (j=0;j<9;j++){
                        Phi[i][j]=0;
                        Q[i][j]=0;

                        if(i==j){
                            Phi[i][j]=1;
                            if(i<3){     // 0 1 2
                                Q[i][j]=Math.exp((double)(-8));
                            }
                            if(2<i&&i<6){// 3 4 5
                                Q[i][j]=Math.exp((double)(-6));
                            }
                            if(5<i){     // 6 7 8
                                Q[i][j]=Math.exp((double)(-4));
                            }
                        }
                        if((i+3)==j){
                            Phi[i][j]=(double)(deltaT/1000d);//*(1d);
                        }
                        if((i+6)==j){
                            Phi[i][j]=Math.pow((double)(deltaT/1000d),2)*0.5;
                        }
                    }
                }
                //inicializo Pkk, Pkk1, xkk, xkk1
                for(i=0;i<9;i++){
                    for(j=0;j<9;j++){
                        Pkk1[i][j]=0;
                        Pkk[i][j]=0;
                        if(i==j){
                            if(i<3){
                                Pkk1[i][j]=(1d)*Math.pow(miVarPos,2);
                            }
                            if(2<i&&i<6){
                                Pkk1[i][j]=(double) 0.1;
                            }
                            if(i>5){
                                Pkk1[i][j]=1;
                            }
                        }
                    }
                    xkk1[i]=0;//debería iniciarlo con la primera muestra de la posición
                    xkk[i]=0;
                }
                break;
        }

        String strPhi="[ \n",strQ="[ \n",strPkk1="[ \n";
        for(i=0;i<numDimensionesModelo;i++){
            for(j=0;j<numDimensionesModelo;j++){
                strPhi=strPhi+Phi[i][j]+" ";
                strQ=strQ+Q[i][j]+" ";
                strPkk1=strPkk1+Pkk1[i][j]+" ";
            }
            strPhi=strPhi+" \n";strQ=strQ+" \n";strPkk1=strPkk1+" \n";
        }
        strPhi=strPhi+"]";strQ=strQ+"]";strPkk1=strPkk1+"]";
        Log.d("matriz Phi",strPhi);
        Log.d("matriz Q",strQ);
        Log.d("matriz Pkk1",strPkk1);

    }

    @Override
    public void onClick(View v) {
        int id=v.getId();

        if(id==R.id.limpiaKalmanEstBtn){
            PizarraKalmanEst.miCanvasKalmanEst.drawColor(0xFFFFFFFF);
            actualizaPizarra();
            Log.d("BETA",""+betaEstimada());
        }//fin de if(id==R.id.limpiaMapaEstBtn)

        if(id==R.id.guardarKalmanEstBtn){
            //Creamos el Fichero cuyo nombre contenga el momento de su generación
            Date currentTime = Calendar.getInstance().getTime();
            String currentTimeStr=""+currentTime;
            currentTimeStr=currentTimeStr.replace("+","_");
            currentTimeStr=currentTimeStr.replace("-","_");
            currentTimeStr=currentTimeStr.replace(".","_");
            currentTimeStr=currentTimeStr.replace(":","_");
            currentTimeStr=currentTimeStr.replace(" ","_");
            FICHERO = "muestrasKDesacoplado"+currentTimeStr+".txt";
            if(!banderaGrabacion){
                banderaGrabacion=true;
                contadorIncrementarPosic=1;
                HiloGrabacion hiloGrabacion=new HiloGrabacion();
                hiloGrabacion.start();
                if(segundos!=40){
                    guardarPosiciones.setVisibility(View.GONE);
                }
                if(segundos==40){
                    banderaIncrementarPosic=true;
                    guardarPosiciones.setText("PARAR");
                }

            }else{
                banderaGrabacion=false;
                if(segundos==40){
                    guardarPosiciones.setText("GUARDAR");
                }
            }
        }

        if(id==R.id.cambiarPosicionKalmanEstBtn){
            banderaCambiarPosic=true;
            Toast.makeText(this, "¡Cambio!", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.incrementarPosicionKalmanEstBtn){
            contadorIncrementarPosic++;
            banderaIncrementarPosic=true;
            Toast.makeText(this, "Posición: "+contadorIncrementarPosic, Toast.LENGTH_SHORT).show();
        }


    }//fin onClick()

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
        String[] selectionArgs = { ""+LoginActivity.mimajor };

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

    public void verBalizas(int sala){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

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
            int numFilas=0;String idBaliza;
            float xLect,yLect,xFinal,yFinal;
            float margen=24;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursor.getFloat(0);
                yLect=cursor.getFloat(1);
                idBaliza=cursor.getString(2);

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMaek[1]-2*margen)/((2* mis_pxMaek[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float)altoCanvas;
                }

                PizarraKalmanEst.miCanvasKalmanEst.drawPoint(xFinal,yFinal,PizarraKalmanEst.miPaintKalmanEstBalizas);

                if(desarrollador){
                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMaek[1]-2*margen)/((2* mis_pxMaek[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal=margen+ xLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        yFinal=margen + yLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        if((mis_pxMaek[1]-2*margen)-3*margen<xFinal){xFinal=xFinal-3*margen;}
                        if(yFinal<4*margen){yFinal=4*margen;}

                    }else{
                        //limita el largo
                        xFinal=margen + xLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                        yFinal=margen+ yLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float)altoCanvas;
                        if(anchoCanvas*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas-3*margen<xFinal){xFinal=xFinal-3*margen;}
                        if(yFinal<4*margen){yFinal=4*margen;}

                    }

                    PizarraKalmanEst.miCanvasKalmanEst.drawText(idBaliza,xFinal,yFinal,PizarraKalmanEst.avisoPaint);
                }

                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();

    }


    public void verCuadros(int sala){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

        Cursor cursor = db.query(
                Estructura_BD.Cuadros.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursor.moveToFirst();

        if(cursor.getCount()==0){
            //sin cuadros
        }else{
            int numFilas=0;
            float xLect,yLect,xFinal,yFinal;
            float margen=24;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursor.getFloat(0);
                yLect=cursor.getFloat(1);

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMaek[1]-2*margen)/((2* mis_pxMaek[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float)altoCanvas;
                }

                Drawable drawableCuadro;
                drawableCuadro=PizarraKalmanEst.sdCuadro;
                drawableCuadro.setBounds((int)xFinal-16,(int)yFinal-16,(int)xFinal+16,(int)yFinal+16);
                drawableCuadro.draw(PizarraKalmanEst.miCanvasKalmanEst);
                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();

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
        String[] selectionArgs = { ""+LoginActivity.mimajor};

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
            Toast.makeText(KalmanEstatica.this,"Sin salas que incluir al diccionario",Toast.LENGTH_LONG).show();
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
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

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
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala};

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
            Toast.makeText(KalmanEstatica.this,"Sin parámetros",Toast.LENGTH_LONG).show();
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
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala};

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
            Toast.makeText(KalmanEstatica.this,"sin sigma ni p",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            sigma= cursor.getFloat(0);
            p= cursor.getFloat(1);
        }
        cursor.close();

    }

    public void actualizaPizarra(){
        PizarraKalmanEst.miCanvasKalmanEst.drawColor(0xFFFFFFFF);
        pizarraKalmanEst.invalidate();
        try{
            verCuadros(salaPresente);
            verBalizas(salaPresente);
        }catch(Exception e){
            Toast.makeText(KalmanEstatica.this, "¡No puedo dibujar!", Toast.LENGTH_SHORT).show();
        }
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
        Toast.makeText(KalmanEstatica.this, "sala "+sala, Toast.LENGTH_SHORT).show();
        salaPresente=sala;
        salaTxt.setText("Sala nº: "+sala);
        leerParamentros(sala);
        leerSigmaYP(sala);
        pem[0]=(double) anchoCanvas/2;
        pem[1]=(double) altoCanvas/2;
        pem[2]=100d;
        int i=0;
        for(i=0;i<3;i++){pemTrilat[i]=pem[i];}
        actualizaPizarra();

    }


    class Inversake extends Thread{
        public Inversake(){}


        @Override public void run(){

            double[][] H = new double[3][numDimensionesModelo];
            double[][] Rmatriz = new double[3][3];
            double[][] K = new double[numDimensionesModelo][3];
            double ox3,oy3;
            double[][]PB=new double[3][3];
            double[]re=new double[3];
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
            boolean hayActualizacionKalman=true;

            while (recalcularPos){
                recalcularPos=false;
                cotaRecalcularPos++;

                for(i=0;i<3;i++){
                    if(salaEncontrada[i]==salaPresente) {
                        PB[i][0] = posXencontrada[i];
                        PB[i][1] = posYencontrada[i];
                        PB[i][2] = posZencontrada[i];
                        re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2) + Math.pow(pem[2] - PB[i][2], 2));
                        A3[i][0] = (PB[i][0] - pem[0]) / re[i];
                        A3[i][1] = (PB[i][1] - pem[1]) / re[i];
                        A3[i][2] = (PB[i][2] - pem[2]) / re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i] = 100 * Math.pow(10, ((RSS0 - rssiEncontrado[i]) / (10 * p)));// * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
                        B3[i] = r[i] - re[i];
                    }else {//otra sala
                        ox3=(double)(Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));
                        oy3=(double)(Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));

                        PB[i][0]=(double)( Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+ox3);
                        PB[i][1]=(double)( Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+oy3);
                        PB[i][2]=posZencontrada[i];

                        re[i]=Math.sqrt(Math.pow(pem[0]-PB[i][0],2)+Math.pow(pem[1]-PB[i][1],2)+Math.pow(pem[2]-PB[i][2],2));
                        A3[i][0]=(PB[i][0]-pem[0])/re[i];
                        A3[i][1]=(PB[i][1]-pem[1])/re[i];
                        A3[i][2]=(PB[i][2]-pem[2])/re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i]=100*Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));//*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B3[i]=r[i]-re[i];
                    }
                }
                i=0;

                RealMatrix a = MatrixUtils.createRealMatrix(A3);

                try {

                    RealMatrix aInverse = new LUDecomposition(a).getSolver().getInverse();

                    for (i=0;i<3;i++){
                        for (j=0;j<3;j++){
                            A3inv[i][j]=aInverse.getEntry(i,j);
                        }
                    }
                    i=0;j=0;
                    for (j=0;j<3;j++){
                        deltaPos[j]=0;
                        for(i=0;i<3;i++){
                            deltaPos[j]=deltaPos[j]+ A3inv[j][i]*B3[i];
                        }
                        pem[j]=pem[j]-0.25*deltaPos[j];

                        //poner aqui los pasos de cuando hay nuevo valor generado
                        //Update Kalman
                        hayActualizacionKalman=true;

                    }
                    if(Math.abs(deltaPos[0])>0.01){
                        recalcularPos=true;
                    }
                    if(Math.abs(deltaPos[1])>0.01){
                        recalcularPos=true;
                    }
                    if(pem[0]>anchoCanvas){
                        pem[0]=anchoCanvas;
                        recalcularPos=true;
                    }
                    if(pem[0]<0){
                        pem[0]=0;
                        recalcularPos=true;
                    }
                    if(pem[1]>altoCanvas){
                        pem[1]=altoCanvas;
                        recalcularPos=true;
                    }
                    if(pem[1]<0){
                        pem[1]=0;
                        recalcularPos=true;
                    }
                    if(pem[2]>0){
                        pem[2]=0;
                        //recalcularPos=true;
                    }
                    if(pem[2]<-130){
                        pem[2]=-130;
                        //recalcularPos=true;
                    }

                }catch (Exception e){
                    //poner aquí los pasos de cuando no hay un nuevo valor generado
                    hayActualizacionKalman=false;
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }
                for(i=0;i<3;i++){
                    pemTrilat[i]=pem[i];
                }

            }//fin del while de TRILATERACIÓN

            if(hayActualizacionKalman){

                //defino la nueva matriz H de Kalman integrado
                for(i=0;i<3;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        H[i][j]=0;
                        if(i==j){
                            H[i][j]=1;
                        }
                    }
                }
                //defino matriz R
                //R depende de sigma de la posición que es 2.6*sigma de los rangos, que depende a su vez de
                //la distancia media a cada baliza desde nuestra posición estimada xkk
                double acumulaDistRmat=0;
                double cteSigmaRango=sigma*Math.log(10)/(10*p);
                double minSigmaRango=200;
                for(i=0;i<3;i++){
                    //distRmat[i]= Math.sqrt(Math.pow(xkk[0] - PB[i][0], 2) + Math.pow(xkk[1] - PB[i][1], 2) + Math.pow(xkk[2] - PB[i][2], 2));
                    //cambio esto a pesar de que lo otro (la expresión de arriba) funcionaba
                    distRmat[i]=100*Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));
                    acumulaDistRmat=acumulaDistRmat+distRmat[i];
                }
                acumulaDistRmat=(acumulaDistRmat)/3d;
                double sigmaPos=2*(  acumulaDistRmat * cteSigmaRango + minSigmaRango  );
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        if(i==j){
                            Rmatriz[i][j]=Math.pow(sigmaPos,2);
                            //Rmatriz[i][j]=Math.pow(sigmaPos,2)/10000;
                            //Rmatriz[i][j]=(1d)*miVarPos;
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

                //¡OJO!
                //Zk==r[i] -->(Zk-H*Xestk)=(r[i]-H*pem[])

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
                        for(j=0;j<numDimensionesModelo;j++){
                            restoPos[i]=restoPos[i]+H[i][j]*xkk1[j];
                        }
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=0;
                        for(j=0;j<3;j++){
                            xkk[i]=xkk[i]+K[i][j]*(pem[j]- restoPos[j]);
                        }
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=xkk[i]+xkk1[i];//valor estimado actualizado

                        if(i<3){
                            pem[i]=xkk[i];
                        }
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
                            if(i<3){
                                pem[i]=xkk[i];
                            }
                        }
                    }
                    //MATRIZ SINGULAR

                }

            }else{//no hay actualizacion de k
                //si no tengo dato de posición, tomo como dato filtrado la predicción pasada
                //(lo necesita la etapa de predicción)
                for (i=0;i<numDimensionesModelo;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        xkk[i]=xkk1[i];
                        Pkk[i][j]=Pkk1[i][j];
                        if(i<3){
                            pem[i]=xkk[i];
                        }
                    }
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
                    Pkk1[i][j]=phipkkphit.getEntry(i,j);
                    Pkk1[i][j]=Pkk1[i][j]+Q[i][j];
                }
            }

            runOnUiThread(new Runnable(){
                @Override public void run(){

                    float xFinal,yFinal,pkkFinal;
                    pkkFinal=(float)(Math.sqrt(Pkk[0][0])+Math.sqrt(Pkk[1][1]))/2f;
                    float margen=24;
                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMaek[1]-2*margen)/((2* mis_pxMaek[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal= margen+ (float)xkk[0]*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        yFinal= margen + (float)xkk[1]*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        pkkFinal=  (float)pkkFinal*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                    }else{
                        //limita el largo
                        xFinal= margen + (float) xkk[0]*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                        yFinal= margen + (float) xkk[1]*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                        pkkFinal=  (float) pkkFinal*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                    }

                    if(!desarrollador){
                        actualizaPizarra();
                    }
                    //DPO E [2,4]-->DPO=3;
                    //PizarraKalmanEst.miPaintKalmanEstIncertidumbre.setStrokeWidth((float)(3*sigma*Math.log(10)*distanciaMediaABalizas()/(10*p)));
                    PizarraKalmanEst.miPaintKalmanEstIncertidumbre.setStrokeWidth((float)(pkkFinal));
                    PizarraKalmanEst.miCanvasKalmanEst.drawPoint(xFinal,yFinal,PizarraKalmanEst.miPaintKalmanEstIncertidumbre);
                    PizarraKalmanEst.miCanvasKalmanEst.drawPoint(xFinal,yFinal,PizarraKalmanEst.miPaintKalmanEstPersona);
                    pizarraKalmanEst.invalidate();
                    textViewPosXKalmanEst.setText(""+Math.ceil(xkk[0]));
                    textViewPosYKalmanEst.setText(""+Math.ceil(xkk[1]));
                    textViewPosZKalmanEst.setText(""+Math.ceil(xkk[2]));

                    banderaPos=true;
                    banderaHilo=true;
                    if(pedirAyuda){
                        if(contadorPedirAyuda>2){
                            recogerUrl();
                            finish();
                        }
                        contadorPedirAyuda++;
                    }

                }
            });


        }
    }


    class MinimosCuadradoske extends Thread{
        private int numBalizasHilo;

        public  MinimosCuadradoske(int numBalizasHilo){
         this.numBalizasHilo=numBalizasHilo;
        }

        @Override public void run(){

            //numBalizasHilo=4;
            double[][] H=new double[3][numDimensionesModelo];
            double[][] Rmatriz=new double[3][3];
            double[][] K=new double[numDimensionesModelo][3];
            double ox4,oy4;
            double[][]PB=new double[numBalizasHilo][3];
            double[]re=new double[numBalizasHilo];
            double[] r=new double[numBalizasHilo];
            double[][] A=new double[numBalizasHilo][3];
            double[] B=new double[numBalizasHilo];
            double[][]transitoMMC=new double[3][numBalizasHilo];
            double[] distRmat=new double[numBalizasHilo];
            double[][]H_Pkk1_Ht_R=new double[3][3];
            double[][]H_Pkk1_Ht_R_INV=new double[3][3];
            double[] restoPos=new double[3];
            double[][] eye=new double[numDimensionesModelo][numDimensionesModelo];
            int i=0;int j=0;
            boolean recalcularPos=true;
            boolean hayActualizacionKalman=true;
            int cotaRecalcularPos=0;

            //PRIMERO: TRILATEARACIÓN: PASAMOS LAS MEDIDAS A RANGOS
            while (recalcularPos){
                recalcularPos=false;
                cotaRecalcularPos++;

                for(i=0;i<numBalizasHilo;i++){
                    if(salaEncontrada[i]==salaPresente) {
                        PB[i][0] = posXencontrada[i];
                        PB[i][1] = posYencontrada[i];
                        PB[i][2] = posZencontrada[i];
                        re[i] = Math.sqrt(Math.pow(pem[0] - PB[i][0], 2) + Math.pow(pem[1] - PB[i][1], 2) + Math.pow(pem[2] - PB[i][2], 2));
                        A[i][0] = (PB[i][0] - pem[0]) / re[i];
                        A[i][1] = (PB[i][1] - pem[1]) / re[i];
                        A[i][2] = (PB[i][2] - pem[2]) / re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i] = 100 * Math.pow(10, ((RSS0 - rssiEncontrado[i]) / (10 * p)));// * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
                        B[i] = r[i] - re[i];
                    }else {//hay que corregir la posición de la baliza
                        for(j=0;j<numBalizasHilo;j++){
                            Log.d("FALLO OTRA SALA","minor "+j+" "+rssiEncontrado[j]);
                        }

                        ox4=(double)(Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));
                        oy4=(double)(Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada[i])-diccionarioSalaOrigenXCoords.get(salaPresente))+Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada[i])-diccionarioSalaOrigenYCoords.get(salaPresente)));

                        PB[i][0]=(double)( Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+ox4);
                        PB[i][1]=(double)( Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posXencontrada[i]+Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada[i]))*posYencontrada[i]+posZencontrada[i]+oy4);
                        PB[i][2]=posZencontrada[i];

                        re[i]=Math.sqrt(Math.pow(pem[0]-PB[i][0],2)+Math.pow(pem[1]-PB[i][1],2)+Math.pow(pem[2]-PB[i][2],2));
                        A[i][0]=(PB[i][0]-pem[0])/re[i];
                        A[i][1]=(PB[i][1]-pem[1])/re[i];
                        A[i][2]=(PB[i][2]-pem[2])/re[i];
                        //convierto distancia en m a cm: (*100)
                        r[i]=100*Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));//*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B[i]=r[i]-re[i];
                    }

                }
                i=0;

                RealMatrix a = MatrixUtils.createRealMatrix(A);
                RealMatrix aTa=(a.transpose()).multiply(a);


                try {

                    Log.d("Minimos","Nº balizas: "+numBalizasHilo);
                    Log.d("Minimos","inicio ataInverse");
                    RealMatrix aTaInverse = new LUDecomposition(aTa).getSolver().getInverse();
                    Log.d("Minimos","¡éxito ataInverse!");
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
                        pem[j]=pem[j]-0.25*deltaPos[j];

                        hayActualizacionKalman=true;
                    }
                    if(Math.abs(deltaPos[0])>0.01){
                        recalcularPos=true;
                    }
                    if(Math.abs(deltaPos[1])>0.01){
                        recalcularPos=true;
                    }
                    if(pem[0]>anchoCanvas){
                        pem[0]=anchoCanvas;
                        recalcularPos=true;
                    }
                    if(pem[0]<0){
                        pem[0]=0;
                        recalcularPos=true;
                    }
                    if(pem[1]>altoCanvas){
                        pem[1]=altoCanvas;
                        recalcularPos=true;
                    }
                    if(pem[1]<0){
                        pem[1]=0;
                        recalcularPos=true;
                    }
                    if(pem[2]>0){
                        pem[2]=0;
                        //recalcularPos=true;
                    }
                    if(pem[2]<-130){
                        pem[2]=-130;
                        //recalcularPos=true;
                    }

                }catch (Exception e){
                    hayActualizacionKalman=false;
                    Log.d("Minimos","fallo ataInverse");
                    String strATA="[ \n";
                    for(i=0;i<3;i++){
                        for(j=0;j<3;j++){
                            strATA=strATA+aTa.getEntry(i,j)+" ";
                        }
                        strATA=strATA+"\n";
                    }
                    strATA=strATA+"]";
                    Log.d("Minimos",strATA);
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }

                for(i=0;i<3;i++){
                    pemTrilat[i]=pem[i];
                }
            }//fin del while


            //sigo con Kalman
            if(hayActualizacionKalman){
                Log.d("Minimos"," hay actualizción kalman");
                //defino la nueva matriz H de Kalman integrado que es igual a la matriz A de antes
                for(i=0;i<3;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        H[i][j]=0;
                        if(i==j){H[i][j]=1;}
                    }
                }
                //defino matriz R
                //R depende de sigma de la posición que es 2.6*sigma de los rangos, que depende a su vez de
                //la distancia media a cada baliza desde nuestra posición estimada xkk
                double acumulaDistRmat=0;
                double cteSigmaRango=sigma*Math.log(10)/(10*p);
                double minSigmaRango=200;
                for(i=0;i<numBalizasHilo;i++){
                    //distRmat[i]= Math.sqrt(Math.pow(xkk[0] - PB[i][0], 2) + Math.pow(xkk[1] - PB[i][1], 2) + Math.pow(xkk[2] - PB[i][2], 2));
                    //cambio ésto a pesar de que lo de arriba funcionaba bien
                    distRmat[i]=100*Math.pow(10,((RSS0-rssiEncontrado[i])/(10*p)));
                    acumulaDistRmat=acumulaDistRmat+distRmat[i];
                }
                acumulaDistRmat=(double)(acumulaDistRmat)/numBalizasHilo;
                double sigmaPos=2*( acumulaDistRmat * cteSigmaRango + minSigmaRango );
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        if(i==j){
                            Rmatriz[i][j]=Math.pow(sigmaPos,2);
                            //Rmatriz[i][j]=Math.pow(sigmaPos,2)/10000;
                            //Rmatriz[i][j]=(1d)*miVarPos;
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

                //¡OJO!
                //Zk==r[i] -->(Zk-H*Xestk)=(r[i]-H*xkk)


                RealMatrix h = MatrixUtils.createRealMatrix(H);
                RealMatrix pkk1 = MatrixUtils.createRealMatrix(Pkk1);
                RealMatrix ht = h.transpose();
                RealMatrix hpkk1ht=((h.multiply(pkk1)).multiply(ht));
                RealMatrix pkk1ht=pkk1.multiply(ht);

                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        H_Pkk1_Ht_R[i][j]=hpkk1ht.getEntry(i,j);
                    }
                }
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        H_Pkk1_Ht_R[i][j]=H_Pkk1_Ht_R[i][j]+Rmatriz[i][j];
                    }
                }

                RealMatrix hpkk1htr = MatrixUtils.createRealMatrix(H_Pkk1_Ht_R);

                try{
                    Log.d("Minimos","inicio hpkk1htrInverse");
                    RealMatrix hpkk1htrInverse = new LUDecomposition(hpkk1htr).getSolver().getInverse();
                    Log.d("Minimos","¡éxito hpkk1htrInverse!");

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
                        for(j=0;j<numDimensionesModelo;j++){
                            restoPos[i]=restoPos[i]+H[i][j]*xkk1[j];
                            //restoPos[i]=restoPos[i]+B[i];
                        }
                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=0;
                        for(j=0;j<3;j++){
                            xkk[i]=xkk[i]+K[i][j]*(pem[j]- restoPos[j]);
                            //xkk[i]=xkk[i]+K[i][j]*(r[j]-restoPos[j]);
                        }

                    }
                    for(i=0;i<numDimensionesModelo;i++){
                        xkk[i]=xkk[i]+xkk1[i];

                        if(i<3){
                            pem[i]=xkk[i];
                        }
                    }

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
                    Log.d("Minimos","fallo hpkk1htrInverse");
                    //si no saliera, hago el else
                    for (i=0;i<numDimensionesModelo;i++){
                        for(j=0;j<numDimensionesModelo;j++){
                            xkk[i]=xkk1[i];
                            Pkk[i][j]=Pkk1[i][j];
                            if(i<3){
                                pem[i]=xkk[i];
                            }
                        }
                    }
                    //MATRIZ SINGULAR

                }

            }else{//no hay actualizacion de k
                //si no tengo dato de posición, tomo como dato filtrado la predicción pasada
                //(lo necesita la etapa de predicción)
                for (i=0;i<numDimensionesModelo;i++){
                    for(j=0;j<numDimensionesModelo;j++){
                        xkk[i]=xkk1[i];
                        Pkk[i][j]=Pkk1[i][j];
                        if(i<3){
                            pem[i]=xkk[i];
                        }
                    }
                }

            }

            //PREDICCIÓN
            for (i=0;i<numDimensionesModelo;i++){
                xkk1[i]=0;
                for(j=0;j<numDimensionesModelo;j++) {
                    xkk1[i] = xkk1[i] + Phi[i][j] * xkk[j];
                }
            }
            RealMatrix phi = MatrixUtils.createRealMatrix(Phi);
            final RealMatrix pkk = MatrixUtils.createRealMatrix(Pkk);
            RealMatrix phipkk = phi.multiply(pkk);
            RealMatrix phit=phi.transpose();
            RealMatrix phipkkphit=phipkk.multiply(phit);

            for (i=0;i<numDimensionesModelo;i++){
                for(j=0;j<numDimensionesModelo;j++){
                    Pkk1[i][j]=phipkkphit.getEntry(i,j);
                    Pkk1[i][j]=Pkk1[i][j]+Q[i][j];
                }
            }

            runOnUiThread(new Runnable(){
                @Override public void run(){
                    float xFinal,yFinal,pkkFinal;
                    float margen=24;
                    pkkFinal=(float)(Math.sqrt(Pkk[0][0])+Math.sqrt(Pkk[1][1]))/2f;
                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMaek[1]-2*margen)/((2* mis_pxMaek[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal=margen+ (float)xkk[0]*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        yFinal=margen + (float)xkk[1]*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;
                        pkkFinal= pkkFinal*(mis_pxMaek[1]-2*margen)/(float)anchoCanvas;


                    }else{
                        //limita el largo
                        xFinal=margen + (float) xkk[0]*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;
                        yFinal=margen+ (float)xkk[1]*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float)altoCanvas;
                        pkkFinal=  pkkFinal*((2/(float)3)*mis_pxMaek[0]-2*margen)/(float) altoCanvas;

                    }

                    if(!desarrollador){
                        actualizaPizarra();
                    }
                    //DPO E [2,4]-->DPO=3;
                    //PizarraKalmanEst.miPaintKalmanEstIncertidumbre.setStrokeWidth((float)(3*sigma*Math.log(10)*distanciaMediaABalizas()/(10*p)));
                    PizarraKalmanEst.miPaintKalmanEstIncertidumbre.setStrokeWidth((float)(pkkFinal));
                    PizarraKalmanEst.miCanvasKalmanEst.drawPoint(xFinal,yFinal,PizarraKalmanEst.miPaintKalmanEstIncertidumbre);
                    PizarraKalmanEst.miCanvasKalmanEst.drawPoint(xFinal,yFinal,PizarraKalmanEst.miPaintKalmanEstPersona);
                    pizarraKalmanEst.invalidate();
                    textViewPosXKalmanEst.setText(""+Math.ceil(xkk[0]));
                    textViewPosYKalmanEst.setText(""+Math.ceil(xkk[1]));
                    textViewPosZKalmanEst.setText(""+Math.ceil(xkk[2]));
                    banderaPos=true;
                    banderaHilo=true;
                    if(pedirAyuda){
                        if(contadorPedirAyuda>2){
                            recogerUrl();
                            finish();
                        }
                        contadorPedirAyuda++;
                    }

                }
            });
        }
    }

    private void peticionIFTTT(String url){

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Toast.makeText(KalmanEstatica.this, ""+response.toString(), Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(KalmanEstatica.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);

    }

    private void recogerUrl(){
        String url = "http://www.museocarandnuria.es/bajarAyuda.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        String urlinicioWeb;
                        String urlfinWeb;
                        String correoWeb;
                        String urlconstruida;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                urlinicioWeb=respuestaJSONobjeto.getString("urlinicio");
                                urlfinWeb=respuestaJSONobjeto.getString("urlfin");
                                correoWeb=respuestaJSONobjeto.getString("correo");
                                urlconstruida="https://"+urlinicioWeb+"URLEncoder.encode("+correoWeb+",”UTF-8”)"+urlfinWeb+"Acudir a la sala "+salaPresente+" a la posición: ( "+Math.ceil(pemTrilat[0])+", "+Math.ceil(pemTrilat[1])+")";
                                peticionIFTTT(urlconstruida);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(KalmanEstatica.this, "Fallo en la descarga", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(KalmanEstatica.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }

    public float betaEstimada(){
        double xt,yt;
        float beta;
        xt=(anchoCanvas/2)-xkk[0];
        yt=(altoCanvas/2)-xkk[1];
        beta=0f;
        if(yt>0){
            if(xt>0){
                beta= (float)Math.toDegrees(Math.atan((yt/xt)));
            }else{
                //if (xt<=0)
                if(xt==0){
                    beta=90f;
                }else{
                    beta= 180f+(float)Math.toDegrees(Math.atan((yt/xt)));
                }
            }
        }else{
            //if (yt=<0)
            if(xt>0){
                if (yt<0){
                    //el fallo era:
                    //beta=360f+ (float)Math.toDegrees(Math.atan((yt/xt)));
                    beta= (float)Math.toDegrees(Math.atan((yt/xt)));
                }else{
                    //yt==0
                    beta=0f;
                }

            }else{
                //if (xt<=0)
                if(xt==0){
                    beta=-90f;
                }else{
                    if (yt<0){
                        beta= 180f+(float)Math.toDegrees(Math.atan((yt/xt)));
                    }else{
                        //yt==0
                        beta=180f;
                    }
                }
            }
        }

        return beta;
    }

    class HiloGrabacion extends Thread{
        public HiloGrabacion(){}
        @Override public void run(){

            boolean tiempoMax=false;
            //boolean reescritura=true;
            Calendar tiempoInicial, tiempoActual;
            long incrementoMax=0;
            long incrementoMinimo=0;
            long tiempoAnterior;
            tiempoInicial = Calendar.getInstance();
            tiempoActual=Calendar.getInstance();

            tiempoInicial.setTimeInMillis(System.currentTimeMillis());
            tiempoActual.setTimeInMillis(System.currentTimeMillis());
            tiempoAnterior=tiempoActual.getTimeInMillis();
            if(banderaGrabacion){

                // while(!tiempoMax){
                while( ((!tiempoMax)&&(segundos!=40)) || (banderaGrabacion&&(segundos==40)) ){

                    tiempoActual.setTimeInMillis(System.currentTimeMillis());
                    incrementoMax=tiempoActual.getTimeInMillis()-tiempoInicial.getTimeInMillis();
                    if(incrementoMax>= (segundos*1000)){
                        //si han pasado 10 segundos o mas se acaba la grabación para tiempo limitado a 10, 20 o 30segs
                        //poner una condición para que con tiempo Indefinido no se ponga a true salvo que se pulse
                        //nuevamente al botón de PARAR
                        tiempoMax=true;
                    }else{
                        //si no ha pasado el tiempo de grabación, puedo seguir grabando
                        tiempoMax=false;
                    }
                    //si han pasado 500ms, actualizo tiempoAnterior y doy permiso para el guardado
                    incrementoMinimo=tiempoActual.getTimeInMillis()-tiempoAnterior;
                    if(incrementoMinimo>=deltaT){//debería cambiarlo a 200ms cuando funcione
                        tiempoAnterior=tiempoActual.getTimeInMillis();
                        //ahora guardo la información en la SD:

                        incrementoMin=incrementoMinimo;
                        runOnUiThread(new Runnable(){
                            @Override public void run(){
                                //Toast.makeText(KalmanEstatica.this, "tiempo transcurrido "+incrementoMin, Toast.LENGTH_SHORT).show();
                                guardarDato(incrementoMin);
                            }
                        });

                    }

                }

                runOnUiThread(new Runnable(){
                    @Override public void run(){
                        banderaGrabacion=false;
                        if(segundos!=40) {
                            guardarPosiciones.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }

        }
    }


    public void guardarDato(long incrementoMin){
        //verificar que hay acceso a la SD:
        String estadoSD=Environment.getExternalStorageState();

        if(estadoSD.equals(Environment.MEDIA_MOUNTED)){

            float beta=betaEstimada();

            try{
                String texto="";
                if(banderaCambiarPosic){
                    texto=texto+"CAMBIO"+"\n";
                    banderaCambiarPosic=false;
                }
                if(banderaIncrementarPosic){
                    texto=texto+"POSICION "+contadorIncrementarPosic+"\n";
                    banderaIncrementarPosic=false;
                }

                int indiceBalizasEncontradas=0;

                if(banderaTresBalizas) {
                    texto = texto + incrementoMin + "," + LoginActivity.mimajor + "," + salaPresente + "," + contadorIncrementarPosic + "," + 3 /*numBalizasEncontradas*/ + ",";

                    for(indiceBalizasEncontradas=0;indiceBalizasEncontradas<12;indiceBalizasEncontradas++){
                       texto=texto + idBalizasEncontradas[indiceBalizasEncontradas] + "," + rssiEncontrado[indiceBalizasEncontradas] + ",";
                    }
                    texto = texto+ + Math.ceil(pemTrilat[0]) + "," + Math.ceil(pemTrilat[1]) + "," + Math.ceil(pemTrilat[2]) + "," + Math.ceil(xkk[0]) + "," + Math.ceil(xkk[1]) + "," + Math.ceil(xkk[2]) + "," + beta + "," + "\n";

                }else {
                    texto = texto + incrementoMin + "," + LoginActivity.mimajor + "," + salaPresente + "," + contadorIncrementarPosic + "," + numBalizasEncontradas /*numBalizasEncontradas*/ + "," ;

                    for(indiceBalizasEncontradas=0;indiceBalizasEncontradas<12;indiceBalizasEncontradas++){
                        texto = texto + idBalizasEncontradas[indiceBalizasEncontradas] + "," + rssiEncontrado[indiceBalizasEncontradas] + ",";
                    }
                    texto = texto + Math.ceil(pemTrilat[0]) + "," + Math.ceil(pemTrilat[1]) + "," + Math.ceil(pemTrilat[2]) + "," + Math.ceil(xkk[0]) + "," + Math.ceil(xkk[1]) + "," + Math.ceil(xkk[2]) + "," + beta + "," + "\n";

                }

                File archivo=new File(generarFicheroEnDescargas().getAbsolutePath(),FICHERO);
                FileWriter fw = new FileWriter(archivo, true);
                fw.append(texto);
                fw.close();

            }catch (Exception e){
                Toast.makeText(this, "No se puede almacenar datos en SD", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public File generarFicheroEnDescargas() {
        File ruta=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"miMuseo");
        if (!ruta.exists()) {
            Toast.makeText(this, "Creando directorio", Toast.LENGTH_SHORT).show();
            ruta.mkdirs();
        }
        return ruta;
    }








}

