from bs4 import BeautifulSoup
import requests


class SitioNoticias(object):
    def __init__(self, endereco, processar_sitio_noticias):
        self._endereco = endereco
        self._processar_sitio_noticias = processar_sitio_noticias
        self._noticias = set()
        self._categorias = set()
        self._etiquetas = set()

    def get_endereco(self):
        return self._endereco

    def set_endereco(self, valor):
        self._endereco = valor

    endereco = property(get_endereco, set_endereco)

    def actualizar_noticias(self):
        pagina = _obter_pagina(self._endereco)
        if pagina is None:
            return False

        # elementos_noticias vai ter uma lista de tuplos (Tag, callable)
        elementos_noticias = self._processar_sitio_noticias(pagina)
        if elementos_noticias is None:
            return False

        noticias = []
        for class_e_elemento in elementos_noticias:
            if class_e_elemento is None:
                return False
            elemento, extrai_noticia = class_e_elemento
            noticias.append(Noticia(self, elemento, extrai_noticia))

        for noticia in noticias:
            self._adicionar_noticia(noticia)

        return True

    def _adicionar_noticia(self, noticia):
        self._noticias.add(noticia)
        self._etiquetas.update(noticia.etiquetas)
        self._categorias.update(noticia.categorias)

    def get_noticias(self):
        return self._noticias

    def get_categorias(self):
        return self._categorias

    def get_etiquetas(self):
        return self._etiquetas

    noticias = property(get_noticias)
    categorias = property(get_categorias)
    etiquetas = property(get_etiquetas)

    def esta_preenchido(self):
        return not (self._noticias is None or len(self._noticias) > 0)


class Noticia(object):
    def __init__(self, sitio_noticias, elemento, extrai_noticia):
        self._elemento = elemento
        self._extrai_noticia = extrai_noticia
        self._sitio_noticias = sitio_noticias
        self._identificacao_noticia = ""
        self._titulo = ""
        self._subtitulo = ""
        self._texto = ""
        self._endereco_noticia = ""
        self._endereco_imagem = ""
        self._etiquetas = set()
        self._categorias = set()
        self._destacada = False

        self._prepara_noticia(self._elemento)

    def _prepara_noticia(self, elemento):
        self._extrai_noticia(self, elemento)

    def get_sitio_noticias(self) -> SitioNoticias:
        return self._sitio_noticias

    def get_identificacao_noticia(self) -> str:
        return self._identificacao_noticia

    def get_titulo(self) -> str:
        return self._titulo

    def get_subtitulo(self):
        return self._subtitulo

    def get_texto(self):
        return self._texto

    def get_endereco_noticia(self):
        return self._endereco_noticia

    def get_endereco_imagem(self):
        return self._endereco_imagem

    def get_categorias(self):
        return self._categorias

    def get_etiquetas(self):
        return self._etiquetas

    def is_destacada(self):
        return self._destacada

    sitio_noticias = property(get_sitio_noticias)
    identificacao_noticia = property(get_identificacao_noticia)
    titulo = property(get_titulo)
    subtitulo = property(get_subtitulo)
    texto = property(get_texto)
    endereco_noticia = property(get_endereco_noticia)
    endereco_imagem = property(get_endereco_imagem)
    categorias = property(get_categorias)
    etiquetas = property(get_etiquetas)
    destacada = property(is_destacada)

    def get_etiquetas_as_string(self, separador=","):
        return separador.join(self._etiquetas)

    def get_categorias_as_string(self, separador=","):
        return separador.join(self._categorias)


def _obter_pagina(endereco):
    pagina = requests.get(endereco)
    documento = BeautifulSoup(pagina.text, "lxml")
    return documento


def obter_noticias(endereco, processar_sitio_noticias) -> SitioNoticias:
    s = SitioNoticias(endereco, processar_sitio_noticias)
    s.actualizar_noticias()
    return s