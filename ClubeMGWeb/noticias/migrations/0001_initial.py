# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Categoria',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, verbose_name='ID', serialize=False)),
                ('designacao', models.CharField(verbose_name='designação', max_length=64)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Etiqueta',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, verbose_name='ID', serialize=False)),
                ('designacao', models.CharField(verbose_name='designação', max_length=128)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.CreateModel(
            name='Noticia',
            fields=[
                ('id_noticia', models.PositiveIntegerField(primary_key=True, verbose_name='identificação da notícia', serialize=False)),
                ('titulo', models.CharField(verbose_name='título', max_length=256)),
                ('subtitulo', models.CharField(verbose_name='subtítulo', max_length=256)),
                ('texto', models.TextField()),
                ('end_noticia', models.URLField(verbose_name='endereço da notícia')),
                ('end_img', models.URLField(verbose_name='endereço da imagem')),
                ('imagem', models.ImageField(upload_to='')),
                ('destacada', models.BooleanField(default=False)),
                ('ultima_actualizacao', models.DateTimeField(verbose_name='última actualização', auto_now=True)),
                ('categorias', models.ManyToManyField(to='noticias.Categoria')),
                ('etiquetas', models.ManyToManyField(to='noticias.Etiqueta')),
            ],
            options={
                'ordering': ['-destacada', '-id_noticia'],
                'get_latest_by': 'ultima_actualizacao',
                'verbose_name': 'notícia',
            },
            bases=(models.Model,),
        ),
    ]
