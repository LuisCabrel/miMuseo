package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Vector;

public class AdaptadorCuadros extends RecyclerView.Adapter<AdaptadorCuadros.ViewHolder> {

    private LayoutInflater inflador;
    private Vector<String> cuadros;
    private Vector<String> imagenes;
    private Vector<String> urls;
    private Context miContexto;
    private boolean compartirContenido;


    protected View.OnClickListener onClickListener;

    public AdaptadorCuadros(Context context, Vector<String> listaCuadros,
                            Vector<String> listaImagenes, Vector<String> listaUrls, boolean compartirContenido ) {
        this.cuadros=listaCuadros;
        this.imagenes=listaImagenes;
        this.urls=listaUrls;
        this.compartirContenido=compartirContenido;
        miContexto=context;
        inflador=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public AdaptadorCuadros.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=inflador.inflate(R.layout.cuadro_adaptador,parent,false);
        v.setOnClickListener(onClickListener);
        return new AdaptadorCuadros.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorCuadros.ViewHolder holder, final int position) {

        holder.cuadro.setText(cuadros.get(position));

        if((position%2)==0){
            holder.ctlyt.setBackgroundColor( miContexto.getResources().getColor(R.color.crema));
            holder.lnlyt.setBackgroundColor(miContexto.getResources().getColor(R.color.colorSecondary));
            holder.boton.setBackgroundColor(miContexto.getResources().getColor(R.color.colorSecondary));
        }else{
            holder.ctlyt.setBackgroundColor(miContexto.getResources().getColor(R.color.sky));
            holder.lnlyt.setBackgroundColor(miContexto.getResources().getColor(R.color.colorPrimaryDark));
            holder.boton.setBackgroundColor(miContexto.getResources().getColor(R.color.colorPrimaryDark));
        }

        Glide.with(miContexto)
                .load(imagenes.get(position))
                .into(holder.imagen);

        if(compartirContenido){
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = urls.get(position);
                    String stringShare="¡Mira lo que estoy viendo con la aplicacion miMuseo!";

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    //shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url +"\n "+stringShare);
                    miContexto.startActivity(Intent.createChooser(shareIntent,"Send To"));

                }
            });
        }else{
            holder.boton.setVisibility(View.INVISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        return cuadros.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView cuadro;
        public ImageView imagen;
        public ImageButton boton;
        public ConstraintLayout ctlyt;
        public LinearLayout lnlyt;

        ViewHolder(View itemview){
            super(itemview);
            //AQUI SE PONEN LOS ID DEL LAYOUT DEL ELEMENTO

            cuadro=(TextView)itemview.findViewById(R.id.txtVwCuadroAdapt);
            imagen=(ImageView) itemview.findViewById(R.id.imgVwCuadroAdapt);
            boton=(ImageButton) itemview.findViewById(R.id.imgBtCuadroAdapt);
            ctlyt=(ConstraintLayout)itemview.findViewById(R.id.ctrlytcrdvw);
            lnlyt=(LinearLayout)itemview.findViewById(R.id.lnlytcrdvw);
        }
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener){
        this.onClickListener=onClickListener;
    }



}



