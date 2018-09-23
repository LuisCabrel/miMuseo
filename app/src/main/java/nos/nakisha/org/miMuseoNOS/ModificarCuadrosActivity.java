package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ModificarCuadrosActivity extends AppCompatActivity {

    //vbles View:
    private EditText posX;
    private EditText posY;
    private EditText posZ;
    private EditText url;
    private EditText media;
    private EditText foto;

    private TextView salaTV;
    private TextView cuadroTV;

    private Button actualizarBtn;
    private Button borrarBtn;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    private int salaPresente;
    private String cuadroPresente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_cuadros);

        Bundle extras=getIntent().getExtras();
        salaPresente=extras.getInt("sala");
        cuadroPresente=extras.getString("cuadro");
        Toast.makeText(this, "sala "+salaPresente+", cuadro "+cuadroPresente, Toast.LENGTH_SHORT).show();

        manejadorBBDD = new BBDD_Helper(ModificarCuadrosActivity.this);

        posX=(EditText)findViewById(R.id.posXcuadroModif);
        posY=(EditText)findViewById(R.id.posYcuadroModif);
        posZ=(EditText)findViewById(R.id.posZcuadroModif);
        url=(EditText)findViewById(R.id.urlCuadroModif);
        media=(EditText)findViewById(R.id.mediaCuadroModif);
        foto=(EditText)findViewById(R.id.fotoCuadroModif);

        salaTV=(TextView) findViewById(R.id.numeroSalaModifTxtVw);
        salaTV.setText("Sala nº: "+salaPresente);

        cuadroTV=(TextView)findViewById(R.id.nombreCuadroModifTxtVw);
        cuadroTV.setText(""+cuadroPresente);

        actualizarBtn=(Button)findViewById(R.id.actualizarCuadroBtn);
        actualizarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarCuadro();
                leerCuadro();
            }
        });

        borrarBtn=(Button)findViewById(R.id.borrarCuadroBtn);
        borrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarCuadro();
                leerCuadro();
            }
        });

        leerCuadro();
    }

    public void leerCuadro(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                //Estructura_BD.Parametros._ID,
                //Estructura_BD.Parametros.NOMBRE_COLUMNA_1,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_2,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_6,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_8,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_9
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_3+" = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+cuadroPresente, ""+salaPresente };

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
            //nada
        }else{
            url.setText(cursor.getString(0));
            posX.setText(cursor.getString(1));
            posY.setText(cursor.getString(2));
            posZ.setText(cursor.getString(3));
            media.setText(cursor.getString(4));
            foto.setText(cursor.getString(5));
        }
        cursor.close();


    }//fin leerCuadro()

    public void borrarCuadro(){

        //recoger lo escrito (arriba)
        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        //String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ?";
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_3 +" LIKE ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7+" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+cuadroPresente,""+salaPresente };
// Issue SQL statement.
        db.delete(Estructura_BD.Cuadros.TABLE_NAME, selection, selectionArgs);

        Toast.makeText(this, "Borrado cuadro "+cuadroPresente, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void actualizarCuadro(){
        if(condicionParaActualizar()){

            //recoger lo escrito (arriba)
            // 2)recojolos valores de la BBDD:
            SQLiteDatabase db =manejadorBBDD.getWritableDatabase();

// New value for one column
            ContentValues cjto_valores_actualizados = new ContentValues();
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_1, ""+LoginActivity.mimajor);
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_2, url.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_3, ""+cuadroPresente);
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_4, posX.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_5, posY.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_6, posZ.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_7, ""+salaPresente);
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_8, media.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_9, foto.getText().toString());

// Which row to update, based on the title
            String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_3+" LIKE ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7+" LIKE ?";
            String[] selectionArgs = { ""+LoginActivity.mimajor,""+cuadroPresente,""+salaPresente };

            int count = db.update(
                    Estructura_BD.Cuadros.TABLE_NAME,
                    cjto_valores_actualizados,
                    selection,
                    selectionArgs);

            Toast.makeText(this, "Modificado cuadro "+cuadroPresente, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean condicionParaActualizar(){
        boolean condicion=false;
        if(!url.getText().toString().isEmpty()&&
                !media.getText().toString().isEmpty()&&
                !foto.getText().toString().isEmpty()&&
                !posX.getText().toString().isEmpty()&&
                !posY.getText().toString().isEmpty()&&
                !posZ.getText().toString().isEmpty()){
            condicion=true;
        } else{
            condicion=false;
            Toast.makeText(ModificarCuadrosActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
        }

        return condicion;
    }

}
