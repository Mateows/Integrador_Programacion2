package integrador.prog2.dao.Impl;

import integrador.prog2.config.ConexionBD;
import integrador.prog2.dao.ProductoDAO;
import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    @Override
    public Producto crear(Producto producto) {
        String sql = "INSERT INTO producto (nombre, precio, descripcion, stock, imagen, disponible, categoria_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, producto.getDisponible());
            ps.setLong(7, producto.getCategoria().getId());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                producto.setId(rs.getLong(1));
            }
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear producto: " + e.getMessage());
        }
    }

    @Override
    public Producto buscarPorId(Long id) {
        String sql = "SELECT p.*, c.nombre as cat_nombre, c.descripcion as cat_descripcion " +
                "FROM producto p JOIN categoria c ON p.categoria_id = c.id " +
                "WHERE p.id = ? AND p.eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró producto con ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> listar() {
        String sql = "SELECT p.*, c.nombre as cat_nombre, c.descripcion as cat_descripcion " +
                "FROM producto p JOIN categoria c ON p.categoria_id = c.id " +
                "WHERE p.eliminado = false";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos: " + e.getMessage());
        }
    }

    @Override
    public List<Producto> listarPorCategoria(Long categoriaId) {
        String sql = "SELECT p.*, c.nombre as cat_nombre, c.descripcion as cat_descripcion " +
                "FROM producto p JOIN categoria c ON p.categoria_id = c.id " +
                "WHERE p.eliminado = false AND p.categoria_id = ?";
        List<Producto> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, categoriaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar productos por categoría: " + e.getMessage());
        }
    }

    @Override
    public Producto editar(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, descripcion = ?, " +
                "stock = ?, imagen = ?, disponible = ?, categoria_id = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setString(3, producto.getDescripcion());
            ps.setInt(4, producto.getStock());
            ps.setString(5, producto.getImagen());
            ps.setBoolean(6, producto.getDisponible());
            ps.setLong(7, producto.getCategoria().getId());
            ps.setLong(8, producto.getId());
            ps.executeUpdate();
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar producto: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "UPDATE producto SET eliminado = true WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage());
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getLong("categoria_id"),
                rs.getString("cat_nombre"),
                rs.getString("cat_descripcion")
        );
        Producto producto = new Producto(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getDouble("precio"),
                rs.getString("descripcion"),
                rs.getInt("stock"),
                rs.getString("imagen"),
                rs.getBoolean("disponible"),
                categoria
        );
        producto.setEliminado(rs.getBoolean("eliminado"));
        return producto;
    }
}
