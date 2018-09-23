package nos.nakisha.org.miMuseoNOS;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/**
 * Created by nakis on 14/06/2018.
 */

public class PreferenciasVisitanteFragment extends PreferenceFragment {


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.config_visitante);

        final EditTextPreference ruido=(EditTextPreference)findPreference("ruidoVAR");
        ruido.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object object) {
                float valor;
                try{
                    valor=Float.parseFloat((String)object);
                }catch (Exception e){
                    Toast.makeText(getActivity(), "No válido", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(valor>=0 && valor<=1.5f){
                    if(valor>=0 && valor<0.5f){
                        ruido.setSummary("Sensibilidad "+valor+" correcta");
                    }else if(valor>=0.5f && valor<1f){
                        ruido.setSummary("Sensibilidad "+valor+" elevada");
                    }else if(valor>=1f && valor<=1.5f){
                        ruido.setSummary("Sensibilidad "+valor+" demasiado elevada");
                    }
                    return true;
                }else{
                    Toast.makeText(getActivity(), "Introduzca un valor entre 0 y 1.5", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }
}
