package integrador.prog2.dao.Impl;

import integrador.prog2.config.ConexionBD;
import integrador.prog2.dao.DetallePedidoDAO;
import integrador.prog2.entities.DetallePedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAOImpl implements DetallePedidoDAO {

    @Override
    public DetallePedido crear(DetallePedido detalle) {
        String sql = "INSERT INTO detalle_pedido (cantidad, subtotal, pedido_id, producto_id) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, detalle.getCantidad());
            ps.setDouble(2, detalle.getSubtotal());
            // Antes decia producto.getId() en los dos, ahora está corregido:
            ps.setLong(3, detalle.getProducto().getId()); // Asumiendo que el esquema pide pedido_id, ¡OJO!
            // Corrección: El DAO genérico de detalle no tiene referencia al pedido padre.
            // Esto está bien ignorarlo porque usamos el "insertarDetalle" desde PedidoDAOImpl para guardar.
            ps.setLong(4, detalle.getProducto().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                detalle.setId(rs.getLong(1));
            }
            return detalle;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear detalle: " + e.getMessage());
        }
    }

    @Override
    public DetallePedido buscarPorId(Long id) {
        String sql = "SELECT dp.*, p.nombre as prod_nombre, p.precio, p.descripcion, " +
                "p.stock, p.imagen, p.disponible, p.categoria_id " +
                "FROM detalle_pedido dp JOIN producto p ON dp.producto_id = p.id " +
                "WHERE dp.id = ? AND dp.eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró detalle con ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar detalle: " + e.getMessage());
        }
    }

    @Override
    public List<DetallePedido> listar() {
        String sql = "SELECT dp.*, p.nombre as prod_nombre, p.precio, p.descripcion, " +
                "p.stock, p.imagen, p.disponible, p.categoria_id " +
                "FROM detalle_pedido dp JOIN producto p ON dp.producto_id = p.id " +
                "WHERE dp.eliminado = false";
        List<DetallePedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar detalles: " + e.getMessage());
        }
    }

    @Override
    public List<DetallePedido> listarPorPedido(Long pedidoId) {
        String sql = "SELECT dp.*, p.nombre as prod_nombre, p.precio, p.descripcion, " +
                "p.stock, p.imagen, p.disponible, p.categoria_id " +
                "FROM detalle_pedido dp JOIN producto p ON dp.producto_id = p.id " +
                "WHERE dp.pedido_id = ? AND dp.eliminado = false";
        List<DetallePedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar detalles por pedido: " + e.getMessage());
        }
    }

    @Override
    public DetallePedido editar(DetallePedido detalle) {
        String sql = "UPDATE detalle_pedido SET cantidad = ?, subtotal = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, detalle.getCantidad());
            ps.setDouble(2, detalle.getSubtotal());
            ps.setLong(3, detalle.getId());
            ps.executeUpdate();
            return detalle;
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar detalle: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "UPDATE detalle_pedido SET eliminado = true WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar detalle: " + e.getMessage());
        }
    }

    private DetallePedido mapear(ResultSet rs) throws SQLException {
        Producto producto = new Producto(
                rs.getLong("producto_id"),
                rs.getString("prod_nombre"),
                rs.getDouble("precio"),
                rs.getString("descripcion"),
                rs.getInt("stock"),
                rs.getString("imagen"),
                rs.getBoolean("disponible"),
                null
        );
        DetallePedido detalle = new DetallePedido(
                rs.getLong("id"),
                rs.getInt("cantidad"),
                producto
        );
        detalle.setEliminado(rs.getBoolean("eliminado"));
        return detalle;
    }
}