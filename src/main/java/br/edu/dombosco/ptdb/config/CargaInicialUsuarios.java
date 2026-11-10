package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.security.AutenticacaoService;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialUsuarios implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AutenticacaoService autenticacaoService;

    public CargaInicialUsuarios(UsuarioRepository usuarioRepository, AutenticacaoService autenticacaoService) {
        this.usuarioRepository = usuarioRepository;
        this.autenticacaoService = autenticacaoService;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        criar("Administrador do Portal", "admin@dombosco.edu.br", "100001", "admin123", PerfilUsuario.ADMINISTRADOR);
        criar("Rafael Lima", "rafael.lima@dombosco.edu.br", "100002", "avaliador123", PerfilUsuario.AVALIADOR);
        criar("Bruno Alves", "bruno.alves@dombosco.edu.br", "100003", "aluno123", PerfilUsuario.ALUNO);
        criar("Ana Souza", "ana.souza@dombosco.edu.br", "100004", "aluno123", PerfilUsuario.ALUNO);
        criar("Carla Mendes", "carla.mendes@dombosco.edu.br", "100005", "aluno123", PerfilUsuario.ALUNO);
    }

    private void criar(String nome, String email, String matricula, String senha, PerfilUsuario perfil) {
        String senhaHash = autenticacaoService.gerarHash(senha);
        usuarioRepository.save(new Usuario(nome, email, matricula, senhaHash, perfil));
    }
}
