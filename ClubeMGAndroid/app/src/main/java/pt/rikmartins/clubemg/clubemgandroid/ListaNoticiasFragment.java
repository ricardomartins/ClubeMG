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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

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

    private BroadcastReceiver broadcastReceiver;
    private ToolbarHolder mToolbarHolder;
    private HashMap<String, NavigationFragment.DescriptorCategoriaConhecida> descricaoCategoriasConhecidas;

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

        setHasOptionsMenu(true);

        descricaoCategoriasConhecidas = NavigationFragment.construirDescricaoCategoriasConhecidas(getResources());
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

        substituirCategoria((getArguments() != null) ? getArguments().getString(ARG_NOME_CATEGORIA, null) : null);

        return mListaNoticiasSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);
        mNoticiasCursorAdapter = new NoticiasSimpleCursorAdapter(getActivity(), null);
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

        mToolbarHolder = getActivity() instanceof ToolbarHolder ? (ToolbarHolder) getActivity() : null;
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

    class NoticiasSimpleCursorAdapter extends CursorAdapter {
        private final LayoutInflater mInflater;

        private int mIndiceId;
        private int mIndiceTitulo;
        private int mIndiceTexto;
        private int mIndiceImagem;
        private int mIndiceEnderecoImagemGrande;
        private int mIndiceDestacada;

        public NoticiasSimpleCursorAdapter(Context context, Cursor c) {
            super(context, c, true);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate((cursor.getInt(mIndiceDestacada) != 1) ? R.layout.noticia_normal_list_item : R.layout.noticia_destacada_list_item, parent, false);
        }

        private void actualizaIndices(Cursor cursor){
            if (cursor == null) return;
            mIndiceId = cursor.getColumnIndex(NoticiaContract.Noticia._ID);
            mIndiceTitulo = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_TITULO);
            mIndiceTexto = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_TEXTO);
            mIndiceImagem = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM);
            mIndiceDestacada = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA);
            mIndiceEnderecoImagemGrande = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE);
        }

        @Override
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            actualizaIndices(cursor);
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            Cursor oldCursor = super.swapCursor(newCursor);
            actualizaIndices(newCursor);
            return oldCursor;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView textViewTitulo = (TextView) view.findViewById(R.id.titulo_noticia);
            TextView textViewTexto = (TextView) view.findViewById(R.id.texto_noticia);
            ImageView imageViewImagem = (ImageView) view.findViewById(R.id.imagem_noticia);

            textViewTitulo.setText(cursor.getString(mIndiceTitulo));
            textViewTexto.setText(cursor.getString(mIndiceTexto));

            byte[] bytesImagem = cursor.getBlob(mIndiceImagem);
            if (bytesImagem == null) {
                imageViewImagem.setImageResource(R.drawable.fundo_noticia);
                obterImagem(cursor.getString(mIndiceEnderecoImagemGrande), cursor.getInt(mIndiceId));
            } else {
                ByteArrayInputStream streamImagem = new ByteArrayInputStream(bytesImagem);
                Bitmap aImagem = BitmapFactory.decodeStream(streamImagem);
                imageViewImagem.setImageBitmap(aImagem);
            }
        }

        private int getItemViewType(Cursor cursor) {
            return cursor.getInt(mIndiceDestacada);
        }

        @Override
        public int getItemViewType(int position) {
            Cursor cursor = (Cursor) getItem(position);
            return getItemViewType(cursor);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        super.onAttach(activity);
    }

    private void substituirCursor(Cursor novoCursor) {
        String idCategoriaCursor = null;
        String designacaoCategoriaCursor = null;
        try {
            if (novoCursor.moveToFirst()) {
                idCategoriaCursor = novoCursor.getString(novoCursor.getColumnIndexOrThrow("cat_id")); // TODO: Livrar deste hack
                designacaoCategoriaCursor = novoCursor.getString(novoCursor.getColumnIndexOrThrow("cat_des")); // TODO: Livrar deste hack
            }
        } catch(IllegalArgumentException e) {

        }
        mNoticiasCursorAdapter.swapCursor(novoCursor);

        if (designacaoCategoriaCursor != null) {
            String titulo;
            if (descricaoCategoriasConhecidas.containsKey(designacaoCategoriaCursor))
                titulo = descricaoCategoriasConhecidas.get(designacaoCategoriaCursor).tituloCategoria;
            else
                titulo = designacaoCategoriaCursor;
            if (mToolbarHolder != null)
                mToolbarHolder.getToolbar().setTitle(titulo);
        } else if (mToolbarHolder != null)
            mToolbarHolder.getToolbar().setTitle(R.string.titulo_fragmento_noticias);

    }

    public void substituirCategoria(String categoria){
        Bundle bundleLoader = new Bundle();
        bundleLoader.putString(ARG_NOME_CATEGORIA, categoria);
        getLoaderManager().restartLoader(ID_LOADER_NOTICIAS, bundleLoader, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String categoria = (args != null) ? args.getString(ARG_NOME_CATEGORIA) : null;

        switch (id) {
            case ID_LOADER_NOTICIAS:
                Uri uriNoticias;
                if (categoria == null) uriNoticias = NoticiaContract.Noticia.CONTENT_URI;
                else uriNoticias = NoticiaContract.Noticia.CONTENT_URI_CATEGORIA.buildUpon().appendPath(categoria).build();

                return new CursorLoader(getActivity(), uriNoticias,
                        NoticiaProvider.getCopyOfNoticiaDefaultProjection(), null, null, NoticiaContract.Noticia.COLUMN_NAME_DESTACADA + " DESC, " + NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA + " DESC");
            default:
                // id inválido
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        substituirCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNoticiasCursorAdapter.swapCursor(null);
    }
}
