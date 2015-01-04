package pt.rikmartins.clubemgandroid;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity
        extends ActionBarActivity {
    private View               mMainLayout;
    private NavigationFragment mNavigationFragment;
    private Toolbar            mToolbar;
    private FrameLayout mMainContainer;

    final static int TIPO_DE_LAYOUT_DRAWER_LAYOUT = 100;
    final static int TIPO_DE_LAYOUT_OUTRO = 0;
    int mTipoDeLayout;

    // Quando é DrawerLayout
    private ActionBarDrawerToggle mNavigationDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SyncUtils.CreateSyncAccount(this);

        setMainLayout(R.id.main_layout);
        mMainContainer = (FrameLayout) findViewById(R.id.main_container);
        setSupportActionBar(mToolbar = (Toolbar) findViewById(R.id.toolbar));
        mNavigationFragment = (NavigationFragment) getFragmentManager().findFragmentById(R.id.navigation_fragment);

        if  (mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT) onCreateWithDrawerLayout(savedInstanceState);
        else assert false; // TODO: Alterar isto

        ListaNoticiasFragment listaNoticiasFragment = ListaNoticiasFragment.newInstance();
        getFragmentManager().beginTransaction().add(R.id.main_container, listaNoticiasFragment).commit();
    }

    private void onCreateWithDrawerLayout(Bundle savedInstanceState){
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        
        mNavigationDrawerToggle = new ActionBarDrawerToggle(this, (DrawerLayout) mMainLayout, mToolbar,
                                                            R.string.navigation_drawer_open,
                                                            R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
    }

    private void setMainLayout(View layout){
        mMainLayout = layout;
        if (mMainLayout instanceof DrawerLayout) mTipoDeLayout = TIPO_DE_LAYOUT_DRAWER_LAYOUT;
        else mTipoDeLayout = TIPO_DE_LAYOUT_OUTRO;
    }

    private void setMainLayout(int id){
        setMainLayout(findViewById(R.id.main_layout));
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void onNavigationEvent(String modo, String dados){
        switch(modo){
            case NavigationFragment.TIPO_ON_CLICK_CATEGORIA:
                getFragmentManager().beginTransaction().replace(R.id.main_container, ListaNoticiasFragment.newInstance(dados)).addToBackStack(null).commit();
                if (mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT)
                    ((DrawerLayout) mMainLayout).closeDrawers();
                break;
        }
    }
}
