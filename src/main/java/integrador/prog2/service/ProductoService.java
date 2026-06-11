package integrador.prog2.service;

import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.exception.StockInvalidoException;

import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    private List<Producto> productos;
    private Long contadorId;

    public ProductoService() {
        this.productos = new ArrayList<>();
        this.contadorId = 1L;
    }

    //Lista solo los que no se han eliminado

    public List<Producto> listar() {
        List<Producto> activos = new ArrayList<>();
        for (Producto p : productos) {
            if (!p.isEliminado()) {
                activos.add(p);
            }
        }
        return activos;
    }

    //Lista por categoria

    public List<Producto> listarPorCategoria(Long categoriaId) {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (!p.isEliminado() && p.getCategoria().getId().equals(categoriaId)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    //Busca por id

    public Producto buscarPorId(Long id) {
        for (Producto p : productos) {
            if (p.getId().equals(id) && !p.isEliminado()) {
                return p;
            }
        }
        throw new EntidadNoEncontradaException("No se encontró producto con el ID: " + id);
    }

    //para crear

    public Producto crear(String nombre, Double precio, String descripcion, Integer stock, String imagen, Boolean disponible, Categoria categoria) {
        validarNombre(nombre);
        validarPrecio(precio);
        validarStock(stock);
        validarCategoria(categoria);

        Producto nuevo = new Producto(contadorId++, nombre.trim(), precio,
                descripcion.trim(), stock, imagen, disponible, categoria);
        productos.add(nuevo);
        categoria.getProductos().add(nuevo);
        return nuevo;
    }

    //Para editar

    public Producto editar(Long id, String nombre, Double precio, String descripcion, Integer stock, Boolean disponible, Categoria categoria) {

        Producto producto = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            producto.setNombre(nombre.trim());
        }
        if (precio != null) {
            validarPrecio(precio);
            producto.setPrecio(precio);
        }
        if (descripcion != null && !descripcion.isBlank()) {
            producto.setDescripcion(descripcion.trim());
        }
        if (stock != null) {
            validarStock(stock);
            producto.setStock(stock);
        }
        if (disponible != null) {
            producto.setDisponible(disponible);
        }
        if (categoria != null) {
            validarCategoria(categoria);
            producto.getCategoria().getProductos().remove(producto);
            producto.setCategoria(categoria);
            categoria.getProductos().add(producto);
        }
        return producto;
    }

    // para eliminar

    public void eliminar(Long id){
        Producto producto = buscarPorId(id);
        producto.setEliminado(true);
    }

    //Validaciones privadas

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


}
