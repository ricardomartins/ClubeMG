from noticias.sitio_cmg import obter_noticias_clubemg

sitio_noticias = obter_noticias_clubemg()
categorias = sitio_noticias.categorias
etiquetas = sitio_noticias.etiquetas
noticias = sitio_noticias.noticias

def actualizarNoticiasLocais():
    pass


    # private ArrayList<SitioNoticias.Noticia> actualizarNoticiasLocais(final SitioNoticiasClubeMG sitioNoticiasClubeMG, final SyncResult syncResult) throws RemoteException, OperationApplicationException {
    #     Cursor cursorNoticias = mContentResolver.query(NoticiaContract.Noticia.CONTENT_URI, null, null, null, NoticiaContract.Noticia.COLUMN_NAME_DESTACADA + " DESC, " + NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA + " DESC");
    #     Log.v(TAG, "noticias anteriores: " + cursorNoticias.getColumnCount());
    #
    #     ArrayList<SitioNoticias.Noticia> noticiasAInserir = new ArrayList<>();
    #     Map<Long, SitioNoticias.Noticia> noticiasAActualizar = new HashMap<>();
    #     HashSet<Long> noticiasAApagar = new HashSet<>();
    #
    #     int indiceIdNoticia = cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA);
    #     int indiceDestacada = cursorNoticias.getColumnIndex(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA);
    #     int indiceId = cursorNoticias.getColumnIndex(NoticiaContract.Noticia._ID);
    #     int i = 0;
    #     for (SitioNoticias.Noticia noticia : sitioNoticiasClubeMG.getNoticias()) {
    #         String idNoticiaExterna = noticia.getIdentificacaoNoticia();
    #         boolean eNoticiaDestacadaExterna = noticia.isDestacada();
    #
    #         boolean adicionarALista = true;
    #         cursorNoticias.moveToFirst();
    #         while(!cursorNoticias.isAfterLast()) {
    #             if (i >= 0) {
    #                 i++;
    #                 if (i > 11) {
    #                     noticiasAApagar.add(cursorNoticias.getLong(indiceId));
    #                     i = -1;
    #                 }
    #             }
    #             if (cursorNoticias.getString(indiceIdNoticia).equals(idNoticiaExterna)) {
    #                 adicionarALista = false;
    #                 if ((cursorNoticias.getInt(indiceDestacada) == 1) ^ eNoticiaDestacadaExterna)
    #                     noticiasAActualizar.put(cursorNoticias.getLong(indiceId), noticia);
    #                 if (i < 0) break;
    #             }
    #             cursorNoticias.moveToNext();
    #         }
    #         i = -1;
    #         if (adicionarALista) noticiasAInserir.add(noticia);
    #     }
    #     Log.v(TAG, "noticias a inserir: " + noticiasAInserir.size());
    #
    #     Cursor cursorCategorias = mContentResolver.query(NoticiaContract.Categoria.CONTENT_URI, null, null, null, null);
    #     Log.v(TAG, "categorias anteriores: " + cursorCategorias.getColumnCount());
    #
    #     ArrayList<String> categoriasAInserir = new ArrayList<>();
    #
    #     int indiceDesignacaoCategoria = cursorCategorias.getColumnIndex(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO);
    #     for (String categoria : sitioNoticiasClubeMG.getCategorias()) {
    #
    #         boolean adicionarALista = true;
    #         cursorCategorias.moveToFirst();
    #         while(!cursorCategorias.isAfterLast()) {
    #             if (cursorCategorias.getString(indiceDesignacaoCategoria).equals(categoria)) {
    #                 adicionarALista = false;
    #                 break;
    #             }
    #             cursorCategorias.moveToNext();
    #
    #         }
    #         if (adicionarALista) categoriasAInserir.add(categoria);
    #     }
    #     Log.v(TAG, "categorias a inserir: " + categoriasAInserir.size());
    #
    #     Cursor cursorEtiquetas = mContentResolver.query(NoticiaContract.Etiqueta.CONTENT_URI, null, null, null, null);
    #     Log.v(TAG, "etiquetas anteriores: " + cursorEtiquetas.getColumnCount());
    #
    #     ArrayList<String> etiquetasAInserir = new ArrayList<>();
    #
    #     int indiceDesignacaoEtiqueta = cursorEtiquetas.getColumnIndex(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO);
    #     for (String etiqueta : sitioNoticiasClubeMG.getEtiquetas()) {
    #
    #         boolean adicionarALista = true;
    #         cursorEtiquetas.moveToFirst();
    #         while(!cursorEtiquetas.isAfterLast()) {
    #             if (cursorEtiquetas.getString(indiceDesignacaoEtiqueta).equals(etiqueta)) {
    #                 adicionarALista = false;
    #                 break;
    #             }
    #             cursorEtiquetas.moveToNext();
    #         }
    #         if (adicionarALista) etiquetasAInserir.add(etiqueta);
    #     }
    #     Log.v(TAG, "etiquetas a inserir: " + etiquetasAInserir.size());
    #
    #     ArrayList<ContentProviderOperation> lote = new ArrayList<>();
    #
    #     for (SitioNoticias.Noticia noticiaDoSitio : noticiasAInserir) {
    #         final ContentProviderOperation.Builder insertOperationBuilder = ContentProviderOperation.newInsert(NoticiaContract.Noticia.CONTENT_URI);
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ID_NOTICIA, noticiaDoSitio.getIdentificacaoNoticia());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TITULO, noticiaDoSitio.getTitulo());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_SUBTITULO, noticiaDoSitio.getSubtitulo());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TEXTO, noticiaDoSitio.getTexto());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_CATEGORIAS, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getCategoriasAsString());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ETIQUETAS, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEtiquetasAsString());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA, noticiaDoSitio.isDestacada());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM, noticiaDoSitio.getEnderecoImagem().toString());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_NOTICIA, noticiaDoSitio.getEnderecoNoticia().toString());
    #         insertOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_ENDERECO_IMAGEM_GRANDE, ((SitioNoticiasClubeMG.NoticiaClubeMG) noticiaDoSitio).getEnderecoImagemGrande().toString());
    #         Log.v(TAG, "operação inserir notícia: " + insertOperationBuilder.toString());
    #
    #         lote.add(insertOperationBuilder.build());
    #     }
    #
    #     for (long idNoticiaAActualizar : noticiasAActualizar.keySet()) {
    #         SitioNoticias.Noticia noticiaDoSitio = noticiasAActualizar.get(idNoticiaAActualizar);
    #
    #         final ContentProviderOperation.Builder updateOperationBuilder = ContentProviderOperation.newUpdate(NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(String.valueOf(idNoticiaAActualizar)).build());
    #         updateOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_TEXTO, noticiaDoSitio.getTexto());
    #         updateOperationBuilder.withValue(NoticiaContract.Noticia.COLUMN_NAME_DESTACADA, noticiaDoSitio.isDestacada());
    #         Log.v(TAG, "operação actualizar notícia: " + updateOperationBuilder.toString());
    #
    #         lote.add(updateOperationBuilder.build());
    #     }
    #
    #     for (long idNoticiaAApagar : noticiasAApagar) {
    #         final ContentProviderOperation.Builder deleteOperationBuilder = ContentProviderOperation.newDelete(NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(String.valueOf(idNoticiaAApagar)).build());
    #         Log.v(TAG, "operação apagar notícia: " + deleteOperationBuilder.toString());
    #
    #         lote.add(deleteOperationBuilder.build());
    #     }
    #
    #     for (String designacaoCategoria : categoriasAInserir)
    #         lote.add(ContentProviderOperation.newInsert(NoticiaContract.Categoria.CONTENT_URI)
    #                 .withValue(NoticiaContract.Categoria.COLUMN_NAME_DESIGNACAO, designacaoCategoria)
    #                 .build());
    #
    #     for (String designacaoEtiqueta : etiquetasAInserir)
    #         lote.add(ContentProviderOperation.newInsert(NoticiaContract.Etiqueta.CONTENT_URI)
    #                 .withValue(NoticiaContract.Etiqueta.COLUMN_NAME_DESIGNACAO, designacaoEtiqueta)
    #                 .build());
    #
    #     syncResult.stats.numEntries = syncResult.stats.numInserts = noticiasAInserir.size() + categoriasAInserir.size() + etiquetasAInserir.size();
    #     syncResult.stats.numEntries += syncResult.stats.numUpdates = noticiasAActualizar.size();
    #     syncResult.stats.numEntries += syncResult.stats.numDeletes = noticiasAApagar.size();
    #     Log.v(TAG, "lote: " + lote.size());
    #
    #     mContentResolver.applyBatch(NoticiaContract.CONTENT_AUTHORITY, lote);
    #
    #     if (cursorNoticias != null && !cursorNoticias.isClosed()) cursorNoticias.close();
    #     if (cursorCategorias != null && !cursorCategorias.isClosed()) cursorCategorias.close();
    #     if (cursorEtiquetas != null && !cursorEtiquetas.isClosed()) cursorEtiquetas.close();
    #
    #     return noticiasAInserir;
    # }
