package integrador.prog2.dao;

import integrador.prog2.entities.Usuario;

public interface UsuarioDAO extends GenericDAO<Usuario> {
    boolean existeMail(String mail, Long idExcluir);
    Usuario buscarPorMail(String mail);
}

