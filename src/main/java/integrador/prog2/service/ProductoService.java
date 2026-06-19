package integrador.prog2.service;

import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.dao.Impl.ProductoDAOImpl;
import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.StockInvalidoException;

import java.util.List;

public class ProductoService {

    private final ProductoDAO productoDAO;

    public ProductoService() {
        this.productoDAO = new ProductoDAOImpl();
    }

    public List<Producto> listar() {
        return productoDAO.listar();
    }

    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoDAO.listarPorCategoria(categoriaId);
    }

    public Producto buscarPorId(Long id) {
        return productoDAO.buscarPorId(id);
    }

    public Producto crear(String nombre, Double precio, String descripcion,
                          Integer stock, String imagen, Boolean disponible,
                          Categoria categoria) {
        validarNombre(nombre);
        validarNombreSoloLetras(nombre);  // ← agregar
        validarPrecio(precio);
        validarPrecioMaximo(precio);      // ← agregar
        validarStock(stock);
        validarStockMaximo(stock);
        validarCategoria(categoria);

        if (imagen == null || imagen.isBlank()) {
            imagen = "sin_imagen.jpg";
        }
        Producto nuevo = new Producto(null, nombre.trim(), precio,
                descripcion.trim(), stock, imagen,
                disponible, categoria);
        return productoDAO.crear(nuevo);
    }

    public Producto editar(Long id, String nombre, Double precio,
                           String descripcion, Integer stock,
                           Boolean disponible, Categoria categoria) {
        Producto producto = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            producto.setNombre(nombre.trim());
        }
        if (precio != null) {
            validarPrecio(precio);
            validarPrecioMaximo(precio);
            producto.setPrecio(precio);
        }
        if (descripcion != null && !descripcion.isBlank()) {
            producto.setDescripcion(descripcion.trim());
        }
        if (stock != null) {
            validarStock(stock);
            validarStockMaximo(stock);
            producto.setStock(stock);
        }
        if (disponible != null) {
            producto.setDisponible(disponible);
        }
        if (categoria != null) {
            validarCategoria(categoria);
            producto.setCategoria(categoria);
        }
        return productoDAO.editar(producto);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        productoDAO.eliminar(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 2) {
            throw new DatoInvalidoException("El nombre debe tener al menos 2 caracteres");
        }
    }

    private void validarPrecio(Double precio) {
        if (precio == null) {
            throw new DatoInvalidoException("El precio no puede ser nulo");
        }
        if (precio < 0) {
            throw new DatoInvalidoException("El precio no puede ser negativo");
        }
    }

    private void validarStock(Integer stock) {
        if (stock == null) {
            throw new StockInvalidoException("El stock no puede ser nulo");
        }
        if (stock < 0) {
            throw new StockInvalidoException("El stock no puede ser negativo");
        }
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new DatoInvalidoException("El producto debe tener una categoría");
        }
        if (categoria.isEliminado()) {
            throw new DatoInvalidoException("La categoría seleccionada no está disponible");
        }
    }

    private void validarNombreSoloLetras(String nombre) {
        if (!nombre.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new DatoInvalidoException("El nombre solo puede contener letras y espacios");
        }
    }

    private void validarPrecioMaximo(Double precio) {
        if (precio > 999999) {
            throw new DatoInvalidoException("El precio no puede superar $999.999");
        }
    }

    private void validarStockMaximo(Integer stock) {
        if (stock > 9999) {
            throw new StockInvalidoException("El stock no puede superar 9.999 unidades");
        }
    }
}