package pt.rikmartins.clubemg.clubemgandroid;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;

import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;

/**
 * Created by ricardo on 10-01-2015.
 */
class ObtensorImagens {
    private static HashSet<String> urlImagensSet = new HashSet<>();

    public static void obterImagem(Context context, String urlImagem, String id) {
        if (!urlImagensSet.contains(urlImagem)) {
            ObtensorAssincronoImagens obtensorAssincronoImagens = new ObtensorAssincronoImagens(context);
            urlImagensSet.add(urlImagem);
            obtensorAssincronoImagens.execute(urlImagem, String.valueOf(id));
        }
    }

    private static class ObtensorAssincronoImagens extends AsyncTask<String, Void, byte[]> {
        private final Context mContext;

        private static final String TAG = ObtensorAssincronoImagens.class.getSimpleName();

        private ObtensorAssincronoImagens(Context context) {
            super();
            mContext = context;
        }

        private byte[] downloadImageAsByteArray(URL urlImagem){
            Log.v(TAG, "downloadImageAsByteArray: " + urlImagem);
            URLConnection con;
            try {
                con = urlImagem.openConnection();
                BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current;
                while ((current = bis.read()) != -1) baf.append((byte) current);

                return baf.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private int saveImageToDatabase(byte[] ba, Uri uriDaNoticia) {
            if (ba == null) return -1;

            Log.v(TAG, "saveImageToDatabase: " + uriDaNoticia);
            ContentValues valores = new ContentValues(1);
            valores.put(NoticiaContract.Noticia.COLUMN_NAME_IMAGEM, ba);
            return mContext.getContentResolver().update(uriDaNoticia, valores, null, null);
        }

        @Override
        protected byte[] doInBackground(String... params) {
            Log.v(TAG, "doInBackground: " + Arrays.toString(params));
            try {
                String oUrl = params[0];
                int indice = oUrl.lastIndexOf("/") + 1;
                String nomeFicheiro = oUrl.substring(indice);
                try {
                    oUrl = oUrl.substring(0, indice) + URLEncoder.encode(nomeFicheiro, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                Log.v(TAG, "URL codificado: " + oUrl);

                byte[] ba = downloadImageAsByteArray(new URL(oUrl));
                int resUpdate = saveImageToDatabase(ba, NoticiaContract.Noticia.CONTENT_URI.buildUpon().appendPath(params[1]).build());
                return ba;
            } catch (MalformedURLException e) {
                return null;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    }
}
