package br.edu.dombosco.ptdb.security;

public class DadosUsuarioLogado {

    private String nome;
    private String tipoUsuario;
    private String perfil;

    public DadosUsuarioLogado(UsuarioLogado usuario) {
        this.nome = usuario.getNome();
        this.tipoUsuario = usuario.getPerfil().name().toLowerCase();
        this.perfil = usuario.getPerfil().getDescricao();
    }

    public String getNome() {
        return nome;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public String getPerfil() {
        return perfil;
    }
}
