import re
from bs4 import BeautifulSoup
from bs4.element import Tag
import noticias.sitio as sitio


def _processar_sitio_noticias_clubemg(pagina: BeautifulSoup) -> list:
    contentwrap = pagina.find(id="contentwrap")
    elementos_noticias = contentwrap.find_all(class_="post")

    return [(elemento_noticia, _extrai_noticia_clubemg) for elemento_noticia in elementos_noticias]


def _extrai_noticia_clubemg(noticia: sitio.Noticia, elemento: Tag):
    classes = set(elemento["class"])

    for classe in classes:
        classe_decomposta = map((lambda el_cl: el_cl.replace("-", " ")), classe.split("-", 1))
        corpo = None
        for i, pedaco in enumerate(classe_decomposta):
            if i == 0:
                cabeca = pedaco
            else:
                corpo = pedaco
        if cabeca == "category":
            noticia._categorias.add(corpo)
        elif cabeca == "tag":
            noticia._etiquetas.add(corpo)
        elif cabeca == "featured":
            noticia._destacada = True
        elif cabeca == "post" and corpo is not None:
            noticia._identificacao_noticia = corpo

    postwrap = elemento.div

    elemento_title = postwrap.find(class_="title")
    noticia._titulo = elemento_title.get_text(strip=True)
    noticia._subtitulo = None
    noticia._texto = postwrap.find(class_="entry").get_text(strip=True).replace(" Ler mais", "")
    noticia._endereco_noticia = elemento_title.a["href"]

    endereco_imagem_html = postwrap.find(class_="thumbwrap").img["src"]
    noticia._endereco_imagem = re.sub("-\\d+x\\d+[.]", ".", endereco_imagem_html)


def obter_noticias_clubemg() -> sitio.SitioNoticias:
    return sitio.obter_noticias("http://www.montanhismo-guarda.pt/portal/", _processar_sitio_noticias_clubemg)