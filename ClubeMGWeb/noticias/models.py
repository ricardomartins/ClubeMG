from django.db import models


class Categoria(models.Model):
    designacao = models.CharField(max_length=64)


class Etiqueta(models.Model):
    designacao = models.CharField(max_length=128)


class Noticia(models.Model):
    id_noticia = models.PositiveIntegerField()
    titulo = models.CharField(max_length=256)
    subtitulo = models.CharField(max_length=256)
    texto = models.TextField()
    end_noticia = models.URLField()
    end_img = models.URLField()
    end_img_grande = models.URLField()
    imagem = models.ImageField()
    destacada = models.BooleanField(default=False)
    categorias = models.ManyToManyField(to=Categoria)
    etiquetas = models.ManyToManyField(to=Etiqueta)
