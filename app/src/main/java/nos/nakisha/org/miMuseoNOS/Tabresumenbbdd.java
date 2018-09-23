package nos.nakisha.org.miMuseoNOS;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by nakis on 22/01/2018.
 */

public class Tabresumenbbdd extends Fragment {
    public BBDD_Helper manejadorBBDD;
    public String infoSalas, infoBalizas, infoCuadros,infoSigmaYP;
    public TextView informacionGeneral;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootViewResumen = inflater.inflate(R.layout.tab_resumenbbdd, container, false);
        manejadorBBDD = new BBDD_Helper(getActivity());
        infoSalas="";infoBalizas="";infoCuadros="";infoSigmaYP="";
        informacionGeneral=(TextView)rootViewResumen.findViewById(R.id.resumenBBDDTxtVw);

        try{
            leerSalas();
        }catch (Exception e){
            Toast.makeText(getActivity(), "Hay un problema", Toast.LENGTH_SHORT).show();
        }

        return rootViewResumen;
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
                Estructura_BD.Parametros.NOMBRE_COLUMNA_7,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_8
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
            Toast.makeText(getActivity(),"Sin salas",Toast.LENGTH_LONG).show();
            infoSalas="Salas: \n Ninguna";
        }else{
            int i=0;
            String ancho,largo,angulo,sala,ocx,ocy,media;
            String resumen="";
            infoSalas="Salas: \n";
            for(i=0;i<cursor.getCount();i++){
                //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
                ancho= cursor.getString(0);
                largo= cursor.getString(1);
                angulo=cursor.getString(2);
                sala=cursor.getString(3);
                ocx=cursor.getString(4);
                ocy=cursor.getString(5);
                media=cursor.getString(6);
                resumen=resumen+"sala "+sala+":\n \t";
                resumen=resumen+"ancho: "+ancho+" cm"+"\n \t";
                resumen=resumen+"largo: "+largo+" cm"+"\n \t";
                resumen=resumen+"desviación: "+angulo+"º"+"\n \t";
                resumen=resumen+"(Ox, Oy)= ("+ocx+", "+ocy+")"+"\n \t";
                resumen=resumen+"url media: "+media+"\n \t";
                resumen=resumen+"\n \t";

                leerBalizas(Integer.parseInt(sala));
                resumen=resumen+infoBalizas;
                resumen=resumen+"\n \t";

                leerCuadros(Integer.parseInt(sala));
                resumen=resumen+infoCuadros;
                resumen=resumen+"\n \t";

                leerSigmaYPSala(Integer.parseInt(sala));
                resumen=resumen+infoSigmaYP;
                resumen=resumen+"\n \t";

                cursor.moveToNext();
            }
            infoSalas=infoSalas+resumen;
            infoBalizas="";
            infoCuadros="";
            informacionGeneral.setText(infoSalas);

        }
        cursor.close();

    }

    public void leerBalizas(int sala){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_3,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_4,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_5,
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ? AND " + Estructura_BD.Balizas.NOMBRE_COLUMNA_6 +" = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

        Cursor cursorBalizas = db.query(
                Estructura_BD.Balizas.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorBalizas.moveToFirst();

        if(cursorBalizas.getCount()==0){
            //Toast.makeText(getActivity(), "BBDD vacía", Toast.LENGTH_SHORT).show();
            infoBalizas="\t \t Balizas: Ninguna";
        }else{
            int numFilas=0;
            float xLect,yLect,zLect;
            String idBaliza;
            infoBalizas="\t \t Balizas:\n \t \t \t \t";
            for(numFilas=0;numFilas<cursorBalizas.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursorBalizas.getFloat(0);
                yLect=cursorBalizas.getFloat(1);
                zLect=cursorBalizas.getFloat(2);
                idBaliza=cursorBalizas.getString(3);
                infoBalizas=infoBalizas+"baliza "+idBaliza+"\n \t \t \t \t \t";
                infoBalizas=infoBalizas+"(x, y, z)= ("+xLect+", "+yLect+", "+zLect+") \n \t \t \t \t ";
                cursorBalizas.moveToNext();
            }
            numFilas=0;

        }
        cursorBalizas.close();
    }//fin leerBalizas(int sala)

    public void leerCuadros(int sala){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_4,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_5,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_6,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_2,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_8,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_9
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ? AND "+ Estructura_BD.Cuadros.NOMBRE_COLUMNA_7 +" = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

        Cursor cursorCuadros = db.query(
                Estructura_BD.Cuadros.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorCuadros.moveToFirst();

        if(cursorCuadros.getCount()==0){
            //Toast.makeText(getActivity(), "BBDD vacía", Toast.LENGTH_SHORT).show();
            infoCuadros="\t \t Cuadros: Ninguno";
        }else{
            int numFilas=0;
            float xLect,yLect,zLect;
            String nombre,url,media,imagen;
            infoCuadros="\t \t Cuadros:\n \t \t \t \t";
            for(numFilas=0;numFilas<cursorCuadros.getCount();numFilas++){
                //EN LECTURA NO ASIGNO
                //si hay algo escrito en X y en Y
                xLect=cursorCuadros.getFloat(0);
                yLect=cursorCuadros.getFloat(1);
                zLect=cursorCuadros.getFloat(2);
                nombre=cursorCuadros.getString(3);
                url=cursorCuadros.getString(4);
                media=cursorCuadros.getString(5);
                imagen=cursorCuadros.getString(6);
                infoCuadros=infoCuadros+"cuadro: "+nombre+"\n \t \t \t \t \t";
                infoCuadros=infoCuadros+"url: "+url+"\n \t \t \t \t \t";
                infoCuadros=infoCuadros+"media: "+media+"\n \t \t \t \t \t";
                infoCuadros=infoCuadros+"imagen: "+imagen+"\n \t \t \t \t \t";
                infoCuadros=infoCuadros+"(x, y, z)= ("+xLect+", "+yLect+", "+zLect+") \n \t \t \t \t";

                cursorCuadros.moveToNext();
            }
            numFilas=0;

        }
        cursorCuadros.close();
    }//fin leerCuadros(int sala)

    public void leerSigmaYPSala(int sala){

        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_1,
                Estructura_BD.SigmaTable.NOMBRE_COLUMNA_3
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.SigmaTable.NOMBRE_COLUMNA_2 + " = ? AND "+ Estructura_BD.SigmaTable.NOMBRE_COLUMNA_4 +" = ? ";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+LoginActivity.mimajor,""+sala };

        Cursor cursorSigmaYP = db.query(
                Estructura_BD.SigmaTable.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null//sortOrder                                 // The sort order
        );

        cursorSigmaYP.moveToFirst();

        if(cursorSigmaYP.getCount()==0){
            //Toast.makeText(getActivity(), "BBDD vacía", Toast.LENGTH_SHORT).show();
            infoSigmaYP="\t \t Sigma y p: Ninguno";
        }else{
            String sigma,p;
            infoSigmaYP="\t \t Sigma y p:\n \t \t \t \t";

            //EN LECTURA NO ASIGNO
            sigma=cursorSigmaYP.getString(0);
            p=cursorSigmaYP.getString(1);
            infoSigmaYP=infoSigmaYP+"sigma: "+sigma+"\n \t \t \t \t";
            infoSigmaYP=infoSigmaYP+"p: "+p+"\n";


        }
        cursorSigmaYP.close();
    }//fin leerCuadros(int sala)


}
