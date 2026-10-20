package br.edu.dombosco.ptdb.usuario;

import br.edu.dombosco.ptdb.security.PerfilUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findFirstByPerfil(PerfilUsuario perfil);
}
