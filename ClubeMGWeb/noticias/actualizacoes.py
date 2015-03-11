import noticias.models as models
import noticias.sitio as sitio


class ResultadoActualizacao(object):
    def __init__(self):
        self.insercoes = 0
        self.actualizacoes = 0
        self.apagoes = 0

    @property
    def alteracoes(self):
        return self.insercoes + self.actualizacoes + self.apagoes


def obter_noticia_actualizada(noticia_sitio: sitio.Noticia) -> models.Noticia:
    try:
        noticia_bd = models.Noticia.objects.get(id_noticia=noticia_sitio.identificacao_noticia)
        if bool(noticia_bd.destacada) != bool(noticia_sitio.destacada):
            return _inserir_noticia(noticia_sitio)
    except models.Noticia.DoesNotExist:
        return _inserir_noticia(noticia_sitio)


def _inserir_noticia(noticia_sitio: sitio.Noticia) -> models.Noticia:
    noticia_bd = models.Noticia()
    noticia_bd.id_noticia = int(noticia_sitio.identificacao_noticia)
    noticia_bd.titulo = noticia_sitio.titulo or ''
    noticia_bd.subtitulo = noticia_sitio.subtitulo or ''
    noticia_bd.texto = noticia_sitio.texto or ''
    noticia_bd.destacada = noticia_sitio.destacada
    noticia_bd.end_img = noticia_sitio.endereco_imagem
    noticia_bd.end_noticia = noticia_sitio.endereco_noticia

    noticia_bd.save()

    for categoria_sitio in noticia_sitio.categorias:
        categoria_bd = obter_categoria_por_designacao(categoria_sitio)
        noticia_bd.categorias.add(categoria_bd)

    for etiqueta_sitio in noticia_sitio.etiquetas:
        etiqueta_bd = obter_etiqueta_por_designacao(etiqueta_sitio)
        noticia_bd.etiquetas.add(etiqueta_bd)

    return noticia_bd


def obter_categoria_por_designacao(designacao_categoria: str) -> models.Categoria:
    try:
        return models.Categoria.objects.get(designacao=designacao_categoria)
    except models.Categoria.DoesNotExist:
        return _inserir_categoria(designacao_categoria)


def _inserir_categoria(designacao_categoria: str) -> models.Categoria:
    categoria_bd = models.Categoria(designacao=designacao_categoria)

    categoria_bd.save()

    return categoria_bd


def obter_etiqueta_por_designacao(designacao_etiqueta: str) -> models.Etiqueta:
    try:
        return models.Etiqueta.objects.get(designacao=designacao_etiqueta)
    except models.Etiqueta.DoesNotExist:
        return _inserir_etiqueta(designacao_etiqueta)


def _inserir_etiqueta(designacao_etiqueta: str) -> models.Etiqueta:
    etiqueta_bd = models.Etiqueta(designacao=designacao_etiqueta)

    etiqueta_bd.save()

    return etiqueta_bd


def actualizar_noticias_locais(sitio_noticias: sitio.SitioNoticias):
    for noticia_sitio in sitio_noticias.noticias:
        obter_noticia_actualizada(noticia_sitio)
