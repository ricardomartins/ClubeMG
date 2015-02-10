from bs4 import BeautifulSoup
import requests


class SitioNoticias(object):
    _noticias = {}
    _categorias = {}
    _etiquetas = {}

    def __init__(self, endereco, processar_sitio_noticias):
        self.endereco = endereco
        self._processar_sitio_noticias = processar_sitio_noticias

    def _obter_pagina(endereco):
        pagina = requests.get(endereco)
        documento = BeautifulSoup(pagina, "lxml")
        return documento

    def get_endereco(self):
        return self._endereco

    def set_endereco(self, valor):
        self._endereco = valor

    endereco = property(get_endereco, set_endereco)


class Noticia:
    pass