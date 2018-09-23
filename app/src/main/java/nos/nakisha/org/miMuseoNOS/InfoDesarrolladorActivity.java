package nos.nakisha.org.miMuseoNOS;

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

public class InfoDesarrolladorActivity extends AppCompatActivity {

    private EditText edMajor;
    private Button btMajor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_desarrollador);

        edMajor=(EditText)findViewById(R.id.edMajor);
        btMajor=(Button)findViewById(R.id.btMajor);
        btMajor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edMajor.getText().toString().isEmpty()){
                    EntradaMuseo.majorMuseo= Integer.parseInt(edMajor.getText().toString());
                    if(EntradaMuseo.museoDetectadoEntradaMuseo==false){
                        EntradaMuseo.museoDetectadoEntradaMuseo=true;
                        EntradaMuseo.unaVezEntradaMuseo=true;
                        descargarComprarTickets();
                        Toast.makeText(InfoDesarrolladorActivity.this, "Entrada habilitada sin BLE", Toast.LENGTH_SHORT).show();
                    }else{
                        EntradaMuseo.museoDetectadoEntradaMuseo=false;
                        EntradaMuseo.unaVezEntradaMuseo=false;
                        Toast.makeText(InfoDesarrolladorActivity.this, "Entrada por detección BLE", Toast.LENGTH_SHORT).show();
                        EntradaMuseo.urlCompraTickets="http://museocarandnuria.es/error_html.html";
                    }

                }

            }
        });
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
                                EntradaMuseo.urlCompraTickets=respuestaJSONobjeto.getString("ticketsat");
                            }


                        }catch (org.json.JSONException eJSON){
                            Toast.makeText(InfoDesarrolladorActivity.this, "Fallo en la descarga de tickets", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(InfoDesarrolladorActivity.this, "Error en respuesta del servidor", Toast.LENGTH_SHORT).show();
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
        SingletonVolley.getInstanciaVolley(this).addColaPeticiones(postRequest);
    }

}
