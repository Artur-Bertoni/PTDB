package br.edu.dombosco.ptdb;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.security.UsuarioLogado;
import br.edu.dombosco.ptdb.security.UsuarioSessao;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.springframework.mock.web.MockHttpSession;

public class Sessoes {

    public static MockHttpSession administrador(UsuarioRepository repository) {
        return sessao(repository, PerfilUsuario.ADMINISTRADOR);
    }

    public static MockHttpSession avaliador(UsuarioRepository repository) {
        return sessao(repository, PerfilUsuario.AVALIADOR);
    }

    public static MockHttpSession aluno(UsuarioRepository repository) {
        return sessao(repository, PerfilUsuario.ALUNO);
    }

    public static UsuarioLogado usuario(UsuarioRepository repository, PerfilUsuario perfil) {
        Usuario usuario = repository.findFirstByPerfil(perfil);
        return new UsuarioLogado(usuario);
    }

    private static MockHttpSession sessao(UsuarioRepository repository, PerfilUsuario perfil) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(UsuarioSessao.ATRIBUTO_USUARIO, usuario(repository, perfil));
        return session;
    }
}
