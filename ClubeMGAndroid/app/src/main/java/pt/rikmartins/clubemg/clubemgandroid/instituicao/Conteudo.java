package pt.rikmartins.clubemg.clubemgandroid.instituicao;

import android.os.Bundle;
import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.rikmartins.clubemg.clubemgandroid.R;

public class Conteudo {

    public static final String CONTEUDO_TEXTO = "conteudo_id_texto";
    public static final String CONTEUDO_IMAGEM = "conteudo_id_imagem";
    public static final String CONTEUDO_ATACADO = "conteudo_id_atacado";

    public static final String[] LISTA_DE = new String[] {
            CONTEUDO_IMAGEM,
            CONTEUDO_TEXTO
    };

    public static final int[] LISTA_PARA = new int[] {
            R.id.image_view_item_navegacao,
            R.id.item_navegacao
    };

    public final static List<Item> ITENS = new ArrayList<>();
    static { // TODO: Isto tem que passar para recurso
//        addItem("Página da internet", null);
//        addItem("Contacta-nos via email", null);
//        addItem("Contacta-nos via telefone", null);
//        addItem("Encontra-nos", null);
//        addItem("Página no Facebook", R.drawable.ic_facebook);
//        addItem("Perfil no Google+", R.drawable.ic_google_plus);
    }

    private static void addItem(String texto, @DrawableRes Integer imagem, Bundle atacado) {
        Item item = new Item(2);
        item.put(CONTEUDO_TEXTO, texto);
        item.put(CONTEUDO_IMAGEM, imagem);
        item.put(CONTEUDO_ATACADO, atacado);
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
