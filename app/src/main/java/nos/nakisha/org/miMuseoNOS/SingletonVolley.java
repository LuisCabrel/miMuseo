package nos.nakisha.org.miMuseoNOS;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by nakis on 03/01/2018.
 */

public class SingletonVolley {
    //de creación Diferida: No quiero que se inicialice al ppio, sino cuando se necesite
    //instancia unica a la clase
    private static SingletonVolley INSTANCIA_VOLLEY= null;//new SingletonVolley();
    //para obtener la instancia desde otras clases, usar:
    //SingletonVolley vble=SingletonVolley.getInstanciaVolley();
    private Context contexto;
    private RequestQueue colaPeticiones;
    //El constructor es private para evitar su acceso desde fuera
    private SingletonVolley(Context contexto){
        this.contexto=contexto;
        //Volley
        if(colaPeticiones==null){
            colaPeticiones= Volley.newRequestQueue(this.contexto.getApplicationContext());
        }
    }
    //Método para obtenerla única instancia de la clase
    public static SingletonVolley getInstanciaVolley(Context context){
        if(INSTANCIA_VOLLEY==null){
            //ATENCIÓN: no sobra ni el primer if(INSTANCIA_VOLLEY==null) ni el segundo
            //aunque desde el pto de vista de la programación no tenga sentido, desde la eficiencia
            //dado que las secciones criticas son costosas, si 2 hilos llegan, solo se crea
            // la primera vez la sección crítica
            synchronized (SingletonVolley.class){
                if(INSTANCIA_VOLLEY==null){
                    INSTANCIA_VOLLEY= new SingletonVolley(context);
                }
            }
        }
        return INSTANCIA_VOLLEY;
    }

    public<T> void addColaPeticiones(Request<T> request){
        colaPeticiones.add(request);
    }
//para usarlo: SingletonVolley.getInstanciaVolley.addColaPeticiones(peticion);
}
