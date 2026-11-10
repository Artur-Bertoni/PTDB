package br.edu.dombosco.ptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

public class PerfilInterceptor implements HandlerInterceptor {

    private final PerfilUsuario[] perfisPermitidos;

    public PerfilInterceptor(PerfilUsuario... perfisPermitidos) {
        this.perfisPermitidos = perfisPermitidos;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        UsuarioLogado usuario = UsuarioSessao.buscar(request);
        if (usuario != null) {
            for (PerfilUsuario perfil : perfisPermitidos) {
                if (usuario.getPerfil() == perfil) {
                    return true;
                }
            }
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }
}
