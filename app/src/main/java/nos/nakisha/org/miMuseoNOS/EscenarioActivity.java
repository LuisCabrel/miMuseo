package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EscenarioActivity extends AppCompatActivity {

    public BBDD_Helper manejadorBBDD;

    //vbles view:
    public CheckBox verSalasChBx;
    public String infoSalas;

    public TextView salasExistentes;

    public EditText anadirSalaEditText;
    public EditText borrarSalaEditText;
    public EditText modificarSalaEditText;

    public Button anadirSalaBtn;
    public Button borrarSalaBtn;
    public Button modificarSalaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escenario);

        manejadorBBDD = new BBDD_Helper(this);

        verSalasChBx=(CheckBox)findViewById(R.id.checkBoxVerSalas);

        verSalasChBx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

                if (verSalasChBx.isChecked()) {
                    salasExistentes.setText(infoSalas);
                    salasExistentes.setVisibility(View.VISIBLE);
                } else {
                    //salasExistentes.setText("");
                    //salasExistentes.setVisibility(View.INVISIBLE);
                    salasExistentes.setVisibility(View.GONE);
                }

            }
        });

        salasExistentes=(TextView)findViewById(R.id.salasExistentesTxtVw);
        salasExistentes.setText("Salas: \n");
        infoSalas="";

        anadirSalaEditText=(EditText)findViewById(R.id.editTextAnadirSala);
        borrarSalaEditText=(EditText)findViewById(R.id.editTextBorrarSala);
        modificarSalaEditText=(EditText)findViewById(R.id.editTextModificarSala);

        anadirSalaBtn=(Button)findViewById(R.id.anadirSalaBtn);
        anadirSalaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!anadirSalaEditText.getText().toString().isEmpty()){
                    try{
                        if(unicidadSala(Integer.parseInt(anadirSalaEditText.getText().toString()))){
                            guardarSala();
                            guardarSigmaYPSala();
                            leerSalas();
                            Toast.makeText(EscenarioActivity.this, "Añadida sala "+anadirSalaEditText.getText().toString(), Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        Toast.makeText(EscenarioActivity.this, "Hay un problema", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(EscenarioActivity.this, "Indique el nº de la sala que quiere añadir", Toast.LENGTH_SHORT).show();
                }

            }
        });

        borrarSalaBtn=(Button)findViewById(R.id.borrarSalaBtn);
        borrarSalaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!borrarSalaEditText.getText().toString().isEmpty()){
                    borrarSala();
                    leerSalas();

                }else{
                    Toast.makeText(EscenarioActivity.this, "Indique qué sala quiere borrar", Toast.LENGTH_SHORT).show();
                }

            }
        });

        modificarSalaBtn=(Button)findViewById(R.id.modificarSalaBtn);
        modificarSalaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!modificarSalaEditText.getText().toString().isEmpty()){
                    if(buscaSala(Integer.parseInt(modificarSalaEditText.getText().toString()))){
                        Intent intentoModificarSala=new Intent(EscenarioActivity.this,ModificarSalaActivity.class);
                        intentoModificarSala.putExtra("sala",Integer.parseInt(modificarSalaEditText.getText().toString()));
                        startActivity(intentoModificarSala);
                    }
                }else{
                    Toast.makeText(EscenarioActivity.this, "Indique qué sala quiere modificar", Toast.LENGTH_SHORT).show();
                }
            }
        });
        

    }//fin del onCreate

    @Override
    public void onResume(){
        super.onResume();
        try{
            leerSalas();
        }catch (Exception e){
            Toast.makeText(this, "Hay un problema", Toast.LENGTH_SHORT).show();
        }
    }

    public void borrarSala(){
        try{
            borrarCuadrosSala();
        }catch (Exception e){
            Toast.makeText(EscenarioActivity.this, "Problema con cuadros", Toast.LENGTH_SHORT).show();
        }

        try{
            borrarBalizasSala();
        }catch (Exception e){
            Toast.makeText(EscenarioActivity.this, "Problema con balizas", Toast.LENGTH_SHORT).show();
        }

        try{
            borrarSigmayPSala();
        }catch (Exception e){
            Toast.makeText(EscenarioActivity.this, "Problema con sigma y p", Toast.LENGTH_SHORT).show();
        }

        try{
            borrarParametrosSala();
        }catch (Exception e){
            Toast.makeText(EscenarioActivity.this, "Problema con parametros", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(EscenarioActivity.this, "Eliminada sala "+borrarSalaEditText.getText().toString()+" y su contenido", Toast.LENGTH_SHORT).show();
    }

    public void borrarParametrosSala(){
        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5+" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,borrarSalaEditText.getText().toString() };
// Issue SQL statement.
        db.delete(Estructura_BD.Parametros.TABLE_NAME, selection, selectionArgs);
    }

    public void borrarBalizasSala(){
        //recoger lo escrito (arriba)
        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        //String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ?";
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6 +" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,borrarSalaEditText.getText().toString() };
// Issue SQL statement.
        db.delete(Estructura_BD.Balizas.TABLE_NAME, selection, selectionArgs);
    }
    public void borrarCuadrosSala(){
        //recoger lo escrito (arriba)
        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        //String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_2 + " LIKE ?";
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " LIKE ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7 +" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,borrarSalaEditText.getText().toString() };
// Issue SQL statement.
        db.delete(Estructura_BD.Cuadros.TABLE_NAME, selection, selectionArgs);
    }
    public void borrarSigmayPSala(){

        SQLiteDatabase db=manejadorBBDD.getReadableDatabase();
        // Define 'where' part of query.
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " LIKE ? AND "+Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4 + " LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { ""+LoginActivity.mimajor,borrarSalaEditText.getText().toString() };
// Issue SQL statement.
        db.delete(Estructura_BD.SigmaTable.TABLE_NAME, selection, selectionArgs);

    }

    public void guardarSala(){

        //añadir un nuevo registro de parámetros
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // 3)Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_1, ""+LoginActivity.mimajor);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_2, ""+1);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_3, ""+1);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_4, ""+0);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_5, anadirSalaEditText.getText().toString());
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_6, ""+0);
        cjto_valores_nuevos.put(Estructura_BD.Parametros.NOMBRE_COLUMNA_7, ""+0);
        // 4)Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.Parametros.TABLE_NAME, null, cjto_valores_nuevos);


    }

    public void guardarSigmaYPSala(){
        SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
        // 3)Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
        ContentValues cjto_valores_nuevos = new ContentValues();
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1, ""+5);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2, ""+LoginActivity.mimajor);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3, ""+2);
        cjto_valores_nuevos.put(Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4, anadirSalaEditText.getText().toString());
        // 4)Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
        long newRowId = db.insert(Estructura_BD.SigmaTable.TABLE_NAME, null, cjto_valores_nuevos);
    }

    public boolean unicidadSala(int salaCandidata){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaCandidata };

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
            //si se puede añadir porque no hay otra igual
            cursor.close();
            return true;
        }else{
            Toast.makeText(this, "La sala "+salaCandidata+ " ha sido añadida anteriormente", Toast.LENGTH_SHORT).show();
            cursor.close();
            return false;
        }

    }

    public void leerSalas(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_2,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_3,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_4,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_6,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_7
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
            Toast.makeText(EscenarioActivity.this,"Sin salas",Toast.LENGTH_LONG).show();
            salasExistentes.setText("Salas: \n Ninguna");
            infoSalas="Salas: \n Ninguna";
        }else{
            int i=0;
            String ancho,largo,angulo,sala,ocx,ocy;
            String resumen="";
            for(i=0;i<cursor.getCount();i++){
                //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
                ancho= cursor.getString(0);
                largo= cursor.getString(1);
                angulo=cursor.getString(2);
                sala=cursor.getString(3);
                ocx=cursor.getString(4);
                ocy=cursor.getString(5);
                resumen=resumen+"sala "+sala+":\n \t";
                resumen=resumen+"ancho: "+ancho+" cm"+"\n \t";
                resumen=resumen+"largo: "+largo+" cm"+"\n \t";
                resumen=resumen+"desviación: "+angulo+"º"+"\n \t";
                resumen=resumen+"(Ox, Oy)= ("+ocx+", "+ocy+")"+"\n \t";
                resumen=resumen+"\n";

                cursor.moveToNext();
            }
            salasExistentes.setText("Salas: \n"+resumen);
            infoSalas="Salas: \n"+resumen;

        }
        cursor.close();

    }

    public boolean buscaSala(int sala){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5
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
            Toast.makeText(EscenarioActivity.this,"Sin coincidencias",Toast.LENGTH_LONG).show();
            cursor.close();
            return false;
        }else{
            int j=0;
            for(j=0;j<cursor.getCount();j++){
                if(cursor.getInt(0)==sala){
                    cursor.close();
                    return true;
                }
                cursor.moveToNext();
            }
            Toast.makeText(EscenarioActivity.this,"Sin coincidencias",Toast.LENGTH_LONG).show();
            cursor.close();
            return false;
        }


    }



}
