package pt.rikmartins.clubemgandroid;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

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
        mCategorias = new String[]{"Montanha", "BTT"};
        mEtiquetas = new String[]{};
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
