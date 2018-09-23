package nos.nakisha.org.miMuseoNOS;

/**
 * Created by nakis on 29/05/2018.
 */

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
//import static nos.nakisha.org.miMuseo.TabkalmanEst.mis_pxMaek;
//import static nos.nakisha.org.miMuseoNOS.KalmanEstatica.mis_pxMaek;
import static nos.nakisha.org.miMuseoNOS.KalmanDin.mis_pxMadin;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

public class PizarraKalmanDin extends View {

    private Bitmap miBitmap=null;
    public static Canvas miCanvasKalmanDin=null;
    //private Path miPath=null;
    private Point miPoint=null;
    private float miX, miY;
    private static final float TOLERANCIA_KALMAN_EST= 4;
    public static Paint miPaintKalmanDinBalizas=null;
    public static Paint miPaintKalmanDinPersona=null;
    public static Paint miPaintKalmanDinIncertidumbre=null;
    public static Paint avisoPaint=null;
    public static ShapeDrawable sdCuadro=null;


    public PizarraKalmanDin(Context context) {
        super(context);
        iniciarPizarraYPincel(context);
    }

    public PizarraKalmanDin(Context context, @Nullable AttributeSet attrs) {
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
        miCanvasKalmanDin=new Canvas(miBitmap);
        //miPath=new Path();
        miPoint=new Point();

        //parte del pincel que dibuja balizas
        miPaintKalmanDinBalizas=new Paint();
        miPaintKalmanDinBalizas.setStyle(Paint.Style.STROKE);
        miPaintKalmanDinBalizas.setStrokeJoin(Paint.Join.ROUND);
        miPaintKalmanDinBalizas.setStrokeCap(Paint.Cap.ROUND);
        miPaintKalmanDinBalizas.setAntiAlias(true);
        miPaintKalmanDinBalizas.setDither(true);
        miPaintKalmanDinBalizas.setColor(0xFFffcd00);
        miPaintKalmanDinBalizas.setStrokeWidth(20);

        //parte del pincel que dibuja a la posición de la persona
        miPaintKalmanDinPersona=new Paint();
        miPaintKalmanDinPersona.setStyle(Paint.Style.STROKE);
        miPaintKalmanDinPersona.setStrokeJoin(Paint.Join.ROUND);
        miPaintKalmanDinPersona.setStrokeCap(Paint.Cap.ROUND);
        miPaintKalmanDinPersona.setAntiAlias(true);
        miPaintKalmanDinPersona.setDither(true);
        miPaintKalmanDinPersona.setColor(0xFF0081cb);
        miPaintKalmanDinPersona.setStrokeWidth(20);

        avisoPaint=new Paint();
        avisoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        avisoPaint.setStrokeJoin(Paint.Join.ROUND);
        avisoPaint.setStrokeCap(Paint.Cap.ROUND);
        avisoPaint.setAntiAlias(true);
        avisoPaint.setDither(true);
        avisoPaint.setColor(0xFFff0000);
        avisoPaint.setStrokeWidth(5);
        avisoPaint.setTextSize(80f);

        miPaintKalmanDinIncertidumbre=new Paint();
        miPaintKalmanDinIncertidumbre.setStyle(Paint.Style.FILL_AND_STROKE);
        miPaintKalmanDinIncertidumbre.setStrokeJoin(Paint.Join.ROUND);
        miPaintKalmanDinIncertidumbre.setStrokeCap(Paint.Cap.ROUND);
        miPaintKalmanDinIncertidumbre.setAntiAlias(true);
        miPaintKalmanDinIncertidumbre.setDither(true);
        miPaintKalmanDinIncertidumbre.setColor(0x7769e2ff);

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
        recojo_mispx[0]=(int)(2* mis_pxMadin[0]/(float)3);//altura cuando está de pie
        if(depie()){
            recojo_mispx[1]=(int) mis_pxMadin[1];//ancho
        }else{
            recojo_mispx[1]=(int)(15* mis_pxMadin[1]/16);//ancho
        }

        int margen=24;
        //rectangulo
        int ancho,largo;
        ancho=anchoCanvas;
        largo=altoCanvas;
        RectF mirec;
        Rect otrorec;
        float casoEscenario=((float)(ancho)/(float)(largo));
        float casoResolucion=((mis_pxMadin[1]-2*margen)/((2* mis_pxMadin[0]/3)-2*margen));

        //antes:
        //mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
        //otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source

        //ahora:
        if(depie()) {
            if (casoEscenario > casoResolucion) {
                //limita el ancho
                mirec = new RectF(margen, margen, (int)((mis_pxMadin[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//destiny
                otrorec = new Rect(margen, margen, (int)((mis_pxMadin[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//source

            } else {
                //limita el largo
                mirec = new RectF(margen, margen, ((int)(((2/(float)3)*mis_pxMadin[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//destiny
                otrorec = new Rect(margen, margen, ((int)(((2/(float)3)*mis_pxMadin[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//source
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
            canvas.drawText("Regrese a la posición vertical",56f,mis_pxMadin[0]/3,avisoPaint);
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
            minw = (int) mis_pxMadin[1];
        }else{
            minw = (int)(mis_pxMadin[1]);
        }
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        //int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
        int minh = (int)(2* mis_pxMadin[0]/3);
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






