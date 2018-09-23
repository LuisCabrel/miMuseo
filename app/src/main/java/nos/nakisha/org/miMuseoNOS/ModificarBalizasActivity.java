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

public class ModificarBalizasActivity extends AppCompatActivity {

    //vbles View:
    private EditText posX;
    private EditText posY;
    private EditText posZ;

    private TextView idEstimote;
    private TextView salaET;

    private Button actualizarBtn;
    private Button borrarBtn;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    private int salaPresente,tagPresente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_balizas);

        Bundle extras=getIntent().getExtras();
        salaPresente=extras.getInt("sala");
        tagPresente=extras.getInt("tag");

        manejadorBBDD = new BBDD_Helper(ModificarBalizasActivity.this);

        posX=(EditText)findViewById(R.id.posXbalizaModif);
        posY=(EditText)findViewById(R.id.posYbalizaModif);
        posZ=(EditText)findViewById(R.id.posZbalizaModif);

        idEstimote=(TextView)findViewById(R.id.id_estimoteModif);
        idEstimote.setText(""+tagPresente);

        salaET=(TextView)findViewById(R.id.numSalaModifET);
        salaET.setText("Sala nº: "+salaPresente);

        actualizarBtn=(Button)findViewById(R.id.actualizarBalizaBtn);
        actualizarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarBaliza();
            }
        });

        borrarBtn=(Button)findViewById(R.id.borrarBalizaBtn);
        borrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarBaliza();
            }
        });


        try{
            leerBaliza();
        }catch (Exception e){

        }

    }//fin onCreate

    public void borrarBaliza(){

        //recoger lo escrito (arriba)
        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        //String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ?";
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6 +" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+tagPresente,""+salaPresente };
// Issue SQL statement.
        db.delete(Estructura_BD.Balizas.TABLE_NAME, selection, selectionArgs);

        Toast.makeText(this, "Borrado tag "+idEstimote.getText().toString(), Toast.LENGTH_SHORT).show();
        finish();
    }

    public void actualizarBaliza(){
        if(condicionParaActualizar()){

            //recoger lo escrito (arriba)
            // 2)recojolos valores de la BBDD:
            SQLiteDatabase db =manejadorBBDD.getWritableDatabase();

// New value for one column
            ContentValues cjto_valores_actualizados = new ContentValues();
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_1, ""+LoginActivity.mimajor);
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_2, ""+tagPresente);
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_3, posX.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_4, posY.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_5, posZ.getText().toString());
            cjto_valores_actualizados.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_6, ""+salaPresente);

// Which row to update, based on the title
            String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_1 +" LIKE ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6 +" LIKE ?";
            String[] selectionArgs = { ""+tagPresente,""+LoginActivity.mimajor,""+salaPresente };

            int count = db.update(
                    Estructura_BD.Balizas.TABLE_NAME,
                    cjto_valores_actualizados,
                    selection,
                    selectionArgs);

            Toast.makeText(this, "Modificado tag "+idEstimote.getText().toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean condicionParaActualizar(){
       boolean condicion=false;
       if(!posX.getText().toString().isEmpty()&&
               !posY.getText().toString().isEmpty()&&
               !posZ.getText().toString().isEmpty()){
           condicion=true;
       } else{
           condicion=false;
           Toast.makeText(ModificarBalizasActivity.this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
       }

        return condicion;
    }

    public void leerBaliza(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6+ " = ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_2+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente,""+tagPresente };

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
            //nada
        }else{
            int posicX,posicY,posicZ;

            posicX=cursor.getInt(0);
            posicY=cursor.getInt(1);
            posicZ=cursor.getInt(2);

            posX.setText(""+posicX);
            posY.setText(""+posicY);
            posZ.setText(""+posicZ);

        }
        cursor.close();
    }//fin de leerBaliza()


}
