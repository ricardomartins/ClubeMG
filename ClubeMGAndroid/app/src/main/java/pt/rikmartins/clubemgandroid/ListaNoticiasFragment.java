package pt.rikmartins.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import pt.rikmartins.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 06-12-2014.
 */
public class ListaNoticiasFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ListaNoticiasFragment.class.getName();

    public static final String ARG_NOME_CATEGORIA = "categoria";

    private ListView mNoticiasListView;

    private Cursor mNoticiasCursor;

    private NoticiasCursorAdapter mNoticiasCursorAdapter;

    private static final int URL_LOADER_NOTICIAS = 0;

    private static final String[] mNoticiasCursorAdapterFrom = new String[]{
            NoticiaContract.Noticia.COLUMN_NAME_TITULO,
            NoticiaContract.Noticia.COLUMN_NAME_TEXTO,
            NoticiaContract.Noticia.COLUMN_NAME_IMAGEM
    };
    private static final int[] mNoticiasCursorAdapterTo = new int[]{
            R.id.titulo_noticia,
            R.id.texto_noticia,
            R.id.imagem_noticia
    };

    public static ListaNoticiasFragment newInstance() {
        return newInstance(null);
    }

    public static ListaNoticiasFragment newInstance(@Nullable String categoria) {
        ListaNoticiasFragment myFragment = new ListaNoticiasFragment();

        if (categoria != null) {
            Bundle args = new Bundle();
            args.putString(ARG_NOME_CATEGORIA, categoria);
            myFragment.setArguments(args);
        }

        return myFragment;
    }

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
        mNoticiasListView = (ListView) inflater.inflate(R.layout.fragment_lista_noticias,
                container, false);
        mNoticiasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_CATEGORIA, (String) ((TextView) ((LinearLayout) view).findViewById(R.id.designacao_categoria)).getText() + String.valueOf(position) + String.valueOf(id));
            }
        });

        getLoaderManager().initLoader(URL_LOADER_NOTICIAS, null, this);

        return mNoticiasListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mNoticiasCursor = new CursorLoader(getActivity(), NoticiaContract.Noticia.CONTENT_URI, NoticiaProvider.getCopyOfNoticiaDefaultProjection(), null, null, null).loadInBackground();

        mNoticiasCursorAdapter = new NoticiasCursorAdapter(getActivity(), R.layout.noticias_list_item, null, mNoticiasCursorAdapterFrom, mNoticiasCursorAdapterTo, 0);
        mNoticiasListView.setAdapter(mNoticiasCursorAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        super.onAttach(activity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER_NOTICIAS:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        NoticiaContract.Noticia.CONTENT_URI,        // Table to query
                        NoticiaProvider.getCopyOfNoticiaDefaultProjection(),     // Projection to return
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
        mNoticiasCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNoticiasCursorAdapter.changeCursor(null);
    }

    class NoticiasCursorAdapter extends SimpleCursorAdapter {

        private int mIndiceId;

        public NoticiasCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);

        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            if (cursor != null)
                mIndiceId = cursor.getColumnIndex(NoticiaContract.Noticia._ID);
        }

        @Override
        public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
            super.bindView(view, context, cursor);
            int id = cursor.getInt(mIndiceId);
            getActivity().getContentResolver().registerContentObserver(NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath("").build(), false, new NoticiaContentObserver(null));
        }
    }

    class NoticiaContentObserver extends ContentObserver {
        public NoticiaContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }
    }
}
