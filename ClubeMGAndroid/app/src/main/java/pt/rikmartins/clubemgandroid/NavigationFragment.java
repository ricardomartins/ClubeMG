package pt.rikmartins.clubemgandroid;

import android.app.Fragment;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import pt.rikmartins.clubemgandroid.modelos.Categoria;
import pt.rikmartins.clubemgandroid.modelos.Etiqueta;

/**
 * Created by ricardo on 08-12-2014.
 */
public class NavigationFragment
        extends Fragment {
    public static final String TIPO_ON_CLICK_CATEGORIA = "categoria";

    private LinearLayout mNavigationLinearLayout;
    private ListView     mCategoriasListView;

    private String[]     mCategorias;
    private String[]     mEtiquetas;
    private CharSequence mNavigationTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Categoria> categorias = null;
        try {
            categorias = Categoria.listAll(Categoria.class);
        } catch(SQLiteException e){
            // TODO: Lidar com a inexistência de Categorias
        }
        if (categorias != null) {
            mCategorias = new String[categorias.size()];

            int i = 0;
            for (Categoria categoria : categorias){
                mCategorias[i] = categoria.designacao;
                i++;
            }
        } else mCategorias = new String[]{"Montanha", "BTT"};

        List<Etiqueta> etiquetas = null;
        try {
            etiquetas = Categoria.listAll(Etiqueta.class);
        } catch(SQLiteException e){
            // TODO: Lidar com a inexistência de Etiquetas
        }
        if (etiquetas != null) {
            mEtiquetas = new String[etiquetas.size()];

            int i = 0;
            for (Etiqueta etiqueta : etiquetas){
                mEtiquetas[i] = etiqueta.designacao;
                i++;
            }
        } else mCategorias = new String[]{"Montanha", "BTT"};
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNavigationLinearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_navigation,
                container, false);
        mCategoriasListView = (ListView) mNavigationLinearLayout.findViewById(
                R.id.left_navigation_drawer_categorias_list);
        mCategoriasListView.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.drawer_list_item, R.id.texto_categoria,
                mCategorias));

        mCategoriasListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity)getActivity()).onNavigationEvent(NavigationFragment.TIPO_ON_CLICK_CATEGORIA, mCategorias[position]);
            }
        });
        return mNavigationLinearLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
