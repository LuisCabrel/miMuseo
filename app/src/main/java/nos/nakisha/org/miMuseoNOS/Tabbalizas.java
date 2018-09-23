package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 14/12/2017.
 */

public class Tabbalizas extends Fragment implements View.OnClickListener {
    public static float[] mis_px;
    public static int[] mis_dp;
    public static boolean depieBalizas;

    private int salaPresente;
    private TextView salaET;
    private TextView balizasTV;

    //vbles View:
    private PizarraBalizas pizarraBalizas;
    public Button botonPosicionaBalizas;
    public Button botonVerBalizas;
    public Button botonModificarBalizas;
    public Button botonGuardarBalizas;
    public static EditText editTextPosX;
    public static EditText editTextPosY;
    public static EditText editTextPosZ;
    public EditText editTextid_estimote;
    public EditText modificarBalizaET;

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle extras=getActivity().getIntent().getExtras();
        salaPresente=extras.getInt("sala");


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        View rootViewBalizas=inflater.inflate(R.layout.tab_balizas,container,false);

        manejadorBBDD = new BBDD_Helper(getContext());
        //miPizarra=(View)getActivity().findViewById(R.id.viewPizarraBalizas);

        mis_px=new float[2];
        mis_dp=new int[2];
        medidas();

        botonPosicionaBalizas=(Button)rootViewBalizas.findViewById(R.id.posicionaBalizasBtn);
        botonPosicionaBalizas.setOnClickListener(this);

        botonVerBalizas=(Button)rootViewBalizas.findViewById(R.id.verBalizasBtn);
        botonVerBalizas.setOnClickListener(this);

        botonModificarBalizas=(Button)rootViewBalizas.findViewById(R.id.modificarBalizasBtn);
        botonModificarBalizas.setOnClickListener(this);

        botonGuardarBalizas=(Button)rootViewBalizas.findViewById(R.id.guardarBalizasBtn);
        botonGuardarBalizas.setOnClickListener(this);

        editTextPosX=(EditText)rootViewBalizas.findViewById(R.id.posXbaliza);

        editTextPosY=(EditText)rootViewBalizas.findViewById(R.id.posYbaliza);

        editTextPosZ=(EditText)rootViewBalizas.findViewById(R.id.posZbaliza);

        editTextid_estimote=(EditText)rootViewBalizas.findViewById(R.id.id_estimote);
        modificarBalizaET=(EditText)rootViewBalizas.findViewById(R.id.modificarBalizaEditText);

        pizarraBalizas=(PizarraBalizas)rootViewBalizas.findViewById(R.id.viewPizarraBalizas);

        salaET=(TextView)rootViewBalizas.findViewById(R.id.numSalaBalizasTxtVw);
        salaET.setText("Sala nº: "+salaPresente);

        balizasTV=(TextView)rootViewBalizas.findViewById(R.id.resumenTagsTxtVw);



