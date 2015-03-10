package pt.rikmartins.clubemg.clubemgandroid;

import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import pt.rikmartins.clubemg.clubemgandroid.definicoes.DefinicoesFragment;
import pt.rikmartins.clubemg.clubemgandroid.instituicao.InstituicaoFragment;
import pt.rikmartins.clubemg.clubemgandroid.listanoticias.ListaNoticiasFragment;
import pt.rikmartins.clubemg.clubemgandroid.sync.SyncUtils;

public class MainActivity
        extends ActionBarActivity implements NavigationEventListener, ToolbarHolder {
    public static final String TAG = MainActivity.class.getSimpleName();

    private View               mMainLayout;
    private NavigationFragment mNavigationFragment;
    private Toolbar            mToolbar;
    private FrameLayout mMainContainer;

    final static int TIPO_DE_LAYOUT_DRAWER_LAYOUT = 100;
    final static int TIPO_DE_LAYOUT_OUTRO = 0;
    int mTipoDeLayout;

    // Quando é DrawerLayout
    private ActionBarDrawerToggle mNavigationDrawerToggle;
    private boolean mUtilizadorAprendeuNavegacao;
    private View mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferencias, false);

        SyncUtils.CreateSyncAccount(this);

        setMainLayout(R.id.main_layout);
        mMainContainer = (FrameLayout) findViewById(R.id.main_container);
        setSupportActionBar(mToolbar = (Toolbar) findViewById(R.id.toolbar));
        mNavigationFragment = (NavigationFragment) getFragmentManager().findFragmentById(R.id.navigation_fragment);
        mNavigationView = findViewById(R.id.navigation_fragment);

        mUtilizadorAprendeuNavegacao = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_utilizador_aprendeu_navegacao), false);

        if  (mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT) onCreateWithDrawerLayout(savedInstanceState);
        else assert false; // TODO: Alterar isto

        if (savedInstanceState == null) {
            ListaNoticiasFragment listaNoticiasFragment = ListaNoticiasFragment.newInstance();
            getFragmentManager().beginTransaction().add(R.id.main_container, listaNoticiasFragment, TAG_FRAGMENTO_LISTA_NOTICIAS).commit();
        }
    }

    private void onCreateWithDrawerLayout(Bundle savedInstanceState){
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorTextLight));

        DrawerLayout drawerLayout = (DrawerLayout) mMainLayout;
        mNavigationDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar,
                                                            R.string.abrir_gaveta_navegacao,
                                                            R.string.fechar_gaveta_navegacao) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!mUtilizadorAprendeuNavegacao) {
                    mUtilizadorAprendeuNavegacao = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);
                    sp.edit().putBoolean(getString(R.string.pref_key_utilizador_aprendeu_navegacao), true).apply();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerLayout.setDrawerListener(mNavigationDrawerToggle);

        if (!mUtilizadorAprendeuNavegacao && savedInstanceState == null) {
            drawerLayout.openDrawer(mNavigationView);
        }
    }

    private void setMainLayout(View layout){
        mMainLayout = layout;
        if (mMainLayout instanceof DrawerLayout) mTipoDeLayout = TIPO_DE_LAYOUT_DRAWER_LAYOUT;
        else mTipoDeLayout = TIPO_DE_LAYOUT_OUTRO;
    }

    private void setMainLayout(int id){
        setMainLayout(findViewById(id));
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    private static final String TAG_FRAGMENTO_DEFINICOES = "definições";
    private static final String TAG_FRAGMENTO_LISTA_NOTICIAS = "notícias";
    private static final String TAG_FRAGMENTO_INSTITUICAO = "instituição";

    public static final String NAVIGATON_KEY_INTERNO_CATEGORIA = "interno_categoria";
    public static final String NAVIGATON_KEY_INTERNO_DEFINICOES = "interno_definicoes";
    public static final String NAVIGATON_KEY_INTERNO_INSTITUICAO = "interno_instituicao";

    public static final String NAVIGATON_KEY_EXTERNO_PRINCIPAL = "externo_principal";
    public static final String NAVIGATON_KEY_EXTERNO_SECUNDARIO = "externo_secundario";

    @Override
    public void onNavigationEvent(Bundle atacado) {
        FragmentManager fragmentManager = getFragmentManager();

        if (atacado.containsKey(NAVIGATON_KEY_INTERNO_CATEGORIA)) {
            String categoria = atacado.getString(NAVIGATON_KEY_INTERNO_CATEGORIA);

            ListaNoticiasFragment listaNoticiasFragment = (ListaNoticiasFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENTO_LISTA_NOTICIAS);
            if (listaNoticiasFragment != null) listaNoticiasFragment.substituirCategoria(categoria);
            else fragmentManager.beginTransaction()
                    .replace(R.id.main_container, ListaNoticiasFragment.newInstance(categoria), TAG_FRAGMENTO_LISTA_NOTICIAS)
                    .commit();
        } else if (atacado.containsKey(NAVIGATON_KEY_INTERNO_DEFINICOES)) {
            // String algo = atacado.getString(NAVIGATON_KEY_INTERNO_DEFINICOES);

            DefinicoesFragment definicoesFragment = (DefinicoesFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENTO_DEFINICOES);
            fragmentManager.beginTransaction()
                    .replace(R.id.main_container, (definicoesFragment == null) ? new DefinicoesFragment() : definicoesFragment, TAG_FRAGMENTO_DEFINICOES).commit();
        } else if (atacado.containsKey(NAVIGATON_KEY_INTERNO_INSTITUICAO)) {
            // String algo = atacado.getString(NAVIGATON_KEY_INTERNO_INSTITUICAO);

            InstituicaoFragment instituicaoFragment = (InstituicaoFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENTO_INSTITUICAO);
            fragmentManager.beginTransaction()
                    .replace(R.id.main_container, (instituicaoFragment == null) ? new InstituicaoFragment() : instituicaoFragment, TAG_FRAGMENTO_INSTITUICAO).commit();
        } else if (atacado.containsKey(NAVIGATON_KEY_EXTERNO_PRINCIPAL) || atacado.containsKey(NAVIGATON_KEY_EXTERNO_SECUNDARIO)) {
            String extPrincipal = atacado.getString(NAVIGATON_KEY_EXTERNO_PRINCIPAL);
            String extSecundario = atacado.getString(NAVIGATON_KEY_EXTERNO_SECUNDARIO);

            Uri uriExtPrincipal = extPrincipal != null ? Uri.parse(extPrincipal) : null;
            Uri uriExtSecundario = extSecundario != null ? Uri.parse(extSecundario) : null;

            if (uriExtPrincipal != null) {
                Intent i = new Intent(Intent.ACTION_VIEW, uriExtPrincipal);
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    if (uriExtSecundario != null) {
                        i = new Intent(Intent.ACTION_VIEW, uriExtSecundario);
                        startActivity(i);
                    }
                }
            }

        }

        if (mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT)
            ((DrawerLayout) mMainLayout).closeDrawer(Gravity.START);
    }

    @Override
    public void onBackPressed() {
        if ((mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT) && ((DrawerLayout) mMainLayout).isDrawerOpen(Gravity.START))
            ((DrawerLayout) mMainLayout).closeDrawer(Gravity.START);
        else if (getFragmentManager().getBackStackEntryCount() > 0)
            getFragmentManager().popBackStack();
        else super.onBackPressed();
    }
}
