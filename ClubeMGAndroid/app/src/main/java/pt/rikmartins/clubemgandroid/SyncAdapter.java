package pt.rikmartins.clubemgandroid;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import pt.rikmartins.clubemg.utilitarios.noticias.SitioNoticiasClubeMG;
import pt.rikmartins.utilitarios.noticias.SitioNoticias;

/**
 * Created by ricardo on 14-12-2014.
 */
public class SyncAdapter
        extends AbstractThreadedSyncAdapter {

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
        SitioNoticiasClubeMG sitioNoticiasClubeMG = new SitioNoticiasClubeMG();

        sitioNoticiasClubeMG.actualizarNoticias();

        // TODO: Triar o que vai entrar para a base de dados, e talvez o que vai sair dela

        for (String designacaoCategoria : sitioNoticiasClubeMG.getCategorias()) {
            Categoria categoria = new Categoria();
            categoria.designacao = designacaoCategoria;
            categoria.save();
        }
        for (String designacaoEtiqueta : sitioNoticiasClubeMG.getEtiquetas()) {
            Etiqueta etiqueta = new Etiqueta();
            etiqueta.designacao = designacaoEtiqueta;
            etiqueta.save();
        }

        for (SitioNoticias.Noticia noticiaDoSitio : sitioNoticiasClubeMG.getNoticias()){
            Noticia noticia = new Noticia();

            noticia.identificacaoNoticia = noticiaDoSitio.getIdentificacaoNoticia();
            noticia.titulo = noticiaDoSitio.getTitulo();
            noticia.subtitulo = noticiaDoSitio.getSubtitulo();
            noticia.texto = noticiaDoSitio.getTexto();
            noticia.enderecoNoticia = noticiaDoSitio.getEnderecoNoticia().toString(); // TODO: Poderá precisar alteração
            noticia.enderecoImagem = noticiaDoSitio.getEnderecoImagem().toString(); // TODO: Poderá precisar alteração
            noticia.categoria = Categoria.find(Categoria.class, "designacao = ?",
                                                 noticiaDoSitio.getCategoria()).get(0);
            noticia.destacada = noticiaDoSitio.isDestacada();
        }
    }
}
