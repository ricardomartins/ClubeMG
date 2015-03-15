package pt.rikmartins.clubemg.clubemgandroid.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import pt.rikmartins.clubemg.clubemgandroid.MainActivity;
import pt.rikmartins.clubemg.clubemgandroid.R;
import pt.rikmartins.clubemg.utilitarios.noticias.SitioNoticiasClubeMG;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;
import pt.rikmartins.utilitarios.noticias.SitioNoticias;

/**
 * Created by ricardo on 14-12-2014.
 */
public class SyncAdapter
        extends AbstractThreadedSyncAdapter {
    public static final String TAG = SyncAdapter.class.getSimpleName();

    public static final String ACTION_INICIA_ACTUALIZACAO = "pt.rikmartins.clubemg.clubemgandroid.action.INICIA_ACTUALIZACAO";
    public static final String ACTION_A_ACTUALIZAR = "pt.rikmartins.clubemg.clubemgandroid.action.A_ACTUALIZAR";
    public static final String ACTION_FINALIZA_ACTUALIZACAO = "pt.rikmartins.clubemg.clubemgandroid.action.FINALIZA_ACTUALIZACAO";

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
        Log.i(TAG, "A iniciar actualização");
        getContext().sendBroadcast(new Intent(ACTION_INICIA_ACTUALIZACAO));
        ArrayList<SitioNoticias.Noticia> noticiasInseridas = null;
        try {
            SitioNoticiasClubeMG sitioNoticiasClubeMG = obterSitioNoticias();
            getContext().sendBroadcast(new Intent(ACTION_A_ACTUALIZAR));
            noticiasInseridas = actualizarNoticiasLocais(sitioNoticiasClubeMG, syncResult);
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
        } finally {
            Log.i(TAG, "A finalizar actualização");
            getContext().sendBroadcast(new Intent(ACTION_FINALIZA_ACTUALIZACAO));
        }

        final Resources resources = getContext().getResources();
        final boolean notificar = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(resources.getString(R.string.pref_key_notificacoes_novas_noticias), false);

        if (notificar && noticiasInseridas.size() > 0) {
            SitioNoticias.Noticia noticia = noticiasInseridas.get(0);
            SyncAdapter.Notificacao.notificar(getContext(), noticia.getTitulo(), noticia.getTexto(), "Uma nova notícia", R.drawable.ic_notificacao);
        }
    }

    private ArrayList<SitioNoticias.Noticia> actualizarNoticiasLocais(final SitioNoticiasClubeMG sitioNoticiasClubeMG, final SyncResult syncResult) throws RemoteException, OperationApplicationException {
        Cursor cursorNoticias = mContentResolver.query(NoticiaContract.Noticia.CONTENT_URI, null, null, null, NoticiaContract.Noticia.COLUMN_NAME_DESTACADA + " DESC, " + NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA + " DESC");
        Log.v(TAG, "noticias anteriores: " + cursorNoticias.getColumnCount());

        ArrayList<SitioNoticias.Noticia> noticiasAInserir = new ArrayList<>();
        Map<Long, SitioNoticias.Noticia> noticiasAActualizar = new HashMap<>();
        HashSet<Long> noticiasAApagar = new HashSet<>();

        int indiceIdNoticia = cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA);
        int indiceDestacada = cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA);
        int indiceId = cursorNoticias.getColumnIndex(NoticiaContract.Noticia._ID);
        int i = 0;
        for (SitioNoticias.Noticia noticia : sitioNoticiasClubeMG.getNoticias()) {
            String idNoticiaExterna = noticia.getIdentificacaoNoticia();
            boolean eNoticiaDestacadaExterna = noticia.isDestacada();

            boolean adicionarALista = true;
            cursorNoticias.moveToFirst();
            while(!cursorNoticias.isAfterLast()) {
                if (i >= 0) {
                    i++;
                    if (i > 11) {
                        noticiasAApagar.add(cursorNoticias.getLong(indiceId));
                        i = -1;
                    }
                }
                if (cursorNoticias.getString(indiceIdNoticia).equals(idNoticiaExterna)) {
                    adicionarALista = false;
                    if ((cursorNoticias.getInt(indiceDestacada) == 1) ^ eNoticiaDestacadaExterna)
                        noticiasAActualizar.put(cursorNoticias.getLong(indiceId), noticia);
                    if (i < 0) break;
                }
                cursorNoticias.moveToNext();
            }
            i = -1;
            if (adicionarALista) noticiasAInserir.add(noticia);
        }
        Log.v(TAG, "noticias a inserir: " + noticiasAInserir.size());

        Cursor cursorCategorias = mContentResolver.query(NoticiaContract.Categoria.CONTENT_URI, null, null, null, null);
        Log.v(TAG, "categorias anteriores: " + cursorCategorias.getColumnCount());

        ArrayList<String> categoriasAInserir = new ArrayList<>();

        int indiceDesignacaoCategoria = cursorCategorias.getColumnIndex(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO);
        for (String categoria : sitioNoticiasClubeMG.getCategorias()) {

            boolean adicionarALista = true;
            cursorCategorias.moveToFirst();
            while(!cursorCategorias.isAfterLast()) {
                if (cursorCategorias.getString(indiceDesignacaoCategoria).equals(categoria)) {
                    adicionarALista = false;
                    break;
                }
                cursorCategorias.moveToNext();

            }
            if (adicionarALista) categoriasAInserir.add(categoria);
        }
        Log.v(TAG, "categorias a inserir: " + categoriasAInserir.size());

        Cursor cursorEtiquetas = mContentResolver.query(NoticiaContract.Etiqueta.CONTENT_URI, null, null, null, null);
        Log.v(TAG, "etiquetas anteriores: " + cursorEtiquetas.getColumnCount());

        ArrayList<String> etiquetasAInserir = new ArrayList<>();

        int indiceDesignacaoEtiqueta = cursorEtiquetas.getColumnIndex(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO);
        for (String etiqueta : sitioNoticiasClubeMG.getEtiquetas()) {

            boolean adicionarALista = true;
            cursorEtiquetas.moveToFirst();
            while(!cursorEtiquetas.isAfterLast()) {
                if (cursorEtiquetas.getString(indiceDesignacaoEtiqueta).equals(etiqueta)) {
                    adicionarALista = false;
                    break;
                }
                cursorEtiquetas.moveToNext();
            }
            if (adicionarALista) etiquetasAInserir.add(etiqueta);
        }
        Log.v(TAG, "etiquetas a inserir: " + etiquetasAInserir.size());

        ArrayList<ContentProviderOperation> lote = new ArrayList<>();

        for (SitioNoticias.Noticia noticiaDoSitio : noticiasAInserir) {
            final ContentProviderOperation.Builder insertOperationBuilder = ContentProviderOperation.newInsert(NoticiaContract.Noticia.CONTENT_URI);
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA, noticiaDoSitio.getIdentificacaoNoticia());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TITULO, noticiaDoSitio.getTitulo());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_SUBTITULO, noticiaDoSitio.getSubtitulo());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TEXTO, noticiaDoSitio.getTexto());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getCategoriasAsString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEtiquetasAsString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA, noticiaDoSitio.isDestacada());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM, noticiaDoSitio.getEnderecoImagem().toString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA, noticiaDoSitio.getEnderecoNoticia().toString());
            insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEnderecoImagemGrande().toString());
            Log.v(TAG, "operação inserir notícia: " + insertOperationBuilder.toString());

            lote.add(insertOperationBuilder.build());
        }

        for (long idNoticiaAActualizar : noticiasAActualizar.keySet()) {
            SitioNoticias.Noticia noticiaDoSitio = noticiasAActualizar.get(idNoticiaAActualizar);

            final ContentProviderOperation.Builder updateOperationBuilder = ContentProviderOperation.newUpdate(NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(String.valueOf(idNoticiaAActualizar)).build());
            updateOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TEXTO, noticiaDoSitio.getTexto());
            updateOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA, noticiaDoSitio.isDestacada());
            Log.v(TAG, "operação actualizar notícia: " + updateOperationBuilder.toString());

            lote.add(updateOperationBuilder.build());
        }

        for (long idNoticiaAApagar : noticiasAApagar) {
            final ContentProviderOperation.Builder deleteOperationBuilder = ContentProviderOperation.newDelete(NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(String.valueOf(idNoticiaAApagar)).build());
            Log.v(TAG, "operação apagar notícia: " + deleteOperationBuilder.toString());

            lote.add(deleteOperationBuilder.build());
        }

        for (String designacaoCategoria : categoriasAInserir)
            lote.add(ContentProviderOperation.newInsert(NoticiaContract.Categoria.CONTENT_URI)
                    .withValue(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO, designacaoCategoria)
                    .build());

        for (String designacaoEtiqueta : etiquetasAInserir)
            lote.add(ContentProviderOperation.newInsert(NoticiaContract.Etiqueta.CONTENT_URI)
                    .withValue(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO, designacaoEtiqueta)
                    .build());

        syncResult.stats.numEntries = syncResult.stats.numInserts = noticiasAInserir.size() + categoriasAInserir.size() + etiquetasAInserir.size();
        syncResult.stats.numEntries += syncResult.stats.numUpdates = noticiasAActualizar.size();
        syncResult.stats.numEntries += syncResult.stats.numDeletes = noticiasAApagar.size();
        Log.v(TAG, "lote: " + lote.size());

        mContentResolver.applyBatch(NoticiaContract.CONTENT_AUTHORITY, lote);

        if (cursorNoticias != null && !cursorNoticias.isClosed()) cursorNoticias.close();
        if (cursorCategorias != null && !cursorCategorias.isClosed()) cursorCategorias.close();
        if (cursorEtiquetas != null && !cursorEtiquetas.isClosed()) cursorEtiquetas.close();

        return noticiasAInserir;
    }

    private SitioNoticiasClubeMG obterSitioNoticias() throws IOException {
        SitioNoticiasClubeMG sitioNoticiasClubeMG = new SitioNoticiasClubeMG();
        Log.v(TAG, "A analizar a página.");
        if (!sitioNoticiasClubeMG.actualizarNoticias()) {
            throw new IOException(); // TODO: Alterar esta excepção para algo que melhor descreva o problema
        }
        Log.v(TAG, "Análize da página completa.");

        return sitioNoticiasClubeMG;
    }

    public static class Notificacao {
        public static void notificar(Context context, CharSequence titulo, CharSequence texto, CharSequence previsao, int iconPequeno){
            Log.i(TAG, "A preparar notificação");
            final Resources resources = context.getResources();

            final String toque = PreferenceManager.getDefaultSharedPreferences(context).getString(resources.getString(R.string.pref_key_notificacoes_som), null);
            final boolean vibrar = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(resources.getString(R.string.pref_key_notificacoes_vibrar), false);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setShowWhen(false)
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(iconPequeno)
                            .setContentTitle(titulo)
                            .setContentText(texto)
                            .setPriority(NotificationCompat.PRIORITY_MIN)
                            .setTicker(previsao)
                            .setContentIntent(
                                    PendingIntent.getActivity(context, 0,
                                            new Intent(context, MainActivity.class),
                                            PendingIntent.FLAG_UPDATE_CURRENT))
                            .setAutoCancel(true);
            if (toque != null) notificationBuilder.setSound(Uri.parse(toque));
            if(vibrar) notificationBuilder.setVibrate(new long[] {0, 150, 100, 150, 100, 150, 100, 150});
            else notificationBuilder.setVibrate(new long[] {0});


            Log.i(TAG, "A notificar");
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notificationBuilder.build());
        }
    }
}
