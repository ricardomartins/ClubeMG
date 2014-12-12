package pt.rikmartins.clubemgandroid.modelos;

import com.orm.SugarRecord;

/**
 * Created by ricardo on 03-12-2014.
 */
public class Noticia extends SugarRecord<Noticia> {
    public String identificacaoNoticia;
    public String titulo;
    public String subtitulo;
    public String texto;
    public String enderecoNoticia;
    public String enderecoImagem;
    public byte[] imagem;
    public Categoria categoria;
    public boolean destacada;

    public Noticia(){
    }
}
