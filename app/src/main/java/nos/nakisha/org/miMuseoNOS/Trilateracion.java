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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 29/01/2018.
 */

public class Trilateracion extends AppCompatActivity implements View.OnClickListener {

    public static float[] mis_pxMae;
    public static int[] mis_dpMae;

    //vbles fichero
    public boolean banderaGrabacion;
    private static String FICHERO;
    public  long incrementoMin;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    //rssi
    public BeaconManager manejadorDeBalizas;
    public BeaconRegion region;

    //vbles view:
    public static TextView textViewPosXMapaEst;
    public static TextView textViewPosYMapaEst;
    public TextView textViewPosZMapaEst;

    public Button limpiarMapaBtn;
    public Button guardarPosicionesBtn;
    public Button cambiarPosicion;
    public Button incrementarPosicion;

    private PizarraMapaEst pizarraMapaEst;

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
    public int segundos;
    public int deltaT;
    public int RSS0;
    public int numDimensionesModelo;

    //private double[] re;
    private double[] deltaPos;
    private int[] idBalizasEncontradas;
    public int[] rssiEncontrado;
    public int numBalizasEncontradas;
    public float sigma;
    public float p;
    public float norteMuseo;

    public boolean banderaPos;
    public boolean banderaHilo;
    public boolean banderaTresBalizas;
    public boolean banderaCambiarPosic;
    public boolean banderaIncrementarPosic;
    public int contadorIncrementarPosic;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_mapa_est);

        manejadorBBDD = new BBDD_Helper(this);
        establecerNorte();

        mis_pxMae=new float[2];
        mis_dpMae=new int[2];
        medidas();

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
        salaTxt=(TextView)findViewById(R.id.salaTrilateracionTxtVw);


        diccionarioSalas();

        verTodasBalizas();
        //diccionarioSalas() no se mete dentro de verBalizas() pq verBalizas() se llama siempre que se limpia la pizarra
        //y no es necesario rellenar el diccionario nuevamente.
        //ahora no se pueden visualizar ni cuadros ni balizas pq no sabemos en qué sala estamos

        textViewPosXMapaEst=(TextView)findViewById(R.id.posXMapaEst);
        textViewPosYMapaEst=(TextView)findViewById(R.id.posYMapaEst);
        textViewPosZMapaEst=(TextView)findViewById(R.id.posZMapaEst);

        pizarraMapaEst=(PizarraMapaEst)findViewById(R.id.viewPizarraMapaEst);

        limpiarMapaBtn=(Button)findViewById(R.id.limpiaMapaEstBtn);
        limpiarMapaBtn.setOnClickListener(this);
        incrementoMin=0;

        banderaGrabacion=false;
        guardarPosicionesBtn=(Button)findViewById(R.id.guardarMapaEstBtn);
        guardarPosicionesBtn.setOnClickListener(this);
        cambiarPosicion=(Button)findViewById(R.id.cambiarPosicionTrilateracionBtn);
        cambiarPosicion.setOnClickListener(this);
        incrementarPosicion=(Button)findViewById(R.id.incrementarPosicionTrilateracionBtn);
        incrementarPosicion.setOnClickListener(this);


        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(Trilateracion.this);
        deltaT=Integer.parseInt(preferences.getString("deltaT",""+250));
        if(deltaT<100){
            deltaT=100;
        }
        int tipoRSS0=Integer.parseInt(preferences.getString("matrizR",""+7));
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
        norteMuseo=(360f-Float.parseFloat(preferences.getString("norte",""+0)));

        if(segundos!=40){//para ejecutar ésto, antes debemos llamar a matricesPreferencias() --> OK
            cambiarPosicion.setVisibility(View.GONE);
            incrementarPosicion.setVisibility(View.GONE);
        }

        manejadorDeBalizas = new BeaconManager(this);
        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        manejadorDeBalizas.setForegroundScanPeriod((long)deltaT,0);
        manejadorDeBalizas.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> list) {

                //IMPORTANTE: ahora supongo un num. max de balizas a encontrar: 12 en concreto
                if (!list.isEmpty()) {

                    Log.d("LISTA", "nueva lista");

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

                                                numBalizasEncontradas++;
                                            }

                                        }
                                    }
                                } catch (Exception e) {
                                    //Toast.makeText(Trilateracion.this, "Se detectan balizas no registradas", Toast.LENGTH_SHORT).show();
                                }
                            }//fin for asignación
                        }//fin if(banderaPos)

                        if(banderaHilo){
                            if(numBalizasEncontradas==3) {
                                Log.d("LISTA","nuevo hilo con lista");
                                banderaPos = false;
                                banderaHilo = false;
                                banderaTresBalizas = true;
                                verificaSala();
                                Inversa hiloInversa = new Inversa();
                                hiloInversa.start();
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
                                    if (baliza.getMajor() == LoginActivity.mimajor) {
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
                                    //Toast.makeText(Trilateracion.this, "Fallo balizas", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        if(banderaHilo){
                            if(numBalizasEncontradas>=4) {
                                Log.d("LISTA","nuevo hilo con lista");
                                banderaPos = false;
                                banderaHilo = false;
                                banderaTresBalizas=false;
                                verificaSala();
                                MinimosCuadrados hiloMMC = new MinimosCuadrados(numBalizasEncontradas);
                                hiloMMC.start();
                            }
                        }
                    }//fin if(list.size()>=4)
                }
                else{
                    //si lista vacía y no se ven balizas no envío informacion a la BD
                }
            }
        });


    }


    @Override
    public void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(Trilateracion.this);
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

        mis_pxMae[0]=metrics.heightPixels;
        mis_pxMae[1]=metrics.widthPixels;
        if(depie()){
            mis_dpMae[0]=Math.round(metrics.heightPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMae[1]=Math.round(metrics.widthPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
        else{
            mis_dpMae[0]=Math.round(metrics.heightPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMae[1]=Math.round(metrics.widthPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        }
    }

    public boolean depie(){
        int orientationdevice = getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();

        if(id==R.id.limpiaMapaEstBtn){
            PizarraMapaEst.miCanvasMapaEst.drawColor(0xFFFFFFFF);
            actualizaPizarra();
        }//fin de if(id==R.id.limpiaMapaEstBtn)

        if(id==R.id.guardarMapaEstBtn){
            Date currentTime = Calendar.getInstance().getTime();
            String currentTimeStr=""+currentTime;
            currentTimeStr=currentTimeStr.replace("+","_");
            currentTimeStr=currentTimeStr.replace("-","_");
            currentTimeStr=currentTimeStr.replace(".","_");
            currentTimeStr=currentTimeStr.replace(":","_");
            currentTimeStr=currentTimeStr.replace(" ","_");
            FICHERO = "muestrasTrilateracion"+currentTimeStr+".txt";
            if(!banderaGrabacion){
                banderaGrabacion=true;
                contadorIncrementarPosic=1;
                HiloGrabacion hiloGrabacion=new HiloGrabacion();
                hiloGrabacion.start();
                if(segundos!=40){
                    guardarPosicionesBtn.setVisibility(View.GONE);
                }
                if(segundos==40){
                    banderaIncrementarPosic=true;
                    guardarPosicionesBtn.setText("PARAR");
                }
            }else{
                banderaGrabacion=false;
                if(segundos==40){
                    guardarPosicionesBtn.setText("GUARDAR");
                }
            }
        }

        if(id==R.id.cambiarPosicionTrilateracionBtn){
            banderaCambiarPosic=true;
            Toast.makeText(this, "¡Cambio!", Toast.LENGTH_SHORT).show();
        }
        if(id==R.id.incrementarPosicionTrilateracionBtn){
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

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float)altoCanvas;
                }

                PizarraMapaEst.miCanvasMapaEst.drawPoint(xFinal,yFinal,PizarraMapaEst.miPaintMapaEstBalizas);

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    if((mis_pxMae[1]-2*margen)-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}

                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float)altoCanvas;
                    if(anchoCanvas*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}

                }

                PizarraMapaEst.miCanvasMapaEst.drawText(idBaliza,xFinal,yFinal,PizarraMapaEst.avisoPaint);

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

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMae[0]-2*margen)/(float)altoCanvas;
                }

                Drawable drawableCuadro;
                drawableCuadro=PizarraMapaEst.sdCuadro;
                drawableCuadro.setBounds((int)xFinal-16,(int)yFinal-16,(int)xFinal+16,(int)yFinal+16);
                drawableCuadro.draw(PizarraMapaEst.miCanvasMapaEst);
                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();

    }
    public void establecerNorte(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(Trilateracion.this);
        norteMuseo=(360f-Float.parseFloat(preferences.getString("norte",""+0)));
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
            Toast.makeText(Trilateracion.this,"Sin salas que incluir al diccionario",Toast.LENGTH_LONG).show();
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
            Toast.makeText(Trilateracion.this,"Sin parametros",Toast.LENGTH_LONG).show();
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
            Toast.makeText(Trilateracion.this,"sin sigma ni p",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            sigma= cursor.getFloat(0);
            p= cursor.getFloat(1);
        }
        cursor.close();

    }

    public void actualizaPizarra(){
        PizarraMapaEst.miCanvasMapaEst.drawColor(0xFFFFFFFF);
        pizarraMapaEst.invalidate();
        try{
            verCuadros(salaPresente);
            verBalizas(salaPresente);
        }catch(Exception e){
            Toast.makeText(Trilateracion.this, "¡No puedo dibujar!", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(Trilateracion.this, "sala "+sala, Toast.LENGTH_SHORT).show();
        salaPresente=sala;
        salaTxt.setText("Sala nº: "+ sala);
        leerParamentros(sala);
        leerSigmaYP(sala);
        pem[0]=(double) anchoCanvas/2;
        pem[1]=(double) altoCanvas/2;
        pem[2]=100d;
        //pizarraMapaEst.invalidate();
        actualizaPizarra();

    }

    class Inversa extends Thread{
        public Inversa(){}


        @Override public void run(){

            double ox3,oy3;
            double[][]PB=new double[3][3];
            double[]re=new double[3];
            double[] r=new double[3];
            double[][] A3=new double[3][3];
            double[][] A3inv=new double[3][3];
            double[] B3=new double[3];
            int i=0;int j=0;
            boolean recalcularPos=true;
            int cotaRecalcularPos=0;


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
                        r[i] = 100 * Math.pow(10, ((-70 - rssiEncontrado[i]) / (10 * p))) * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
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
                        r[i]=100*Math.pow(10,((-70-rssiEncontrado[i])/(10*p)))*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B3[i]=r[i]-re[i];
                    }
                }//fin del for

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
                        pem[j]=pem[j]-deltaPos[j];
                        //ANTES:
                        /*
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
                        if(pem[2]<-200){
                            pem[2]=-200;
                            //recalcularPos=true;
                        }
                        */
                    }
                    if(deltaPos[0]>0.01){
                        recalcularPos=true;
                    }
                    if(deltaPos[1]>0.01){
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
                    if(pem[2]<-200){
                        pem[2]=-200;
                        //recalcularPos=true;
                    }

                }catch (Exception e){
                    runOnUiThread(new Runnable(){
                        @Override public void run(){
                            //MATRIZ SINGULAR
                            Toast.makeText(Trilateracion.this, "Fallo RealMatrix", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }

            }//fin del while

            runOnUiThread(new Runnable(){
                @Override public void run(){

                    float xFinal,yFinal;
                    float margen=24;
                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal=margen+ (float)pem[0]*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                        yFinal=margen + (float)pem[1]*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    }else{
                        //limita el largo
                        xFinal=margen + (float) pem[0]*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas;
                        yFinal=margen+ (float)pem[1]*((2/(float)3)*mis_pxMae[0]-2*margen)/(float)altoCanvas;
                    }
                    PizarraMapaEst.miCanvasMapaEst.drawPoint(xFinal,yFinal,PizarraMapaEst.miPaintMapaEstPersona);
                    pizarraMapaEst.invalidate();
                    textViewPosXMapaEst.setText(""+Math.ceil(pem[0]));
                    textViewPosYMapaEst.setText(""+Math.ceil(pem[1]));
                    textViewPosZMapaEst.setText(""+Math.ceil(pem[2]));
                    //Toast.makeText(getActivity(), "x: "+pem[0]+", y: "+pem[1], Toast.LENGTH_SHORT).show();
                    banderaPos=true;
                    banderaHilo=true;

                }
            });


        }//fin del run()
    }//fin de Inversa


    class MinimosCuadrados extends Thread{
        private int numBalizasHilo;

        public  MinimosCuadrados(int numBalizasHilo){
            this.numBalizasHilo=numBalizasHilo;
        }

        @Override public void run(){

            double ox4,oy4;
            double[][]PB=new double[numBalizasHilo][3];
            double[]re=new double[numBalizasHilo];
            double[] r=new double[numBalizasHilo];
            double[][] A=new double[numBalizasHilo][3];
            double[] B=new double[numBalizasHilo];
            double[][]transitoMMC=new double[3][numBalizasHilo];
            int i=0;int j=0;
            boolean recalcularPos=true;
            int cotaRecalcularPos=0;

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
                        r[i] = 100 * Math.pow(10, ((-70 - rssiEncontrado[i]) / (10 * p))) * Math.exp(-0.5 * Math.pow(((sigma * Math.log(10)) / (10 * p)), 2));
                        B[i] = r[i] - re[i];
                    }else {//hay que corregir la posición de la baliza
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
                        r[i]=100*Math.pow(10,((-70-rssiEncontrado[i])/(10*p)))*Math.exp(-0.5*Math.pow(((sigma*Math.log(10))/(10*p)),2));
                        B[i]=r[i]-re[i];

                    }
                }//fin del for

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
                        pem[j]=pem[j]-0.25*deltaPos[j];
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
                    runOnUiThread(new Runnable(){
                        @Override public void run(){
                            Toast.makeText(Trilateracion.this, "Fallo RealMatrix", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //si he iterado varias veces, rompo el bucle:
                if(cotaRecalcularPos>=25){
                    recalcularPos=false;
                }

            }//fin del while


            runOnUiThread(new Runnable(){
                @Override public void run(){

                    float xFinal,yFinal;
                    float margen=24;
                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal=margen+ (float)pem[0]*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                        yFinal=margen + (float)pem[1]*(mis_pxMae[1]-2*margen)/(float)anchoCanvas;
                    }else{
                        //limita el largo
                        xFinal=margen + (float) pem[0]*((2/(float)3)*mis_pxMae[0]-2*margen)/(float) altoCanvas;
                        yFinal=margen+ (float)pem[1]*((2/(float)3)*mis_pxMae[0]-2*margen)/(float)altoCanvas;
                    }
                    PizarraMapaEst.miCanvasMapaEst.drawPoint(xFinal,yFinal,PizarraMapaEst.miPaintMapaEstPersona);
                    pizarraMapaEst.invalidate();
                    textViewPosXMapaEst.setText(""+Math.ceil(pem[0]));
                    textViewPosYMapaEst.setText(""+Math.ceil(pem[1]));
                    textViewPosZMapaEst.setText(""+Math.ceil(pem[2]));
                    //Toast.makeText(getActivity(), "x: "+pem[0]+", y: "+pem[1], Toast.LENGTH_SHORT).show();
                    banderaPos=true;
                    banderaHilo=true;

                }
            });
        }
    }

    public float betaEstimada(){
        double xt,yt;
        float beta;
        xt=(anchoCanvas/2)-pem[0];
        yt=(altoCanvas/2)-pem[1];
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

                while(((!tiempoMax)&&(segundos!=40)) || (banderaGrabacion&&(segundos==40))){
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
                            guardarPosicionesBtn.setVisibility(View.VISIBLE);
                        }
                    }
                });

            }

        }
    }



    public void guardarDato(long incrementoMin){
        //verificar que hay acceso a la SD:
        String estadoSD= Environment.getExternalStorageState();

        if(estadoSD.equals(Environment.MEDIA_MOUNTED)){

            float beta=betaEstimada();

            try{
                /*
                //antes:
                String texto="POS_ESTIM:,"+"incremT,"+incrementoMin+",posición,"+Math.ceil(pem[0])+","+Math.ceil(pem[1])+","+Math.ceil(pem[2])+"\n";
                if(banderaTresBalizas){
                    int contador=0;
                    for(contador=0;contador<3;contador++){
                        texto=texto+"RSS:,"+"incremT,"+incrementoMin+",rss,"+rssiEncontrado[contador]+",id,"+idBalizasEncontradas[contador]+",posición,"+posXencontrada[contador]+","+posYencontrada[contador]+","+posZencontrada[contador]+","+"\n";
                    }

                }else{
                    int contador=0;
                    for(contador=0;contador<4;contador++){
                        texto=texto+"RSS:,"+"incremT,"+incrementoMin+",rss,"+rssiEncontrado[contador]+",id,"+idBalizasEncontradas[contador]+",posición,"+posXencontrada[contador]+","+posYencontrada[contador]+","+posZencontrada[contador]+","+"\n";
                    }
                }*/

                //ahora
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
                    texto = texto+ + Math.ceil(pem[0]) + "," + Math.ceil(pem[1]) + "," + Math.ceil(pem[2]) + ",0,0,0," + beta + "," + "\n";

                }else {
                    texto = texto + incrementoMin + "," + LoginActivity.mimajor + "," + salaPresente + "," + contadorIncrementarPosic + "," + numBalizasEncontradas /*numBalizasEncontradas*/ + "," ;

                    for(indiceBalizasEncontradas=0;indiceBalizasEncontradas<12;indiceBalizasEncontradas++){
                        texto = texto + idBalizasEncontradas[indiceBalizasEncontradas] + "," + rssiEncontrado[indiceBalizasEncontradas] + ",";
                    }
                    texto = texto + Math.ceil(pem[0]) + "," + Math.ceil(pem[1]) + "," + Math.ceil(pem[2]) + ",0,0,0," + beta + "," + "\n";

                }

                File archivo=new File(generarFichero().getAbsolutePath(),FICHERO);
                FileWriter fw = new FileWriter(archivo, true);
                fw.append(texto);
                fw.close();


            }catch (Exception e){
                Toast.makeText(this, "No se puede almacenar datos en SD", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public File generarFichero() {

        File ruta=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"mimuseo");
        if (!ruta.exists()) {
            Toast.makeText(this, "Creando directorio", Toast.LENGTH_SHORT).show();
            ruta.mkdirs();
        }
        return ruta;
    }




}

