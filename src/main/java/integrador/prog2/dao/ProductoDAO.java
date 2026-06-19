package integrador.prog2.dao;

import integrador.prog2.entities.Producto;
import java.util.List;

public interface ProductoDAO extends GenericDAO<Producto> {
    List<Producto> listarPorCategoria(Long categoriaId);
}
