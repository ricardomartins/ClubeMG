package pt.rikmartins.clubemg.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.ByteArrayInputStream;

import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaProvider;
import pt.rikmartins.clubemg.clubemgandroid.sync.SyncAdapter;
import pt.rikmartins.clubemg.clubemgandroid.sync.SyncUtils;

/**
 * Created by ricardo on 06-12-2014.
 */
public class ListaNoticiasFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ListaNoticiasFragment.class.getSimpleName();

    public static final String ARG_NOME_CATEGORIA = "categoria";

    private SwipeRefreshLayout mListaNoticiasSwipeRefreshLayout;
    private ListView mListaNoticiasListView;

    private NoticiasSimpleCursorAdapter mNoticiasCursorAdapter;

    private static final int ID_LOADER_NOTICIAS = 0;

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

    private String mCategoria = null;
    private BroadcastReceiver broadcastReceiver;

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
        if (getArguments() != null) mCategoria = getArguments().getString(ARG_NOME_CATEGORIA, null);
        else mCategoria = null;

        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "Creating view");
        mListaNoticiasSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_lista_noticias,
                container, false);
        mListaNoticiasSwipeRefreshLayout.setOnRefreshListener(this);

        mListaNoticiasListView = (ListView) mListaNoticiasSwipeRefreshLayout.findViewById(R.id.lista_noticias);
        mListaNoticiasListView.setEmptyView(mListaNoticiasSwipeRefreshLayout.findViewById(R.id.sem_noticias));

        getLoaderManager().initLoader(ID_LOADER_NOTICIAS, null, this);

        return mListaNoticiasSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);
        mNoticiasCursorAdapter = new NoticiasSimpleCursorAdapter(getActivity(), R.layout.noticias_list_item, null, mNoticiasCursorAdapterFrom, mNoticiasCursorAdapterTo, 0);
        mNoticiasCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view instanceof ImageView) {
                    byte[] bytesImagem = cursor.getBlob(columnIndex);
                    if (bytesImagem == null) {
                        obterImagem(cursor.getString(cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE)), cursor.getInt(cursor.getColumnIndex(NoticiaContract.Noticia._ID)));
                    } else {
                        ByteArrayInputStream streamImagem = new ByteArrayInputStream(bytesImagem);
                        Bitmap aImagem = BitmapFactory.decodeStream(streamImagem);
                        ((ImageView) view).setImageBitmap(aImagem);
                    }
                    return true;
                }
                return false;
            }
        });
        mListaNoticiasListView.setAdapter(mNoticiasCursorAdapter);
        mListaNoticiasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursorNoticias = mNoticiasCursorAdapter.getCursor();
                String enderecoNoticia = cursorNoticias.getString(cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA));

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(enderecoNoticia));
                getActivity().startActivity(i);
            }
        });
    }

    @Override
    public void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncAdapter.ACTION_INICIA_ACTUALIZACAO);
        intentFilter.addAction(SyncAdapter.ACTION_FINALIZA_ACTUALIZACAO);

        getActivity().registerReceiver(broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case SyncAdapter.ACTION_INICIA_ACTUALIZACAO:
                    case SyncAdapter.ACTION_A_ACTUALIZAR:
                        iniciarAnimacaoActualizacao();
                        break;
                    case SyncAdapter.ACTION_FINALIZA_ACTUALIZACAO:
                        finalizarAnimacaoActualizacao();
                        break;
                }
            }
        }, intentFilter);
        super.onResume();
    }

    @Override
    public void onPause() {
        finalizarAnimacaoActualizacao();
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private void obterImagem(String urlImagem, int id) {
        ObtensorImagens.obterImagem(getActivity(), urlImagem, String.valueOf(id));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actions_fragment_lista_noticias, menu);
    }

    private void iniciarAnimacaoActualizacao(){
        if (!mListaNoticiasSwipeRefreshLayout.isRefreshing()) mListaNoticiasSwipeRefreshLayout.setRefreshing(true);
    }

    private void finalizarAnimacaoActualizacao(){
        if (mListaNoticiasSwipeRefreshLayout.isRefreshing()) mListaNoticiasSwipeRefreshLayout.setRefreshing(false);
    }

    private void iniciarActualizacao(){
        SyncUtils.TriggerRefresh();
        iniciarAnimacaoActualizacao();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_actualizar:
                iniciarActualizacao();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        iniciarActualizacao();
    }

    class NoticiasSimpleCursorAdapter extends SimpleCursorAdapter {

        public NoticiasSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(@NonNull View view, Context context, @NonNull Cursor cursor) {
            super.bindView(view, context, cursor);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        super.onAttach(activity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_NOTICIAS:
                Uri uriNoticias;
                if (mCategoria == null) uriNoticias = NoticiaContract.Noticia.CONTENT_URI;
                else
                    uriNoticias = NoticiaContract.Noticia.CONTENT_URI_CATEGORIA.buildUpon().appendPath(mCategoria).build();

                return new CursorLoader(getActivity(), uriNoticias,
                        NoticiaProvider.getCopyOfNoticiaDefaultProjection(), null, null, NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA + " DESC");
            default:
                // id inválido
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNoticiasCursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNoticiasCursorAdapter.changeCursor(null);
    }
}
