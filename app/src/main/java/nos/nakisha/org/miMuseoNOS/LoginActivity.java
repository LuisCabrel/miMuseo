package nos.nakisha.org.miMuseoNOS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
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


public class LoginActivity extends AppCompatActivity {

    public Button inicioBtn;
    public boolean entrar;
    public EditText correo;
    public EditText clave;
    public TextView textoCambiante;
    public CheckBox mostrarClave;
    public static int mimajor;//para la BBDD desarrollador

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        entrar=false;
        correo=(EditText)findViewById(R.id.login_emailid);
        clave=(EditText)findViewById(R.id.login_password);
        textoCambiante=(TextView)findViewById(R.id.tVcambiante);

        mostrarClave = (CheckBox) findViewById(R.id.show_hide_password);
        mostrarClave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

             @Override
             public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

                 if (mostrarClave.isChecked()) {
                     clave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                 } else {
                     clave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                 }

             }
            }
        );

        inicioBtn=(Button)findViewById(R.id.loginBtn);
        inicioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                camposRellenos();
                if(entrar) {
                    cambiarAEsperando();

                    verificacion(correo.getText().toString(),clave.getText().toString());
                    //volver a cambiar esperando

                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        textoCambiante.setText("Acceso a configuración");
    }

    protected void cambiarAEsperando(){
        textoCambiante.setText("Espere a la verificación");
    }

    protected void camposRellenos() {
        if(correo.getText().toString().isEmpty()||clave.getText().toString().isEmpty()){
            entrar=false;
            Toast.makeText(this, "Por favor, rellene los campos", Toast.LENGTH_SHORT).show();
        }else{
            //peticion Volley
            entrar=true;
        }
    }

    protected void verificacion(final String museo, final String clave){
        String url = "http://www.museocarandnuria.es/loginDesarrollador.php?";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
        new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {

                try{
                JSONArray respuestaJSONarray=new JSONArray(response.toString());
                JSONObject respuestaJSONobjeto=respuestaJSONarray.getJSONObject(0);

                    int acceso=respuestaJSONobjeto.getInt("resultados");
                    mimajor=respuestaJSONobjeto.getInt("major");

                    String norte=respuestaJSONobjeto.getString("norte");
                    String ruido=respuestaJSONobjeto.getString("ruido");
                    String dt=respuestaJSONobjeto.getString("dt");
                    String mov=respuestaJSONobjeto.getString("mov");
                    String r=respuestaJSONobjeto.getString("r");
                    String segundos=respuestaJSONobjeto.getString("segundos");

                    if(acceso==0){
                        textoCambiante.setText("Nombre o clave incorrectos");

                    }else{
                        if(acceso==1){
                            Intent intent_formulario = new Intent(LoginActivity.this, FormularioActivity.class);
                            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getPreferences(MODE_PRIVATE);
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString("norte",norte);
                            editor.putString("ruidoVAR",ruido);
                            editor.putString("deltaT",dt);
                            editor.putString("movimiento",mov);
                            editor.putString("matrizR",r);
                            editor.putString("segundosGuardado",segundos);
                            editor.apply();
                            startActivity(intent_formulario);
                        }
                    }
                }catch (org.json.JSONException eJSON){
                    Toast.makeText(LoginActivity.this, "Fallo en Internet", Toast.LENGTH_SHORT).show();
                }
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Toast.makeText(LoginActivity.this, "Error en respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
                ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("fecha", f);
                params.put("museo", museo );
                params.put("clave",clave );

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //queue.add(postRequest);
        SingletonVolley.getInstanciaVolley(getApplicationContext()).addColaPeticiones(postRequest);
    }





}
