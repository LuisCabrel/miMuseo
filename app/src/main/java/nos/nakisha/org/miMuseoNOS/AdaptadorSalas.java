package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Vector;

/**
 * Created by nakis on 25/01/2018.
 */

public class AdaptadorSalas extends RecyclerView.Adapter<AdaptadorSalas.ViewHolder> {

    private LayoutInflater inflador;
    private Vector<String> salas;


    protected View.OnClickListener onClickListener;

    public AdaptadorSalas(Context context, Vector<String> listaSalas) {
        this.salas=listaSalas;
        inflador=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public AdaptadorSalas.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflador.inflate(R.layout.sala_adaptador,parent,false);
        v.setOnClickListener(onClickListener);
        return new AdaptadorSalas.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorSalas.ViewHolder holder, int position) {
        holder.sala.setText(salas.get(position));

    }

    @Override
    public int getItemCount() {
        return salas.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView sala;
        ViewHolder(View itemview){
            super(itemview);
            //AQUI SE PONEN LOS ID DEL LAYOUT DEL ELEMENTO

            sala=(TextView)itemview.findViewById(R.id.elementoSala);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener=onClickListener;
    }



}


