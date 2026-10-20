package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UsuarioPadraoInterceptor implements HandlerInterceptor {

    private final UsuarioRepository usuarioRepository;

    public UsuarioPadraoInterceptor(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (UsuarioSessao.buscar(request) == null) {
            Usuario administrador = usuarioRepository.findFirstByPerfil(PerfilUsuario.ADMINISTRADOR);
            if (administrador != null) {
                UsuarioSessao.definir(request, administrador);
            }
        }
        return true;
    }
}
