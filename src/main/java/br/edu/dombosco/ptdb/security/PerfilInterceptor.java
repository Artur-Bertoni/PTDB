package br.edu.dombosco.ptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

public class PerfilInterceptor implements HandlerInterceptor {

    private final PerfilUsuario perfilPermitido;

    public PerfilInterceptor(PerfilUsuario perfilPermitido) {
        this.perfilPermitido = perfilPermitido;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        UsuarioLogado usuario = UsuarioSessao.buscar(request);
        if (usuario != null && usuario.getPerfil() == perfilPermitido) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
