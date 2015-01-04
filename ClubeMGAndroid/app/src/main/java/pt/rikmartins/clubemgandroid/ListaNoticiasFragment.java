package pt.rikmartins.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

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

    private NoticiasSimpleCursorAdapter mNoticiasCursorAdapter;

    private static final int ID_LOADER_NOTICIAS = 0;
    private static final int ID_LOADER_IMAGEM_NOTICIA = 1;
    private static final String LOADER_ARG_URL_IMAGEM = NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE;
    private static final String LOADER_ARG_ID_NOTICIA = NoticiaContract.Noticia._ID;

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

        getLoaderManager().initLoader(ID_LOADER_NOTICIAS, null, this);

        return mNoticiasListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mNoticiasCursor = new CursorLoader(getActivity(), NoticiaContract.Noticia.CONTENT_URI, NoticiaProvider.getCopyOfNoticiaDefaultProjection(), null, null, null).loadInBackground();

        mNoticiasCursorAdapter = new NoticiasSimpleCursorAdapter(getActivity(), R.layout.noticias_list_item, null, mNoticiasCursorAdapterFrom, mNoticiasCursorAdapterTo, 0);
        mNoticiasCursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view instanceof ImageView) {
                    byte[] bytesImagem = cursor.getBlob(columnIndex);
                    if (bytesImagem == null){
                        obterImagem(cursor.getString(cursor.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE)), cursor.getInt(cursor.getColumnIndex(NoticiaContract.Noticia._ID)), (ImageView) view);
                        return false;
                    }

                    ByteArrayInputStream streamImagem = new ByteArrayInputStream(bytesImagem);
                    Bitmap aImagem = BitmapFactory.decodeStream(streamImagem);
                    ((ImageView) view).setImageBitmap(aImagem);
                    return true;
                }
                return false;
            }
        });
        mNoticiasListView.setAdapter(mNoticiasCursorAdapter);
    }

    private void obterImagem(String urlImagem, int id, ImageView view) {
        new ObtensorImagens(getActivity(), view).execute(urlImagem, String.valueOf(id));
    }

    private static class ObtensorImagens extends AsyncTask<String, Void, byte[]> {
        private final Context mContext;
        private final ImageView mView;

        private static final String TAG = ObtensorImagens.class.getSimpleName();

        private ObtensorImagens(Context context, ImageView view) {
            super();
            mContext = context;
            mView = view;
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
        public void bindView(View view, Context context, Cursor cursor) {
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
        mNoticiasCursorAdapter.changeCursor((Cursor) data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNoticiasCursorAdapter.changeCursor(null);
    }

}
