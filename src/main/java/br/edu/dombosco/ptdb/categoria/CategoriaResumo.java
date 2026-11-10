package br.edu.dombosco.ptdb.categoria;

public class CategoriaResumo {

    private Long id;
    private String nome;
    private String descricao;
    private long quantidade;

    public CategoriaResumo(Categoria categoria, Long quantidade) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
        this.descricao = categoria.getDescricao();
        if (quantidade != null) {
            this.quantidade = quantidade;
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public long getQuantidade() {
        return quantidade;
    }
}
