package nos.nakisha.org.miMuseoNOS;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //pantalla completa sin barra
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //cargamos el layout
        setContentView(R.layout.activity_splash);
        //usamos una animación
        FrameLayout frameLayoutLogo = (FrameLayout) findViewById(R.id.frameLayoutLogoSplash);
        Animation loadingEntrada = AnimationUtils.loadAnimation(this, R.anim.loading);
        frameLayoutLogo.startAnimation(loadingEntrada);
        loadingEntrada.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
                startActivity(new Intent(SplashActivity.this, EntradaMuseo.class));
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
