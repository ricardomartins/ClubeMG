from rest_framework import serializers

import noticias.models as models


class NoticiaSerializer(serializers.ModelSerializer):
    class Meta:
        model = models.Noticia
        fields = ('id_noticia', 'titulo', 'subtitulo', 'texto', 'end_noticia', 'end_img', 'imagem', 'destacada',
                  'categorias', 'etiquetas', 'codigo_actualizacao', )
        depth = 1
