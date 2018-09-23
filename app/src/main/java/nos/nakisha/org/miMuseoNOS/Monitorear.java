package nos.nakisha.org.miMuseoNOS;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by nakis on 15/12/2017.
 */

public class Monitorear extends Application {

    private BeaconManager beaconManager;
    static final String CANAL_ID = "canal_miMuseo";
    private int NOTIFICACION_ID = 0;
    final int segundosEsperarMonitorear=1200;//20*60=1200
    final int segundosEscaneoMonitorear=2;
    NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setBackgroundScanPeriod(segundosEscaneoMonitorear*1000,segundosEsperarMonitorear*1000);
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener(){


            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> list) {
                mostrarNotificacion("Bienvenido a miMuseo", "Un museo con contexto");
            }

            @Override
            public void onExitedRegion(BeaconRegion region) {
                // could add an "exit" notification too if you want (-:
                mostrarNotificacion("Hasta Pronto", "miMuseo agradece su visita");
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        null, null));
            }
        });
    }

    public void mostrarNotificacion(String titulo, String mensaje) {
        //este método le pasa la información a las notificaciones

        //ANTES
        /*
        Intent notifyIntent = new Intent(this, EntradaMuseo.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificacion = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificacion.defaults |= Notification.DEFAULT_SOUND;
*/
        //AHORA
        Intent intent =new Intent(this, EntradaMuseo.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent pendingIntentMuseo = PendingIntent.getActivity(Monitorear.this, 0, intent , PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intencionAbrirApp = new Intent(Monitorear.this, EntradaMuseo.class);
        final PendingIntent intencionPendienteAbrirApp = PendingIntent.getActivity(Monitorear.this, 0, intencionAbrirApp, 0);
        List<NotificationCompat.Action> acciones = new ArrayList<NotificationCompat.Action>();
        NotificationCompat.Action accionAbrirApp = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_view, "Abrir App", intencionPendienteAbrirApp).build();
        acciones.add(accionAbrirApp);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(false)
                .addActions(acciones);


        NotificationManager manejadorNotificaciones =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CANAL_ID, "Museos cercanos", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Avisa de la presencia de museos con contenido ampliado");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 300, 100});
            notificationChannel.enableVibration(true);
            manejadorNotificaciones.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificacionMuseo = new NotificationCompat.Builder( Monitorear.this, CANAL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("La aplicación miMuseo ha detectado la presencia de balizas BLE. Es posible que haya un museo en los alrededores con contenido ampliado"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .extend(wearableExtender)
                .setContentIntent(pendingIntentMuseo);//.setContentIntent(pendingIntent);

        //manejadorNotificaciones.notify(NOTIFICACION_ID, notificacion);

        notificationManagerCompat = NotificationManagerCompat.from(Monitorear.this);
        notificationManagerCompat.notify(NOTIFICACION_ID ++,notificacionMuseo.build());
        //manejadorNotificaciones.notify(NOTIFICACION_ID, notificacionMuseo.build());
    }

}

