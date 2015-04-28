from rest_framework.routers import Route, SimpleRouter


class CustomReadOnlyRouter(SimpleRouter):
    """
    A router for read-only APIs, which doesn't use trailing slashes.
    """
    routes = SimpleRouter.routes + [
        Route(
            url=r'^{prefix}/coiso/{lookup}$',
            mapping={'get': 'coiso'},
            name='{basename}-coiso',
            initkwargs={'suffix': 'Coiso'}
        ),
    ]
