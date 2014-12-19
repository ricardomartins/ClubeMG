package pt.rikmartins.clubemgandroid.provider;

import android.content.ContentResolver;
import android.net.Uri;

/**
 * Created by ricardo on 14-12-2014.
 */
public class NoticiaContract {
    public static final String CONTENT_AUTHORITY = "pt.rikmartins.clubemg.clubemgandroid";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_NOTICIA = Noticia.TABLE_NAME_SINGULAR; // TODO: Mudar isto de sítio
    private static final String PATH_CATEGORIA = Categoria.TABLE_NAME_SINGULAR; // TODO: Mudar isto de sítio
    private static final String PATH_ETIQUETA = Etiqueta.TABLE_NAME_SINGULAR; // TODO: Mudar isto de sítio

    private NoticiaContract() {
    }

    public static class Noticia
            extends NoticiaProvider.NoticiaDatabase.Noticia {
        /**
         * URI completo para recursos do tipo "noticia".
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTICIA)
                .build();
        /**
         * Etiquetas da notícia
         */
        public static final String COLUMN_NAME_ETIQUETAS = "etiquetas";
        /**
         * MIME para listas de "noticias".
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Noticia.TABLE_NAME_PLURAL;
        /**
         * MIME para "noticia" individual.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Noticia.TABLE_NAME_SINGULAR;

    }

    public static class Categoria
            extends NoticiaProvider.NoticiaDatabase.Categoria {
        /**
         * URI completo para recursos do tipo "categoria".
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIA).build();
        /**
         * MIME para listas de "categorias".
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Categoria.TABLE_NAME_PLURAL;
        /**
         * MIME para "categoria" individual.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Categoria.TABLE_NAME_SINGULAR;
    }

    public static class Etiqueta
            extends NoticiaProvider.NoticiaDatabase.Etiqueta {
        /**
         * URI completo para recursos do tipo "etiqueta".
         */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ETIQUETA).build();
        /**
         * MIME para listas de "etiquetas".
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Etiqueta.TABLE_NAME_PLURAL;
        /**
         * MIME para "etiqueta" individual.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." +
                NoticiaContract.CONTENT_AUTHORITY + "." + Etiqueta.TABLE_NAME_SINGULAR;
    }
}
