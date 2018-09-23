package nos.nakisha.org.miMuseoNOS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 09/01/2018.
 */

public class TabMuestreoEst extends Fragment implements View.OnClickListener {

    public static float[] mis_pxMe;
    public static int[] mis_dpMe;
    public float norteMuseo;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    //rssi
    public BeaconManager manejadorDeBalizas;
    public BeaconRegion region;

    ///vbles view:
    public static EditText editTextPosXMuestreoEst;
    public static EditText editTextPosYMuestreoEst;
    public EditText editTextPosZMuestreoEst;
    public EditText numMuestras;
    public TextView calibrarTxtVw;

    private int salaPresente;

    public Button posicionaPersona;
    public Button pararMuestreo;
    public Button limpiarPersona;
    public Button muestreoPosEst;
    public Button calculoMas;

    private PizarraMuestreoEst pizarraMuestreoEst;

    public HashMap<Integer,Float> mapX;
    public HashMap<Integer,Float> mapY;
    public HashMap<Integer,Float> mapZ;
    public HashMap<Integer,Double> diccionarioSalaAngulo;
    public HashMap<Integer,Double> diccionarioSalaOrigenXCoords;
    public HashMap<Integer,Double> diccionarioSalaOrigenYCoords;

    public float posXencontrada;
    public float posYencontrada;
    public float posZencontrada;
    public int rssiEncontrado;

    public boolean banderaMuestreo;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras=getActivity().getIntent().getExtras();
        salaPresente=extras.getInt("sala");
        establecerNorte();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootViewMuestreoEst = inflater.inflate(R.layout.tab_muestreo_est, container, false);

        banderaMuestreo=false;

        Toast.makeText(getActivity(), "sala "+salaPresente, Toast.LENGTH_SHORT).show();

        manejadorBBDD = new BBDD_Helper(getContext());

        mis_pxMe=new float[2];
        mis_dpMe=new int[2];
        medidas();

        editTextPosXMuestreoEst=(EditText)rootViewMuestreoEst.findViewById(R.id.posXpersona);
        editTextPosYMuestreoEst=(EditText)rootViewMuestreoEst.findViewById(R.id.posYpersona);
        editTextPosZMuestreoEst=(EditText)rootViewMuestreoEst.findViewById(R.id.posZpersona);
        numMuestras=(EditText)rootViewMuestreoEst.findViewById(R.id.numMuestrasEditText);
        calibrarTxtVw=(TextView)rootViewMuestreoEst.findViewById(R.id.calibrarTxtVw);
        calibrarTxtVw.setText("Calibrar sala nº "+salaPresente);

        pizarraMuestreoEst=(PizarraMuestreoEst) rootViewMuestreoEst.findViewById(R.id.viewPizarraMuestreoEst);

        mapX=new HashMap<Integer, Float>();
        mapY=new HashMap<Integer, Float>();
        mapZ=new HashMap<Integer, Float>();
        diccionarioSalaAngulo=new HashMap<Integer, Double>();
        diccionarioSalaOrigenXCoords=new HashMap<Integer,Double>();
        diccionarioSalaOrigenYCoords=new HashMap<Integer,Double>();

        posicionaPersona=(Button)rootViewMuestreoEst.findViewById(R.id.posicionaPersonaBtn);
        posicionaPersona.setOnClickListener(this);
        pararMuestreo=(Button)rootViewMuestreoEst.findViewById(R.id.pararMuestreoBtn);
        pararMuestreo.setOnClickListener(this);
        limpiarPersona=(Button)rootViewMuestreoEst.findViewById(R.id.limpiaPersonaBtn);
        limpiarPersona.setOnClickListener(this);
        muestreoPosEst=(Button)rootViewMuestreoEst.findViewById(R.id.muestreoPosicionEstBtn);
        muestreoPosEst.setOnClickListener(this);
        calculoMas=(Button)rootViewMuestreoEst.findViewById(R.id.calculoMasBtn);
        calculoMas.setOnClickListener(this);

