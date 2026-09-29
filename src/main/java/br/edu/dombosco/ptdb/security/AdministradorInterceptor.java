package br.edu.dombosco.ptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdministradorInterceptor implements HandlerInterceptor {

    private final UsuarioSessao usuarioSessao;

    public AdministradorInterceptor(UsuarioSessao usuarioSessao) {
        this.usuarioSessao = usuarioSessao;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (usuarioSessao.perfil(request).filter(p -> p == PerfilUsuario.ADMINISTRADOR).isPresent()) {
            return true;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
