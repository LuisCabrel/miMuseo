package nos.nakisha.org.miMuseoNOS;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class FormularioActivity extends AppCompatActivity {

    protected static final int SOLICITUD_PERMISO_ALMACENAMIENTO=1;

    public Button insertarDatosBtn;
    public Button muestreoEst;
    public Button kalmanEst;
    public Button gestionBBDD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);

        insertarDatosBtn=(Button)findViewById(R.id.insertarDatosBtn);
        insertarDatosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarEscenario();
            }
        });

        muestreoEst=(Button)findViewById(R.id.muestreoEstBtn);
        muestreoEst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarMuestreoEstatica();
            }
        });
        kalmanEst=(Button)findViewById(R.id.escEstKalmanBtn);
        kalmanEst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(permisoEscribirFichero()){
                    lanzarKalmanEstatica();
                }

            }
        });
        gestionBBDD=(Button)findViewById(R.id.gestionBBDD);
        gestionBBDD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lanzarGestionBBDD();
            }
        });

        Toast.makeText(FormularioActivity.this, ""+LoginActivity.mimajor, Toast.LENGTH_SHORT).show();

        lanzarSolicitarPermiso();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.desarrollador,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        if(id==R.id.preferencias){
            lanzarPreferencias();
            return true;
        }
        if(id==R.id.informacion){
            Intent intentInformacion=new Intent(FormularioActivity.this,InfoDesarrolladorActivity.class);
            startActivity(intentInformacion);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void lanzarPreferencias(){
        Intent intent_preferencias=new Intent(FormularioActivity.this,PreferenciasActivity.class);
        startActivity(intent_preferencias);
    }

    public void lanzarInsertarDtos(){
        Intent intent_insertarDatos=new Intent(FormularioActivity.this, InsertarDatos.class);
        startActivity(intent_insertarDatos);
    }

    public void lanzarEscenario(){
        Intent intent_escenario=new Intent(FormularioActivity.this, EscenarioActivity.class);
        startActivity(intent_escenario);
    }

    public void lanzarMuestreoEstatica(){
        Intent intentoMuestreoEst=new Intent(FormularioActivity.this, SelectorDeSalaActivity.class);
        startActivity(intentoMuestreoEst);
    }

    public void lanzarKalmanEstatica(){
        //Intent intentoKalmanEst=new Intent(FormularioActivity.this, KalmanEst.class);
        //startActivity(intentoKalmanEst);
        Intent intentoPosicionamiento=new Intent(FormularioActivity.this, TiposDePosicionamientoActivity.class);
        startActivity(intentoPosicionamiento);
    }

    public void lanzarGestionBBDD(){
        Intent intentoGestionBBDD=new Intent(FormularioActivity.this, GestionBBDD.class);
        startActivity(intentoGestionBBDD);
    }



    //PARTE COMÚN PARA POSICIONAMIENTO --> QUITAR DE AQUI

    @Override public void onRequestPermissionsResult(int requestCode,
                                                     String[] permissions, int[] grantResults) {
        if (requestCode == SOLICITUD_PERMISO_ALMACENAMIENTO) {
            if (grantResults.length== 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //lanzarSolicitarPermiso();


            } else {
                Toast.makeText(this, "Sin el permiso, no puedo realizar la acción", Toast.LENGTH_SHORT).show();
                lanzarSolicitarPermiso();
            }
        }

    }

    public void lanzarSolicitarPermiso(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //Toast.makeText(this, "lalala bien", Toast.LENGTH_SHORT).show();

        } else {
            solicitarPermiso(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Sin el permiso"+
                            " de escritura no se puede rellenar el fichero de posicionamiento.",
                    SOLICITUD_PERMISO_ALMACENAMIENTO, this);
        }

    }



    public static void solicitarPermiso(final String permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)){
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode);
                        }})
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
        }
    }

    public boolean permisoEscribirFichero(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            //si tengo el permiso, puedo escribir en la SD
            return true;
        }
        lanzarSolicitarPermiso();
        return false;
    }

}
