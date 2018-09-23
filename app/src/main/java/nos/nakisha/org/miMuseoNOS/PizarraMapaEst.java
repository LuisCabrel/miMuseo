package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
//import static nos.nakisha.org.miMuseo.TabMapaEst.mis_pxMae;
import static nos.nakisha.org.miMuseoNOS.Trilateracion.mis_pxMae;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 12/01/2018.
 */

public class PizarraMapaEst extends View {

    private Bitmap miBitmap=null;
    public static Canvas miCanvasMapaEst=null;
    //private Path miPath=null;
    private Point miPoint=null;
    private float miX, miY;
    private static final float TOLERANCIA_MAPA_EST= 4;
    public static Paint miPaintMapaEstBalizas=null;
    public static Paint miPaintMapaEstPersona=null;
    public static Paint avisoPaint=null;
    public static ShapeDrawable sdCuadro=null;


    public PizarraMapaEst(Context context) {
        super(context);
        iniciarPizarraYPincel(context);
    }

    public PizarraMapaEst(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        iniciarPizarraYPincel(context);
    }


    private void iniciarPizarraYPincel(Context contexto){

        //parte de la pantalla
        //obtenemos dimensiones de la pantalla
        Display display=((WindowManager)contexto.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point=new Point();
        display.getSize(point);
        miBitmap=Bitmap.createBitmap(point.x,point.y,Bitmap.Config.ARGB_8888);
        miCanvasMapaEst=new Canvas(miBitmap);
        //miPath=new Path();
        miPoint=new Point();

        //parte del pincel que dibuja balizas
        miPaintMapaEstBalizas=new Paint();
        miPaintMapaEstBalizas.setStyle(Paint.Style.STROKE);
        miPaintMapaEstBalizas.setStrokeJoin(Paint.Join.ROUND);
        miPaintMapaEstBalizas.setStrokeCap(Paint.Cap.ROUND);
        miPaintMapaEstBalizas.setAntiAlias(true);
        miPaintMapaEstBalizas.setDither(true);
        miPaintMapaEstBalizas.setColor(0xFFffcd00);
        miPaintMapaEstBalizas.setStrokeWidth(20);

        //parte del pincel que dibuja a la posición de la persona
        miPaintMapaEstPersona=new Paint();
        miPaintMapaEstPersona.setStyle(Paint.Style.STROKE);
        miPaintMapaEstPersona.setStrokeJoin(Paint.Join.ROUND);
        miPaintMapaEstPersona.setStrokeCap(Paint.Cap.ROUND);
        miPaintMapaEstPersona.setAntiAlias(true);
        miPaintMapaEstPersona.setDither(true);
        miPaintMapaEstPersona.setColor(0xFF0081cb);
        miPaintMapaEstPersona.setStrokeWidth(20);

        avisoPaint=new Paint();
        avisoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        avisoPaint.setStrokeJoin(Paint.Join.ROUND);
        avisoPaint.setStrokeCap(Paint.Cap.ROUND);
        avisoPaint.setAntiAlias(true);
        avisoPaint.setDither(true);
        avisoPaint.setColor(0xFFff0000);
        avisoPaint.setStrokeWidth(5);
        avisoPaint.setTextSize(80f);

        //forma del cuadro
        Path pathCuadro =new Path();
        pathCuadro.moveTo((float) 0.0, (float) 0.0);
        pathCuadro.lineTo((float) 1.0, (float) 0.0);
        pathCuadro.lineTo((float) 1.0, (float) 1.0);
        pathCuadro.lineTo((float) 0.0, (float) 1.0);
        pathCuadro.lineTo((float) 0.0, (float) 0.0);
        sdCuadro=new ShapeDrawable(new PathShape(pathCuadro,1,1));
        sdCuadro.getPaint().setColor(getResources().getColor(R.color.colorCuadro));
        sdCuadro.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);//antes STROKE
        sdCuadro.setIntrinsicWidth(32);
        sdCuadro.setIntrinsicHeight(32);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        //solo muestra información. Como no la guarda, no tiene entrada de datos por onTouchEvent
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas){

        int[] recojo_mispx=new int[2];
        recojo_mispx[0]=(int)(2* mis_pxMae[0]/(float)3);//altura cuando está de pie
        if(depie()){
            recojo_mispx[1]=(int) mis_pxMae[1];//ancho
        }else{
            recojo_mispx[1]=(int)(15* mis_pxMae[1]/16);//ancho
        }

        int margen=24;
        //rectangulo
        int ancho,largo;
        ancho=anchoCanvas;
        largo=altoCanvas;
        RectF mirec;
        Rect otrorec;
        float casoEscenario=((float)(ancho)/(float)(largo));
        float casoResolucion=((mis_pxMae[1]-2*margen)/((2* mis_pxMae[0]/3)-2*margen));

        //antes:
        //mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
        //otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source

        //ahora:
        if(depie()) {
            if (casoEscenario > casoResolucion) {
                //limita el ancho
                mirec = new RectF(margen, margen, (int)((mis_pxMae[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//destiny
                otrorec = new Rect(margen, margen, (int)((mis_pxMae[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//source

            } else {
                //limita el largo
                mirec = new RectF(margen, margen, ((int)(((2/(float)3)*mis_pxMae[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//destiny
                otrorec = new Rect(margen, margen, ((int)(((2/(float)3)*mis_pxMae[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//source
            }
        }else{
            mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
            otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source
        }



        //fondo
        canvas.drawColor(0xff00b0ff);
        canvas.clipRect(mirec);
        canvas.drawColor(0xffffffff);
        //imagen?


        //lo ya pintado

        canvas.drawBitmap(miBitmap,otrorec,mirec,null);
        //drawBitmap(bitmap, srcRect, destRect, paint)

        // el trazo actual PARA QUE HAYA FEEDBACK MIENTRAS SE DIBUJA, PERO TENEMOS PTOS, NO TRAZOS
        //canvas.drawPoint((float)miPoint.x,(float)miPoint.y,miPaintBalizas);

        if(!depie()){
            canvas.drawText("Regrese a la posición vertical",56f,mis_pxMae[0]/3,avisoPaint);
        }




        //   (``/)
        //_    |     _
        //  \_ | _/
    }

    @Override
    protected void onSizeChanged(int ancho, int alto, int ancho_anterior,int alto_anterior){
        super.onSizeChanged(ancho,alto,ancho_anterior,alto_anterior);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        //int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int minw;

        if(depie()){
            minw = (int) mis_pxMae[1];
        }else{
            minw = (int)(mis_pxMae[1]);
        }
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        //int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
        int minh = (int)(2* mis_pxMae[0]/3);
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);

        setMeasuredDimension(w, h);
    }
    public boolean depie(){

        int orientationdevice = Resources.getSystem().getConfiguration().orientation;
        boolean depie=true;
        if(orientationdevice==ORIENTATION_PORTRAIT) depie=true;
        else depie=false;
        return depie;
    }



}





