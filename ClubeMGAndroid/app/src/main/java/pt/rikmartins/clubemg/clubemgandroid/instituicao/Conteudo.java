package pt.rikmartins.clubemg.clubemgandroid.instituicao;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.rikmartins.clubemg.clubemgandroid.R;

public class Conteudo {

    public static final String CONTEUDO_TEXTO = "conteudo_id_texto";
    public static final String CONTEUDO_IMAGEM = "conteudo_id_imagem";

    public static final String[] LISTA_DE = new String[] {
            CONTEUDO_IMAGEM,
            CONTEUDO_TEXTO
    };

    public static final int[] LISTA_PARA = new int[] {
            R.id.image_view_item_navegacao,
            R.id.item_navegacao
    };

    public final static List<Item> ITENS = new ArrayList<>();
    static {
        addItem("Página no Facebook", R.drawable.ic_facebook);
        addItem("Página no Google+", R.drawable.ic_google_plus);
    }

    private static void addItem(String texto, @DrawableRes int imagem) {
        Item item = new Item(2);
        item.put(CONTEUDO_TEXTO, texto);
        item.put(CONTEUDO_IMAGEM, imagem);
        ITENS.add(item);
    }

    public static class Item extends HashMap<String, Object> {
        public Item() {
        }

        public Item(int capacity) {
            super(capacity);
        }
    }
}
