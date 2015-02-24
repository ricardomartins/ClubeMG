from django.contrib import admin
from noticias.models import Noticia

@admin.register(Noticia)
class NoticiaAdmin(admin.ModelAdmin):
    pass
