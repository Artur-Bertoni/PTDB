package br.edu.dombosco.ptdb.security;

import br.edu.dombosco.ptdb.usuario.Usuario;
import br.edu.dombosco.ptdb.usuario.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String gerarHash(String senha) {
        return passwordEncoder.encode(senha);
    }

    public Usuario login(String usuario, String senha) {
        if (usuario == null || usuario.isBlank() || senha == null || senha.isEmpty()) {
            return null;
        }

        Usuario usuarioBanco = usuarioRepository.findByMatricula(usuario.trim());
        if (usuarioBanco != null && senhaConfere(senha, usuarioBanco)) {
            return usuarioBanco;
        }

        usuarioBanco = usuarioRepository.findByEmailIgnoreCase(usuario.trim());
        if (usuarioBanco != null && senhaConfere(senha, usuarioBanco)) {
            return usuarioBanco;
        }

        return null;
    }

    private boolean senhaConfere(String senha, Usuario usuario) {
        return passwordEncoder.matches(senha, usuario.getSenhaHash());
    }
}
