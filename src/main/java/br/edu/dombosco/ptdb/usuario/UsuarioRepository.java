package br.edu.dombosco.ptdb.usuario;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNomeAsc();

    Usuario findFirstByPerfil(PerfilUsuario perfil);

    Usuario findByMatricula(String matricula);

    Usuario findByEmailIgnoreCase(String email);
}
