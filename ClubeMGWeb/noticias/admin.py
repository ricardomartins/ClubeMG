from django.contrib import admin
from noticias.models import Noticia


@admin.register(Noticia)
class NoticiaAdmin(admin.ModelAdmin):
    filter_vertical = ("etiquetas", "categorias")
    readonly_fields = ('ultima_actualizacao', )
    fieldsets = (
        (None, {
            'fields': (
                ('id_noticia', 'ultima_actualizacao'), ('titulo', 'subtitulo'), 'texto', 'end_noticia', 'end_img', 'imagem', ('categorias',
                                                                                                     'etiquetas'))
        }),
    )
