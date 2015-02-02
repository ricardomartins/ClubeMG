package pt.rikmartins.clubemg.clubemgandroid.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.android.common.db.SelectionBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class NoticiaProvider
        extends ContentProvider {
    private static final String TAG = NoticiaProvider.class.getSimpleName();

    private static final int QUALIDADE_JPG_NA_BD = 35;

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /noticia/{ID}
     */
    public static final int ROUTE_NOTICIA_ID = 11;
    /**
     * URI ID for route: /noticia
     */
    public static final int ROUTE_NOTICIA = 10;
    /**
     * URI ID for route: /noticia/categoria/{ID}
     */
    public static final int ROUTE_NOTICIA_CATEGORIA_ID = 15;
    /**
     * URI ID for route: /categoria
     */
    public static final int ROUTE_CATEGORIA = 20;
    /**
     * URI ID for route: /categoria/{ID}
     */
    public static final int ROUTE_CATEGORIA_ID = 21;
    /**
     * URI ID for route: /etiqueta
     */
    public static final int ROUTE_ETIQUETA = 30;
    /**
     * URI ID for route: /etiqueta/{ID}
     */
    public static final int ROUTE_ETIQUETA_ID = 31;

    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = NoticiaContract.CONTENT_AUTHORITY;
    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "noticia", ROUTE_NOTICIA);
        sUriMatcher.addURI(AUTHORITY, "noticia/#", ROUTE_NOTICIA_ID);
        sUriMatcher.addURI(AUTHORITY, "noticia/categoria/*", ROUTE_NOTICIA_CATEGORIA_ID);

        sUriMatcher.addURI(AUTHORITY, "categoria", ROUTE_CATEGORIA);
        sUriMatcher.addURI(AUTHORITY, "categoria/#", ROUTE_CATEGORIA_ID);

        sUriMatcher.addURI(AUTHORITY, "etiqueta", ROUTE_ETIQUETA);
        sUriMatcher.addURI(AUTHORITY, "etiqueta/#", ROUTE_ETIQUETA_ID);
    }

    private static final String[] NOTICIA_DEFAULT_PROJECTION = new String[]{
            NoticiaContract.Noticia._ID,
            NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA,
            NoticiaContract.Noticia.COLUMN_NAME_TITULO,
            NoticiaContract.Noticia.COLUMN_NAME_SUBTITULO,
            NoticiaContract.Noticia.COLUMN_NAME_TEXTO,
            NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA,
            NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM,
            NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE,
            NoticiaContract.Noticia.COLUMN_NAME_IMAGEM,
            NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS,
            NoticiaContract.Noticia.COLUMN_NAME_DESTACADA,
            NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS
    };
    private static final String NOTICIA_TABLE_JOIN = NoticiaDatabase.Noticia.TABLE_NAME + " LEFT JOIN " +
            NoticiaDatabase.EtiquetaDaNoticia.TABLE_NAME + " ON " + NoticiaDatabase.Noticia.TABLE_NAME + "." +
            NoticiaDatabase.Noticia._ID + "=" + NoticiaDatabase.EtiquetaDaNoticia.TABLE_NAME + "." +
            NoticiaDatabase.EtiquetaDaNoticia.COLUMN_NAME_NOTICIA + " LEFT JOIN " + NoticiaDatabase.Etiqueta.TABLE_NAME + " ON " + NoticiaDatabase.EtiquetaDaNoticia.TABLE_NAME + "." +
            NoticiaDatabase.EtiquetaDaNoticia.COLUMN_NAME_ETIQUETA + "=" + NoticiaDatabase.Etiqueta.TABLE_NAME + "." + NoticiaDatabase.Etiqueta._ID + " LEFT JOIN " + NoticiaDatabase.CategoriaDaNoticia.TABLE_NAME + " ON " + NoticiaDatabase.Noticia.TABLE_NAME + "." +
            NoticiaDatabase.Noticia._ID + "=" + NoticiaDatabase.CategoriaDaNoticia.TABLE_NAME + "." +
            NoticiaDatabase.CategoriaDaNoticia.COLUMN_NAME_NOTICIA + " LEFT JOIN " + NoticiaDatabase.Categoria.TABLE_NAME + " ON " +
            NoticiaDatabase.CategoriaDaNoticia.TABLE_NAME + "." + NoticiaDatabase.CategoriaDaNoticia.COLUMN_NAME_CATEGORIA + "=" +
            NoticiaDatabase.Categoria.TABLE_NAME + "." + NoticiaDatabase.Categoria._ID;
    private static final String NOTICIA_COLUMN_ID = NoticiaDatabase.Noticia.TABLE_NAME + "." + NoticiaDatabase.Noticia._ID;
    private static final String NOTICIA_COLUMN_CATEGORIAS = "GROUP_CONCAT(" +
            NoticiaDatabase.Categoria.TABLE_NAME + "." + NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO + ")";
    private static final String NOTICIA_COLUMN_ETIQUETAS = "GROUP_CONCAT(" +
            NoticiaDatabase.Etiqueta.TABLE_NAME + "." + NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO + ")";
    private static final String NOTICIA_GROUP_BY = NoticiaDatabase.Noticia.TABLE_NAME + "." + NoticiaDatabase.Noticia._ID;

    private static final String[] CATEGORIA_DEFAULT_PROJECTION = new String[]{
            NoticiaContract.Categoria._ID,
            NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO
    };

    private static final String[] ETIQUETA_DEFAULT_PROJECTION = new String[]{
            NoticiaContract.Etiqueta._ID,
            NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO
    };
    NoticiaDatabase mDatabaseHelper;

    public static String[] getCopyOfNoticiaDefaultProjection() {
        return Arrays.copyOf(NOTICIA_DEFAULT_PROJECTION, NOTICIA_DEFAULT_PROJECTION.length);
    }

    public static String[] getCopyOfCategoriaDefaultProjection() {
        return Arrays.copyOf(CATEGORIA_DEFAULT_PROJECTION, CATEGORIA_DEFAULT_PROJECTION.length);
    }

    public static String[] getCopyOfEtiquetaDefaultProjection() {
        return Arrays.copyOf(ETIQUETA_DEFAULT_PROJECTION, ETIQUETA_DEFAULT_PROJECTION.length);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new NoticiaDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_NOTICIA:
            case ROUTE_NOTICIA_CATEGORIA_ID:
                return NoticiaContract.Noticia.CONTENT_TYPE;
            case ROUTE_NOTICIA_ID:
                return NoticiaContract.Noticia.CONTENT_ITEM_TYPE;
            case ROUTE_CATEGORIA:
                return NoticiaContract.Categoria.CONTENT_TYPE;
            case ROUTE_CATEGORIA_ID:
                return NoticiaContract.Categoria.CONTENT_ITEM_TYPE;
            case ROUTE_ETIQUETA:
                return NoticiaContract.Etiqueta.CONTENT_TYPE;
            case ROUTE_ETIQUETA_ID:
                return NoticiaContract.Etiqueta.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     * <p/>
     * <p>Currently supports returning all entries (/entries) and individual entries by ID
     * (/entries/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.v(TAG, "Query: " + uri.toString());
        Cursor c;
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case ROUTE_NOTICIA_ID:
            case ROUTE_NOTICIA:
            case ROUTE_NOTICIA_CATEGORIA_ID:
                c = queryNoticiaQuery(db, uri, uriMatch, projection, selection, selectionArgs, sortOrder);
                break;
            case ROUTE_CATEGORIA_ID:
            case ROUTE_CATEGORIA:
                c = queryCategoriaQuery(db, uri, uriMatch, projection, selection, selectionArgs, sortOrder);
                break;
            case ROUTE_ETIQUETA_ID:
            case ROUTE_ETIQUETA:
                c = queryEtiquetaQuery(db, uri, uriMatch, projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        Context ctx = getContext();
        assert ctx != null;
        c.setNotificationUri(ctx.getContentResolver(), uri);
        return c;
    }

    public Cursor queryNoticiaQuery(SQLiteDatabase db, Uri uri, int uriMatch, String[] projection, String selection,
                                    String[] selectionArgs, String sortOrder) {
        if (projection == null) projection = NoticiaProvider.getCopyOfNoticiaDefaultProjection();

        // SELECT Noticia.Titulo, Categoria.designacao AS "Categoria",
        // group_concat(Etiqueta.designacao) AS Etiquetas FROM Noticia LEFT JOIN Etiqueta_da_Noticia ON
        // Noticia._ID=Etiqueta_da_Noticia.Noticia LEFT JOIN Etiqueta ON Etiqueta_da_Noticia.Etiqueta=Etiqueta._ID
        // LEFT JOIN Categoria ON Noticia.categoria=Categoria._ID GROUP BY Noticia.Titulo;

        SelectionBuilder builder = new SelectionBuilder().table(NOTICIA_TABLE_JOIN);

        // Desambiguar a coluna _ID
        builder.map(NoticiaContract.Noticia._ID, NOTICIA_COLUMN_ID);
        // Obter as etiquetas duma notícia como uma lista separada por vírgulas
        builder.map(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS, NOTICIA_COLUMN_ETIQUETAS);
        // Obter a categoria da tabela categorias
        builder.map(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS, NOTICIA_COLUMN_CATEGORIAS);

        switch (uriMatch) {
            case ROUTE_NOTICIA_ID:
                // Devolver uma notícia, pelo ID.
                builder.where(NoticiaDatabase.Noticia.TABLE_NAME + "." + NoticiaDatabase.Noticia._ID + "=?", uri.getLastPathSegment());
                break;
            case ROUTE_NOTICIA:
                // Devolver todas as notícias.
                // Nada a fazer
                break;
            case ROUTE_NOTICIA_CATEGORIA_ID:
                // Devolver todas as notícia duma categoria.
                builder.where(NoticiaDatabase.Categoria.TABLE_NAME + "." + NoticiaDatabase.Categoria._ID+ "=?", uri.getLastPathSegment());
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        builder.where(selection, selectionArgs);

        Log.i(TAG, "builder: " + builder);
        return builder.query(db, projection, NOTICIA_GROUP_BY, null, sortOrder, null);
    }

    public Cursor queryCategoriaQuery(SQLiteDatabase db, Uri uri, int uriMatch, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {
        if (projection == null) projection = NoticiaProvider.getCopyOfCategoriaDefaultProjection();

        SelectionBuilder builder = new SelectionBuilder().table(NoticiaContract.Categoria.TABLE_NAME);

        switch (uriMatch) {
            case ROUTE_CATEGORIA_ID:
                // Devolver uma categoria, pelo ID.
                builder.where(NoticiaContract.Categoria._ID + "=?", uri.getLastPathSegment());
                break;
            case ROUTE_CATEGORIA:
                // Devolver todas as categorias.
                // Nada a fazer
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        builder.where(selection, selectionArgs);

        return builder.query(db, projection, sortOrder);
    }

    public Cursor queryEtiquetaQuery(SQLiteDatabase db, Uri uri, int uriMatch, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
        if (projection == null) projection = NoticiaProvider.getCopyOfEtiquetaDefaultProjection();

        SelectionBuilder builder = new SelectionBuilder().table(NoticiaContract.Etiqueta.TABLE_NAME);

        switch (uriMatch) {
            case ROUTE_ETIQUETA_ID:
                // Devolver uma etiqueta, pelo ID.
                builder.where(NoticiaContract.Etiqueta._ID + "=?", uri.getLastPathSegment());
                break;
            case ROUTE_ETIQUETA:
                // Devolver todas as etiquetas.
                // Nada a fazer
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        builder.where(selection, selectionArgs);

        return builder.query(db, projection, sortOrder);
    }

    private long inserirCategoria(SQLiteDatabase db, String categoriaStr) {
        ContentValues values = new ContentValues(1);
        values.put(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO, categoriaStr);
        return inserirCategoria(db, values);
    }

    private long inserirCategoria(SQLiteDatabase db, ContentValues values) {
        return inserirMonoValor(db, values, NoticiaDatabase.Categoria.TABLE_NAME,
                NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO, NoticiaDatabase.Categoria._ID);
    }

    private long inserirNoticia(SQLiteDatabase db, ContentValues values) {
        ContentValues novaNoticia = new ContentValues(values);
        novaNoticia.remove(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS);
        novaNoticia.remove(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS);

        return db.insertOrThrow(NoticiaDatabase.Noticia.TABLE_NAME, null, novaNoticia);
    }

    private ArrayList<Long> inserirEtiquetas(SQLiteDatabase db, String etiquetasNoticia) {
        String[] etiquetasNoticiaSeparadas = etiquetasNoticia.split(",");

        StringBuilder etiquetasWhereStringBuilder = new StringBuilder(NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO + " IN (");
        for (int i = 0; i < etiquetasNoticiaSeparadas.length; i++) {
            etiquetasWhereStringBuilder.append("?");
            if (i < etiquetasNoticiaSeparadas.length - 1) etiquetasWhereStringBuilder.append(",");
        }
        etiquetasWhereStringBuilder.append(")");

        SelectionBuilder builder = new SelectionBuilder().table(NoticiaDatabase.Etiqueta.TABLE_NAME).where(
                etiquetasWhereStringBuilder.toString(), etiquetasNoticiaSeparadas);

        Cursor etiquetasDaBD = builder.query(db, new String[]{NoticiaDatabase.Etiqueta._ID, NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO},
                null);
        ArrayList<Long> idsEtiquetas = new ArrayList<>();
        if (etiquetasDaBD.getCount() < etiquetasNoticiaSeparadas.length) {
            // Algumas etiquetas não constam da base de dados
            int indiceDesignacao = etiquetasDaBD.getColumnIndex(NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO);
            int indiceId = etiquetasDaBD.getColumnIndex(NoticiaDatabase.Etiqueta._ID);

            ArrayList<String> etiquetasAInserir = new ArrayList<>(Arrays.asList(etiquetasNoticiaSeparadas));
            etiquetasDaBD.moveToNext();
            while (!etiquetasDaBD.isAfterLast()) {
                String etiquetaJaNaBD = etiquetasDaBD.getString(indiceDesignacao);

                idsEtiquetas.add(etiquetasDaBD.getLong(indiceId));

                boolean encontrado = false;
                for (String etiquetaDaNoticia : etiquetasNoticiaSeparadas)
                    if (etiquetaJaNaBD.equals(etiquetaDaNoticia)) {
                        encontrado = true;
                        break;
                    }
                if (encontrado) etiquetasAInserir.remove(etiquetaJaNaBD);
                etiquetasDaBD.moveToNext();
            }

            for (String etiquetaNovaStr : etiquetasAInserir) {
                ContentValues novaEtiqueta = new ContentValues(1);
                novaEtiqueta.put(NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO, etiquetaNovaStr);
                idsEtiquetas.add(db.insertOrThrow(NoticiaDatabase.Etiqueta.TABLE_NAME, null, novaEtiqueta));
            }
        } else {
            int indiceId = etiquetasDaBD.getColumnIndex(NoticiaDatabase.Etiqueta._ID);
            etiquetasDaBD.moveToNext();
            while (!etiquetasDaBD.isAfterLast()) {
                idsEtiquetas.add(etiquetasDaBD.getLong(indiceId));
                etiquetasDaBD.moveToNext();
            }
        }
        return idsEtiquetas;
    }

    private ArrayList<Long> inserirCategorias(SQLiteDatabase db, String categoriasNoticia) {
        String[] categoriasNoticiaSeparadas = categoriasNoticia.split(",");

        StringBuilder categoriasWhereStringBuilder = new StringBuilder(NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO + " IN (");
        for (int i = 0; i < categoriasNoticiaSeparadas.length; i++) {
            categoriasWhereStringBuilder.append("?");
            if (i < categoriasNoticiaSeparadas.length - 1) categoriasWhereStringBuilder.append(",");
        }
        categoriasWhereStringBuilder.append(")");

        SelectionBuilder builder = new SelectionBuilder().table(NoticiaDatabase.Categoria.TABLE_NAME).where(
                categoriasWhereStringBuilder.toString(), categoriasNoticiaSeparadas);

        Cursor categoriasDaBD = builder.query(db, new String[]{NoticiaDatabase.Categoria._ID, NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO},
                null);
        ArrayList<Long> idsCategorias = new ArrayList<>();
        if (categoriasDaBD.getCount() < categoriasNoticiaSeparadas.length) {
            // Algumas categorias não constam da base de dados
            int indiceDesignacao = categoriasDaBD.getColumnIndex(NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO);
            int indiceId = categoriasDaBD.getColumnIndex(NoticiaDatabase.Categoria._ID);

            ArrayList<String> categoriasAInserir = new ArrayList<>(Arrays.asList(categoriasNoticiaSeparadas));
            categoriasDaBD.moveToNext();
            while (!categoriasDaBD.isAfterLast()) {
                String categoriaJaNaBD = categoriasDaBD.getString(indiceDesignacao);

                idsCategorias.add(categoriasDaBD.getLong(indiceId));

                boolean encontrado = false;
                for (String categoriaDaNoticia : categoriasNoticiaSeparadas)
                    if (categoriaJaNaBD.equals(categoriaDaNoticia)) {
                        encontrado = true;
                        break;
                    }
                if (encontrado) categoriasAInserir.remove(categoriaJaNaBD);
                categoriasDaBD.moveToNext();
            }

            for (String categoriaNovaStr : categoriasAInserir) {
                ContentValues novaCategoria = new ContentValues(1);
                novaCategoria.put(NoticiaDatabase.Categoria.COLUMN_NAME_DESIGNACAO, categoriaNovaStr);
                idsCategorias.add(db.insertOrThrow(NoticiaDatabase.Categoria.TABLE_NAME, null, novaCategoria));
            }
        } else {
            int indiceId = categoriasDaBD.getColumnIndex(NoticiaDatabase.Categoria._ID);
            categoriasDaBD.moveToNext();
            while (!categoriasDaBD.isAfterLast()) {
                idsCategorias.add(categoriasDaBD.getLong(indiceId));
                categoriasDaBD.moveToNext();
            }
        }
        return idsCategorias;
    }

    private long inserirEtiqueta(SQLiteDatabase db, String etiquetaStr) {
        ContentValues values = new ContentValues(1);
        values.put(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO, etiquetaStr);
        return inserirEtiqueta(db, values);
    }

    private long inserirEtiqueta(SQLiteDatabase db, ContentValues values) {
        return inserirMonoValor(db, values, NoticiaDatabase.Etiqueta.TABLE_NAME,
                NoticiaDatabase.Etiqueta.COLUMN_NAME_DESIGNACAO, NoticiaDatabase.Etiqueta._ID);
    }

    // Função genérica da inserirEtiqueta e da inserirCategoria
    private long inserirMonoValor(SQLiteDatabase db, ContentValues values, String tabela,
                                  String nomeColunaAInserir, String nomeColunaId) {
        SelectionBuilder builder = new SelectionBuilder().table(tabela).where(
                nomeColunaAInserir + " = ?", values.getAsString(nomeColunaAInserir));
        Cursor colunaDaBD = null;
        long id;
        try {
            colunaDaBD = builder.query(db, new String[]{nomeColunaId}, null);
            if (colunaDaBD.getCount() > 0) {
                colunaDaBD.moveToFirst();
                id = colunaDaBD.getLong(colunaDaBD.getColumnIndex(nomeColunaId));
            } else {
                id = db.insertOrThrow(tabela, null, values);
            }
        } finally {
            if (colunaDaBD != null) colunaDaBD.close();
        }
        return id;
    }

    private ArrayList<Long> inserirEtiquetasDasNoticias(SQLiteDatabase db, long idNoticia, ArrayList<Long> idsEtiquetas) {
        ArrayList<Long> idsEtiquetasDasNoticias = new ArrayList<>(idsEtiquetas.size());

        ContentValues novaEtiquetaDaNoticia = new ContentValues(2);
        novaEtiquetaDaNoticia.put(NoticiaDatabase.EtiquetaDaNoticia.COLUMN_NAME_NOTICIA, idNoticia);
        for (long idEtiqueta : idsEtiquetas) {
            novaEtiquetaDaNoticia.put(NoticiaDatabase.EtiquetaDaNoticia.COLUMN_NAME_ETIQUETA, idEtiqueta);
            idsEtiquetasDasNoticias.add(db.insertOrThrow(NoticiaDatabase.EtiquetaDaNoticia.TABLE_NAME, null,
                    novaEtiquetaDaNoticia));
        }
        return idsEtiquetasDasNoticias;
    }

    private ArrayList<Long> inserirCategoriasDasNoticias(SQLiteDatabase db, long idNoticia, ArrayList<Long> idsCategorias) {
        ArrayList<Long> idsCategoriasDasNoticias = new ArrayList<>(idsCategorias.size());

        ContentValues novaCategoriaDaNoticia = new ContentValues(2);
        novaCategoriaDaNoticia.put(NoticiaDatabase.CategoriaDaNoticia.COLUMN_NAME_NOTICIA, idNoticia);
        for (long idCategoria : idsCategorias) {
            novaCategoriaDaNoticia.put(NoticiaDatabase.CategoriaDaNoticia.COLUMN_NAME_CATEGORIA, idCategoria);
            idsCategoriasDasNoticias.add(db.insertOrThrow(NoticiaDatabase.CategoriaDaNoticia.TABLE_NAME, null,
                    novaCategoriaDaNoticia));
        }
        return idsCategoriasDasNoticias;
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_NOTICIA:
                final long idNoticia = inserirNoticia(db, values);
                final ArrayList<Long> idsCategorias = inserirCategorias(db, values.getAsString(
                        NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS));
                final ArrayList<Long> idsEtiquetas = inserirEtiquetas(db, values.getAsString(
                        NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS));
                final ArrayList<Long> idsCategoriasDasNoticias = inserirCategoriasDasNoticias(db, idNoticia, idsCategorias);
                final ArrayList<Long> idsEtiquetasDasNoticias = inserirEtiquetasDasNoticias(db, idNoticia, idsEtiquetas);

                result = Uri.parse(NoticiaContract.Noticia.CONTENT_URI + "/" + idNoticia);
                break;
            case ROUTE_CATEGORIA:
                final long idCategoria = inserirCategoria(db, values);
                result = Uri.parse(NoticiaContract.Categoria.CONTENT_URI + "/" + idCategoria);
                break;
            case ROUTE_ETIQUETA:
                final long idEtiqueta = inserirEtiqueta(db, values);
                result = Uri.parse(NoticiaContract.Etiqueta.CONTENT_URI + "/" + idEtiqueta);
                break;
            case ROUTE_NOTICIA_ID:
            case ROUTE_NOTICIA_CATEGORIA_ID:
            case ROUTE_CATEGORIA_ID:
            case ROUTE_ETIQUETA_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        SelectionBuilder builder = new SelectionBuilder().where(selection, selectionArgs);
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_NOTICIA_ID:
                String idNoticia = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Noticia._ID + "=?", idNoticia);
            case ROUTE_NOTICIA:
                builder.table(NoticiaDatabase.Noticia.TABLE_NAME);
                break;
            case ROUTE_CATEGORIA_ID:
                String idCategoria = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Categoria._ID + "=?", idCategoria);
            case ROUTE_CATEGORIA:
                builder.table(NoticiaDatabase.Categoria.TABLE_NAME);
                break;
            case ROUTE_ETIQUETA_ID:
                String idEtiqueta = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Etiqueta._ID + "=?", idEtiqueta);
            case ROUTE_ETIQUETA:
                builder.table(NoticiaDatabase.Etiqueta.TABLE_NAME);
                break;
            case ROUTE_NOTICIA_CATEGORIA_ID:
                throw new UnsupportedOperationException("Delete not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        count = builder.delete(db);
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    private static final int LARGURA_IMAGEM_NORMAL = 480;
    private static final int ALTURA_IMAGEM_NORMAL = 320;
    private static final float RACIO_IMAGEM_NORMAL = ((float) LARGURA_IMAGEM_NORMAL) / ALTURA_IMAGEM_NORMAL;

    private static final int LARGURA_IMAGEM_DESTACADA = 480;
    private static final int ALTURA_IMAGEM_DESTACADA = 640;
    private static final float RACIO_IMAGEM_DESTACADA = ((float) LARGURA_IMAGEM_DESTACADA) / ALTURA_IMAGEM_DESTACADA;

    /**
     * Update an entry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        SelectionBuilder builder = new SelectionBuilder().where(selection, selectionArgs);
        ContentValues valoresFinais = new ContentValues(values);
        switch (match) {
            case ROUTE_NOTICIA_ID:
                String idNoticia = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Noticia._ID + "=?", idNoticia);
            case ROUTE_NOTICIA:
                builder.table(NoticiaDatabase.Noticia.TABLE_NAME);

                if (valoresFinais.containsKey(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS)) {
                    // TODO: Implementar actualização das categorias de uma notícia
                    throw new UnsupportedOperationException("Alterar categorias de uma notícia");
                }
                if (valoresFinais.containsKey(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM)) {
                    byte[] imagemOriginal = valoresFinais.getAsByteArray(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM);
                    Log.d(TAG, "Imagem original: " + imagemOriginal);
                    if (imagemOriginal != null) {
                        byte[] imagem = redimensionarImagem(imagemOriginal);
                        valoresFinais.put(NoticiaDatabase.Noticia.COLUMN_NAME_IMAGEM, imagem);
                    }
                }
                if (values.containsKey(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS)) {
                    // TODO: Implementar actualização das etiquetas de uma notícia
                    throw new UnsupportedOperationException("Alterar etiquetas de uma notícia");
                }
                break;
            case ROUTE_CATEGORIA_ID:
                String idCategoria = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Categoria._ID + "=?", idCategoria);
            case ROUTE_CATEGORIA:
                builder.table(NoticiaDatabase.Categoria.TABLE_NAME);
                break;
            case ROUTE_ETIQUETA_ID:
                String idEtiqueta = uri.getLastPathSegment();
                builder.where(NoticiaDatabase.Etiqueta._ID + "=?", idEtiqueta);
            case ROUTE_ETIQUETA:
                builder.table(NoticiaDatabase.Etiqueta.TABLE_NAME);
                break;
            case ROUTE_NOTICIA_CATEGORIA_ID:
                throw new UnsupportedOperationException("Delete not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    count = builder.update(db, valoresFinais);

        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    private byte[] redimensionarImagem(byte[] imagemDeEntrada) {
        Bitmap btmOriginal = BitmapFactory.decodeByteArray(imagemDeEntrada, 0, imagemDeEntrada.length);

        int larguraOriginal = btmOriginal.getWidth();
        int alturaOriginal = btmOriginal.getHeight();
        if (larguraOriginal < 64 || alturaOriginal < 64) return null;

        float racioOriginal = ((float) larguraOriginal) / alturaOriginal;

        int larguraNova = larguraOriginal;
        int xNovo = 0;
        int alturaNova = alturaOriginal;
        int yNovo = 0;
        if (racioOriginal > RACIO_IMAGEM_NORMAL) {
            // Muito Larga
            larguraNova = (int) (alturaOriginal * RACIO_IMAGEM_NORMAL);
            xNovo = (larguraOriginal - larguraNova) / 2;
        } else if (racioOriginal < RACIO_IMAGEM_NORMAL) {
            // Muito Alta
            alturaNova = (int) (larguraOriginal / RACIO_IMAGEM_NORMAL);
            yNovo = (alturaOriginal - alturaNova) / 2;
        }

        float escalonamento = ((float) LARGURA_IMAGEM_NORMAL) / larguraNova;
        Matrix matrix = new Matrix();
        matrix.postScale(escalonamento, escalonamento);

        Bitmap btmRedimensionado = Bitmap.createBitmap(btmOriginal, xNovo, yNovo, larguraNova, alturaNova, matrix, true);
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        btmRedimensionado.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPG_NA_BD, byteArrayBitmapStream);
        return byteArrayBitmapStream.toByteArray();
    }

    /**
     * SQLite backend for @{link NoticiaProvider}.
     * <p/>
     * Provides access to an disk-backed, SQLite datastore which is utilized by NoticiaProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class NoticiaDatabase
            extends SQLiteOpenHelper {
        private static final String TAG = NoticiaProvider.class.getName() + "." + NoticiaDatabase.class.getName();
        /**
         * Schema version.
         */
        public static final int DATABASE_VERSION = 1;
        /**
         * Filename for SQLite file.
         */
        public static final String DATABASE_NAME = "noticias.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String TYPE_BLOB = " BLOB";
        private static final String COMMA_SEP = ",";

        /**
         * SQL statement to create "noticia" table.
         */
        private static final String SQL_CREATE_NOTICIA = "CREATE TABLE " + Noticia.TABLE_NAME + " (" +
                Noticia._ID + " INTEGER PRIMARY KEY," +
                Noticia.COLUMN_NAME_ID_NOTICIA + TYPE_INTEGER + " UNIQUE" + COMMA_SEP +
                Noticia.COLUMN_NAME_TITULO + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_SUBTITULO + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_TEXTO + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_ENDERECO_NOTICIA + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_ENDERECO_IMAGEM + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE + TYPE_TEXT + COMMA_SEP +
                Noticia.COLUMN_NAME_IMAGEM + TYPE_BLOB + COMMA_SEP +
                Noticia.COLUMN_NAME_DESTACADA + TYPE_INTEGER + ")";
        /**
         * SQL statement to create "categoria" table.
         */
        private static final String SQL_CREATE_CATEGORIA = "CREATE TABLE " + Categoria.TABLE_NAME + " (" +
                Categoria._ID + " INTEGER PRIMARY KEY," +
                Categoria.COLUMN_NAME_DESIGNACAO + TYPE_TEXT + " UNIQUE ON CONFLICT IGNORE)";
        /**
         * SQL statement to create "etiqueta_da_noticia" table.
         */
        private static final String SQL_CREATE_CATEGORIA_DA_NOTICIA = "CREATE TABLE " +
                CategoriaDaNoticia.TABLE_NAME + " (" +
                CategoriaDaNoticia._ID + " INTEGER PRIMARY KEY," +
                CategoriaDaNoticia.COLUMN_NAME_NOTICIA + TYPE_INTEGER + COMMA_SEP +
                CategoriaDaNoticia.COLUMN_NAME_CATEGORIA + TYPE_INTEGER + ")";
        /**
         * SQL statement to create "etiqueta" table.
         */
        private static final String SQL_CREATE_ETIQUETA = "CREATE TABLE " + Etiqueta.TABLE_NAME + " " +
                "(" +
                Etiqueta._ID + " INTEGER PRIMARY KEY," +
                Etiqueta.COLUMN_NAME_DESIGNACAO + TYPE_TEXT + " UNIQUE ON CONFLICT IGNORE)";
        /**
         * SQL statement to create "etiqueta_da_noticia" table.
         */
        private static final String SQL_CREATE_ETIQUETA_DA_NOTICIA = "CREATE TABLE " +
                EtiquetaDaNoticia.TABLE_NAME + " (" +
                EtiquetaDaNoticia._ID + " INTEGER PRIMARY KEY," +
                EtiquetaDaNoticia.COLUMN_NAME_NOTICIA + TYPE_INTEGER + COMMA_SEP +
                EtiquetaDaNoticia.COLUMN_NAME_ETIQUETA + TYPE_INTEGER + ")";

        /**
         * SQL statement to drop "noticia" table.
         */
        private static final String SQL_DELETE_NOTICIA = "DROP TABLE IF EXISTS " + Noticia.TABLE_NAME;
        /**
         * SQL statement to drop "categoria" table.
         */
        private static final String SQL_DELETE_CATEGORIA = "DROP TABLE IF EXISTS " + Categoria.TABLE_NAME;
        /**
         * SQL statement to drop "categoria_da_noticia" table.
         */
        private static final String SQL_DELETE_CATEGORIA_DA_NOTICIA = "DROP TABLE IF EXISTS " + CategoriaDaNoticia.TABLE_NAME;
        /**
         * SQL statement to drop "etiqueta" table.
         */
        private static final String SQL_DELETE_ETIQUETA = "DROP TABLE IF EXISTS " + Etiqueta.TABLE_NAME;
        /**
         * SQL statement to drop "etiqueta_da_noticia" table.
         */
        private static final String SQL_DELETE_ETIQUETA_DA_NOTICIA = "DROP TABLE IF EXISTS " + EtiquetaDaNoticia.TABLE_NAME;

        // TODO: IMPORTANTE, triggers devem ser revistos,

        private static final String TRIGGER_SQL_DELETE_ETIQUETAS_DAS_NOTICIAS = "DELETE FROM " + EtiquetaDaNoticia.TABLE_NAME + " " +
                "WHERE " + EtiquetaDaNoticia.COLUMN_NAME_NOTICIA + " = OLD." + BaseColumns._ID;

        private static final String TRIGGER_SQL_DELETE_CATEGORIAS_DAS_NOTICIAS = "DELETE FROM " + CategoriaDaNoticia.TABLE_NAME + " " +
                "WHERE " + CategoriaDaNoticia.COLUMN_NAME_NOTICIA + " = OLD." + BaseColumns._ID;

        private static final String SQL_CREATE_TRIGGER_APAGAR_NOTICIA = "CREATE TRIGGER IF NOT EXISTS apagar_" + Noticia.TABLE_NAME_SINGULAR + " AFTER DELETE ON " + Noticia.TABLE_NAME + " FOR EACH ROW BEGIN " +
                TRIGGER_SQL_DELETE_ETIQUETAS_DAS_NOTICIAS + ";" + TRIGGER_SQL_DELETE_CATEGORIAS_DAS_NOTICIAS + "; END";

        private static final String SQL_CREATE_TRIGGER_APAGAR_CATEGORIA = "CREATE TRIGGER IF NOT EXISTS apagar_" +
                Categoria.TABLE_NAME_SINGULAR + " AFTER DELETE ON " + Categoria.TABLE_NAME + " FOR EACH ROW BEGIN " +
                TRIGGER_SQL_DELETE_CATEGORIAS_DAS_NOTICIAS + "; END";

        private static final String SQL_CREATE_TRIGGER_APAGAR_ETIQUETA = "CREATE TRIGGER IF NOT EXISTS apagar_" +
                Etiqueta.TABLE_NAME_SINGULAR + " AFTER DELETE ON " + Etiqueta.TABLE_NAME + " FOR EACH ROW BEGIN " +
                TRIGGER_SQL_DELETE_ETIQUETAS_DAS_NOTICIAS + "; END";

        public NoticiaDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, SQL_CREATE_NOTICIA);
            db.execSQL(SQL_CREATE_NOTICIA);
            Log.i(TAG, SQL_CREATE_CATEGORIA);
            db.execSQL(SQL_CREATE_CATEGORIA);
            Log.i(TAG, SQL_CREATE_CATEGORIA_DA_NOTICIA);
            db.execSQL(SQL_CREATE_CATEGORIA_DA_NOTICIA);
            Log.i(TAG, SQL_CREATE_ETIQUETA);
            db.execSQL(SQL_CREATE_ETIQUETA);
            Log.i(TAG, SQL_CREATE_ETIQUETA_DA_NOTICIA);
            db.execSQL(SQL_CREATE_ETIQUETA_DA_NOTICIA);
            Log.i(TAG, SQL_CREATE_TRIGGER_APAGAR_NOTICIA);
            db.execSQL(SQL_CREATE_TRIGGER_APAGAR_NOTICIA);
            Log.i(TAG, SQL_CREATE_TRIGGER_APAGAR_CATEGORIA);
            db.execSQL(SQL_CREATE_TRIGGER_APAGAR_CATEGORIA);
            Log.i(TAG, SQL_CREATE_TRIGGER_APAGAR_ETIQUETA);
            db.execSQL(SQL_CREATE_TRIGGER_APAGAR_ETIQUETA);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_NOTICIA);
            db.execSQL(SQL_DELETE_CATEGORIA);
            db.execSQL(SQL_DELETE_CATEGORIA_DA_NOTICIA);
            db.execSQL(SQL_DELETE_ETIQUETA);
            db.execSQL(SQL_DELETE_ETIQUETA_DA_NOTICIA);
            onCreate(db);
        }

        public static class Noticia
                implements BaseColumns {
            public static final String TABLE_NAME_SINGULAR = "noticia";
            public static final String TABLE_NAME_PLURAL = "noticias";

            /**
             * Nome da tabela onde são guardados os registos para os recursos do tipo "noticia".
             */
            public static final String TABLE_NAME = Noticia.TABLE_NAME_SINGULAR;
            /**
             * Identificação da notícia. (Nota: Não confundir com a chave primária da base de dados, que é _ID)
             */
            public static final String COLUMN_NAME_ID_NOTICIA = "id_noticia";
            /**
             * Título da notícia
             */
            public static final String COLUMN_NAME_TITULO = "titulo";
            /**
             * Subtítulo da notícia
             */
            public static final String COLUMN_NAME_SUBTITULO = "subtitulo";
            /**
             * Texto da notícia
             */
            public static final String COLUMN_NAME_TEXTO = "texto";
            /**
             * Endereço da notícia completa
             */
            public static final String COLUMN_NAME_ENDERECO_NOTICIA = "end_noticia";
            /**
             * Endereço da imagem da notícia
             */
            public static final String COLUMN_NAME_ENDERECO_IMAGEM = "end_img";
            /**
             * Endereço da imagem da notícia
             */
            public static final String COLUMN_NAME_ENDERECO_IMAGEM_GRANDE = "end_img_grande";
            /**
             * Imagem a guardada
             */
            public static final String COLUMN_NAME_IMAGEM = "imagem";
            /**
             * Indicação de que a notícia está destacada no sítio de origem
             */
            public static final String COLUMN_NAME_DESTACADA = "destacada";
        }

        public static class Categoria
                implements BaseColumns {
            public static final String TABLE_NAME_SINGULAR = "categoria";
            public static final String TABLE_NAME_PLURAL = "categorias";

            /**
             * Nome da tabela onde são guardados os registos para os recursos do tipo "categoria".
             */
            public static final String TABLE_NAME = Categoria.TABLE_NAME_SINGULAR;
            /**
             * Designação da categoria
             */
            public static final String COLUMN_NAME_DESIGNACAO = "designacao";
        }

        public static class CategoriaDaNoticia
                implements BaseColumns {
            public static final String TABLE_NAME_SINGULAR = "categoria_da_noticia";
            public static final String TABLE_NAME_PLURAL = "categorias_das_noticias";

            /**
             * Nome da tabela onde são guardados os registos para os recursos do tipo "categoria_noticia".
             */
            public static final String TABLE_NAME = TABLE_NAME_SINGULAR;
            /**
             * Categoria que liga
             */
            public static final String COLUMN_NAME_CATEGORIA = Categoria.TABLE_NAME_SINGULAR;
            /**
             * Noticia que liga
             */
            public static final String COLUMN_NAME_NOTICIA = Noticia.TABLE_NAME_SINGULAR;
        }

        public static class Etiqueta
                implements BaseColumns {
            public static final String TABLE_NAME_SINGULAR = "etiqueta";
            public static final String TABLE_NAME_PLURAL = "etiquetas";

            /**
             * Nome da tabela onde são guardados os registos para os recursos do tipo "etiqueta".
             */
            public static final String TABLE_NAME = Etiqueta.TABLE_NAME_SINGULAR;
            /**
             * Designação da etiqueta
             */
            public static final String COLUMN_NAME_DESIGNACAO = "designacao";
        }

        public static class EtiquetaDaNoticia
                implements BaseColumns {
            public static final String TABLE_NAME_SINGULAR = "etiqueta_da_noticia";
            public static final String TABLE_NAME_PLURAL = "etiquetas_das_noticias";

            /**
             * Nome da tabela onde são guardados os registos para os recursos do tipo "etiqueta_noticia".
             */
            public static final String TABLE_NAME = TABLE_NAME_SINGULAR;
            /**
             * Etiqueta que liga
             */
            public static final String COLUMN_NAME_ETIQUETA = Etiqueta.TABLE_NAME_SINGULAR;
            /**
             * Noticia que liga
             */
            public static final String COLUMN_NAME_NOTICIA = Noticia.TABLE_NAME_SINGULAR;
        }
    }
}
