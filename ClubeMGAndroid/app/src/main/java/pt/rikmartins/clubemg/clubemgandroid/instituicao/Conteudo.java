package pt.rikmartins.clubemg.clubemgandroid.instituicao;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.rikmartins.clubemg.clubemgandroid.MainActivity;
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
        Bundle atacado = new Bundle(1);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, "http://www.montanhismo-guarda.pt/portal/");
        addItem("Página na internet", null, atacado);

        atacado = new Bundle(1);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, Uri.fromParts(
                "mailto", "clube@montanhismo-guarda.pt", null).toString());
        addItem("Contacta-nos via email", null, atacado);

        atacado = new Bundle(1);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_CHAMADA, Uri.fromParts(
                "tel", "271222840", null).toString());
        addItem("Contacta-nos via telefone", null, atacado);

        atacado = new Bundle(1);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, Uri.parse(
                "geo:0,0?q=40.541155,-7.266898").toString());
        addItem("Encontra-nos", null, atacado);

        atacado = new Bundle(2);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, "fb://page/123780544307693");
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_SECUNDARIO, "https://www.facebook.com/pages/Clube-de-Montanhismo-da-Guarda/123780544307693");
        addItem("Página no Facebook", R.drawable.ic_facebook, atacado);

        atacado = new Bundle(1);
        atacado.putString(MainActivity.NAVIGATON_KEY_EXTERNO_PRINCIPAL, "https://plus.google.com/u/1/+ClubedeMontanhismodaGuarda/posts");
        addItem("Perfil no Google+", R.drawable.ic_google_plus, atacado);
    }

    private static void addItem(String texto, @DrawableRes Integer imagem, Bundle atacado) {
        Item item = new Item(3);
        item.put(CONTEUDO_TEXTO, texto);
        item.put(CONTEUDO_IMAGEM, imagem);
        item.put(CONTEUDO_ATACADO, atacado);
        ITENS.add(item);
    }

    public static class Item extends HashMap<String, Object> {
        public Item(int capacity) {
            super(capacity);
        }
    }
}
