package pt.rikmartins.clubemgandroid;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by ricardo on 06-12-2014.
 */
public class ListaNoticiasFragment
        extends Fragment {
    public static final String ARG_NOME_CATEGORIA = "categoria";

    public static ListaNoticiasFragment newInstance(String categoria) {
        ListaNoticiasFragment myFragment = new ListaNoticiasFragment();

        Bundle args = new Bundle();
        args.putString(ARG_NOME_CATEGORIA, categoria);
        myFragment.setArguments(args);

        return myFragment;
    }
}
