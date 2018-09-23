package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;


import java.util.Vector;

/**
 * Created by nakis on 02/02/2018.
 */

public class SelectorDeCuadros extends AppCompatActivity {

    public BBDD_Helper manejadorBBDD;
    public Vector<String> cuadros;
    public Vector<String> imagenes;
    public Vector<String> urls;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private AdaptadorCuadros adaptadorCuadros;

    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_de_cuadros);

        toolbar=(Toolbar)findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        manejadorBBDD = new BBDD_Helper(this);
        cuadros=new Vector<String>();
        imagenes=new Vector<String>();
        urls=new Vector<String>();

        try{
            leerCuadros();
        }catch (Exception e){

        }

        recyclerView=(RecyclerView)findViewById(R.id.recyclerViewCuadros);
        adaptadorBusquedas(this,cuadros,imagenes,urls,true);

    }//fin onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cuadros, menu);


        MenuItem searchItem=menu.findItem(R.id.busqueda);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){

                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        int j;
                        Vector<String> cF, iF, uF;
                        cF=new Vector<String>();
                        iF=new Vector<String>();
                        uF=new Vector<String>();

                        for(j=0;j<cuadros.size();j++){
                            String obra=cuadros.get(j).toLowerCase();
                            if(obra.contains(query)){
                                cF.add(cuadros.get(j));
                                iF.add(imagenes.get(j));
                                uF.add(urls.get(j));
                            }
                        }
                        if(cF.size()==0){
                            cF.add(0,"Sin cuadros");
                            iF.add(0,"https://i.stack.imgur.com/WOlr3.png");
                            uF.add(0,"http://www.museocarandnuria.es");
                        }

                        adaptadorBusquedas(getApplicationContext(),cF,iF,uF,true);
                        adaptadorCuadros.notifyDataSetChanged();

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {

                        int j;
                        Vector<String> cF, iF, uF;
                        cF=new Vector<String>();
                        iF=new Vector<String>();
                        uF=new Vector<String>();

                        for(j=0;j<cuadros.size();j++){
                            String obra=cuadros.get(j).toLowerCase();
                            if(obra.contains(query)){
                                cF.add(cuadros.get(j));
                                iF.add(imagenes.get(j));
                                uF.add(urls.get(j));
                            }
                        }
                        if(cF.size()==0){
                            cF.add(0,"Sin cuadros");
                            iF.add(0,"https://i.stack.imgur.com/WOlr3.png");
                            uF.add(0,"http://www.museocarandnuria.es");
                        }

                        adaptadorBusquedas(getApplicationContext(),cF,iF,uF,true);
                        adaptadorCuadros.notifyDataSetChanged();

                        return true;
                    }
                }
        );

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener(){

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {

                        adaptadorBusquedas(getApplicationContext(),cuadros,imagenes,urls,true);
                        adaptadorCuadros.notifyDataSetChanged();
                        return true;//Para permitir cierre
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        return true;//Para permitir expansión
                    }
                });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id==R.id.busqueda){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void adaptadorBusquedas(final Context context,final Vector<String> c,final Vector<String> i,final Vector<String> u, Boolean b){

        adaptadorCuadros=new AdaptadorCuadros(context,c,i,u,b);
        recyclerView.setAdapter(adaptadorCuadros);
        layoutManager=new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        adaptadorCuadros.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((!c.isEmpty())   ){
                    String soporte="Sin cuadros";
                    try{
                        soporte=c.get(0);
                    }catch (Exception e){
                        Toast.makeText(context, "No se ha cargado aún las obras", Toast.LENGTH_SHORT).show();
                    }

                    if(!((c.size()==1)&&soporte.contains("Sin cuadros"))){
                        String nombreCuadro;
                        int pos=recyclerView.getChildAdapterPosition(v);

                        try{
                            nombreCuadro=c.get(pos);
                            Intent intent=new Intent(SelectorDeCuadros.this,RutaGuiadaActivity.class);
                            intent.putExtra("cuadro",nombreCuadro);
                            //startActivity(intent);

                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    SelectorDeCuadros.this,
                                    new Pair<View, String>(v.findViewById(R.id.imgVwCuadroAdapt), getString(R.string.transition_cuadro)));
                            ActivityCompat.startActivity(SelectorDeCuadros.this, intent, options.toBundle());

                        }catch (Exception e){
                            Toast.makeText(SelectorDeCuadros.this, "Sin cuadros", Toast.LENGTH_SHORT).show();
                        }
                    }

                }else{
                    Toast.makeText(context, "No se ha cargado aún las obras", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    public void leerCuadros(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_3,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_9,
                Estructura_BD.Cuadros.NOMBRE_COLUMNA_2

        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Cuadros.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo };

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
            Toast.makeText(SelectorDeCuadros.this,"Sin cuadros",Toast.LENGTH_LONG).show();
            cuadros.add(0,"Sin cuadros");
            imagenes.add(0,"https://i.stack.imgur.com/WOlr3.png");
            urls.add(0,"http://www.museocarandnuria.es");
        }else{
            int i=0;
            String cuadro,imagen,url;
            cursor.moveToFirst();
            for(i=0;i<cursor.getCount();i++){
                //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
                cuadro=cursor.getString(0);
                imagen=cursor.getString(1);
                url=cursor.getString(2);
                cuadros.add(i,cuadro);
                imagenes.add(i,imagen);
                urls.add(i,url);
                cursor.moveToNext();
            }
        }
        cursor.close();

    }//fin leerCuadros()


}//fin clase


