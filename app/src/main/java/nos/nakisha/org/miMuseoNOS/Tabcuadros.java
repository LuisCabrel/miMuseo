package nos.nakisha.org.miMuseoNOS;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
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

public class Tabcuadros extends Fragment implements View.OnClickListener {
    public static float[] mis_pxC;
    public static int[] mis_dpC;

    private int salaPresente;
    private TextView salaET;
    private TextView cuadrosTV;

    //vbles View:
    private PizarraCuadros pizarraCuadros;
    public Button botonPosicionarCuadro;
    public Button botonVerCuadros;
    public Button botonModificarCuadros;
    public Button botonGuardarCuadro;
    public static EditText editTextPosXcuadro;
    public static EditText editTextPosYcuadro;
    public static EditText editTextPosZcuadro;
    public EditText nombreCuadro;
    public EditText urlCuadro;
    public EditText modificarCuadroET;
    public EditText mediaCuadro;
    public EditText fotoCuadro;

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
        View rootViewCuadros=inflater.inflate(R.layout.tab_cuadros,container,false);

        manejadorBBDD = new BBDD_Helper(getContext());

        mis_pxC=new float[2];
        mis_dpC=new int[2];
        medidas();

        editTextPosXcuadro=(EditText)rootViewCuadros.findViewById(R.id.posXcuadro);

        editTextPosYcuadro=(EditText)rootViewCuadros.findViewById(R.id.posYcuadro);

        editTextPosZcuadro=(EditText)rootViewCuadros.findViewById(R.id.posZcuadro);

        nombreCuadro=(EditText)rootViewCuadros.findViewById(R.id.nombreCuadroEditText);
        urlCuadro=(EditText)rootViewCuadros.findViewById(R.id.urlCuadro);
        modificarCuadroET=(EditText)rootViewCuadros.findViewById(R.id.modificarCuadroEditText);
        mediaCuadro=(EditText)rootViewCuadros.findViewById(R.id.mediaCuadro);
        fotoCuadro=(EditText)rootViewCuadros.findViewById(R.id.fotoCuadro);

        botonGuardarCuadro=(Button)rootViewCuadros.findViewById(R.id.guardarCuadrosBtn);
        botonGuardarCuadro.setOnClickListener(this);

        botonModificarCuadros=(Button)rootViewCuadros.findViewById(R.id.modificarCuadrosBtn);
        botonModificarCuadros.setOnClickListener(this);

        botonPosicionarCuadro=(Button)rootViewCuadros.findViewById(R.id.posicionaCuadrosBtn);
        botonPosicionarCuadro.setOnClickListener(this);

        botonVerCuadros=(Button)rootViewCuadros.findViewById(R.id.verCuadrosBtn);
        botonVerCuadros.setOnClickListener(this);

        pizarraCuadros=(PizarraCuadros)rootViewCuadros.findViewById(R.id.viewPizarraCuadros);

        salaET=(TextView)rootViewCuadros.findViewById(R.id.numSalaCuadrosTxtVw);
        salaET.setText("Sala nº: "+salaPresente);
        cuadrosTV=(TextView)rootViewCuadros.findViewById(R.id.resumenCuadrosTxtVw);

