package br.edu.dombosco.ptdb.usuario;

import br.edu.dombosco.ptdb.common.RecursoNaoEncontradoException;
import br.edu.dombosco.ptdb.common.RegraNegocioException;
import br.edu.dombosco.ptdb.security.PerfilUsuario;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<UsuarioResumo> listar() {
        List<UsuarioResumo> lista = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAllByOrderByNomeAsc()) {
            lista.add(new UsuarioResumo(usuario));
        }
        return lista;
    }

    @PostMapping("/{id}/promover")
    public Map<String, String> promover(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }
        if (usuario.getPerfil() != PerfilUsuario.ALUNO) {
            throw new RegraNegocioException("Somente alunos podem ser promovidos a avaliador.");
        }
        usuario.setPerfil(PerfilUsuario.AVALIADOR);
        usuarioRepository.save(usuario);
        return Map.of("mensagem", usuario.getNome() + " agora é avaliador.");
    }
}
