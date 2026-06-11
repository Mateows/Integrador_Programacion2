package integrador.prog2.service;

import integrador.prog2.entities.Categoria;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.util.ArrayList;
import java.util.List;

public class CategoriaService {

    private List<Categoria> categorias;
    private Long contadorId;

    public CategoriaService(){
        this.categorias = new ArrayList<>();
        this.contadorId = 1L;
    }

    //Lista solo las eliminadas

    public List<Categoria> listar() {
        List<Categoria> activas = new ArrayList<>();
        for (Categoria c : categorias) {
            if (!c.isEliminado()) {
                activas.add(c);
            }
        }
        return activas;
    }

    //Busca por ID

    public Categoria buscarPorId(Long id){
        for(Categoria c : categorias){
            if(c.getId().equals(id) && !c.isEliminado()){
                return c;
            }
        }
        throw new EntidadNoEncontradaException("No se encontró categoría con ID: " + id);
    }

    //Crear

    public Categoria crear(String nombre, String descripcion) {
        validarNombre(nombre);
        validarDescripcion(descripcion);
        validarNombreUnico(nombre, null);

        Categoria nueva = new Categoria(contadorId++, nombre.trim(), descripcion.trim());
        categorias.add(nueva);
        return nueva;
    }

    //Editar

    public Categoria editar(Long id, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            validarNombreUnico(nombre, id);
            categoria.setNombre(nombre.trim());
        }
        if (descripcion != null && !descripcion.isBlank()) {
            categoria.setDescripcion(descripcion.trim());
        }
        return categoria;
    }

    //Eliminar

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        if (!categoria.getProductos().isEmpty()) {
            throw new DatoInvalidoException(
                    "No se puede eliminar la categoría porque tiene productos asociados"
            );
        }
        categoria.setEliminado(true);
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

    private void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new DatoInvalidoException("La descripción no puede estar vacía");
        }
    }

    private void validarNombreUnico(String nombre, Long idExcluir) {
        for (Categoria c : categorias) {
            if (!c.isEliminado() &&
                    c.getNombre().equalsIgnoreCase(nombre.trim()) &&
                    !c.getId().equals(idExcluir)) {
                throw new DatoInvalidoException("Ya existe una categoría con el nombre: " + nombre);
            }
        }
    }

    public void validarNombreUnico(String nombre) {
        validarNombreUnico(nombre, null);
    }
}
