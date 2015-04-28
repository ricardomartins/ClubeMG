from rest_framework.decorators import list_route, detail_route
from rest_framework.response import Response
from noticias.models import Noticia
from rest_framework import viewsets
from noticias.serializers import NoticiaSerializer


class NoticiaViewSet(viewsets.ReadOnlyModelViewSet):
    """
    API endpoint that allows noticias to be viewed or edited.
    """

    lookup_value_regex = '[1-9][0-9]*'

    cabecalho_marca_tempo = 'ClubeMG-MarcaTempo'

    queryset = Noticia.objects.order_by('-destacada', '-id_noticia')
    serializer_class = NoticiaSerializer

    @list_route(methods=['head', 'get'])
    def head(self, request):
        ultima_actualizacao_codificada = Noticia.objects.order_by('-codigo_actualizacao')[0].codigo_actualizacao
        dados = {self.cabecalho_marca_tempo: ultima_actualizacao_codificada}
        return Response(data=dados, headers=dados)

    # Para obter notícias posteriores ao valor enviado
    def coiso(self, request, pk=None):
        noticias_actualizadas = self.get_queryset().filter(codigo_actualizacao__gt=pk)
        serializer = self.get_serializer(noticias_actualizadas, many=True)
        return Response(serializer.data)
