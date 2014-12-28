package pt.rikmartins.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.URI;

import pt.rikmartins.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 08-12-2014.
 */
public class NavigationFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = NavigationFragment.class.getName();

    public static final String TIPO_ON_CLICK_CATEGORIA = "categoria";

    private LinearLayout mNavigationLinearLayout;
    private ListView     mCategoriasListView;

    private String[]     mCategorias;
    private String[]     mEtiquetas;
    private CharSequence mNavigationTitle;

    private Cursor mCursorCategorias;
    private Cursor mCursorEtiquetas;

    private SimpleCursorAdapter mCategoriasCursorAdapter;

    private static final int URL_LOADER_CATEGORIAS = 10;
    private static final int URL_LOADER_ETIQUETAS = 20;

    private static final String[] coiso_from = new String[]{
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO
    };
    private static final int[] coiso_to = new int[]{
            R.id.designacao_categoria
    };

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
        mCategoriasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_CATEGORIA, (String)((TextView)((LinearLayout)view).findViewById(R.id.designacao_categoria)).getText() + String.valueOf(position) + String.valueOf(id));
            }
        });

        getLoaderManager().initLoader(URL_LOADER_CATEGORIAS, null, this);

        return mNavigationLinearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mCursorCategorias = new CursorLoader(getActivity(), NoticiaContract.Categoria.CONTENT_URI, NoticiaProvider.getCopyOfCategoriaDefaultProjection(), null, null, NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO).loadInBackground();
        mCursorEtiquetas = new CursorLoader(getActivity(), NoticiaContract.Etiqueta.CONTENT_URI, NoticiaProvider.getCopyOfEtiquetaDefaultProjection(), null, null, NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO). loadInBackground();

        mCategoriasCursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.drawer_list_item, null, coiso_from, coiso_to, 0);
        mCategoriasListView.setAdapter(mCategoriasCursorAdapter);
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
            case URL_LOADER_ETIQUETAS:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        NoticiaContract.Etiqueta.CONTENT_URI,        // Table to query
                        NoticiaProvider.getCopyOfEtiquetaDefaultProjection(),     // Projection to return
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
}
