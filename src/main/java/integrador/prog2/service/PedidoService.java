package integrador.prog2.service;

import integrador.prog2.dao.PedidoDAO;
import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.dao.Impl.PedidoDAOImpl;
import integrador.prog2.dao.Impl.ProductoDAOImpl;
import integrador.prog2.entities.DetallePedido;
import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.DatoInvalidoException;

import java.util.List;

/**
 * Servicio encargado de gestionar la lógica de negocio de los Pedidos.
 * Implementa validaciones de stock, coherencia de datos y actualización de inventario.
 */
public class PedidoService {

    private final PedidoDAO pedidoDAO;
    private final ProductoDAO productoDAO; // Necesitamos este DAO para actualizar el stock

    public PedidoService() {
        this.pedidoDAO = new PedidoDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
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

    /**
     * Procesa un pedido completo. Valida que el usuario sea válido, que no tenga pedidos
     * pendientes, verifica el stock de cada producto y finalmente descuenta el inventario.
     * * @param pedido El pedido armado en memoria con todos sus detalles.
     * @return El pedido guardado en la base de datos con su ID generado.
     */
    public Pedido procesarPedidoCompleto(Pedido pedido) {
        validarUsuario(pedido.getUsuario());
        validarFormaPago(pedido.getFormaPago());
        validarPedidoPendiente(pedido.getUsuario());
        validarPedidoConDetalles(pedido);

        // 1. Validamos que haya stock de TODO antes de intentar guardar algo
        for (DetallePedido dp : pedido.getDetalles()) {
            Producto p = dp.getProducto();
            if (p.getStock() < dp.getCantidad()) {
                throw new DatoInvalidoException("Stock insuficiente para el producto: " + p.getNombre() + ". Quedan " + p.getStock() + " unidades.");
            }
        }

        // 2. Guardamos el pedido y sus detalles en MySQL (Acá actúa la transacción del DAO)
        Pedido pedidoGuardado = pedidoDAO.crear(pedido);

        // 3. Si todo salió bien, descontamos el stock de los productos
        for (DetallePedido dp : pedido.getDetalles()) {
            Producto p = dp.getProducto();
            p.setStock(p.getStock() - dp.getCantidad());
            productoDAO.editar(p); // Actualizamos el stock en la base de datos
        }

        return pedidoGuardado;
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

    // --- MÉTODOS DE VALIDACIÓN INTERNA ---

    private void validarUsuario(Usuario usuario) {
        if (usuario == null) throw new DatoInvalidoException("El pedido debe tener un usuario");
        if (usuario.isEliminado()) throw new DatoInvalidoException("El usuario seleccionado no está disponible");
    }

    private void validarFormaPago(FormaPago formaPago) {
        if (formaPago == null) throw new DatoInvalidoException("Debe seleccionar una forma de pago");
    }

    private void validarCambioEstado(Estado estadoActual, Estado estadoNuevo) {
        if (estadoActual == Estado.CANCELADO) throw new DatoInvalidoException("No se puede modificar un pedido cancelado");
        if (estadoActual == Estado.TERMINADO) throw new DatoInvalidoException("No se puede modificar un pedido terminado");
    }

    private void validarPedidoPendiente(Usuario usuario) {
        List<Pedido> pedidos = pedidoDAO.listarPorUsuario(usuario.getId());
        for (Pedido p : pedidos) {
            if (p.getEstado() == Estado.PENDIENTE) {
                throw new DatoInvalidoException("El usuario ya tiene un pedido PENDIENTE (ID: " + p.getId() + "). Confirmalo o cancelalo primero.");
            }
        }
    }

    private void validarPedidoConDetalles(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            throw new DatoInvalidoException("El pedido debe tener al menos un producto agregado.");
        }
    }
}