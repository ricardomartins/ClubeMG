package pt.rikmartins.clubemg.clubemgandroid.definicoes;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.HashMap;
import java.util.Map;

import pt.rikmartins.clubemg.clubemgandroid.R;
import pt.rikmartins.clubemg.clubemgandroid.ToolbarHolder;
import pt.rikmartins.clubemg.clubemgandroid.sync.SyncUtils;

public class DefinicoesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Resources resources;

    private static Map<String, String> freqSincValorTitulo = null;
    private ToolbarHolder mToolbarHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        resources = getActivity().getResources();
        DefinicoesFragment.inicializarFreqSincValorTitulo(resources);

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferencias);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actualizaSumarioFrequenciaSincronizacao(getPreferenceManager().getSharedPreferences());

        mToolbarHolder = getActivity() instanceof ToolbarHolder ? (ToolbarHolder) getActivity() : null;
    }

    private static void inicializarFreqSincValorTitulo(Resources resources){
        if (freqSincValorTitulo == null) {
            String[] titulosFreqSinc = resources.getStringArray(R.array.pref_freq_sinc_titulos);
            String[] valoresFreqSinc = resources.getStringArray(R.array.pref_freq_sinc_valores);

            freqSincValorTitulo = new HashMap<>(titulosFreqSinc.length);
            for (int i = 0; i < valoresFreqSinc.length; i++)
                freqSincValorTitulo.put(valoresFreqSinc[i], titulosFreqSinc[i]);
        }
    }

    public void actualizaSumarioFrequenciaSincronizacao(SharedPreferences sharedPreferences){
        String valorFreqSinc = sharedPreferences.getString(resources.getString(R.string.pref_key_freq_sinc), "");

        alteraSumarioFrequenciaSincronizacao(freqSincValorTitulo.get(valorFreqSinc));
    }

    public void alteraSumarioFrequenciaSincronizacao(String novoSumario){
        Preference connectionPref = findPreference(resources.getString(R.string.pref_key_freq_sinc));
        // Set summary to be the user-description for the selected value
        connectionPref.setSummary(novoSumario);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(resources.getString(R.string.pref_key_freq_sinc))){
            actualizaSumarioFrequenciaSincronizacao(sharedPreferences);
            SyncUtils.UpdateSyncPeriod(getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        if (mToolbarHolder != null)
            mToolbarHolder.getToolbar().setTitle(R.string.titulo_fragmento_definicoes);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
