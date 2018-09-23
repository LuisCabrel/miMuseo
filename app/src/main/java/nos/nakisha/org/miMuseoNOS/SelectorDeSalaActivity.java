package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import java.util.Vector;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

public class SelectorDeSalaActivity extends AppCompatActivity {

    public BBDD_Helper manejadorBBDD;
    public Vector<String>salas;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdaptadorSalas adaptadorSalas;//recyclerViewSala

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_de_sala);

        manejadorBBDD = new BBDD_Helper(this);
        salas=new Vector<String>();
        try{
            leerSalas();
        }catch (Exception e){

        }

        recyclerView=(RecyclerView)findViewById(R.id.recyclerViewSala);
        adaptadorSalas=new AdaptadorSalas(this,salas);
        recyclerView.setAdapter(adaptadorSalas);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adaptadorSalas.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroSala;
                int pos=recyclerView.getChildAdapterPosition(v);

                try{
                    numeroSala=salas.get(pos);
                    inicializarParametrosSala(numeroSala);
                    Intent intent=new Intent(SelectorDeSalaActivity.this,MuestreoEstatica.class);
                    intent.putExtra("sala",Integer.parseInt(numeroSala));
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(SelectorDeSalaActivity.this, "Sin salas que calibrar", Toast.LENGTH_SHORT).show();
                }



            }
        });

    }


    public void leerSalas(){

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
            Toast.makeText(SelectorDeSalaActivity.this,"Sin salas",Toast.LENGTH_LONG).show();
            salas.add(0,"Sin salas");
        }else{
            int i=0;
            String sala;

            for(i=0;i<cursor.getCount();i++){
                //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
                sala=cursor.getString(0);
                salas.add(i,""+sala);
                cursor.moveToNext();
            }
        }
        cursor.close();

    }//fin leerSalas()

    public void inicializarParametrosSala( String numSala){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_2,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_3

        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Parametros.NOMBRE_COLUMNA_5+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,numSala };

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
            //nada
        }else{
            anchoCanvas=cursor.getInt(0);
            altoCanvas=cursor.getInt(1);
        }
        cursor.close();

    }//fin inicializarParametrosSala(String numSala)
}
