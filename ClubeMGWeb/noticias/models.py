from datetime import datetime
from django.db import models
from django.utils import timezone


class Categoria(models.Model):
    designacao = models.CharField("designação", max_length=64)

    def __str__(self):
        return self.designacao


class Etiqueta(models.Model):
    designacao = models.CharField("designação", max_length=128)

    def __str__(self):
        return self.designacao


class Noticia(models.Model):
    class Meta:
        get_latest_by = "ultima_actualizacao"
        ordering = ['-destacada', '-id_noticia']
        verbose_name = "notícia"

    id_noticia = models.PositiveIntegerField("identificação da notícia", primary_key=True)
    titulo = models.CharField("título", max_length=256)
    subtitulo = models.CharField("subtítulo", max_length=256)
    texto = models.TextField()
    end_noticia = models.URLField("endereço da notícia")
    end_img = models.URLField("endereço da imagem")
    imagem = models.ImageField()
    destacada = models.BooleanField(default=False)
    categorias = models.ManyToManyField(to=Categoria)
    etiquetas = models.ManyToManyField(to=Etiqueta)

    ultima_actualizacao = models.DateTimeField("última actualização", auto_now=True, editable=False, blank=True)
    codigo_actualizacao = models.PositiveIntegerField("código de actualização", null=True, blank=True)

    def save(self, force_insert=False, force_update=False, using=None, update_fields=None):
        super().save(force_insert, force_update, using, update_fields)
        self.codigo_actualizacao = Noticia.codifica_data(self.ultima_actualizacao)
        super().save()

    def __str__(self):
        return self.titulo

    @staticmethod
    def codifica_data(data: datetime) -> int:
        return data.year * 372 + (data.month - 1) * 31 + data.day - 1

    @staticmethod
    def descodifica_data(data_codificada: int) -> datetime:
        ano, mes_dia = data_codificada // 372, data_codificada % 372
        mes, dia = (mes_dia // 31) + 1, mes_dia % 31 + 1
        return datetime(ano, mes, dia, tzinfo=timezone.now().tzinfo)
