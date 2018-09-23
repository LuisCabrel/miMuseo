package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_BALIZAS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_CUADROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_PARAMETROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_SIGMATABLE;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_BALIZAS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_CUADROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_PARAMETROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_DELETE_TABLA_SIGMATABLE;

/**
 * Created by nakis on 06/01/2018.
 */

public class Tabalmacenamientodatos extends Fragment {

    public BBDD_Helper manejadorBBDD;

    private Button subirNube;
    private Button descargarNube;
    private Button borrarBBDD;
    private Button borrarBBDDServidor;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootViewAlmacenamDatos=inflater.inflate(R.layout.tab_almacenamientodatos,container,false);

        manejadorBBDD = new BBDD_Helper(getActivity());
        subirNube=(Button)rootViewAlmacenamDatos.findViewById(R.id.subirNubeBtn);
        subirNube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                leerPreferencias();
                leerParamentros();
                leerTodasBalizas();
                leerTodosCuadros();
                leerTodosSigmaP();

            }
        });

        descargarNube=(Button)rootViewAlmacenamDatos.findViewById(R.id.bajarNubeBtn);
        descargarNube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descargarPreferencias();
                descargarParametros();
                descargarBalizas();
                descargarCuadros();
                descargarSigmaP();
                Toast.makeText(getActivity(), "Descargando datos...", Toast.LENGTH_SHORT).show();

            }
        });

        borrarBBDD=(Button)rootViewAlmacenamDatos.findViewById(R.id.borrarBBDDBtn);
        borrarBBDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Se borran las 3 tablas:
                //parámetros, balizas y  cuadros
                SQLiteDatabase db=manejadorBBDD.getWritableDatabase() ;
                db.execSQL(SQL_DELETE_TABLA_PARAMETROS);
                db.execSQL(SQL_CREATE_TABLA_PARAMETROS);
                db.execSQL(SQL_DELETE_TABLA_BALIZAS);
                db.execSQL(SQL_CREATE_TABLA_BALIZAS);
                db.execSQL(SQL_DELETE_TABLA_CUADROS);
                db.execSQL(SQL_CREATE_TABLA_CUADROS);
                db.execSQL(SQL_DELETE_TABLA_SIGMATABLE);
                db.execSQL(SQL_CREATE_TABLA_SIGMATABLE);

                Toast.makeText(getActivity(), "Datos borrados", Toast.LENGTH_SHORT).show();

            }
        });

        borrarBBDDServidor=(Button)rootViewAlmacenamDatos.findViewById(R.id.borrarBBDDNubeBtn);
        borrarBBDDServidor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentoBorrarBBDDSV=new Intent(getActivity(),BorrarBBDDServidorActivity.class);
                startActivity(intentoBorrarBBDDSV);
            }
        });





        return rootViewAlmacenamDatos;
    }

    public void leerPreferencias(){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        float norte=Float.parseFloat(preferences.getString("norte",""+0));;
        float ruido=Float.parseFloat(preferences.getString("ruidoVAR",""+0));;
        int dt=Integer.parseInt(preferences.getString("deltaT",""+100));;
        int mov=Integer.parseInt(preferences.getString("movimiento",""+0));;
        int r=Integer.parseInt(preferences.getString("matrizR",""+0));;
        int segundos=Integer.parseInt(preferences.getString("segundosGuardado",""+1));
        subirPreferencias(""+norte,""+ruido,""+dt,""+mov,""+r,""+segundos);
    }

    public void leerParamentros(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.Parametros._ID,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_1,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_2,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_3,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_4,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_6,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_7,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_8
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor };

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
            //Sin coincidencias
        }else{
            int numFilas=0;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                subirParametros(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7));
                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();

    }

    public void leerTodasBalizas(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.Balizas._ID,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_1,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_6
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
            //"Sin coincidencias"
        }else{

            int numFilas=0;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){

                subirBalizas(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5));

                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();


    }//fin leerTodasBalizas

    public void leerTodosCuadros(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.Cuadros._ID,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_1,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_2,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_6,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_7,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_8,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_9
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor };

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
            //"Sin coincidencias"
        }else{
            String infoBBDD="\n";//infoBBDD=infoBBDD+"...\n";
            String salto="\n";
            int numFilas=0;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                subirCuadros(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getString(8));
                cursor.moveToNext();
            }
            numFilas=0;

        }
        cursor.close();


    }//fin leerTodosCuadros

    public void leerTodosSigmaP(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.SigmaTable._ID,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor };

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
            //"Sin coincidencias"
            Toast.makeText(getActivity(), "No leo ni sigmas ni ps", Toast.LENGTH_SHORT).show();
        }else{
            int numFilas=0;
            float sigma,p;
            int major,sala;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                sigma=cursor.getFloat(0);
                major=cursor.getInt(1);
                p=cursor.getFloat(2);
                sala=cursor.getInt(3);
                subirSigmaP(""+sigma,""+major,""+p,""+sala);
                Toast.makeText(getActivity(), "sigma: "+sigma+", p: "+p, Toast.LENGTH_SHORT).show();
                cursor.moveToNext();
            }
            numFilas=0;
        }
        cursor.close();

    }//fin leerTodosSigmaP

    protected void subirPreferencias( final String norte,final String ruido, final String dt, final String mov, final String r, final String segundos){

        String url = "http://www.museocarandnuria.es/actualizarPreferencias.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            //int acceso=respuestaJSONobjeto.getInt("resultados");


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subiendo datos", Toast.LENGTH_SHORT).show();
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

                params.put("major", ""+LoginActivity.mimajor );
                params.put("norte", norte );
                params.put("ruido",ruido );
                params.put("dt",dt );
                params.put("mov",mov );
                params.put("r",r );
                params.put("segundos",segundos );


                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }



    protected void subirParametros( final String major,final String ancho, final String largo, final String angulo, final String sala, final String ocx, final String ocy, final String media){

        String url = "http://www.museocarandnuria.es/subirParametros.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            //int acceso=respuestaJSONobjeto.getInt("resultados");


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subiendo datos", Toast.LENGTH_SHORT).show();
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

                params.put("major", major );
                params.put("ancho",ancho );
                params.put("largo",largo );
                params.put("angulo",angulo );
                params.put("sala",sala );
                params.put("ocx",ocx );
                params.put("ocy",ocy );
                params.put("media",media );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }



    protected void subirBalizas( final String major,final String idEstimote, final String posX, final String posY,final String posZ,final String sala){

        String url = "http://www.museocarandnuria.es/subirBalizas.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            //int acceso=respuestaJSONobjeto.getInt("resultados");

                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subiendo datos", Toast.LENGTH_SHORT).show();
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
                params.put("major", major );
                params.put("idEstimote",idEstimote );
                params.put("posX",posX );
                params.put("posY",posY );
                params.put("posZ",posZ );
                params.put("sala",sala );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }



    protected void subirCuadros( final String major,final String miurl, final String nombre, final String posX, final String posY,final String posZ,final String sala,final String mimedia,final String mifoto){

        String url = "http://www.museocarandnuria.es/subirCuadros.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            //int acceso=respuestaJSONobjeto.getInt("resultados");

                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subiendo datos", Toast.LENGTH_SHORT).show();
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
                params.put("major", major );
                params.put("url",miurl );
                params.put("nombre",nombre );
                params.put("posX",posX );
                params.put("posY",posY );
                params.put("posZ",posZ );
                params.put("sala",sala );
                params.put("media",mimedia );
                params.put("foto",mifoto );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }

    protected void subirSigmaP(final String sigma, final String major, final String p, final String sala){

        String url = "http://www.museocarandnuria.es/subirSigmaP.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);
                            //int acceso=respuestaJSONobjeto.getInt("resultados");

                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Subiendo datos", Toast.LENGTH_SHORT).show();
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
                params.put("sigma", sigma );
                params.put("major", major );
                params.put("p", p );
                params.put("sala", sala );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }

    public void guardarSigmaP(final String sigma,final String major,final String p, final String sala){
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

    protected void descargarSigmaP(){

        String url = "http://www.museocarandnuria.es/bajarSigma.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        int majorWeb;
                        String sigmaWeb;
                        String pWeb;
                        int salaWeb;

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            for(int i=0;i<respuestaJSONarray.length();i++){
                                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(i);
                                majorWeb=respuestaJSONobjeto.getInt("major");
                                sigmaWeb=respuestaJSONobjeto.getString("sigma");
                                pWeb=respuestaJSONobjeto.getString("p");
                                salaWeb=respuestaJSONobjeto.getInt("sala");
                                guardarSigmaP(sigmaWeb,""+majorWeb,pWeb,""+salaWeb);
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(getActivity(), "Fallo en la descarga", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
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
                            Toast.makeText(getActivity(), "Fallo en la descarga", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
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
                            Toast.makeText(getActivity(), "Fallo en la descarga", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
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
                            Toast.makeText(getActivity(), "Fallo en la descarga", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }

    public void guardarPreferencias(final String norte,final String ruido,final String dt, final String mov, final String r, final String segundos){
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this.getActivity());
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
                            Toast.makeText(getActivity(), "Fallo en la descarga", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(10 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SingletonVolley.getInstanciaVolley(getContext()).addColaPeticiones(postRequest);
    }



}
