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

    public String getIniciais() {
        String[] partes = nome.trim().split(" ");
        String iniciais = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciais = iniciais + partes[partes.length - 1].substring(0, 1);
        }
        return iniciais.toUpperCase();
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
