package pt.rikmartins.clubemgandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.CursorLoader;
import android.database.Cursor;
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

import pt.rikmartins.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemgandroid.provider.NoticiaProvider;

/**
 * Created by ricardo on 06-12-2014.
 */
public class ListaNoticiasFragment
        extends ListFragment {
    private static final String TAG = ListaNoticiasFragment.class.getName();

    public static final String ARG_NOME_CATEGORIA = "categoria";

    private ListView mNoticiasListView;

    private Cursor mCursorNoticias;

    private static final String[] coiso_from = new String[]{
            NoticiaContract.Noticia.COLUMN_NAME_TITULO
    };
    private static final int[] coiso_to = new int[]{
            R.id.titulo_noticia
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
        return mNoticiasListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "Activity created");
        super.onActivityCreated(savedInstanceState);

        mCursorNoticias = new CursorLoader(getActivity(), NoticiaContract.Noticia.CONTENT_URI, NoticiaProvider.getCopyOfNoticiaDefaultProjection(), null, null, null).loadInBackground();

        mNoticiasListView.setAdapter(new SimpleCursorAdapter(getActivity(), R.layout.noticias_list_item, mCursorNoticias, coiso_from, coiso_to, 0));
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        super.onAttach(activity);
    }
}
