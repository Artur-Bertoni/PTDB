package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class UsuarioSessao {

    public static final String ATRIBUTO_USUARIO = "usuarioLogado";

    public static UsuarioLogado buscar(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object usuario = session.getAttribute(ATRIBUTO_USUARIO);
        if (usuario instanceof UsuarioLogado) {
            return (UsuarioLogado) usuario;
        }
        return null;
    }

    public static UsuarioLogado buscar(HttpServletRequest request) {
        return buscar(request.getSession(false));
    }

    public static void definir(HttpServletRequest request, Usuario usuario) {
        request.getSession(true).setAttribute(ATRIBUTO_USUARIO, new UsuarioLogado(usuario));
    }
}