        return rootViewBalizas;
    }//fin onCreatView

    @Override
    public void onResume(){
        super.onResume();
        try{
            PizarraBalizas.miCanvasBalizas.drawColor(0xFFFFFFFF);
            verBalizas();
        }catch (Exception e){

        }
    }



    public void medidas(){

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        mis_px[0]=metrics.heightPixels;
        mis_px[1]=metrics.widthPixels;
        if(depie()){
            mis_dp[0]=Math.round(metrics.heightPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dp[1]=Math.round(metrics.widthPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
        else{
            mis_dp[0]=Math.round(metrics.heightPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dp[1]=Math.round(metrics.widthPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        }
    }

    public boolean depie(){

        int orientationdevice = getActivity().getApplicationContext().getResources().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.posicionaBalizasBtn){
            if(!editTextPosX.getText().toString().isEmpty() &&!editTextPosY.getText().toString().isEmpty() ){
                //si hay algo escrito en X y en Y
                float xEdit,yEdit,xFinal,yFinal;
                float margen=24;
                xEdit=Float.parseFloat(editTextPosX.getText().toString());
                yEdit=Float.parseFloat(editTextPosY.getText().toString());
                //Toast.makeText(getContext(), "editX: " +xEdit +" editY: "+yEdit, Toast.LENGTH_SHORT).show();

                if((float)anchoCanvas/(float)altoCanvas>(mis_px[1]-2*margen)/((2* mis_px[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xEdit*(mis_px[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yEdit*(mis_px[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xEdit*((2/(float)3)*mis_px[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yEdit*((2/(float)3)*mis_px[0]-2*margen)/(float)altoCanvas;
                }
                if(xEdit<=anchoCanvas && yEdit<=altoCanvas){
                    PizarraBalizas.miCanvasBalizas.drawPoint(xFinal,yFinal,PizarraBalizas.miPaintBalizas);
                }else{
                    Toast.makeText(getContext(), "Verifique las dimensiones", Toast.LENGTH_SHORT).show();
                }



            }else{
                Toast.makeText(getContext(), "Inserte Posición", Toast.LENGTH_SHORT).show();
            }

            actualizaPizarraBalizas();
        }//fin de id==R.id.posicionaBalizasBtn


        if(id==R.id.guardarBalizasBtn){
            guardarBaliza();
            PizarraBalizas.miCanvasBalizas.drawColor(0xFFFFFFFF);
            verBalizas();
        }//fin if(id==R.id.guardarBalizasBtn)

        if(id==R.id.modificarBalizasBtn){
            //Toast.makeText(getContext(), "¡Por completar Modificar!", Toast.LENGTH_SHORT).show();
            if(!modificarBalizaET.getText().toString().isEmpty()){
                if(buscaBaliza(Integer.parseInt(modificarBalizaET.getText().toString()))){
                    Intent intentModificar=new Intent(getContext(),ModificarBalizasActivity.class);
                    intentModificar.putExtra("sala",salaPresente);
                    intentModificar.putExtra("tag",Integer.parseInt(modificarBalizaET.getText().toString()));
                    startActivity(intentModificar);
                }else{
                    Toast.makeText(getActivity(), "Indique un tag previamente almacenado", Toast.LENGTH_SHORT).show();
                }


            }else{
                Toast.makeText(getActivity(), "Indique qué tag quiere modificar/borrar", Toast.LENGTH_SHORT).show();
            }

        }

        if(id==R.id.verBalizasBtn){
            PizarraBalizas.miCanvasBalizas.drawColor(0xFFFFFFFF);
            verBalizas();
        }//fin if(id==R.id.verBalizasBtn)
    }//fin del onClick()

    public void actualizaPizarraBalizas(){
        pizarraBalizas.invalidate();

    }

    public boolean unicidadBalizas(int identificadorBaliza){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+ Estructura_BD.Balizas.NOMBRE_COLUMNA_2+" = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+identificadorBaliza };

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
            //se puede añadir
            cursor.close();
            return true;
        }else{
            Toast.makeText(getActivity(), "El tag "+identificadorBaliza+" ha sido añadidoanteriormente", Toast.LENGTH_SHORT).show();
            cursor.close();
            return false;
        }

    }

    public void verBalizas(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            Toast.makeText(getActivity(), "BBDD vacía", Toast.LENGTH_SHORT).show();
            balizasTV.setText("Sin balizas");
        }else{
            int numFilas=0;
            float xLect,yLect,xFinal,yFinal;
            float zLect;
            String idBaliza; String resumen="Balizas: \n";
            float margen=24;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursor.getFloat(0);
                yLect=cursor.getFloat(1);
                idBaliza=cursor.getString(2);
                zLect=cursor.getFloat(3);
                resumen=resumen+"baliza (id): "+idBaliza+"\n";
                resumen=resumen+"(x, y, z)= ("+xLect+", "+yLect+", "+zLect+") \n";
                resumen=resumen+"\n";

                if((float)anchoCanvas/(float)altoCanvas>(mis_px[1]-2*margen)/((2* mis_px[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_px[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_px[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_px[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_px[0]-2*margen)/(float)altoCanvas;
                }
                PizarraBalizas.miCanvasBalizas.drawPoint(xFinal,yFinal,PizarraBalizas.miPaintBalizas);


                if((float)anchoCanvas/(float)altoCanvas>(mis_px[1]-2*margen)/((2* mis_px[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_px[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_px[1]-2*margen)/(float)anchoCanvas;

                    if((mis_px[1]-2*margen)-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_px[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_px[0]-2*margen)/(float)altoCanvas;

                    //if(xFinal<3*margen){xFinal=3*margen;}
                    if(anchoCanvas*((2/(float)3)*mis_px[0]-2*margen)/(float) altoCanvas-3*margen<xFinal){xFinal=xFinal-3*margen;}
                    if(yFinal<4*margen){yFinal=4*margen;}
                    //if(altoCanvas-3*margen<yFinal){yFinal=altoCanvas-3*margen;}
                }

                PizarraBalizas.miCanvasBalizas.drawText(idBaliza,xFinal,yFinal,PizarraBalizas.avisoPaint);

                cursor.moveToNext();
            }//fin del bucle for
            numFilas=0;
            balizasTV.setText(resumen);

        }
        cursor.close();

        actualizaPizarraBalizas();


    }//fin de verBalizas()

    public void guardarBaliza(){

        //recoger lo escrito y que no esté vacío
        if(!editTextid_estimote.getText().toString().isEmpty()&&!editTextPosX.getText().toString().isEmpty()&&!editTextPosY.getText().toString().isEmpty()&&!editTextPosZ.getText().toString().isEmpty()){
            if(unicidadBalizas(Integer.parseInt(editTextid_estimote.getText().toString()))){
                //para recoger valores de la BBDD:
                SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
                // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
                ContentValues cjto_valores_nuevos = new ContentValues();
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_1, ""+LoginActivity.mimajor);
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_2, editTextid_estimote.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_3, editTextPosX.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_4, editTextPosY.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_5, editTextPosZ.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Balizas.NOMBRE_COLUMNA_6, salaPresente);
                // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
                long newRowId = db.insert(Estructura_BD.Balizas.TABLE_NAME, null, cjto_valores_nuevos);
                Toast.makeText(getContext(),"se guardó el tag "+editTextid_estimote.getText().toString(),Toast.LENGTH_LONG).show();

            }

        }else{
            Toast.makeText(getActivity(), "Rellene todos los campos para poder guardar", Toast.LENGTH_SHORT).show();
        }

    }//fin guardarBaliza()

    public boolean buscaBaliza(int idTag){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Balizas.NOMBRE_COLUMNA_6+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            cursor.close();
            return false;
        }else{
            int numFilas=0;
            int idBaliza;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                idBaliza=cursor.getInt(0);
                if(idBaliza==Integer.parseInt(modificarBalizaET.getText().toString())){
                    cursor.close();
                    return true;
                }
                cursor.moveToNext();
            }
            numFilas=0;
            cursor.close();
            return false;
        }

    }//fin buscaBaliza()

}
