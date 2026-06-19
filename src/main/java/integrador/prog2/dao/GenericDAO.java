package integrador.prog2.dao;

import java.util.List;

public interface GenericDAO<T> {
    T crear(T entidad);
    T buscarPorId(Long id);
    List<T> listar();
    T editar(T entidad);
    void eliminar(Long id);
}
/*¿Qué es <T>? Es un tipo genérico — significa "cualquier tipo".
Así no repetimos los mismos métodos en cada interfaz.
Cuando CategoriaDAO extiende GenericDAO<Categoria>,
el T se reemplaza por Categoria automáticamente.*/
