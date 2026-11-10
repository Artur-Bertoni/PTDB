package br.edu.dombosco.ptdb.usuario;

public class UsuarioResumo {

    private Long id;
    private String nome;
    private String email;
    private String matricula;
    private String perfil;
    private String perfilDescricao;

    public UsuarioResumo(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.matricula = usuario.getMatricula();
        this.perfil = usuario.getPerfil().name();
        this.perfilDescricao = usuario.getPerfil().getDescricao();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getPerfilDescricao() {
        return perfilDescricao;
    }
}
