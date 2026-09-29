package br.edu.dombosco.ptdb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UsuarioSessao {

    public static final String ATRIBUTO_PERFIL = "perfilUsuario";

    private final boolean autoAdmin;

    public UsuarioSessao(@Value("${ptdb.dev.auto-admin:false}") boolean autoAdmin) {
        this.autoAdmin = autoAdmin;
    }

    public Optional<PerfilUsuario> perfil(HttpServletRequest request) {
        HttpSession session = request.getSession(autoAdmin);
        if (session == null) {
            return Optional.empty();
        }
        Object perfil = session.getAttribute(ATRIBUTO_PERFIL);
        if (perfil == null && autoAdmin) {
            perfil = PerfilUsuario.ADMINISTRADOR;
            session.setAttribute(ATRIBUTO_PERFIL, perfil);
        }
        return perfil instanceof PerfilUsuario p ? Optional.of(p) : Optional.empty();
    }
}
