package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class ModificarSalaActivity extends AppCompatActivity implements SensorEventListener {

    public BBDD_Helper manejadorBBDD;

    //vbles Brújula
    private ImageView mibrujula;
    private TextView anguloDesviacion;
    private SensorManager mSensorManager;
    private Sensor miacelerometro;
    private Sensor mimagnetometro;
    private boolean banderaEventoAcelerometro=false;//false si no lo hay; true si hay evento
    private boolean banderaEventoMagnetometro=false;
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    private double gradosactuales;

    //vbles view:
    public TextView numSala;
    public int salaPresente;

    public EditText ocxET;
    public EditText ocyET;
    public EditText anchoET;
    public EditText largoET;
    public EditText anguloET;
    public EditText mediaSalaET;

    public Button capturarBTN;
    public Button actualiarBTN;
    public Button introducirBTN;

    //vbles para el Canvas:

    public static int anchoCanvas=1;
    public static int altoCanvas=1;
    private double miDesviacion;//uso esta vble para almacenar en la BBDD (por si muevo el movil antes de guardar)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_sala);

        manejadorBBDD = new BBDD_Helper(this);

        numSala=(TextView)findViewById(R.id.numSalaTxtVw);
        Bundle extras=getIntent().getExtras();
        salaPresente=extras.getInt("sala");
        numSala.setText("Sala nº "+extras.getInt("sala"));
        //Toast.makeText(ModificarSalaActivity.this, "sala "+extras.getInt("sala"), Toast.LENGTH_SHORT).show();

        gradosactuales=0f;
        anguloDesviacion=(TextView)findViewById(R.id.angulotextView);
        mibrujula=(ImageView)findViewById(R.id.flechanegra);
        mibrujula.setImageResource(R.drawable.ic_arrow);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        miacelerometro=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mimagnetometro=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ocxET=(EditText)findViewById(R.id.ocxEditText);
        ocyET=(EditText)findViewById(R.id.ocyEditText);
        anchoET=(EditText)findViewById(R.id.anchoEditText);
        largoET=(EditText)findViewById(R.id.largoEditText);
        anguloET=(EditText)findViewById(R.id.anguloEditText);
        mediaSalaET=(EditText)findViewById(R.id.mediaSalaEditText);

        capturarBTN=(Button)findViewById(R.id.capturarAnguloBtn);
        capturarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturoAngDesv();
            }
        });
        actualiarBTN=(Button)findViewById(R.id.actualizarDatosBtn);
        actualiarBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recojoDatos();
                if(anchoET.getText().toString().isEmpty() ||
                        largoET.getText().toString().isEmpty()||
                        anguloET.getText().toString().isEmpty()||
                        ocxET.getText().toString().isEmpty()||
                        ocyET.getText().toString().isEmpty()||
                        mediaSalaET.getText().toString().isEmpty()){
                    Toast.makeText(ModificarSalaActivity.this, "Por favor, rellene todos los campos para poder guardar", Toast.LENGTH_SHORT).show();
                }else{
                    //borrarParamentros();
                    //guardarParamentros(anchoCanvas,altoCanvas,Double.parseDouble(anguloET.getText().toString()),Integer.parseInt(ocxET.getText().toString()),Integer.parseInt(ocyET.getText().toString()));
                    actualizarParamentros(anchoCanvas,altoCanvas,Double.parseDouble(anguloET.getText().toString()),Integer.parseInt(ocxET.getText().toString()),Integer.parseInt(ocyET.getText().toString()),mediaSalaET.getText().toString());
                    Toast.makeText(ModificarSalaActivity.this, "Campos actualizados", Toast.LENGTH_SHORT).show();
                }

            }
        });
        introducirBTN=(Button)findViewById(R.id.introducirMasDatosBtn);
        introducirBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ModificarSalaActivity.this,InsertarDatos.class);
                intent.putExtra("sala",salaPresente);
                startActivity(intent);
            }
        });

        try{
            leerSalas();
        }catch (Exception e){
            Toast.makeText(ModificarSalaActivity.this, "Hay un problema", Toast.LENGTH_SHORT).show();
        }

    }//fin onCreate

    @Override
    public void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        mSensorManager.registerListener(this, miacelerometro, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mimagnetometro, SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    public void onPause() {
        super.onPause();
        // Don't receive any more updates from either sensor.
        mSensorManager.unregisterListener(this,mimagnetometro);
        mSensorManager.unregisterListener(this,miacelerometro);
    }

    @Override
    public void onSaveInstanceState(Bundle estado) {
        //definimos estado
        estado.putDouble("gradosactuales",gradosactuales);
        //lo guardamos
        super.onSaveInstanceState(estado);
    }

    @Override
    public  void onRestoreInstanceState(Bundle estado){
        //recuperamos información almacenada
        super.onRestoreInstanceState(estado);
        //la utilizamos
        gradosactuales=estado.getFloat("gradosactuales");
        anguloDesviacion.setText(""+gradosactuales);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == miacelerometro) {
            System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
            banderaEventoAcelerometro=true;
        }
        else if (event.sensor == mimagnetometro) {
            System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
            banderaEventoMagnetometro=true;
        }

        if(banderaEventoMagnetometro&&banderaEventoAcelerometro){
            //actualizamos las matrices: primero la de rotacion y luego la de orientacion
            // Update rotation matrix, which is needed to update orientation angles.
            mSensorManager.getRotationMatrix(mRotationMatrix, null, mAccelerometerReading, mMagnetometerReading);

            // "mRotationMatrix" now has up-to-date information.

            mSensorManager.getOrientation(mRotationMatrix, mOrientationAngles);

            // "mOrientationAngles" now has up-to-date information.
            //con la matriz de orientacion actualizada, saco el azimuth
            float azimuthInRadians=mOrientationAngles[0];
            //float azimuthInDegress=(float)(Math.toDegrees(azimuthInRadians)+360)%360;
            //AQUI CORREGIMOS ÁNGULO FINAL
            float azimuthInDegress;

            if(depie()){
                azimuthInDegress=(float)(Math.toDegrees(azimuthInRadians)-90);
            }else{
                if(cantoizqarriba()){
                    azimuthInDegress=(float)(Math.toDegrees(azimuthInRadians)-180);
                }else{
                    azimuthInDegress=(float)(Math.toDegrees(azimuthInRadians)-0);
                }

            }

            RotateAnimation ra= new RotateAnimation((float)gradosactuales,-azimuthInDegress, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            ra.setDuration(250);
            ra.setFillAfter(true);

            try{
                if(ra!=null){
                    mibrujula.startAnimation(ra);
                }
            }
            catch(Exception e){
                Log.d("Animation","falla");
            }

            gradosactuales=-azimuthInDegress;

            if(gradosactuales<0){
                anguloDesviacion.setText(""+(180-Math.ceil(gradosactuales)) );//try floor
            }else{
                if (gradosactuales>180){
                    anguloDesviacion.setText(""+(360-(Math.ceil(gradosactuales)-180)) );//try floor
                }else{
                    anguloDesviacion.setText(""+(180-Math.ceil(gradosactuales)) );//try floor
                }
            }


        }
        //bajo las banderas
        //banderaEventoAcelerometro=false;
        //banderaEventoMagnetometro=false;
    }//fin onSensorChanged(SensorEvent event)

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //no hago nada
    }

    public boolean cantoizqarriba(){

        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        int orientation = display.getRotation();

        if (orientation == Surface.ROTATION_90){
            //Toast.makeText(this,"90",Toast.LENGTH_LONG).show();
            return false;
        }
        if( orientation == Surface.ROTATION_270) {
            //Toast.makeText(this,"270",Toast.LENGTH_LONG).show();
            return true;
        }

        return true;
    }

    public boolean depie(){

        int orientationdevice = getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }



    public void leerSalas(){
        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_2,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_3,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_4,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_6,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_7,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_8
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            Toast.makeText(ModificarSalaActivity.this,"Sin salas",Toast.LENGTH_LONG).show();

        }else{
            int i=0;
            String ancho,largo,angulo,ocx,ocy,media;
            String resumen="";

            //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
            ancho= cursor.getString(0);
            largo= cursor.getString(1);
            angulo=cursor.getString(2);
            ocx=cursor.getString(3);
            ocy=cursor.getString(4);
            media=cursor.getString(5);

            anchoCanvas=Integer.parseInt(ancho);
            altoCanvas=Integer.parseInt(largo);
            miDesviacion=Double.parseDouble(angulo);
            anchoET.setText(ancho);
            largoET.setText(largo);
            anguloET.setText(angulo);
            ocxET.setText(ocx);
            ocyET.setText(ocy);
            mediaSalaET.setText(media);

        }
        cursor.close();

    }//fin de leerSalas()

    public void recojoDatos(){

        if(anchoET.getText().toString().isEmpty() ||
                largoET.getText().toString().isEmpty()||
                anguloET.getText().toString().isEmpty()||
                ocxET.getText().toString().isEmpty()||
                ocyET.getText().toString().isEmpty()||
                mediaSalaET.getText().toString().isEmpty()){
            Toast.makeText(ModificarSalaActivity.this, "Por favor, rellene todos los campos para poder guardar", Toast.LENGTH_SHORT).show();
        }else{
            int ancho,largo,soporte;
            ancho=1;largo=1;soporte=1;
            ancho=Integer.parseInt(anchoET.getText().toString());
            largo=Integer.parseInt(largoET.getText().toString());
            if(ancho>largo){
                soporte=ancho;ancho=largo;largo=soporte;
            }
            anchoCanvas=ancho;altoCanvas=largo;
        }

    }//fin de recojoDatos()

    public void capturoAngDesv(){
        anguloET.setText(anguloDesviacion.getText().toString());
        miDesviacion=gradosactuales;
    }

    public void borrarParamentros(){

        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };
