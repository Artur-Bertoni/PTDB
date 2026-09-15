package br.edu.dombosco.ptdb.security;

/**
 * Perfis de acesso do portal (RF02).
 *
 * <p><b>STUB do RF01/RF02.</b> Definido aqui apenas para dar suporte a
 * regra do RF06 de restringir o gerenciamento de videos ao Administrador.
 * Sera consolidado quando o login (RF01) e os perfis (RF02) forem
 * implementados.
 */
public enum PerfilUsuario {
    ADMINISTRADOR,
    AVALIADOR,
    ALUNO
}
