package nos.nakisha.org.miMuseoNOS;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TiposDePosicionamientoActivity extends AppCompatActivity {

    public Button trilateracionBtn;
    public Button kalmanEstBtn;
    public Button kalmanDinBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipos_de_posicionamiento);

        trilateracionBtn=(Button)findViewById(R.id.trilateracionBtn);
        trilateracionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentoTrilateracion=new Intent(TiposDePosicionamientoActivity.this,Trilateracion.class);
                startActivity(intentoTrilateracion);
            }
        });
        kalmanEstBtn=(Button)findViewById(R.id.kalmanEstBtn);
        kalmanEstBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentoKalmanEstatica=new Intent(TiposDePosicionamientoActivity.this, KalmanEstatica.class);
                intentoKalmanEstatica.putExtra("desarrollador",1);
                startActivity(intentoKalmanEstatica);

            }
        });
        kalmanDinBtn=(Button)findViewById(R.id.kalmanDinBtn);
        kalmanDinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentoKalmanDinamica=new Intent(TiposDePosicionamientoActivity.this, KalmanDin.class);
                intentoKalmanDinamica.putExtra("desarrollador",1);
                startActivity(intentoKalmanDinamica);
            }
        });
    }
}
