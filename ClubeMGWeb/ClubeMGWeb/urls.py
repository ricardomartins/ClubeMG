from django.conf.urls import url, include
from django.contrib import admin
from noticias import routers
from noticias import views

router = routers.CustomReadOnlyRouter()
router.register(r'noticias', views.NoticiaViewSet)

# Wire up our API using automatic URL routing.
# Additionally, we include login URLs for the browsable API.
urlpatterns = [
    url(r'^api/v1/', include(router.urls)),
    url(r'^api-auth/', include('rest_framework.urls', namespace='rest_framework')),
    url(r'^admin/', include(admin.site.urls)),
]
