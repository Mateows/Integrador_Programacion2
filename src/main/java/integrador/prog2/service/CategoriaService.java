package integrador.prog2.service;

import integrador.prog2.dao.CategoriaDAO;
import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.dao.Impl.CategoriaDaoImpl;
import integrador.prog2.dao.Impl.ProductoDAOImpl;
import integrador.prog2.entities.Categoria;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.util.List;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO; // Necesario para chequear productos asociados antes de eliminar

    public CategoriaService() {
        this.categoriaDAO = new CategoriaDaoImpl();
        this.productoDAO = new ProductoDAOImpl();
    }

    public List<Categoria> listar() {
        return categoriaDAO.listar();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaDAO.buscarPorId(id);
    }

    public Categoria crear(String nombre, String descripcion) {
        validarNombre(nombre);
        validarNombreSoloLetras(nombre);
        validarDescripcion(descripcion);
        validarNombreUnico(nombre, null);

        Categoria nueva = new Categoria(null, nombre.trim(), descripcion.trim());
        return categoriaDAO.crear(nueva);
    }

    public Categoria editar(Long id, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            validarNombreSoloLetras(nombre);
            validarNombreUnico(nombre, id);
            categoria.setNombre(nombre.trim());
        }
        if (descripcion != null && !descripcion.isBlank()) {
            categoria.setDescripcion(descripcion.trim());
        }
        return categoriaDAO.editar(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        List<integrador.prog2.entities.Producto> productosAsociados = productoDAO.listarPorCategoria(id);
        if (!productosAsociados.isEmpty()) {
            throw new DatoInvalidoException(
                    "No se puede eliminar la categoría porque tiene productos asociados"
            );
        }
        categoriaDAO.eliminar(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 2) {
            throw new DatoInvalidoException("El nombre debe tener al menos 2 caracteres");
        }
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new DatoInvalidoException("La descripción no puede estar vacía");
        }
    }

    private void validarNombreUnico(String nombre, Long idExcluir) {
        if (categoriaDAO.existeNombre(nombre.trim(), idExcluir)) {
            throw new DatoInvalidoException("Ya existe una categoría con el nombre: " + nombre);
        }
    }
    private void validarNombreSoloLetras(String nombre) {
        if (!nombre.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new DatoInvalidoException("El nombre solo puede contener letras y espacios");
        }
    }
    public void validarNombreUnico(String nombre) {
        validarNombreUnico(nombre, null);
    }
}