        return rootViewCuadros;
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            PizarraCuadros.miCanvasCuadros.drawColor(0xFFFFFFFF);
            verCuadros();
        }catch (Exception e){
        }
    }

    public void medidas(){

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        mis_pxC[0]=metrics.heightPixels;
        mis_pxC[1]=metrics.widthPixels;
        if(depie()){
            mis_dpC[0]=Math.round(metrics.heightPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpC[1]=Math.round(metrics.widthPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
        else{
            mis_dpC[0]=Math.round(metrics.heightPixels / (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            mis_dpC[1]=Math.round(metrics.widthPixels / (metrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
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

        if(id==R.id.modificarCuadrosBtn){
            if(!modificarCuadroET.getText().toString().isEmpty()){
                if(buscaCuadro(modificarCuadroET.getText().toString())){
                    Intent intentModificar=new Intent(getContext(),ModificarCuadrosActivity.class);
                    intentModificar.putExtra("sala",salaPresente);
                    intentModificar.putExtra("cuadro",modificarCuadroET.getText().toString());
                    startActivity(intentModificar);
                }else{
                    Toast.makeText(getActivity(), "Indique un cuadro previamente almacenado", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(), "Indique qué cuadro quiere modificar/borrar", Toast.LENGTH_SHORT).show();
            }

        }

        if(id==R.id.verCuadrosBtn){
            PizarraCuadros.miCanvasCuadros.drawColor(0xFFFFFFFF);
            verCuadros();
        }//fin del if(id==R.id.verCuadrosBtn)
        if(id==R.id.guardarCuadrosBtn) {
            guardarCuadro();
            PizarraCuadros.miCanvasCuadros.drawColor(0xFFFFFFFF);
            verCuadros();
        }//fin del if(id==R.id.guardarCuadrosBtn)
        if(id==R.id.posicionaCuadrosBtn){
            if(!editTextPosXcuadro.getText().toString().isEmpty() &&!editTextPosYcuadro.getText().toString().isEmpty() ){
                //si hay algo escrito en X y en Y
                float xEdit,yEdit,xFinal,yFinal;
                float margen=24;
                xEdit=Float.parseFloat(editTextPosXcuadro.getText().toString());
                yEdit=Float.parseFloat(editTextPosYcuadro.getText().toString());
                //Toast.makeText(getContext(), "editX: " +xEdit +" editY: "+yEdit, Toast.LENGTH_SHORT).show();

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxC[1]-2*margen)/((2* mis_pxC[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xEdit*(mis_pxC[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yEdit*(mis_pxC[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xEdit*((2/(float)3)*mis_pxC[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yEdit*((2/(float)3)*mis_pxC[0]-2*margen)/(float)altoCanvas;
                }
                if(xEdit<=anchoCanvas && yEdit<=altoCanvas){
                    Drawable drawableCuadro;
                    drawableCuadro=PizarraCuadros.sdCuadro;
                    drawableCuadro.setBounds((int)xFinal-16,(int)yFinal-16,(int)xFinal+16,(int)yFinal+16);
                    drawableCuadro.draw(PizarraCuadros.miCanvasCuadros);
                }else{
                    Toast.makeText(getContext(), "Verifique las dimensiones", Toast.LENGTH_SHORT).show();
                }



            }else{
                Toast.makeText(getContext(), "Inserte Posición", Toast.LENGTH_SHORT).show();
            }

            actualizaPizarraCuadros();
        }//fin de id==R.id.posicionaBalizasBtn



    }//fin del onClick()


    public void actualizaPizarraCuadros(){
        pizarraCuadros.invalidate();

    }

    public boolean unicidadCuadro(String nombreCuadro){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_3+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor, nombreCuadro };

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
            cursor.close();
            return true;
        }else{
            Toast.makeText(getActivity(), "El cuadro "+nombreCuadro+" ha sido añadido anteriormente", Toast.LENGTH_SHORT).show();
            cursor.close();
            return false;
        }

    }

    public void verCuadros(){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_6,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_9,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_8
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            Toast.makeText(getActivity(), "BBDD vacía", Toast.LENGTH_SHORT).show();
            cuadrosTV.setText("Sin cuadros");
        }else{
            int numFilas=0;
            float xLect,yLect,xFinal,yFinal;
            float zLect;
            String nombre,foto,media; String resumen="Cuadros: \n";
            float margen=24;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursor.getFloat(0);
                yLect=cursor.getFloat(1);
                nombre=cursor.getString(2);
                zLect=cursor.getFloat(3);
                foto=cursor.getString(4);
                media=cursor.getString(5);
                resumen=resumen+"cuadro (nombre): "+nombre+"\n";
                resumen=resumen+"(x, y, z)= ("+xLect+", "+yLect+", "+zLect+") \n";
                resumen=resumen+"url imagen: "+foto+"\n";
                resumen=resumen+"url media: "+media+"\n";
                resumen=resumen+"\n";

                if((float)anchoCanvas/(float)altoCanvas>(mis_pxC[1]-2*margen)/((2* mis_pxC[0]/3)-2*margen)){
                    //limita el ancho
                    xFinal=margen+ xLect*(mis_pxC[1]-2*margen)/(float)anchoCanvas;
                    yFinal=margen + yLect*(mis_pxC[1]-2*margen)/(float)anchoCanvas;
                }else{
                    //limita el largo
                    xFinal=margen + xLect*((2/(float)3)*mis_pxC[0]-2*margen)/(float) altoCanvas;
                    yFinal=margen+ yLect*((2/(float)3)*mis_pxC[0]-2*margen)/(float)altoCanvas;
                }

                Drawable drawableCuadro;
                drawableCuadro=PizarraCuadros.sdCuadro;
                drawableCuadro.setBounds((int)xFinal-16,(int)yFinal-16,(int)xFinal+16,(int)yFinal+16);
                drawableCuadro.draw(PizarraCuadros.miCanvasCuadros);
                cursor.moveToNext();
            }//fin del bucle for
            numFilas=0;
            cuadrosTV.setText(resumen);

        }
        cursor.close();

        actualizaPizarraCuadros();


    }//fin de verCuadros()

    public void guardarCuadro(){

        //recoger lo escrito y que no esté vacío
        if(!nombreCuadro.getText().toString().isEmpty()&&
                !urlCuadro.getText().toString().isEmpty()&&
                !mediaCuadro.getText().toString().isEmpty()&&
                !fotoCuadro.getText().toString().isEmpty()&&
                !editTextPosXcuadro.getText().toString().isEmpty()&&
                !editTextPosYcuadro.getText().toString().isEmpty()&&
                !editTextPosZcuadro.getText().toString().isEmpty()){
            if(unicidadCuadro(nombreCuadro.getText().toString())){
                //para recoger valores de la BBDD:
                SQLiteDatabase db = manejadorBBDD.getWritableDatabase();
                // Creamos un nuevo registro de valores, donde los nombres de la columna son la clave
                ContentValues cjto_valores_nuevos = new ContentValues();
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_1, ""+LoginActivity.mimajor);
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_2, urlCuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_3, nombreCuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_4, editTextPosXcuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_5, editTextPosYcuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_6, editTextPosZcuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_7, salaPresente);
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_8, mediaCuadro.getText().toString());
                cjto_valores_nuevos.put(Estructura_BD.Cuadros.NOMBRE_COLUMNA_9, fotoCuadro.getText().toString());
                // Insertamos la fila nueva, y devuelve el valor de clave primaria de la fila nueva
                long newRowId = db.insert(Estructura_BD.Cuadros.TABLE_NAME, null, cjto_valores_nuevos);
                Toast.makeText(getContext(),"Se guardó el cuadro "+nombreCuadro.getText().toString(),Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(getActivity(), "Rellene todos los campos para poder guardar", Toast.LENGTH_SHORT).show();
        }

    }

    public boolean buscaCuadro(String nombre){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+Estructura_BD.Cuadros.NOMBRE_COLUMNA_7+" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+salaPresente };

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
            cursor.close();
            return false;
        }else{
            int numFilas=0;
            String nombreCuadro;
            for(numFilas=0;numFilas<cursor.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                nombreCuadro=cursor.getString(0);
                if(nombreCuadro.equals(nombre)){
                    cursor.close();
                    return true;
                }
                cursor.moveToNext();
            }
            numFilas=0;
            cursor.close();
            return false;
        }

    }

}
