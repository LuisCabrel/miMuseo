package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ParametrosSigmaPActivity extends AppCompatActivity {

    public EditText sigmaET;
    public EditText pET;
    public Button forzarBtn;
    public Button sigmaBtn;
    public Button pBtn;
    public Button obtenerBtn;
    public Button verSigmayP;
    public int salaActual;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametros_sigma_p);

        Bundle extras=getIntent().getExtras();
        salaActual=extras.getInt("sala");
        Toast.makeText(ParametrosSigmaPActivity.this, "sala: "+salaActual, Toast.LENGTH_SHORT).show();

        manejadorBBDD = new BBDD_Helper(ParametrosSigmaPActivity.this);

        sigmaET=(EditText)findViewById(R.id.sigmaEditText);
        pET=(EditText)findViewById(R.id.pEditText);

        forzarBtn=(Button)findViewById(R.id.forzarSigmaPBtn);
        forzarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forzarSigmaP();
            }
        });

        sigmaBtn=(Button)findViewById(R.id.actualizarSigmaBtn);
        sigmaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarSigma();
            }
        });

        pBtn=(Button)findViewById(R.id.actualizarPBtn);
        pBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarP();
            }
        });

        obtenerBtn=(Button)findViewById(R.id.descargarSigmaPBtn);
        obtenerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bajarSigmayP();

            }
        });

        verSigmayP=(Button)findViewById(R.id.verSigmayPBtn);
        verSigmayP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try {
                    leerSigmaYP();
                //}catch (Exception e){}

            }
        });

        try {
            leerSigmaYP();
        }catch (Exception e){}

    }//fin onCreate

    protected void bajarSigmayP(){

        String url = "http://www.museocarandnuria.es/bajarSigmayPsala.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            double sigma, p;
                            int major, sala;
                            for(int i=0;i<respuestaJSONarray.length();i++) {
                                JSONObject respuestaJSONobjeto = respuestaJSONarray.getJSONObject(i);
                                sigma = respuestaJSONobjeto.getDouble("sigma");
                                p = respuestaJSONobjeto.getDouble("p");
                                major = respuestaJSONobjeto.getInt("major");
                                sala = respuestaJSONobjeto.getInt("sala");
                                sigmaET.setText("" + sigma);
                                pET.setText("" + p);
                                try {
                                    borrarSigmayP();
                                } catch (Exception e) {
                                    Toast.makeText(ParametrosSigmaPActivity.this, "No he podido BORRAR ni p ni sigma en BBDD local", Toast.LENGTH_SHORT).show();
                                }
                                try {
                                    guardarSigmayP("" + major, "" + sigma, "" + p, "" + sala);
                                } catch (Exception e) {
                                    Toast.makeText(ParametrosSigmaPActivity.this, "No he podido guardar sigma ni p en BBDD local", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(ParametrosSigmaPActivity.this, "Fallo en Internet", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(ParametrosSigmaPActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("major", ""+LoginActivity.mimajor );
                params.put("sala",""+salaActual);

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }

    protected void forzarSigmaP(){

        String url = "http://www.museocarandnuria.es/subirSigmaPForzados.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(ParametrosSigmaPActivity.this, "Petición enviada", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(ParametrosSigmaPActivity.this, "Error en subida", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("major", ""+LoginActivity.mimajor );
                params.put("p", ""+pET.getText().toString() );
                params.put("sigma", ""+sigmaET.getText().toString() );
                params.put("sala", ""+salaActual );

                return params;
            }
        };
        if(!pET.getText().toString().isEmpty()&&!sigmaET.getText().toString().isEmpty()){
            postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
        } else{
            Toast.makeText(ParametrosSigmaPActivity.this, "Rellene p y sigma", Toast.LENGTH_SHORT).show();
        }
    }

    protected void actualizarSigma(){

        String url = "http://www.museocarandnuria.es/subirSigma.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(ParametrosSigmaPActivity.this, "Petición enviada", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(ParametrosSigmaPActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("major", ""+LoginActivity.mimajor );
                params.put("p", ""+pET.getText().toString() );
                params.put("sala", ""+salaActual );

                return params;
            }
        };
        if(!pET.getText().toString().isEmpty()){
            postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
        } else{
            Toast.makeText(ParametrosSigmaPActivity.this, "Rellene p", Toast.LENGTH_SHORT).show();
        }


    }

    protected void actualizarP(){

        String url = "http://www.museocarandnuria.es/subirP.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONArray respuestaJSONarray=new JSONArray(response.toString());
                            JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(ParametrosSigmaPActivity.this, "p subido al SV", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(ParametrosSigmaPActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("major", ""+LoginActivity.mimajor );
                params.put("p", ""+pET.getText().toString() );
                params.put("sala", ""+salaActual );

                return params;
            }
        };
        if(!pET.getText().toString().isEmpty()){
            postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
        } else{
            Toast.makeText(ParametrosSigmaPActivity.this, "Rellene p", Toast.LENGTH_SHORT).show();
        }
    }


    public void leerSigmaYP(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.SigmaTable._ID,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1,
                //Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " = ? AND " + Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = {""+LoginActivity.mimajor,""+salaActual };

        //Cómo quieres que se ordenen los resultados del resultado Cursor
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
            //Toast.makeText(getApplicationContext(),"Sin coincidencias",Toast.LENGTH_LONG).show();
        }else{
            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            sigmaET.setText(cursor.getString(0));
            pET.setText(cursor.getString(1));

        }
        cursor.close();

    }

    public void borrarSigmayP(){

        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " LIKE ? AND "+Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4 + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaActual };
// Issue SQL statement.
        db.delete(Estructura_BD.SigmaTable.TABLE_NAME, selection, selectionArgs);

    }

    public void guardarSigmayP(final String major, final String sigma, final String p, final String sala){

        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // 3)Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1, sigma);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2, major);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3, p);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4, sala);
        // 4)Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.SigmaTable.TABLE_NAME, null, cjto_valores_nuevos);

    }

}
