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

    public static final String TELA_INICIAL = "/tela_inicial.html";

    private final UsuarioRepository usuarioRepository;

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:" + TELA_INICIAL;
    }

    @PostMapping("/perfil")
    public String simularLogin(@RequestParam PerfilUsuario perfil, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findFirstByPerfil(perfil);
        UsuarioSessao.definir(request, usuario);
        return "redirect:" + TELA_INICIAL;
    }
}
