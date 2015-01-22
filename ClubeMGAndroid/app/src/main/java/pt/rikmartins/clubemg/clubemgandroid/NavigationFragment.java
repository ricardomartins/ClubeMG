package pt.rikmartins.clubemg.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 08-12-2014.
 */
public class NavigationFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = NavigationFragment.class.getSimpleName();

    public static final String TIPO_ON_CLICK_NOTICIAS = "noticias";
    public static final String TIPO_ON_CLICK_CATEGORIA = "categoria";
    public static final String TIPO_ON_CLICK_DEFINICOES = "definicoes";

    private LinearLayout mNavigationLinearLayout;
    private ListView     mCategoriasListView;

    private MergeAdapter mNavigationMergeAdapter;

    private static final int URL_LOADER_CATEGORIAS = 10;

    private static final String[] CATEGORIAS_FROM = new String[]{
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO,
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO
    };

    private static final String OUTROS_ICON = "icon";
    private static final String OUTROS_TITULO = "título";
    private static final String[] OUTROS_FROM = new String[] {
            OUTROS_ICON,
            OUTROS_TITULO
    };

    private static final int[] DRAWER_LIST_ITEM_TO = new int[]{
            R.id.image_view_item_navegacao,
            R.id.item_navegacao,
    };


    private SimpleCursorAdapter mCategoriasCursorAdapter;

    private HashMap<String, DescriptorCategoriaConhecida> descricaoCategoriasConhecidas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Creating");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "Creating view");
        mNavigationLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_navigation,
                container, false);
        mCategoriasListView = (ListView) mNavigationLinearLayout.findViewById(
                R.id.left_navigation_drawer_categorias_list);
        getLoaderManager().initLoader(URL_LOADER_CATEGORIAS, null, this);

        return mNavigationLinearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mNavigationMergeAdapter = new MergeAdapter();

        ArrayList<HashMap<String, Object>> simpleAdapterData = new ArrayList<>(1);
        simpleAdapterData.add(new HashMap<String, Object>(2));
        simpleAdapterData.get(0).put(OUTROS_ICON, R.drawable.ic_dashboard_grey600_24dp);
        simpleAdapterData.get(0).put(OUTROS_TITULO, "Notícias");
        SimpleAdapter semFiltroAdapter = new SimpleAdapter(getActivity(), simpleAdapterData, R.layout.drawer_list_item, OUTROS_FROM, DRAWER_LIST_ITEM_TO);

        mNavigationMergeAdapter.addAdapter(semFiltroAdapter);

        descricaoCategoriasConhecidas = construirDescricaoCategoriasConhecidas();

        mCategoriasCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.drawer_list_item, null, CATEGORIAS_FROM, DRAWER_LIST_ITEM_TO, 0);
        mCategoriasCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                String categoria = cursor.getString(cursor.getColumnIndex(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO));
                if (descricaoCategoriasConhecidas.containsKey(categoria)) {
                    if (view instanceof TextView) {
                        if (view.getId() == R.id.item_navegacao) {
                            ((TextView) view).setText(descricaoCategoriasConhecidas.get(categoria).tituloCategoria);
                            return true;
                        }
                    } else if (view instanceof ImageView) {
                        if (view.getId() == R.id.image_view_item_navegacao) {
                            ((ImageView) view).setImageDrawable(descricaoCategoriasConhecidas.get(categoria).iconeCategoria);
                            return true;
                        }
                    }
                } else {
                    if (view instanceof TextView) {
                        if (view.getId() == R.id.item_navegacao) {
                            String categoriaCapitalizada = categoria.substring(0, 1).toUpperCase() + categoria.substring(1);
                            ((TextView) view).setText(categoriaCapitalizada);
                            return true;
                        }
                    }
                }

                return false;
            }
        });
        mNavigationMergeAdapter.addAdapter(mCategoriasCursorAdapter);

        simpleAdapterData = new ArrayList<>(1);
        simpleAdapterData.add(new HashMap<String, Object>(2));
        simpleAdapterData.get(0).put(OUTROS_ICON, R.drawable.ic_settings_grey600_24dp);
        simpleAdapterData.get(0).put(OUTROS_TITULO, "Definições");
        SimpleAdapter definicoesAdapter = new SimpleAdapter(getActivity(), simpleAdapterData, R.layout.drawer_list_item, OUTROS_FROM, DRAWER_LIST_ITEM_TO);

        mNavigationMergeAdapter.addAdapter(definicoesAdapter);

        mCategoriasListView.setAdapter(mNavigationMergeAdapter);

        mCategoriasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) ((MainActivity) getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_NOTICIAS, null);
                else if (position == (parent.getCount() - 1)) ((MainActivity) getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_DEFINICOES, null);
                else ((MainActivity) getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_CATEGORIA, String.valueOf(id));
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        super.onAttach(activity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER_CATEGORIAS:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        NoticiaContract.Categoria.CONTENT_URI,        // Table to query
                        NoticiaProvider.getCopyOfCategoriaDefaultProjection(),     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCategoriasCursorAdapter.changeCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCategoriasCursorAdapter.changeCursor(null);
    }

    private HashMap<String, DescriptorCategoriaConhecida> construirDescricaoCategoriasConhecidas
            (){
        Resources res = getResources();
        String[] categorias = res.getStringArray(R.array.lista_categorias);
        TypedArray iconesCategorias = res.obtainTypedArray(R.array.lista_icones_categorias);
        String[] titulosCategorias = res.getStringArray(R.array.lista_titulos_categorias);
        int quantidadeCategorias = categorias.length;

        HashMap<String, DescriptorCategoriaConhecida> resultado = new HashMap<>(quantidadeCategorias);
        for (int i = 0; i < quantidadeCategorias; i++)
            resultado.put(categorias[i], new DescriptorCategoriaConhecida(categorias[i], iconesCategorias.getDrawable(i), titulosCategorias[i]));

        return resultado;
    }

    private static class DescriptorCategoriaConhecida {
        public DescriptorCategoriaConhecida(String categoria, Drawable iconeCategoria, String tituloCategoria){
            this.categoria = categoria;
            this.iconeCategoria = iconeCategoria;
            this.tituloCategoria = tituloCategoria;
        }

        public String categoria;
        public Drawable iconeCategoria;
        public String tituloCategoria;
    }
}
