package br.edu.dombosco.ptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Restringe o acesso as telas administrativas ao perfil Administrador
 * (regra do RF06).
 *
 * <p><b>STUB do RF01/RF02.</b> Enquanto o login e os perfis nao existem, o
 * perfil e lido da sessao. Com {@code ptdb.dev.auto-admin=true} o perfil e
 * simulado como ADMINISTRADOR para permitir o uso em desenvolvimento. Ao
 * implementar o RF01/RF02, o flag deve ser desligado e a sessao passa a ser
 * populada pelo fluxo de autenticacao real.
 */
@Component
public class AdministradorInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_PERFIL = "perfilUsuario";

    private final boolean autoAdmin;

    public AdministradorInterceptor(@Value("${ptdb.dev.auto-admin:false}") boolean autoAdmin) {
        this.autoAdmin = autoAdmin;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(true);
        Object perfil = session.getAttribute(ATRIBUTO_PERFIL);

        if (perfil == null && autoAdmin) {
            // Simulacao de Administrador autenticado (apenas dev).
            perfil = PerfilUsuario.ADMINISTRADOR;
            session.setAttribute(ATRIBUTO_PERFIL, perfil);
        }

        if (perfil == PerfilUsuario.ADMINISTRADOR) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        try {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().write(
                    "<h1>403 - Acesso restrito</h1>"
                    + "<p>Esta area e exclusiva do perfil Administrador (RF06).</p>");
        } catch (Exception ignored) {
            // resposta ja comprometida; nada a fazer
        }
        return false;
    }
}
