package pt.rikmartins.clubemg.clubemgandroid.instituicao;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import pt.rikmartins.clubemg.clubemgandroid.R;

public class InstituicaoFragment extends ListFragment implements AbsListView.OnItemClickListener {

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mListAdapter;

    public InstituicaoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListAdapter = new SimpleAdapter(getActivity(), Conteudo.ITENS, R.layout.imagem_texto_list_item, Conteudo.LISTA_DE, Conteudo.LISTA_PARA);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setListAdapter(mListAdapter);

        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
