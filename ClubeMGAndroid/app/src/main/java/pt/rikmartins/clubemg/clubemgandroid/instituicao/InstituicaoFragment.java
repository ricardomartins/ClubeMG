package pt.rikmartins.clubemg.clubemgandroid.instituicao;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import pt.rikmartins.clubemg.clubemgandroid.NavigationEventListener;
import pt.rikmartins.clubemg.clubemgandroid.R;
import pt.rikmartins.clubemg.clubemgandroid.ToolbarHolder;

public class InstituicaoFragment extends ListFragment implements AbsListView.OnItemClickListener {
    private static final String TAG = InstituicaoFragment.class.getSimpleName();

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mListAdapter;

    private NavigationEventListener navigationEventListener;
    private ToolbarHolder mToolbarHolder;

    public InstituicaoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new SimpleAdapter(getActivity(), Conteudo.ITENS, R.layout.imagem_texto_list_item, Conteudo.LISTA_DE, Conteudo.LISTA_PARA);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(TAG, "Fragment attached to Activity");
        if (activity instanceof NavigationEventListener) navigationEventListener = (NavigationEventListener) activity;
        else navigationEventListener = null;
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setListAdapter(mListAdapter);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mToolbarHolder = getActivity() instanceof ToolbarHolder ? (ToolbarHolder) getActivity() : null;

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        if (mToolbarHolder != null)
            mToolbarHolder.getToolbar().setTitle(R.string.titulo_fragmento_instituicao);

        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        navigationEventListener.onNavigationEvent((Bundle) Conteudo.ITENS.get(position).get(Conteudo.CONTEUDO_ATACADO));
    }
}
