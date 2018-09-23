package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by nakis on 22/01/2018.
 */

public class AdaptadorOpciones  extends RecyclerView.Adapter<AdaptadorOpciones.ViewHolder> {

    private LayoutInflater inflador;
    private Vector<String> titulos;
    private Vector<String> descripciones;
    private Vector<Integer> fotos;
    private Context miContexto;
    //int coloOart;

    private int lastPosition = -1;


    protected View.OnClickListener onClickListener;

    public AdaptadorOpciones(Context context, Vector<String> listaTitulos, Vector<Integer> listaFotos, Vector<String> listaDescripciones) {
        this.titulos=listaTitulos;
        inflador=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.descripciones=listaDescripciones;
        fotos=listaFotos;
        this.miContexto=context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflador.inflate(R.layout.opcionesentradamuseo,parent,false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorOpciones.ViewHolder holder, int position) {
        holder.titulo.setText(titulos.get(position));
        holder.descripcion.setText(descripciones.get(position));
        holder.imagen.setImageResource(fotos.get(position));

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);



    }



    @Override
    public int getItemCount() {
        return titulos.size();
        //return 5;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView titulo,descripcion;
        public ImageView imagen;
        ViewHolder(View itemview){
            super(itemview);
            //AQUI SE PONEN LOS ID DEL LAYOUT DEL ELEMENTO

            titulo=(TextView)itemview.findViewById(R.id.tituloTxtVwOpcs);
            descripcion=(TextView)itemview.findViewById(R.id.descripcionTxtVwOpcs);
            imagen=(ImageView)itemview.findViewById(R.id.imgOpcs);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener=onClickListener;
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(miContexto, R.anim.item_animation_fall_down );
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(400);
            if(position<5) {
                animation.setStartOffset(200 * position);
            }
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}