        diccionarioSalas();
        rellenarMapPosicionBalizas();
        verBalizas();
        verCuadros();

        manejadorDeBalizas = new BeaconManager(getActivity());
        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        manejadorDeBalizas.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> list) {

                //IMPORTANTE: ahora supongo un num. max de balizas a encontrar: 12 en concreto
                if (!list.isEmpty()) {
                    //RECUERDA BLOQUEAR EN -119!!!
                    Beacon balizaInmediata = list.get(0);
                    //Toast.makeText(getActivity(), ""+balizaInmediata.getMinor()+", "+balizaInmediata.getRssi(), Toast.LENGTH_SHORT).show();
                    try{
                        if(balizaInmediata.getMajor()==LoginActivity.mimajor){
                            if(!numMuestras.getText().toString().isEmpty()&&Integer.parseInt(numMuestras.getText().toString())>0){
                                if(banderaMuestreo){
                                    if(!editTextPosXMuestreoEst.getText().toString().isEmpty()&&!editTextPosYMuestreoEst.getText().toString().isEmpty()&&!editTextPosZMuestreoEst.getText().toString().isEmpty()){
                                        int iteracionMuestreada=Integer.parseInt(numMuestras.getText().toString());
                                        iteracionMuestreada--;
                                        numMuestras.setText(""+iteracionMuestreada);
                                        posXencontrada= mapX.get(balizaInmediata.getMinor());
                                        posYencontrada= mapY.get(balizaInmediata.getMinor());
                                        posZencontrada= mapZ.get(balizaInmediata.getMinor());
                                        rssiEncontrado=balizaInmediata.getRssi();
                                        Toast.makeText(getActivity(), "rssi: "+rssiEncontrado+" posX: "+posXencontrada+" posY: "+posYencontrada, Toast.LENGTH_SHORT).show();
                                        subirMuestreo(editTextPosXMuestreoEst.getText().toString(),editTextPosYMuestreoEst.getText().toString(),editTextPosZMuestreoEst.getText().toString(),""+rssiEncontrado,""+posXencontrada,""+posYencontrada,""+posZencontrada);

                                    }else{
                                        Toast.makeText(getActivity(), "Inserte posición", Toast.LENGTH_SHORT).show();
                                        numMuestras.setText("0");
                                        banderaMuestreo=false;
                                    }

                                }
                            }else {
                                if(Integer.parseInt(numMuestras.getText().toString())==0){
                                    banderaMuestreo=false;
                                }
                            }

                        }

                    }catch (Exception e){

                    }



                }
                else{
                    //si lista vacía y no se ven balizas no envío informacion a la BD
                }
            }
        });

        return rootViewMuestreoEst;

    }//fin onCreatView

    @Override
    public void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());

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

    public void establecerNorte(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getActivity());
        norteMuseo=(360f-Float.parseFloat(preferences.getString("norte",""+0)));
        Toast.makeText(getActivity(), "norte: "+ norteMuseo, Toast.LENGTH_SHORT).show();
    }

    public void medidas(){

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        mis_pxMe[0]=metrics.heightPixels;
        mis_pxMe[1]=metrics.widthPixels;
        if(depie()){
            mis_dpMe[0]=Math.round(metrics.heightPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMe[1]=Math.round(metrics.widthPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
        else{
            mis_dpMe[0]=Math.round(metrics.heightPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpMe[1]=Math.round(metrics.widthPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        }
    }

    public boolean depie(){

        int orientationdevice = getActivity().getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }

        @Override
    public void onClick(View v) {
        int id=v.getId();
            if(id==R.id.calculoMasBtn){

                Intent intentoObtenerSigmaYP=new Intent(getContext(),ParametrosSigmaPActivity.class);
                intentoObtenerSigmaYP.putExtra("sala",salaPresente);
                startActivity(intentoObtenerSigmaYP);

            }//fin de if(id==R.id.calculoMasBtn)
            if(id==R.id.muestreoPosicionEstBtn){
                banderaMuestreo=true;

            }//fin de if(id==R.id.muestreoEstBtn
            if(id==R.id.limpiaPersonaBtn){

                PizarraMuestreoEst.miCanvasMuestreoEst.drawColor(0xFFFFFFFF);
                actualizaPizarra();

            }//fin de if(id==R.id.limpiaPersonaBtn)
            if(id==R.id.pararMuestreoBtn){
                    try{
                        numMuestras.setText("0");
                        banderaMuestreo=false;
                    }catch (Exception e){}


            }//fin de if(id==R.id.pararMuestreoBtn)
            if(id==R.id.posicionaPersonaBtn){

                if(!editTextPosXMuestreoEst.getText().toString().isEmpty() &&!editTextPosYMuestreoEst.getText().toString().isEmpty() ){
                    //si hay algo escrito en X y en Y
                    float xEdit,yEdit,xFinal,yFinal;
                    float margen=24;
                    xEdit=Float.parseFloat(editTextPosXMuestreoEst.getText().toString());
                    yEdit=Float.parseFloat(editTextPosYMuestreoEst.getText().toString());
                    //Toast.makeText(getContext(), "editX: " +xEdit +" editY: "+yEdit, Toast.LENGTH_SHORT).show();

                    if((float)anchoCanvas/(float)altoCanvas>(mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen)){
                        //limita el ancho
                        xFinal=margen+ xEdit*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                        yFinal=margen + yEdit*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                    }else{
                        //limita el largo
                        xFinal=margen + xEdit*((2/(float)3)*mis_pxMe[0]-2*margen)/(float) altoCanvas;
                        yFinal=margen+ yEdit*((2/(float)3)*mis_pxMe[0]-2*margen)/(float)altoCanvas;
                    }
                    if(xEdit<=anchoCanvas && yEdit<=altoCanvas){
                        PizarraMuestreoEst.miCanvasMuestreoEst.drawPoint(xFinal,yFinal,PizarraMuestreoEst.miPaintMuestreoEstPersona);
                    }else{
                        Toast.makeText(getContext(), "Verifique las dimensiones", Toast.LENGTH_SHORT).show();
                    }



                }else{
                    Toast.makeText(getContext(), "Inserte Posición", Toast.LENGTH_SHORT).show();
                }

            }//fin de if(id==R.id.posicionaPersonaBtn)

    }//fin onClick

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
            Toast.makeText(getActivity(),"Sin salas que incluir al diccionario",Toast.LENGTH_LONG).show();
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
                diccionarioSalaOrigenXCoords.put(sala,ocx);
                diccionarioSalaOrigenYCoords.put(sala,ocy);
                diccionarioSalaAngulo.put(sala,ang);
                cursorSalas.moveToNext();
            }
        }
        cursorSalas.close();

    }

    public void rellenarMapPosicionBalizas(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_6
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ?";
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
            int numFilas,salaEncontrada;
            //String idBaliza;
            Float xBal,yBal,zBal;
            Float oxSalaEncontrada,oySalaEncontrada,posXBalizaEncontrada,posYBalizaEncontrada,posZBalizaEncontrada;

            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                salaEncontrada=cursor.getInt(4);
                xBal=cursor.getFloat(1);
                yBal=cursor.getFloat(2);
                zBal=cursor.getFloat(3);

                if(salaEncontrada==salaPresente){
                    mapX.put(cursor.getInt(0),xBal);
                    mapY.put(cursor.getInt(0),yBal);
                    mapZ.put(cursor.getInt(0),zBal);//debería ser: (-1)*zBal ya que TODAS las z son negativas
                    //pero como voy a considerarlas todas positivas,
                    // y lo que importa es el incremento, al final no importa
                }else{
                    oxSalaEncontrada=(float)(Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada)-diccionarioSalaOrigenXCoords.get(salaPresente))+(-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada)-diccionarioSalaOrigenYCoords.get(salaPresente)));
                    oySalaEncontrada=(float)(Math.sin(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenXCoords.get(salaEncontrada)-diccionarioSalaOrigenXCoords.get(salaPresente))+     Math.cos(diccionarioSalaAngulo.get(salaPresente)-(double)norteMuseo)*(diccionarioSalaOrigenYCoords.get(salaEncontrada)-diccionarioSalaOrigenYCoords.get(salaPresente)));

                    posXBalizaEncontrada=(float)( Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada))*xBal + (-1)*Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada))*yBal + zBal + oxSalaEncontrada);
                    posYBalizaEncontrada=(float)( Math.sin(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada))*xBal +      Math.cos(diccionarioSalaAngulo.get(salaPresente)-diccionarioSalaAngulo.get(salaEncontrada))*yBal + zBal + oySalaEncontrada);
                    posZBalizaEncontrada=zBal;

                    mapX.put(cursor.getInt(0),posXBalizaEncontrada);
                    mapY.put(cursor.getInt(0),posYBalizaEncontrada);
                    mapZ.put(cursor.getInt(0),posZBalizaEncontrada);
                }


                cursor.moveToNext();
            }
            numFilas=0;
            pizarraMuestreoEst.invalidate();

        }
        cursor.close();
    }

    public void verBalizas(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+ Estructura_BD.Balizas.NOMBRE_COLUMNA_6 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            int numFilas=0; String idBaliza;
            float xLect,yLect,xFinal,yFinal;
            float margen=24;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursor.getFloat(1);
                yLect=cursor.getFloat(2);
                idBaliza=cursor.getString(0);

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float)altoCanvas;
                }

                PizarraMuestreoEst.miCanvasMuestreoEst.drawPoint(xFinal,yFinal,PizarraMuestreoEst.miPaintMuestreoEstBalizas);

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;

                    if((mis_pxMe[1]-2*margen)-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}

                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float)altoCanvas;

                    if(anchoCanvas*((2/(float)3)*mis_pxMe[0]-2*margen)/(float) altoCanvas-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}

                }
                PizarraMuestreoEst.miCanvasMuestreoEst.drawText(idBaliza,xFinal,yFinal,PizarraMuestreoEst.avisoPaint);

                //mapX.put(cursor.getInt(0),cursor.getFloat(1));
                //mapY.put(cursor.getInt(0),cursor.getFloat(2));
                //mapZ.put(cursor.getInt(0),cursor.getFloat(3));

                cursor.moveToNext();
            }
            numFilas=0;
            pizarraMuestreoEst.invalidate();

        }
        cursor.close();

    }

    public void verCuadros(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+ Estructura_BD.Cuadros.NOMBRE_COLUMNA_7 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxMe[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxMe[0]-2*margen)/(float)altoCanvas;
                }

                Drawable drawableCuadro;
                drawableCuadro=PizarraMuestreoEst.sdCuadro;
                drawableCuadro.setBounds((int)xFinal-16,(int)yFinal-16,(int)xFinal+16,(int)yFinal+16);
                drawableCuadro.draw(PizarraMuestreoEst.miCanvasMuestreoEst);
                cursor.moveToNext();
            }
            numFilas=0;
            pizarraMuestreoEst.invalidate();
        }
        cursor.close();

    }

    public void actualizaPizarra(){
        pizarraMuestreoEst.invalidate();
        verCuadros();
        verBalizas();

    }


    protected void subirMuestreo(final String posXpersona, final String posYpersona,final String posZpersona,final String rssi,final String posXbaliza,final String posYbaliza,final String posZbaliza){

        String url = "http://www.museocarandnuria.es/subirMuestreo.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subido dato "+(Integer.parseInt(numMuestras.getText().toString())+1), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(getActivity(), "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("major", ""+LoginActivity.mimajor );
                params.put("posXpersona",posXpersona );
                params.put("posYpersona",posYpersona );
                params.put("posZpersona",posZpersona );
                params.put("rssi",rssi );
                params.put("posXbaliza",posXbaliza );
                params.put("posYbaliza",posYbaliza );
                params.put("posZbaliza",posZbaliza );
                params.put("sala",""+salaPresente);
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getActivity()).addColaPeticiones(postRequest);
    }




}
