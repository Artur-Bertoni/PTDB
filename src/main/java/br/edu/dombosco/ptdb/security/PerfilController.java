package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String inicio(HttpServletRequest request) {
        UsuarioLogado usuario = UsuarioSessao.buscar(request);
        return "redirect:" + paginaInicial(usuario.getPerfil());
    }

    @PostMapping("/perfil")
    public String trocar(@RequestParam PerfilUsuario perfil, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findFirstByPerfil(perfil);
        UsuarioSessao.definir(request, usuario);
        return "redirect:" + paginaInicial(perfil);
    }

    public static String paginaInicial(PerfilUsuario perfil) {
        if (perfil == PerfilUsuario.ADMINISTRADOR) {
            return "/admin";
        }
        if (perfil == PerfilUsuario.AVALIADOR) {
            return "/avaliacao";
        }
        return "/catalogo";
    }
}
