package integrador.prog2.dao;

import integrador.prog2.entities.Categoria;

public interface CategoriaDAO extends GenericDAO<Categoria> {
    boolean existeNombre(String nombre, Long idExcluir);
}
