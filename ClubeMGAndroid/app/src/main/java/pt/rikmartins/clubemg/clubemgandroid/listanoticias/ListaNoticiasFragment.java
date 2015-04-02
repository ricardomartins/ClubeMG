package pt.rikmartins.clubemg.clubemgandroid.listanoticias;

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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import pt.rikmartins.clubemg.clubemgandroid.NavigationFragment;
import pt.rikmartins.clubemg.clubemgandroid.R;
import pt.rikmartins.clubemg.clubemgandroid.ToolbarHolder;
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
    private RecyclerView mListaNoticiasRecyclerView;

    private NoticiasSimpleCursorAdapter mNoticiasCursorAdapter;

    private static final int ID_LOADER_NOTICIAS = 0;

    private String mIdCategoria = "-1";
    private String mDesignacaoCategoria = null;

    private BroadcastReceiver broadcastReceiver;
    private ToolbarHolder mToolbarHolder;
    private HashMap<String, NavigationFragment.DescriptorCategoriaConhecida> descricaoCategoriasConhecidas;

    private RecicladorOnClickListener mRecicladorOnClickListener;

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

        mRecicladorOnClickListener = new RecicladorOnClickListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "Creating view");
        mListaNoticiasSwipeRefreshLayout = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_lista_noticias,
                container, false);
        mListaNoticiasSwipeRefreshLayout.setOnRefreshListener(this);

        mListaNoticiasRecyclerView = (RecyclerView) mListaNoticiasSwipeRefreshLayout.findViewById(R.id.lista_noticias);

        StaggeredGridLayoutManager mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);

        mListaNoticiasRecyclerView.setLayoutManager(mLayoutManager);

        substituirCategoria((getArguments() != null) ? getArguments().getString(ARG_NOME_CATEGORIA, null) : null);

        return mListaNoticiasSwipeRefreshLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);
        mNoticiasCursorAdapter = new NoticiasSimpleCursorAdapter(getActivity(), null);
        mListaNoticiasRecyclerView.setAdapter(mNoticiasCursorAdapter);

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

    class NoticiasSimpleCursorAdapter extends RecyclerView.Adapter<NoticiasSimpleCursorAdapter.ViewHolder> {
        private final LayoutInflater mInflater;

        private Cursor mCursor;

        protected int mRowIDColumn;

        protected boolean mDataValid;

        private int mIndiceId;
        private int mIndiceTitulo;
        private int mIndiceTexto;
        private int mIndiceImagem;
        private int mIndiceEnderecoImagemGrande;
        private int mIndiceDestacada;
        private int mIndiceEnderecoNoticia;

        public NoticiasSimpleCursorAdapter(Context context, Cursor cursor) {
            mCursor = cursor;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private void actualizaIndices(Cursor cursor){
            if (cursor == null) return;
            mIndiceId = cursor.getColumnIndex(NoticiaContract.Noticia._ID);
            mIndiceTitulo = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_TITULO);
            mIndiceTexto = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_TEXTO);
            mIndiceImagem = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM);
            mIndiceDestacada = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA);
            mIndiceEnderecoImagemGrande = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE);
            mIndiceEnderecoNoticia = cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA);
        }

        public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
            actualizaIndices(cursor);
        }

        public Cursor swapCursor(Cursor newCursor) {
            if (newCursor == mCursor) {
                return null;
            }
            Cursor oldCursor = mCursor;
            if (oldCursor != null) {
//                if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
//                if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
            mCursor = newCursor;
            if (newCursor != null) {
//                if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
//                if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
                mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
                mDataValid = true;
                // notify the observers about the new cursor
                notifyDataSetChanged();
            } else {
                mRowIDColumn = -1;
                mDataValid = false;
                // notify the observers about the lack of a data set
//                notifyDataSetInvalidated();
            }

            actualizaIndices(newCursor);
            return oldCursor;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Create a new view.
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(viewType != 1 ? R.layout.noticia_normal_list_item : R.layout.noticia_destacada_list_item, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.getTextViewTitulo().setText(mCursor.getString(mIndiceTitulo));
            holder.getTextViewTexto().setText(mCursor.getString(mIndiceTexto));

            byte[] bytesImagem = mCursor.getBlob(mIndiceImagem);
            if (bytesImagem == null) {
                holder.getImageViewImagem().setImageResource(R.drawable.fundo_noticia);
                obterImagem(mCursor.getString(mIndiceEnderecoImagemGrande), mCursor.getInt(mIndiceId));
            } else {
                ByteArrayInputStream streamImagem = new ByteArrayInputStream(bytesImagem);
                Bitmap aImagem = BitmapFactory.decodeStream(streamImagem);
                holder.getImageViewImagem().setImageBitmap(aImagem);
            }
            holder.itemView.setOnClickListener(mRecicladorOnClickListener.getOnClickListener(mCursor.getString(mIndiceEnderecoNoticia)));
        }

        @Override
        public int getItemViewType(int position) {
            mCursor.moveToPosition(position);
            return getItemViewType(mCursor);
        }

        private int getItemViewType(Cursor cursor) {
            return cursor.getInt(mIndiceDestacada);
        }

        @Override
        public int getItemCount() {
            return mCursor == null ? -1 : mCursor.getCount();
        }

//        protected void onContentChanged() {
//            if (mAutoRequery && mCursor != null && !mCursor.isClosed()) {
//                if (false) Log.v("Cursor", "Auto requerying " + mCursor + " due to update");
//                mDataValid = mCursor.requery();
//            }
//        }
//
//        private class ChangeObserver extends ContentObserver {
//            public ChangeObserver() {
//                super(new Handler());
//            }
//
//            @Override
//            public boolean deliverSelfNotifications() {
//                return true;
//            }
//
//            @Override
//            public void onChange(boolean selfChange) {
//                onContentChanged();
//            }
//        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textViewTitulo;
            private final TextView textViewTexto;
            private final ImageView imageViewImagem;

            public ViewHolder(View itemView) {
                super(itemView);
                textViewTitulo = (TextView) itemView.findViewById(R.id.titulo_noticia);
                textViewTexto = (TextView) itemView.findViewById(R.id.texto_noticia);
                imageViewImagem = (ImageView) itemView.findViewById(R.id.imagem_noticia);
            }

            public TextView getTextViewTitulo() {
                return textViewTitulo;
            }

            public TextView getTextViewTexto() {
                return textViewTexto;
            }

            public ImageView getImageViewImagem() {
                return imageViewImagem;
            }
        }
    }

    private class RecicladorOnClickListener {
        private final Map<String, View.OnClickListener> mAlvos;

        RecicladorOnClickListener() {
            mAlvos = new HashMap<>();
        }

        public View.OnClickListener getOnClickListener(String alvo){
            if (mAlvos.containsKey(alvo))
                return mAlvos.get(alvo);
            else {
                OnClickListenerReciclavel onClickListenerReciclavel = new OnClickListenerReciclavel(alvo);
                mAlvos.put(alvo, onClickListenerReciclavel);
                return onClickListenerReciclavel;
            }

        }

        public class OnClickListenerReciclavel implements View.OnClickListener {

            public String mAlvo;

            public OnClickListenerReciclavel(String alvo) {
                this.mAlvo = alvo;
            }
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mAlvo));
                getActivity().startActivity(i);
            }

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

        if ((idCategoriaCursor == null || !idCategoriaCursor.equals(mIdCategoria)) && ((idCategoriaCursor != null || (mIdCategoria != null))))
            mListaNoticiasRecyclerView.smoothScrollToPosition(0);

        setCategoria(idCategoriaCursor, designacaoCategoriaCursor);

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
        if ((categoria == null || !categoria.equals(mIdCategoria)) && (categoria != null || mIdCategoria != null)) {
            Bundle bundleLoader = new Bundle();
            bundleLoader.putString(ARG_NOME_CATEGORIA, categoria);
            getLoaderManager().restartLoader(ID_LOADER_NOTICIAS, bundleLoader, this);
        }
    }

    private void setCategoria(String id, String designacao) {
        this.mIdCategoria = id;
        this.mDesignacaoCategoria = designacao;
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
