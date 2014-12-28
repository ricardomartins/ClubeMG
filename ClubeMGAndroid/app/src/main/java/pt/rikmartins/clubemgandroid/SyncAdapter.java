package pt.rikmartins.clubemgandroid;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import pt.rikmartins.clubemg.utilitarios.noticias.SitioNoticiasClubeMG;
import pt.rikmartins.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.clubemgandroid.provider.NoticiaProvider;
import pt.rikmartins.utilitarios.noticias.SitioNoticias;

/**
 * Created by ricardo on 14-12-2014.
 */
public class SyncAdapter
        extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
                              SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            SitioNoticiasClubeMG sitioNoticiasClubeMG = obterSitioNoticias();
            actualizarNoticiasLocais(sitioNoticiasClubeMG, syncResult);
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }
        Log.i(TAG, "Network synchronization complete");
    }

    private void actualizarNoticiasLocais(final SitioNoticiasClubeMG sitioNoticiasClubeMG, final SyncResult syncResult) throws RemoteException, OperationApplicationException {
        Cursor cursorNoticias = mContentResolver.query(NoticiaContract.Noticia.CONTENT_URI, null, null, null, null);
        Cursor cursorCategorias = mContentResolver.query(NoticiaContract.Categoria.CONTENT_URI, null, null, null, null);
        Cursor cursorEtiquetas = mContentResolver.query(NoticiaContract.Etiqueta.CONTENT_URI, null, null, null, null);

        ArrayList<SitioNoticias.Noticia> noticiasAInserir = new ArrayList<>();
        ArrayList<String> categoriasAInserir = new ArrayList<>();
        ArrayList<String> etiquetasAInserir = new ArrayList<>();

        int indiceIdNoticia = cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA);
        for (SitioNoticias.Noticia noticia : sitioNoticiasClubeMG.getNoticias()) {
            String idNoticia = noticia.getIdentificacaoNoticia();

            boolean adicionarALista = true;
            cursorNoticias.moveToFirst();
            while(!cursorNoticias.isAfterLast()) {
                if (cursorNoticias.getString(indiceIdNoticia).equals(idNoticia))
                    adicionarALista = false;
                cursorNoticias.moveToNext();
            }
            if (adicionarALista) noticiasAInserir.add(noticia);
        }

        int indiceDesignacaoCategoria = cursorCategorias.getColumnIndex(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO);
        for (String categoria : sitioNoticiasClubeMG.getCategorias()) {

            boolean adicionarALista = true;
            cursorCategorias.moveToFirst();
            while(!cursorCategorias.isAfterLast()) {
                if (cursorCategorias.getString(indiceDesignacaoCategoria).equals(categoria))
                    adicionarALista = false;
                cursorCategorias.moveToNext();

            }
            if (adicionarALista) categoriasAInserir.add(categoria);
        }

        int indiceDesignacaoEtiqueta = cursorEtiquetas.getColumnIndex(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO);
        for (String etiqueta : sitioNoticiasClubeMG.getEtiquetas()) {

            boolean adicionarALista = true;
            cursorEtiquetas.moveToFirst();
            while(!cursorEtiquetas.isAfterLast()) {
                if (cursorEtiquetas.getString(indiceDesignacaoEtiqueta).equals(etiqueta))
                    adicionarALista = false;
                cursorEtiquetas.moveToNext();
            }
            if (adicionarALista) etiquetasAInserir.add(etiqueta);
        }

        ArrayList<ContentProviderOperation> lote = new ArrayList<>();

        for (SitioNoticias.Noticia noticiaDoSitio : noticiasAInserir) {
            final ContentProviderOperation.Builder insertOperationBuilder = ContentProviderOperation.newInsert(NoticiaContract.Noticia.CONTENT_URI);
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA, noticiaDoSitio.getIdentificacaoNoticia());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TITULO, noticiaDoSitio.getTitulo());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_SUBTITULO, noticiaDoSitio.getSubtitulo());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TEXTO, noticiaDoSitio.getTexto());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIA, noticiaDoSitio.getCategoria());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEtiquetasAsString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA, noticiaDoSitio.isDestacada());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM, noticiaDoSitio.getEnderecoImagem().toString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA, noticiaDoSitio.getEnderecoNoticia().toString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEnderecoImagemGrande().toString());

            lote.add(insertOperationBuilder.build());
        }
        for (String designacaoCategoria : categoriasAInserir)
            lote.add(ContentProviderOperation.newInsert(NoticiaContract.Categoria.CONTENT_URI)
                    .withValue(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO, designacaoCategoria)
                    .build());
        for (String designacaoEtiqueta : etiquetasAInserir)
            lote.add(ContentProviderOperation.newInsert(NoticiaContract.Etiqueta.CONTENT_URI)
                    .withValue(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO, designacaoEtiqueta)
                    .build());
        syncResult.stats.numInserts = syncResult.stats.numEntries = noticiasAInserir.size() + categoriasAInserir.size() + etiquetasAInserir.size();

        mContentResolver.applyBatch(NoticiaContract.CONTENT_AUTHORITY, lote);
    }

    private SitioNoticiasClubeMG obterSitioNoticias() throws IOException {
        SitioNoticiasClubeMG sitioNoticiasClubeMG = new SitioNoticiasClubeMG();
        Log.i(TAG, "Parsing page.");
        sitioNoticiasClubeMG.actualizarNoticias();
        Log.i(TAG, "Parsing complete.");

        return sitioNoticiasClubeMG;
    }
}
