package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static nos.nakisha.org.miMuseoNOS.Tabbalizas.mis_px;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.altoCanvas;
import static nos.nakisha.org.miMuseoNOS.ModificarSalaActivity.anchoCanvas;

/**
 * Created by nakis on 18/12/2017.
 */

public class PizarraBalizas extends View {

    private Bitmap miBitmap=null;
    public static Canvas miCanvasBalizas=null;
    //private Path miPath=null;
    private Point miPoint=null;
    private float miX, miY;
    private static final float TOLERANCIA_BALIZAS= 4;
    public static Paint miPaintBalizas=null;
    public static Paint avisoPaint=null;

    private RectF mirecForzarRepintado;
    private Rect otrorecForzarRepintado;

    public PizarraBalizas(Context context) {
        super(context);
        iniciarPizarraYPincel(context);

    }

    public PizarraBalizas(Context context, @Nullable AttributeSet attrs) {
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
        miCanvasBalizas=new Canvas(miBitmap);
        //miPath=new Path();
        miPoint=new Point();

        //parte del pincel
        miPaintBalizas=new Paint();
        miPaintBalizas.setStyle(Paint.Style.STROKE);
        miPaintBalizas.setStrokeJoin(Paint.Join.ROUND);
        miPaintBalizas.setStrokeCap(Paint.Cap.ROUND);
        miPaintBalizas.setAntiAlias(true);
        miPaintBalizas.setDither(true);
        miPaintBalizas.setColor(0xFFffcd00);
        miPaintBalizas.setStrokeWidth(20);
        avisoPaint=new Paint();
        avisoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        avisoPaint.setStrokeJoin(Paint.Join.ROUND);
        avisoPaint.setStrokeCap(Paint.Cap.ROUND);
        avisoPaint.setAntiAlias(true);
        avisoPaint.setDither(true);
        avisoPaint.setColor(0xFFff0000);
        avisoPaint.setStrokeWidth(5);
        avisoPaint.setTextSize(80f);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        float x=event.getX();
        float y=event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                empiezaaTocar(x,y);
                invalidate();

                //Toast.makeText(getContext(), "x: "+x+", y: "+y, Toast.LENGTH_SHORT).show();
                //actualizo los EditText de la posición
                float alto=altoCanvas;
                float ancho=anchoCanvas;
                float margen=24;
                float casoEscenario=((float)(anchoCanvas)/(float)(altoCanvas));
                float casoResolucion=((mis_px[1]-2*margen)/((2* mis_px[0]/3)-2*margen));

                if(casoEscenario>casoResolucion){
                    if(margen<=x && x<=((mis_px[1]-2*margen)+margen) && (margen<=y) && (y<=(alto/ancho)*(mis_px[1]-2*margen)+margen)){
                        //limita el ancho
                        //Tabbalizas.editTextPosX.setText(""+(x-margen)*ancho/(mis_px[1]-2*margen)+62);
                        //Tabbalizas.editTextPosY.setText(""+((y-margen)*ancho/(mis_px[1]-2*margen)-40));//40=factor de correción
                        Tabbalizas.editTextPosX.setText(""+(((x-margen)*ancho)/(mis_px[1]-2*margen)));
                        Tabbalizas.editTextPosY.setText(""+(((y-margen)*ancho)/(mis_px[1]-2*margen)));
                        Tabbalizas.editTextPosZ.setText("0");
                    }


                }else{
                    if(margen<=x && x<=((ancho/alto)*((2/(float)3)*mis_px[0]-2*margen)+margen) && (margen<=y) && (y<=(2* mis_px[0]/(float)3-margen))){
                        //limita el largo
                        Tabbalizas.editTextPosX.setText(""+((x-margen)*alto/((2/(float)3)*mis_px[0]-2*margen)));//30=factor de correción
                        Tabbalizas.editTextPosY.setText(""+(y-margen)*alto/((2/(float)3)*mis_px[0]-2*margen));
                        Tabbalizas.editTextPosZ.setText("0");
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
        if(Math.abs(x-miX)>=TOLERANCIA_BALIZAS||Math.abs(y-miY)>=TOLERANCIA_BALIZAS){
            //un trazo se rechaza
            //seHaTocadoLaPantalla=false;
            miX=x;
            miY=y;
        }
    }
    private  void levantarDedo(){
        miCanvasBalizas.drawPoint((float)miPoint.x,(float)miPoint.y,miPaintBalizas);
    }

    @Override
    protected void onDraw(Canvas canvas){

        int[] recojo_mispx=new int[2];
        recojo_mispx[0]=(int)(2* mis_px[0]/(float)3);//altura cuando está de pie
        if(depie()){
            recojo_mispx[1]=(int) mis_px[1];//ancho
        }else{
            recojo_mispx[1]=(int)(15* mis_px[1]/16);//ancho
        }

        int margen=24;
        //rectangulo
        int ancho,largo;
        ancho=anchoCanvas;
        largo=altoCanvas;
        RectF mirec;
        Rect otrorec;
        float casoEscenario=((float)(ancho)/(float)(largo));
        float casoResolucion=((mis_px[1]-2*margen)/((2* mis_px[0]/3)-2*margen));

        //antes:
        //mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
        //otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source

        //ahora:
        if(depie()) {
            if (casoEscenario > casoResolucion) {
                //limita el ancho
                mirec = new RectF(margen, margen, (int)((mis_px[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//destiny
                otrorec = new Rect(margen, margen, (int)((mis_px[1] - 2 * (float)margen) + (float)margen), ((int)((recojo_mispx[1] - 2 * margen) * largo / (float)ancho) + margen));//source

            } else {
                //limita el largo
                mirec = new RectF(margen, margen, ((int)(((2/(float)3)*mis_px[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//destiny
                otrorec = new Rect(margen, margen, ((int)(((2/(float)3)*mis_px[0] - 2 * margen) * ancho / (float)largo) + margen), ((recojo_mispx[0] - 2 * margen) + margen));//source
            }
        }else{
            mirec=new RectF(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//destiny
            otrorec=new Rect(margen,margen,recojo_mispx[1]-margen,recojo_mispx[0]-margen);//source
        }

        mirecForzarRepintado=mirec;
        otrorecForzarRepintado=otrorec;


        //fondo
        canvas.drawColor(0xff00b0ff);
        canvas.clipRect(mirec);
        canvas.drawColor(0xffffffff);
        //lo ya pintado
        //canvas.drawBitmap(miBitmap, 0, 0,null);
        canvas.drawBitmap(miBitmap,otrorec,mirec,null);
        //drawBitmap(bitmap, srcRect, destRect, paint)

        // el trazo actual PARA QUE HAYA FEEDBACK MIENTRAS SE DIBUJA, PERO TENEMOS PTOS, NO TRAZOS
        //canvas.drawPoint((float)miPoint.x,(float)miPoint.y,miPaintBalizas);

        if(!depie()){
            canvas.drawText("Regrese a la posición vertical",56f,mis_px[0]/3,avisoPaint);
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
            minw = (int) mis_px[1];
        }else{
            minw = (int)(mis_px[1]);
        }
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        //int minh = MeasureSpec.getSize(w) - (int)mTextWidth + getPaddingBottom() + getPaddingTop();
        int minh = (int)(2* mis_px[0]/3);
        //int h = resolveSizeAndState(MeasureSpec.getSize(w) - (int)Tabbalizas.mis_px[0], heightMeasureSpec, 0);
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


