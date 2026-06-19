package integrador.prog2.service;

import integrador.prog2.dao.PedidoDAO;
import integrador.prog2.dao.Impl.PedidoDAOImpl;
import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.entities.DetallePedido;

import java.util.List;

public class PedidoService {

    private final PedidoDAO pedidoDAO;

    public PedidoService() {
        this.pedidoDAO = new PedidoDAOImpl();
    }

    public List<Pedido> listar() {
        return pedidoDAO.listar();
    }

    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoDAO.listarPorUsuario(usuarioId);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoDAO.buscarPorId(id);
    }

    public Pedido crear(Usuario usuario, FormaPago formaPago) {
        validarUsuario(usuario);
        validarFormaPago(formaPago);
        validarPedidoPendiente(usuario);

        Pedido nuevo = new Pedido(null, formaPago, usuario);
        usuario.getPedidos().add(nuevo);
        return pedidoDAO.crear(nuevo);
    }

    public void agregarDetalle(Pedido pedido, Producto producto, Integer cantidad) {
        validarProducto(producto);
        validarProductoNoDuplicado(pedido, producto);
        if (cantidad == null || cantidad <= 0) {
            throw new DatoInvalidoException("La cantidad debe ser mayor a 0");
        }
        if (producto.getStock() < cantidad) {
            throw new DatoInvalidoException("Stock insuficiente. Stock disponible: "
                    + producto.getStock());
        }
        try {
            pedido.addDetallePedido(cantidad, producto.getPrecio(), producto);
            DetallePedido ultimoDetalle = pedido.getDetalles().get(pedido.getDetalles().size() - 1);
            ((PedidoDAOImpl) pedidoDAO).insertarDetalle(pedido.getId(), ultimoDetalle);
            pedidoDAO.editar(pedido);
        } catch (Exception e) {
            throw new DatoInvalidoException("Error al agregar detalle: " + e.getMessage());
        }
    }

    public Pedido actualizar(Long id, Estado estado, FormaPago formaPago) {
        Pedido pedido = buscarPorId(id);

        if (estado != null) {
            validarCambioEstado(pedido.getEstado(), estado);
            pedido.setEstado(estado);
        }
        if (formaPago != null) {
            pedido.setFormaPago(formaPago);
        }
        return pedidoDAO.editar(pedido);
    }

    public void eliminar(Long id) {
        buscarPorId(id);
        pedidoDAO.eliminar(id);
    }

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
    private void validarPedidoPendiente(Usuario usuario) {
        List<Pedido> pedidos = pedidoDAO.listarPorUsuario(usuario.getId());
        for (Pedido p : pedidos) {
            if (p.getEstado() == Estado.PENDIENTE) {
                throw new DatoInvalidoException(
                        "El usuario ya tiene un pedido PENDIENTE (ID: " + p.getId() + "). Confirmalo o cancelalo primero."
                );
            }
        }
    }

    private void validarProductoNoDuplicado(Pedido pedido, Producto producto) {
        if (pedido.findDetallePedidoByProducto(producto) != null) {
            throw new DatoInvalidoException(
                    "El producto '" + producto.getNombre() + "' ya está en el pedido"
            );
        }
    }

    private void validarPedidoConDetalles(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            throw new DatoInvalidoException("El pedido debe tener al menos un producto");
        }
    }
}