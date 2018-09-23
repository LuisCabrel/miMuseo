package nos.nakisha.org.miMuseoNOS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;



import static nos.nakisha.org.miMuseoNOS.FormularioActivity.SOLICITUD_PERMISO_ALMACENAMIENTO;

//public interface RealidadAumentada{}

@RequiresApi(24)
public class RealidadAumentada extends AppCompatActivity {

    //vbles BBDD
    public BBDD_Helper manejadorBBDD;

    //nuevo manejador de balizas:
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothAdapter.LeScanCallback leScanCallback;
    private boolean mScanning;
    private Handler mHandler=new Handler();
    // Stops scanning after 3 seconds.
    private static final long SCAN_PERIOD = 3000;
    private HashMap<Integer,Integer> salasDetectadas,cantidadDetectadas;

    public HashMap<Integer,Integer> diccionarioTagSala;
    public HashMap<Integer,String> diccionarioSalaMedia;
    public int salaPresente;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ModelRenderable dinoRenderable;
    private ViewRenderable mediaAndyRenderable;
    private ViewRenderable mediaDinoRenderable;
    private ConstraintLayout ct;

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private String urlMediaModel;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    private ImageButton changeCharacter;
    private ImageButton actualizarBtn;
    private ImageButton papelera;

    public int contadorAlterno;
    private boolean existeAndy;
    private boolean existeDino;
    private AnchorNode anchorNodeAndy;
    private AnchorNode anchorNodeDino;
    private Node mediaControlsAndy;
    private Node mediaControlsDino;
    private TransformableNode modelAndy;
    private TransformableNode modelDino;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realidad_aumentada);

        manejadorBBDD = new BBDD_Helper(this);

        diccionarioTagSala=new HashMap<Integer, Integer>();
        diccionarioSalaMedia=new HashMap<Integer, String>();
        salaPresente=2;

        diccionarioSalas();
        leerMediaSalas();

        urlMediaModel="http://www.museocarandnuria.es/mp3/"+EntradaMuseo.majorMuseo+"/andy/andy.mp4";
        existeAndy=false;
        existeDino=false;

        contadorAlterno=0;

        ct=(ConstraintLayout)findViewById(R.id.ctrlytar);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        salasDetectadas=new HashMap<Integer, Integer>();
        cantidadDetectadas=new HashMap<Integer, Integer>();

        leScanCallback=new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {

                    // GRACIAS https://github.com/kiteflo/iBeaconAndroidDemo
                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5) {
                    if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                            ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }

                if (patternFound) {
                    int major = (scanRecord[startByte + 20] & 0xff) * 0x100 + (scanRecord[startByte + 21] & 0xff);
                    int minor = (scanRecord[startByte + 22] & 0xff) * 0x100 + (scanRecord[startByte + 23] & 0xff);

                    Log.d("Nueva lista","major: "+major+", minor: "+minor);

                    if(major==EntradaMuseo.majorMuseo){
                        Log.d("Nueva lista","major coincide");
                        if(diccionarioTagSala.get(minor)!=null){
                            Log.d("Nueva lista","minor válido->voy a meter sala");
                            if(salasDetectadas.size()==0){
                                salasDetectadas.put(0,diccionarioTagSala.get(minor));
                                cantidadDetectadas.put(0,1);
                            }else{
                                if(salasDetectadas.containsValue(diccionarioTagSala.get(minor))){
                                    int k,cantidadAnterior;
                                    for(k=0;k<salasDetectadas.size();k++){
                                        if(salasDetectadas.get(k)==diccionarioTagSala.get(minor)){
                                            cantidadAnterior=cantidadDetectadas.get(k);
                                            cantidadDetectadas.put(k,cantidadAnterior+1);
                                        }
                                    }

                                }else{
                                    salasDetectadas.put(salasDetectadas.size(),diccionarioTagSala.get(minor));
                                    cantidadDetectadas.put(cantidadDetectadas.size(),1);
                                }
                            }
                        }else{
                            Log.d("Nueva lista","elemento descartado");
                        }
                    }

                    //elegir sala
                    int contadorSalas,indice,valor;
                    Log.d("Nueva lista", "paso a ver las salas:");
                    if(salasDetectadas.size()!=0){
                        Log.d("Nueva lista", "TOTAL nº salas = "+salasDetectadas.size());
                        indice=0;valor=0;
                        for(contadorSalas=0; contadorSalas<salasDetectadas.size(); contadorSalas++){
                            Log.d("Nueva lista", "sala encontrada: " + salasDetectadas.get(contadorSalas));
                            if(contadorSalas==0){
                                valor=cantidadDetectadas.get(0);
                                indice=contadorSalas;
                            }else{
                                if(cantidadDetectadas.get(contadorSalas)>valor){
                                    valor=cantidadDetectadas.get(contadorSalas);
                                    indice=contadorSalas;
                                }
                            }
                        }
                        salaPresente=salasDetectadas.get(indice);
                        Log.d("Nueva lista", "SALA PRESENTE: "+salaPresente);
                    }else{
                        Log.d("Nueva lista", "paso olímpicamente de las salas:");
                    }
                    //ESTO FUNCIONABA
                    //urlMediaModel="http://www.museocarandnuria.es/mediasalas/"+EntradaMuseo.majorMuseo+"/sala"+salaPresente+".mp3";

                    try{
                        urlMediaModel=diccionarioSalaMedia.get(salaPresente);
                    }catch (Exception e){
                        urlMediaModel="http://www.museocarandnuria.es/mp3/"+EntradaMuseo.majorMuseo+"/andy/andy.mp4";;
                    }
                }

            }
        };

        changeCharacter=(ImageButton)findViewById(R.id.changeCharacter);
        changeCharacter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //alternar muñecos
                contadorAlterno++;
                if((contadorAlterno%2)==0){
                    Toast toast=Toast.makeText(RealidadAumentada.this, "Andy seleccionado", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }else{
                    Toast toast=Toast.makeText(RealidadAumentada.this, "Dinosaurio seleccionado", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    toast.show();
                }
                //urlMediaModel="http://www.museocarandnuria.es/mediasalas/"+EntradaMuseo.majorMuseo+"/sala"+salaPresente+".mp3";

            }
        });

        actualizarBtn=(ImageButton)findViewById(R.id.actualizarBtnIB);
        actualizarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RealidadAumentada.this, "Comprobando sala...", Toast.LENGTH_LONG).show();
                actualizarSala();
                Toast.makeText(RealidadAumentada.this, "Listo", Toast.LENGTH_SHORT).show();
            }
        });
        papelera=(ImageButton)findViewById(R.id.deleteIB);
        papelera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(existeAndy){
                    modelAndy.removeChild(mediaControlsAndy);
                    anchorNodeAndy.removeChild(modelAndy);
                    existeAndy=false;
                }
                if(existeDino){
                    modelDino.removeChild(mediaControlsDino);
                    anchorNodeDino.removeChild(modelDino);
                    existeDino=false;
                }

            }
        });

        final FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fabAR);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                if(permisoAlmacenarFotos()){
                    hacerFoto();
                }

            }
        });

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

        inicializarRenderables();

        // Create the transformable andy and add it to the anchor.
        modelAndy = new TransformableNode(arFragment.getTransformationSystem());
        modelDino = new TransformableNode(arFragment.getTransformationSystem());
        mediaControlsAndy = new Node();
        mediaControlsDino = new Node();


        if(Build.VERSION.SDK_INT>=24) {
            arFragment.setOnTapArPlaneListener(
                    (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                        if ((andyRenderable == null) || (dinoRenderable == null)) {
                            return;
                        }

                        // Create the Anchor.
                        Anchor anchor = hitResult.createAnchor();

                        if ((contadorAlterno % 2) == 0) {
                            if (!existeAndy) {
                                anchorNodeAndy = new AnchorNode(anchor);
                                anchorNodeAndy.setParent(arFragment.getArSceneView().getScene());
                                crearModelo(anchorNodeAndy, true);
                                existeAndy = true;

                                modelAndy.setOnTouchListener(
                                        (hitTestResult, event) -> {
                                            releasePlayer();
                                            initializePlayer();
                                            return true;
                                        });


                            } else {
                                modelAndy.removeChild(mediaControlsAndy);
                                anchorNodeAndy.removeChild(modelAndy);
                                existeAndy = false;
                            }


                        } else {

                            if (!existeDino) {
                                anchorNodeDino = new AnchorNode(anchor);
                                anchorNodeDino.setParent(arFragment.getArSceneView().getScene());
                                crearModelo(anchorNodeDino, false);
                                existeDino = true;

                                modelDino.setOnTouchListener(
                                        (hitTestResult, event) -> {
                                            releasePlayer();
                                            initializePlayer();
                                            return true;
                                        });

                            } else {
                                modelDino.removeChild(mediaControlsDino);
                                anchorNodeDino.removeChild(modelDino);
                                existeDino = false;
                            }

                        }

                    });
        }

        actualizarSala();

    }//fin onCreate

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }

    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        Uri uri= Uri.parse(urlMediaModel);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    @RequiresApi(24)
    private void inicializarRenderables(){

        if(Build.VERSION.SDK_INT>=24) {
            ModelRenderable.builder()
                    //.setSource(this, R.raw.andy)
                    .setSource(this, Uri.parse("andy.sfb"))
                    .build()
                    .thenAccept(renderable -> andyRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Imposible cargar a Andy", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });

            ModelRenderable.builder()
                    //.setSource(this, R.raw.andy)
                    .setSource(this, Uri.parse("TrexByJoel3d.sfb"))
                    .build()
                    .thenAccept(renderable -> dinoRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Imposible cargar dinosaurio", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            ViewRenderable.builder()
                    .setView(this, R.layout.models_exta_content)
                    .build()
                    .thenAccept(
                            (renderable) -> {
                                mediaAndyRenderable = renderable;
                            })
                    .exceptionally(
                            (throwable) -> {
                                throw new AssertionError("Could not load plane card view.", throwable);
                            });
            ViewRenderable.builder()
                    .setView(this, R.layout.models_exta_content)
                    .build()
                    .thenAccept(
                            (renderable) -> {
                                mediaDinoRenderable = renderable;
                            })
                    .exceptionally(
                            (throwable) -> {
                                throw new AssertionError("Could not load plane card view.", throwable);
                            });
        }
    }

    @RequiresApi(24)
    private void crearModelo(AnchorNode anchorNode, Boolean crearAndy){

        View mediaView;

        if(crearAndy){
            modelAndy.setParent(anchorNode);
            modelAndy.setRenderable(andyRenderable);
            modelAndy.select();

            mediaControlsAndy.setParent(modelAndy);
            mediaControlsAndy.setRenderable(mediaAndyRenderable);
            mediaControlsAndy.setLocalPosition(new Vector3(0.0f, 0.25f, 0.0f));

            mediaView = mediaAndyRenderable.getView();

        }else{
            modelDino.setParent(anchorNode);
            modelDino.setRenderable(dinoRenderable);
            modelDino.select();

            mediaControlsDino.setParent(modelDino);
            mediaControlsDino.setRenderable(mediaDinoRenderable);
            mediaControlsDino.setLocalPosition(new Vector3(0.0f, 0.35f, 0.0f));

            mediaView = mediaDinoRenderable.getView();
        }

        playerView=mediaView.findViewById(R.id.video_AR);

        if(player == null){
            initializePlayer();
        }else{
            releasePlayer();
            initializePlayer();
        }

    }

    public void diccionarioSalas(){
        SQLiteDatabase db =manejadorBBDD.getReadableDatabase();
        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Balizas.NOMBRE_COLUMNA_2, //id_estimote
                Estructura_BD.Balizas.NOMBRE_COLUMNA_6  //sala
        };
        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Balizas.NOMBRE_COLUMNA_1 + " = ?";
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo };

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
            //no hay balizas alacenadas
            Log.d("diccionario","vacío");
        }else{
            int numFilas=0;
            int idBaliza,idSala;
            for(numFilas=0;numFilas<cursorBalizas.getCount();numFilas++){
                idBaliza=cursorBalizas.getInt(0);
                idSala=cursorBalizas.getInt(1);
                diccionarioTagSala.put(idBaliza,idSala);
                //Toast.makeText(this, "baliza "+idBaliza+" en sala "+sala, Toast.LENGTH_SHORT).show();
                cursorBalizas.moveToNext();
                Log.d("diccionario","lleno");
            }
            numFilas=0;
        }
        cursorBalizas.close();
    }


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }
    private void actualizarSala(){
        //buscar sala
        salasDetectadas.clear(); cantidadDetectadas.clear();
        scanLeDevice(true);
    }

    public File generarFichero() {
        File ruta=new File(Environment.getExternalStoragePublicDirectory("miMuseo"),"Media");
        if (!ruta.exists()) {
            Toast.makeText(this, "Creando directorio", Toast.LENGTH_SHORT).show();
            ruta.mkdirs();
        }
        return ruta;
    }

    @RequiresApi(24)
    private void hacerFoto() {

        if(Build.VERSION.SDK_INT>=24){
            ArSceneView view = arFragment.getArSceneView();

            // Create a bitmap the size of the scene view.
            final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                    Bitmap.Config.ARGB_8888);

            String timeStamp = new  SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String mPath = generarFichero().getAbsolutePath().toString() + "/" + timeStamp + ".jpg";

            // Create a handler thread to offload the processing of the image.
            final HandlerThread handlerThread = new HandlerThread("PixelCopier");
            handlerThread.start();
            // Make the request to copy.
            PixelCopy.request(view, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    try {

                        FileOutputStream outputStream = new FileOutputStream(mPath);
                        ByteArrayOutputStream outputData = new ByteArrayOutputStream();

                        int quality = 100;
                        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
                        outputData.writeTo(outputStream);
                        outputStream.flush();
                        outputStream.close();

                    } catch (IOException e) {
                        Toast toast = Toast.makeText(RealidadAumentada.this, e.toString(),
                                Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Foto guardada", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Compartir", v -> {
                        File photoFile = new File(mPath);

                        Uri photoURI = FileProvider.getUriForFile(RealidadAumentada.this,
                                RealidadAumentada.this.getPackageName() + ".fileprovider",
                                photoFile);
                        if(photoURI!=null){
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setDataAndType(photoURI, "image/jpg");
                            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                            intent.putExtra(Intent.EXTRA_TEXT, "¡Mira lo que estoy viendo con la aplicación miMuseo!");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(intent, "Seleccione una aplicación"));
                        }

                    });

                    snackbar.show();

                } else {
                    Toast toast = Toast.makeText(RealidadAumentada.this,
                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }


    }


    public void lanzarSolicitarPermiso(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //No hago nada
        } else {
            solicitarPermiso(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Sin este permiso"+
                            " no se pueden hacer fotografías",
                    SOLICITUD_PERMISO_ALMACENAMIENTO, this);
        }

    }

    public void solicitarPermiso(final String permiso, String
            justificacion, final int requestCode, final Activity actividad) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(actividad, permiso)){
            new AlertDialog.Builder(actividad)
                    .setTitle("Solicitud de permiso")
                    .setMessage(justificacion)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ActivityCompat.requestPermissions(actividad,
                                    new String[]{permiso}, requestCode);
                        }})
                    .show();
        } else {
            ActivityCompat.requestPermissions(actividad, new String[]{permiso}, requestCode);
        }
    }

    public boolean permisoAlmacenarFotos(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            //si tengo el permiso, puedo almacenar fotos
            return true;
        }
        lanzarSolicitarPermiso();
        return false;
    }

    public void leerMediaSalas(){

        SQLiteDatabase db = manejadorBBDD.getReadableDatabase();

        String[] projection = {
                //campos a devolver tras la consulta
                Estructura_BD.Parametros.NOMBRE_COLUMNA_5,
                Estructura_BD.Parametros.NOMBRE_COLUMNA_8
        };

        //nombre de la columna dnd se encuentra el dato para el WHERE
        String selection = Estructura_BD.Parametros.NOMBRE_COLUMNA_1 + " = ?" ;
        //valor de dicho dato en la columna
        String[] selectionArgs = { ""+EntradaMuseo.majorMuseo };

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
            //nada
        }else{
            int i=0;
            String sala,media;
            for(i=0;i<cursor.getCount();i++){
                //EN LECTURA NO ASIGNO (solo a un TextView para ver los valores)
                sala=cursor.getString(0);
                media=cursor.getString(1);
                diccionarioSalaMedia.put(Integer.parseInt(sala),media);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

}//fin clase

