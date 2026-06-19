package integrador.prog2.dao.Impl;

import integrador.prog2.config.ConexionBD;
import integrador.prog2.dao.PedidoDAO;
import integrador.prog2.entities.DetallePedido;
import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAOImpl implements PedidoDAO {

    @Override
    public Pedido crear(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedido (fecha, estado, total, forma_pago, usuario_id) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_pedido (cantidad, subtotal, pedido_id, producto_id) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = ConexionBD.getConnection();
            con.setAutoCommit(false); // Iniciamos la transacción

            // 1. Insertar el pedido
            PreparedStatement psPedido = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS);
            psPedido.setDate(1, Date.valueOf(pedido.getFecha()));
            psPedido.setString(2, pedido.getEstado().name());
            psPedido.setDouble(3, pedido.getTotal());
            psPedido.setString(4, pedido.getFormaPago().name());
            psPedido.setLong(5, pedido.getUsuario().getId());
            psPedido.executeUpdate();

            ResultSet rs = psPedido.getGeneratedKeys();
            if (rs.next()) {
                pedido.setId(rs.getLong(1));
            }

            // 2. Insertar cada detalle
            for (DetallePedido detalle : pedido.getDetalles()) {
                PreparedStatement psDetalle = con.prepareStatement(sqlDetalle, Statement.RETURN_GENERATED_KEYS);
                psDetalle.setInt(1, detalle.getCantidad());
                psDetalle.setDouble(2, detalle.getSubtotal());
                psDetalle.setLong(3, pedido.getId());
                psDetalle.setLong(4, detalle.getProducto().getId());
                psDetalle.executeUpdate();

                ResultSet rsDetalle = psDetalle.getGeneratedKeys();
                if (rsDetalle.next()) {
                    detalle.setId(rsDetalle.getLong(1));
                }
            }

            con.commit(); // Todo salió bien, confirmamos
            return pedido;

        } catch (SQLException e) {
            // Si algo falla, revertimos todo
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error en rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error al crear pedido (rollback ejecutado): " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Pedido buscarPorId(Long id) {
        String sql = "SELECT p.*, u.nombre as u_nombre, u.apellido as u_apellido, " +
                "u.mail, u.celular, u.contrasena, u.rol " +
                "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                "WHERE p.id = ? AND p.eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró pedido con ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar pedido: " + e.getMessage());
        }
    }

    @Override
    public List<Pedido> listar() {
        String sql = "SELECT p.*, u.nombre as u_nombre, u.apellido as u_apellido, " +
                "u.mail, u.celular, u.contrasena, u.rol " +
                "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                "WHERE p.eliminado = false";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos: " + e.getMessage());
        }
    }

    @Override
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        String sql = "SELECT p.*, u.nombre as u_nombre, u.apellido as u_apellido, " +
                "u.mail, u.celular, u.contrasena, u.rol " +
                "FROM pedido p JOIN usuario u ON p.usuario_id = u.id " +
                "WHERE p.eliminado = false AND p.usuario_id = ?";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar pedidos por usuario: " + e.getMessage());
        }
    }

    @Override
    public Pedido editar(Pedido pedido) {
        String sql = "UPDATE pedido SET estado = ?, forma_pago = ?, total = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, pedido.getEstado().name());
            ps.setString(2, pedido.getFormaPago().name());
            ps.setDouble(3, pedido.getTotal());
            ps.setLong(4, pedido.getId());
            ps.executeUpdate();
            return pedido;
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar pedido: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) {
        String sqlPedido = "UPDATE pedido SET eliminado = true WHERE id = ?";
        String sqlDetalles = "UPDATE detalle_pedido SET eliminado = true WHERE pedido_id = ?";

        Connection con = null;
        try {
            con = ConexionBD.getConnection();
            con.setAutoCommit(false);

            PreparedStatement psPedido = con.prepareStatement(sqlPedido);
            psPedido.setLong(1, id);
            psPedido.executeUpdate();

            PreparedStatement psDetalles = con.prepareStatement(sqlDetalles);
            psDetalles.setLong(1, id);
            psDetalles.executeUpdate();

            con.commit();
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error en rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error al eliminar pedido: " + e.getMessage());
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }
    public void insertarDetalle(Long pedidoId, DetallePedido detalle) {
        String sql = "INSERT INTO detalle_pedido (cantidad, subtotal, pedido_id, producto_id) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, detalle.getCantidad());
            ps.setDouble(2, detalle.getSubtotal());
            ps.setLong(3, pedidoId);
            ps.setLong(4, detalle.getProducto().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                detalle.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar detalle: " + e.getMessage());
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getLong("usuario_id"),
                rs.getString("u_nombre"),
                rs.getString("u_apellido"),
                rs.getString("mail"),
                rs.getString("celular"),
                rs.getString("contrasena"),
                Rol.valueOf(rs.getString("rol"))
        );
        Pedido pedido = new Pedido(
                rs.getLong("id"),
                FormaPago.valueOf(rs.getString("forma_pago")),
                usuario
        );
        pedido.setEstado(Estado.valueOf(rs.getString("estado")));
        pedido.setTotal(rs.getDouble("total"));
        pedido.setFecha(rs.getDate("fecha").toLocalDate());
        pedido.setEliminado(rs.getBoolean("eliminado"));
        return pedido;
    }
}
/* Lo más importante de este DAO:
con.setAutoCommit(false) → inicia la transacción
con.commit() → confirma todo si salió bien
con.rollback() → revierte todo si algo falló
Primero inserta el pedido, después cada detalle — si falla un detalle, se borra todo
*/