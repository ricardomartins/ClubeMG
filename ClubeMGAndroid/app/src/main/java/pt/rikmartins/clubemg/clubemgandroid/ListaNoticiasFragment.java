package pt.rikmartins.clubemg.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Random;

import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 06-12-2014.
 */
public class ListaNoticiasFragment
        extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ListaNoticiasFragment.class.getName();

    public static final String ARG_NOME_CATEGORIA = "categoria";

    private ListView mNoticiasListView;

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

    public static ListaNoticiasFragment newInstance() {
        return newInstance(null);
    }

    private static int[] coresNoticias = null;
    private static int tamanhoCoresNoticias = 0;

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
        if (coresNoticias == null) {
            coresNoticias = getActivity().getResources().getIntArray(R.array.cores_noticias);
            tamanhoCoresNoticias = coresNoticias.length;
            Random random = new Random(tamanhoCoresNoticias * 2 + 1);
            for (int i = 0; i < tamanhoCoresNoticias; i++) {
                int n = random.nextInt(tamanhoCoresNoticias);
                int tmp = coresNoticias[i];
                coresNoticias[i] = coresNoticias[n];
                coresNoticias[n] = tmp;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "Creating view");
        mNoticiasListView = (ListView) inflater.inflate(R.layout.fragment_lista_noticias,
                container, false);
        getLoaderManager().initLoader(ID_LOADER_NOTICIAS, null, this);

        return mNoticiasListView;
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
                    if (bytesImagem == null){
                        obterImagem(cursor.getString(cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE)), cursor.getInt(cursor.getColumnIndex(NoticiaContract.Noticia._ID)));
                    } else {
                        ByteArrayInputStream streamImagem = new ByteArrayInputStream(bytesImagem);
                        Bitmap aImagem = BitmapFactory.decodeStream(streamImagem);
                        ((ImageView) view).setImageBitmap(aImagem);
                        ((ImageView) view).setColorFilter(coresNoticias[cursor.getInt(cursor.getColumnIndex(NoticiaContract.Noticia._ID)) % tamanhoCoresNoticias]);
                    }
                    return true;
                }
                return false;
            }
        });
        mNoticiasListView.setAdapter(mNoticiasCursorAdapter);
        mNoticiasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursorNoticias = mNoticiasCursorAdapter.getCursor();
                String enderecoNoticia = cursorNoticias.getString(cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA));

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(enderecoNoticia));
                getActivity().startActivity(i);
            }
        });
    }

    private void obterImagem(String urlImagem, int id) {
        new ObtensorImagens(getActivity()).execute(urlImagem, String.valueOf(id));
    }

    private static class ObtensorImagens extends AsyncTask<String, Void, byte[]> {
        private final Context mContext;

        private static final String TAG = ObtensorImagens.class.getSimpleName();

        private ObtensorImagens(Context context) {
            super();
            mContext = context;
        }

        private byte[] downloadImageAsByteArray(URL urlImagem){

            Log.v(TAG, "downloadImageAsByteArray: " + urlImagem);
            URLConnection con;
            try {
                con = urlImagem.openConnection();
                BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current;
                while ((current = bis.read()) != -1) baf.append((byte) current);

                return baf.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int saveImageToDatabase(byte[] ba, Uri uriDaNoticia) {
            Log.v(TAG, "saveImageToDatabase: " + uriDaNoticia);
            ContentValues valores = new ContentValues(1);
            valores.put(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM, ba);
            return mContext.getContentResolver().update(uriDaNoticia, valores, null, null);
        }

        @Override
        protected byte[] doInBackground(String... params) {
            Log.v(TAG, "doInBackground: " + Arrays.toString(params));
            try {
                byte[] ba = downloadImageAsByteArray(new URL(params[0]));
                int resUpdate = saveImageToDatabase(ba, NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(params[1]).build());
                return ba;
            } catch (MalformedURLException e) {
                return null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

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
