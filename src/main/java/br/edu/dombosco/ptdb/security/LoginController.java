package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    public static final String TELA_LOGIN = "/tela_login.html";

    private final AutenticacaoService autenticacaoService;

    public LoginController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @GetMapping("/login")
    public String telaLogin() {
        return "redirect:" + TELA_LOGIN;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        Usuario usuario = autenticacaoService.login(request.getUsuario(), request.getSenha());
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuário ou senha inválidos");
        }
        UsuarioSessao.definir(httpRequest, usuario);
        return ResponseEntity.ok(new DadosUsuarioLogado(UsuarioSessao.buscar(httpRequest)));
    }

    @GetMapping("/api/sessao")
    @ResponseBody
    public DadosUsuarioLogado sessao(HttpServletRequest request) {
        return new DadosUsuarioLogado(UsuarioSessao.buscar(request));
    }

    @PostMapping("/sair")
    public String sair(HttpServletRequest request) {
        UsuarioSessao.sair(request);
        return "redirect:" + TELA_LOGIN;
    }
}
