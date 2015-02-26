package pt.rikmartins.clubemg.clubemgandroid;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;

import pt.rikmartins.clubemg.clubemgandroid.sync.SyncUtils;

public class MainActivity
        extends ActionBarActivity implements NavigationFragment.ItemClickListener, ToolbarHolder {
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

    @Override
    public void onNavigationEvent(String modo, String dados) {
        FragmentManager fragmentManager = getFragmentManager();
        switch(modo){
            case NavigationFragment.TIPO_ON_CLICK_CATEGORIA:
            case NavigationFragment.TIPO_ON_CLICK_NOTICIAS:
                ListaNoticiasFragment listaNoticiasFragment = (ListaNoticiasFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENTO_LISTA_NOTICIAS);
                if (listaNoticiasFragment != null) {
                    listaNoticiasFragment.substituirCategoria(dados);
                } else {
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_container, ListaNoticiasFragment.newInstance(dados), TAG_FRAGMENTO_LISTA_NOTICIAS)
                            .commit();
                }
                break;
            case NavigationFragment.TIPO_ON_CLICK_DEFINICOES:
                DefinicoesFragment definicoesFragment = (DefinicoesFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENTO_DEFINICOES);
                fragmentManager.beginTransaction()
                    .replace(R.id.main_container, (definicoesFragment == null) ? new DefinicoesFragment() : definicoesFragment, TAG_FRAGMENTO_DEFINICOES).commit();
                break;
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
