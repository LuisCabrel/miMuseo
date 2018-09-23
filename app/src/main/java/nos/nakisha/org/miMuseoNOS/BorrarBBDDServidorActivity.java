package nos.nakisha.org.miMuseoNOS;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class BorrarBBDDServidorActivity extends AppCompatActivity {

    public BBDD_Helper manejadorBBDD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrar_bbddservidor);

        manejadorBBDD = new BBDD_Helper(getApplicationContext());

        final FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fabBorrarBBDDSV);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Snackbar.make(view,"¿Está seguro?", Snackbar.LENGTH_LONG)
                        .setAction("SI", new View.OnClickListener() {
                            @Override public void onClick(View view) {
                                borrado();
                            }
                        }).show();
            }
        });

        Toast.makeText(BorrarBBDDServidorActivity.this, "¡Acción Peligrosa!", Toast.LENGTH_LONG).show();
    }

    public void borrado(){
        borrarParametrosAnteriores();
        borrarBalizasAnteriores();
        borrarCuadrosAnteriores();
        borrarSigmaPAnteriores();
    }

    protected void borrarParametrosAnteriores(){
        String url = "http://www.museocarandnuria.es/borrarParametrosAnteriores.php?";
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
                            Toast.makeText(BorrarBBDDServidorActivity.this, "Parámetros borrados", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(BorrarBBDDServidorActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
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
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }

    protected void borrarBalizasAnteriores(){
        String url = "http://www.museocarandnuria.es/borrarBalizasAnteriores.php?";
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
                            Toast.makeText(BorrarBBDDServidorActivity.this, "Balizas borradas", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(BorrarBBDDServidorActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
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
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }

    protected void borrarCuadrosAnteriores(){
        String url = "http://www.museocarandnuria.es/borrarCuadrosAnteriores.php?";
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
                            Toast.makeText(BorrarBBDDServidorActivity.this, "Cuadros borrados", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(BorrarBBDDServidorActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
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
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }

    protected void borrarSigmaPAnteriores(){
        String url = "http://www.museocarandnuria.es/borrarSigmaPAnteriores.php?";
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
                            Toast.makeText(BorrarBBDDServidorActivity.this, "Sigma y p borrados", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(BorrarBBDDServidorActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
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
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }
}
