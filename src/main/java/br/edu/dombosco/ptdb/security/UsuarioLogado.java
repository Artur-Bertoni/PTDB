package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import java.io.Serializable;

public class UsuarioLogado implements Serializable {

    private Long id;
    private String nome;
    private PerfilUsuario perfil;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.perfil = usuario.getPerfil();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAluno() {
        return perfil == PerfilUsuario.ALUNO;
    }

    public boolean isAvaliador() {
        return perfil == PerfilUsuario.AVALIADOR;
    }

    public boolean isAdministrador() {
        return perfil == PerfilUsuario.ADMINISTRADOR;
    }
}
