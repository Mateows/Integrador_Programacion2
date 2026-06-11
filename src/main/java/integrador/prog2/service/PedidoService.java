package integrador.prog2.service;

import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.util.ArrayList;
import java.util.List;

public class PedidoService {

    private List<Pedido> pedidos;
    private Long contadorId;

    public PedidoService() {
        this.pedidos = new ArrayList<>();
        this.contadorId = 1L;
    }

    // Lista solo los no eliminados
    public List<Pedido> listar() {
        List<Pedido> activos = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (!p.isEliminado()) {
                activos.add(p);
            }
        }
        return activos;
    }

    // Lista por usuario
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        List<Pedido> resultado = new ArrayList<>();
        for (Pedido p : pedidos) {
            if (!p.isEliminado() && p.getUsuario().getId().equals(usuarioId)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // Busca por ID

    public Pedido buscarPorId(Long id) {
        for (Pedido p : pedidos) {
            if (p.getId().equals(id) && !p.isEliminado()) {
                return p;
            }
        }
        throw new EntidadNoEncontradaException("No se encontró pedido con el ID: " + id);
    }

    // Crear

    public Pedido crear(Usuario usuario, FormaPago formaPago) {
        validarUsuario(usuario);
        validarFormaPago(formaPago);

        Pedido nuevo = new Pedido(contadorId++, formaPago, usuario);
        pedidos.add(nuevo);
        usuario.getPedidos().add(nuevo);
        return nuevo;
    }

    // Agregar detalle al pedido

    public void agregarDetalle(Pedido pedido, Producto producto, Integer cantidad) {
        validarProducto(producto);
        if (cantidad == null || cantidad <= 0) {
            throw new DatoInvalidoException("La cantidad debe ser mayor a 0");
        }
        if (producto.getStock() < cantidad) {
            throw new DatoInvalidoException("Stock insuficiente. Stock disponible: "
                    + producto.getStock());
        }
        try {
            pedido.addDetallePedido(cantidad, producto.getPrecio(), producto);
        } catch (Exception e) { // Si falla, el pedido se queda sin ese detalle (no hay inconsistencia)
            throw new DatoInvalidoException("Error al agregar detalle: " + e.getMessage());
        }
    }

    // Actualizar el estado y la forma de pago
    public Pedido actualizar(Long id, Estado estado, FormaPago formaPago) {
        Pedido pedido = buscarPorId(id);

        if (estado != null) {
            validarCambioEstado(pedido.getEstado(), estado);
            pedido.setEstado(estado);
        }
        if (formaPago != null) {
            pedido.setFormaPago(formaPago);
        }
        return pedido;
    }

    // Eliminar
    public void eliminar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedido.setEliminado(true);
        // Eliminar también los detalles
        pedido.getDetalles().forEach(d -> d.setEliminado(true));
    }

    // Validaciones privadas

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new DatoInvalidoException("El pedido debe tener un usuario");
        }
        if (usuario.isEliminado()) {
            throw new DatoInvalidoException("El usuario seleccionado no está disponible");
        }
    }

    private void validarFormaPago(FormaPago formaPago) {
        if (formaPago == null) {
            throw new DatoInvalidoException("Debe seleccionar una forma de pago");
        }
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new DatoInvalidoException("El producto no puede ser nulo");
        }
        if (producto.isEliminado()) {
            throw new DatoInvalidoException("El producto seleccionado no está disponible");
        }
        if (!producto.getDisponible()) {
            throw new DatoInvalidoException("El producto no está disponible para la venta");
        }
    }

    private void validarCambioEstado(Estado estadoActual, Estado estadoNuevo) {
        if (estadoActual == Estado.CANCELADO) {
            throw new DatoInvalidoException("No se puede modificar un pedido cancelado");
        }
        if (estadoActual == Estado.TERMINADO) {
            throw new DatoInvalidoException("No se puede modificar un pedido terminado");
        }
    }
}
