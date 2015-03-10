package pt.rikmartins.clubemg.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.HashMap;

import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 08-12-2014.
 */
public class NavigationFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {
    private static final String TAG = NavigationFragment.class.getSimpleName();

    private LinearLayout mNavigationLinearLayout;
    private ListView     mCategoriasListView;

    private SimpleCursorAdapter mSimpleCursorAdapter;

    private static final int URL_LOADER_CATEGORIAS = 10;

    private NavigationEventListener navigationEventListener;

    private static final String[] CATEGORIAS_FROM = new String[]{
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO,
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO
    };

    private static final int[] DRAWER_LIST_ITEM_TO = new int[]{
            R.id.image_view_item_navegacao,
            R.id.item_navegacao,
    };

    private HashMap<String, DescriptorCategoriaConhecida> descricaoCategoriasConhecidas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Creating");
        super.onCreate(savedInstanceState);
    }

    private void criarItensNavegacaoEstaticos(LayoutInflater inflater){
        View cabecalhoNavegacao = inflater.inflate(R.layout.cabecalho_navigation, mCategoriasListView, false);
        cabecalhoNavegacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle atacado = new Bundle(1);
                atacado.putBoolean(MainActivity.NAVIGATON_KEY_INTERNO_INSTITUICAO, true);
                navigationEventListener.onNavigationEvent(atacado);
            }
        });

        cabecalhoNavegacao.findViewById(R.id.icone_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriPrincipal = "fb://page/123780544307693"; // TODO: Passar as identificações para XML
                String uriSecundario = "https://www.facebook.com/pages/Clube-de-Montanhismo-da-Guarda/123780544307693"; // TODO: Passar as identificações para XML

                Bundle atacado = new Bundle(2);
                atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, uriPrincipal);
                atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_SECUNDARIO, uriSecundario);
                navigationEventListener.onNavigationEvent(atacado);
            }
        });
        cabecalhoNavegacao.findViewById(R.id.icone_googleplus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriPrincipal = "https://plus.google.com/u/1/+ClubedeMontanhismodaGuarda/posts"; // TODO: Passar as identificações para XML

                Bundle atacado = new Bundle(1);
                atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, uriPrincipal);
                navigationEventListener.onNavigationEvent(atacado);
            }
        });
        mCategoriasListView.addHeaderView(cabecalhoNavegacao);
        mCategoriasListView.addHeaderView(inflater.inflate(R.layout.separador_lista_transparente, mCategoriasListView, false));

        View itemNavegacaoNoticias = inflater.inflate(R.layout.imagem_texto_list_item, mCategoriasListView, false);
        ((ImageView) itemNavegacaoNoticias.findViewById(R.id.image_view_item_navegacao)).setImageResource(R.drawable.ic_dashboard_grey600_24dp);
        ((TextView) itemNavegacaoNoticias.findViewById(R.id.item_navegacao)).setText(R.string.titulo_fragmento_noticias);
        itemNavegacaoNoticias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle atacado = new Bundle(1);
                atacado.putString(MainActivity.NAVIGATON_KEY_INTERNO_CATEGORIA, null);
                navigationEventListener.onNavigationEvent(atacado);
            }
        });
        mCategoriasListView.addHeaderView(itemNavegacaoNoticias);

        mCategoriasListView.addFooterView(inflater.inflate(R.layout.separador_lista_linha, mCategoriasListView, false));

        View itemNavegacaoDefinicoes = inflater.inflate(R.layout.imagem_texto_list_item, mCategoriasListView, false);
        ((ImageView) itemNavegacaoDefinicoes.findViewById(R.id.image_view_item_navegacao)).setImageResource(R.drawable.ic_settings_grey600_24dp);
        ((TextView) itemNavegacaoDefinicoes.findViewById(R.id.item_navegacao)).setText(R.string.titulo_fragmento_definicoes);
        itemNavegacaoDefinicoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle atacado = new Bundle(1);
                atacado.putBoolean(MainActivity.NAVIGATON_KEY_INTERNO_DEFINICOES, true);
                navigationEventListener.onNavigationEvent(atacado);
            }
        });
        mCategoriasListView.addFooterView(itemNavegacaoDefinicoes);
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

        criarItensNavegacaoEstaticos(inflater);

        getLoaderManager().initLoader(URL_LOADER_CATEGORIAS, null, this);

        return mNavigationLinearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mSimpleCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.imagem_texto_list_item, null, CATEGORIAS_FROM, DRAWER_LIST_ITEM_TO, 0);

        descricaoCategoriasConhecidas = construirDescricaoCategoriasConhecidas(getResources()); // TODO: Colocar esta estrutura num local mais acessivel (actividade)

        mSimpleCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
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
                    } else if (view instanceof ImageView) {
                        if (view.getId() == R.id.image_view_item_navegacao) {
                            ((ImageView) view).setImageResource(R.drawable.ic_cat_generico_grey600_24dp);
                            return true;
                        }
                    }
                }

                return false;
            }
        });

        mCategoriasListView.setAdapter(mSimpleCursorAdapter);
        mCategoriasListView.setOnItemClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        if (activity instanceof NavigationEventListener) navigationEventListener = (NavigationEventListener) activity;
        else navigationEventListener = null;
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
                        NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSimpleCursorAdapter.changeCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSimpleCursorAdapter.changeCursor(null);
    }

    public static HashMap<String, DescriptorCategoriaConhecida> construirDescricaoCategoriasConhecidas
            (Resources res){
        String[] categorias = res.getStringArray(R.array.lista_categorias);
        TypedArray iconesCategorias = res.obtainTypedArray(R.array.lista_icones_categorias);
        String[] titulosCategorias = res.getStringArray(R.array.lista_titulos_categorias);
        int quantidadeCategorias = categorias.length;

        HashMap<String, DescriptorCategoriaConhecida> resultado = new HashMap<>(quantidadeCategorias);
        for (int i = 0; i < quantidadeCategorias; i++)
            resultado.put(categorias[i], new DescriptorCategoriaConhecida(categorias[i], iconesCategorias.getDrawable(i), titulosCategorias[i]));

        iconesCategorias.recycle();
        return resultado;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick -> Position: " + position + "; Id: " + id);
        if (id != -1) {
            Bundle atacado = new Bundle(1);
            atacado.putString(MainActivity.NAVIGATON_KEY_INTERNO_CATEGORIA, String.valueOf(id));
            navigationEventListener.onNavigationEvent(atacado);
        }

    }

    public static class DescriptorCategoriaConhecida {
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
