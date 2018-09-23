package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.TabMuestreoEst.mis_pxMe;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 09/01/2018.
 */

public class PizarraMuestreoEst extends View {

    private Bitmap miBitmap=null;
    public static Canvas miCanvasMuestreoEst=null;
    //private Path miPath=null;
    private Point miPoint=null;
    private float miX, miY;
    private static final float TOLERANCIA_MUESTREO_EST= 4;
    public static Paint miPaintMuestreoEstBalizas=null;
    public static Paint miPaintMuestreoEstPersona=null;
    public static Paint avisoPaint=null;
    public static ShapeDrawable sdCuadro=null;


    public PizarraMuestreoEst(Context context) {
        super(context);
        iniciarPizarraYPincel(context);
    }

    public PizarraMuestreoEst(Context context, @Nullable AttributeSet attrs) {
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
        miCanvasMuestreoEst=new Canvas(miBitmap);
        //miPath=new Path();
        miPoint=new Point();

        //parte del pincel que dibuja balizas
        miPaintMuestreoEstBalizas=new Paint();
        miPaintMuestreoEstBalizas.setStyle(Paint.Style.STROKE);
        miPaintMuestreoEstBalizas.setStrokeJoin(Paint.Join.ROUND);
        miPaintMuestreoEstBalizas.setStrokeCap(Paint.Cap.ROUND);
        miPaintMuestreoEstBalizas.setAntiAlias(true);
        miPaintMuestreoEstBalizas.setDither(true);
        miPaintMuestreoEstBalizas.setColor(0xFFffcd00);
        miPaintMuestreoEstBalizas.setStrokeWidth(20);

        //parte del pincel que dibuja a la posición de la persona
        miPaintMuestreoEstPersona=new Paint();
        miPaintMuestreoEstPersona.setStyle(Paint.Style.STROKE);
        miPaintMuestreoEstPersona.setStrokeJoin(Paint.Join.ROUND);
        miPaintMuestreoEstPersona.setStrokeCap(Paint.Cap.ROUND);
        miPaintMuestreoEstPersona.setAntiAlias(true);
        miPaintMuestreoEstPersona.setDither(true);
        miPaintMuestreoEstPersona.setColor(0xFF0081cb);
        miPaintMuestreoEstPersona.setStrokeWidth(20);

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
        float x=event.getX();
        float y=event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                empiezaaTocar(x,y);
                invalidate();

                //actualizo los EditText de la posición
                float alto=altoCanvas;
                float ancho=anchoCanvas;
                float margen=24;
                float casoEscenario=((float)(anchoCanvas)/(float)(altoCanvas));
                float casoResolucion=((mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen));

                if(casoEscenario>casoResolucion){
                    if(margen<=x && x<=((mis_pxMe[1]-2*margen)+margen) && (margen<=y) && (y<=(alto/ancho)*(mis_pxMe[1]-2*margen)+margen)){
                        //limita el ancho
                        TabMuestreoEst.editTextPosXMuestreoEst.setText(""+(((x-margen)*ancho)/(mis_pxMe[1]-2*margen)));
                        TabMuestreoEst.editTextPosYMuestreoEst.setText(""+(((y-margen)*ancho)/(mis_pxMe[1]-2*margen)));
                    }


                }else{
                    if(margen<=x && x<=((ancho/alto)*((2/(float)3)*mis_pxMe[0]-2*margen)+margen) && (margen<=y) && (y<=(2* mis_pxMe[0]/(float)3-margen))){
                        //limita el largo
                        TabMuestreoEst.editTextPosXMuestreoEst.setText(""+((x-margen)*alto/((2/(float)3)*mis_pxMe[0]-2*margen)));
                        TabMuestreoEst.editTextPosYMuestreoEst.setText(""+(y-margen)*alto/((2/(float)3)*mis_pxMe[0]-2*margen));
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                movimientoPulsando(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                levantarDedo();
                invalidate();
                break;
        }
        return true;
    }

    private void empiezaaTocar(float x,float y){
        miX=x;
        miY=y;
        miPoint.set((int)x,(int)y);
    }
    private void movimientoPulsando(float x,float y){
        if(Math.abs(x-miX)>=TOLERANCIA_MUESTREO_EST||Math.abs(y-miY)>=TOLERANCIA_MUESTREO_EST){
            //un trazo se rechaza
            //seHaTocadoLaPantalla=false;
            miX=x;
            miY=y;
        }
    }
    private  void levantarDedo(){
        miCanvasMuestreoEst.drawPoint((float)miPoint.x,(float)miPoint.y,miPaintMuestreoEstPersona);
    }

    @Override
    protected void onDraw(Canvas canvas){

        int[] recojo_mispx=new int[2];
        recojo_mispx[0]=(int)(2* mis_pxMe[0]/(float)3);//altura cuando está de pie
        if(depie()){
            recojo_mispx[1]=(int) mis_pxMe[1];//ancho
        }else{
            recojo_mispx[1]=(int)(15* mis_pxMe[1]/16);//ancho
        }

        int margen=24;
        //rectangulo
        int ancho,largo;
        ancho=anchoCanvas;
        largo=altoCanvas;
        RectF mirec;
        Rect otrorec;
        float casoEscenario=((float)(ancho)/(float)(largo));
        float casoResolucion=((mis_pxMe[1]-2*margen)/((2* mis_pxMe[0]/3)-2*margen));

        //antes:
        //mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
        //otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source

        //ahora:
        if(depie()) {
            if (casoEscenario > casoResolucion) {
                //limita el ancho
                mirec = new RectF(margen, margen, (int)((mis_pxMe[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//destiny
                otrorec = new Rect(margen, margen, (int)((mis_pxMe[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//source

            } else {
                //limita el largo
                mirec = new RectF(margen, margen, ((int)(((2/(float)3)*mis_pxMe[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//destiny
                otrorec = new Rect(margen, margen, ((int)(((2/(float)3)*mis_pxMe[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//source
            }
        }else{
            mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
            otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source
        }



        //fondo
        canvas.drawColor(0xff00b0ff);
        canvas.clipRect(mirec);
        canvas.drawColor(0xffffffff);

        //lo ya pintado

        canvas.drawBitmap(miBitmap,otrorec,mirec,null);
        //drawBitmap(bitmap, srcRect, destRect, paint)

        // el trazo actual PARA QUE HAYA FEEDBACK MIENTRAS SE DIBUJA, PERO TENEMOS PTOS, NO TRAZOS
        //canvas.drawPoint((float)miPoint.x,(float)miPoint.y,miPaintBalizas);

        if(!depie()){
            canvas.drawText("Regrese a la posición vertical",56f,mis_pxMe[0]/3,avisoPaint);
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
            minw = (int) mis_pxMe[1];
        }else{
            minw = (int)(mis_pxMe[1]);
        }
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        //int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
        int minh = (int)(2* mis_pxMe[0]/3);
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




