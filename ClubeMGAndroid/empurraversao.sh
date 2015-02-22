#!/bin/sh
if [ $# -ne 1 ]; then
    echo "Utilização: empurraversao.sh <nome da nova versão>"
    exit 1
fi
ORIGINALLITERAL=`grep -m 1 -o "versionName '[^']*'" app/build.gradle | grep -o "'[^']*'" | grep -o "[^']*"`
ORIGINALNUMERICA=`grep -m 1 -o "versionCode [[:digit:]]*" app/build.gradle | grep -o "[[:digit:]]*"`
VERSAONUMERICA=$((ORIGINALNUMERICA+1))
echo "Vão ser efectuadas as seguintes alterações (ao ficheiro ./app/build.gradle):"
echo "  Versão: $ORIGINALNUMERICA -> $VERSAONUMERICA"
echo "  Nome da versão: \"$ORIGINALLITERAL\" -> \"$1\""
read -p "Tem acerteza que deseja continuar? [S/n] " RESPOSTA
if [[ $RESPOSTA =~ ^([sS]([Ii][Mm])?)$ ]] || [ -z $RESPOSTA ]; then
    sed -i -e "s/versionCode [[:digit:]]*/versionCode $VERSAONUMERICA/g" -e "s/versionName '[^']*'/versionName '$1'/g" app/build.gradle
    echo "Versão empurrada para: $VERSAONUMERICA '$1'"
fi
