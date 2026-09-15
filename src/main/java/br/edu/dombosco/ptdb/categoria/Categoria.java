package br.edu.dombosco.ptdb.categoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Categoria de video.
 *
 * <p><b>STUB do RF13 / RF10.</b> O RF06 depende de uma categoria para
 * classificar o video (campo obrigatorio). Esta entidade minima existe
 * apenas para dar suporte ao RF06 e sera substituida/expandida quando o
 * RF13 (organizacao por categoria) e o RF10 (modelagem definitiva do
 * banco) forem implementados.
 */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    public Categoria() {
    }

    public Categoria(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
