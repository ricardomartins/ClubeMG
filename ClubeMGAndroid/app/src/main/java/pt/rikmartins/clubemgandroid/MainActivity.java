package pt.rikmartins.clubemgandroid;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class MainActivity
        extends ActionBarActivity {
    private View               mMainLayout;
    private NavigationFragment mNavigationFragment;
    private Toolbar            mToolbar;

    final static int TIPO_DE_LAYOUT_DRAWER_LAYOUT = 100;
    final static int TIPO_DE_LAYOUT_OUTRO = 0;
    int mTipoDeLayout;

    // Quando é DrawerLayout
    private ActionBarDrawerToggle mNavigationDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setMainLayout(R.id.main_layout);
        setSupportActionBar(mToolbar = (Toolbar) findViewById(R.id.toolbar));
        mNavigationFragment = (NavigationFragment) getFragmentManager().findFragmentById(R.id.navigation_fragment);

        if  (mTipoDeLayout == TIPO_DE_LAYOUT_DRAWER_LAYOUT) onCreateWithDrawerLayout(savedInstanceState);
        else assert false; // TODO: Alterar isto
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

    public void onNavigationEvent(String modo, String coisas){
        Toast toast = Toast.makeText(this, modo + " " + coisas, Toast.LENGTH_SHORT); // TODO: A apagar
        toast.show(); // TODO: A apagar

        switch(modo){
            case NavigationFragment.TIPO_ON_CLICK_CATEGORIA:
                // TODO: Alterar a lista de notícias visíveis
                break;
        }
    }

}
