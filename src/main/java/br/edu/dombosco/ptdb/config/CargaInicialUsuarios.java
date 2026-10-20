package br.edu.dombosco.ptdb.config;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CargaInicialUsuarios implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public CargaInicialUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(new Usuario("Administrador do Portal", PerfilUsuario.ADMINISTRADOR));
        usuarioRepository.save(new Usuario("Rafael Lima", PerfilUsuario.AVALIADOR));
        usuarioRepository.save(new Usuario("Bruno Alves", PerfilUsuario.ALUNO));
    }
}