// Issue SQL statement.
        db.delete(Estructura_BD.Parametros.TABLE_NAME, selection, selectionArgs);

    }


    public void guardarParamentros(int ancho, int largo, double angCorreccion, int ocx, int ocy,String media){

        //añadir un nuevo registro de parámetros
        // 1)recojo los argumentos (arriba)
        // 2)recojo los valores de la BBDD:
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // 3)Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_1, LoginActivity.mimajor);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_2, ancho);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_3, largo);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_4, angCorreccion);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_5, salaPresente);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_6, ocx);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_7, ocy);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_8, media);
        // 4)Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.Parametros.TABLE_NAME, null, cjto_valores_nuevos);
    }

    public void actualizarParamentros(int ancho, int largo, double angCorreccion, int ocx, int ocy, String media){

        //recoger lo escrito (arriba)
        // 2)recojolos valores de la BBDD:
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();

// New value for one column
        ContentValues cjto_valores_actualizados = new ContentValues();
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_1, LoginActivity.mimajor);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_2, ancho);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_3, largo);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_4, angCorreccion);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_5, salaPresente);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_6, ocx);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_7, ocy);
        cjto_valores_actualizados.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_8, media);

// Which row to update, based on the title
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5 +" LIKE ?";
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

        int count = db.update(
                Estructura_BD.Parametros.TABLE_NAME,
                cjto_valores_actualizados,
                selection,
                selectionArgs);

    }
}
