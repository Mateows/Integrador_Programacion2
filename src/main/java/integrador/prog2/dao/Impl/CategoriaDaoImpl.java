package integrador.prog2.dao.Impl;

import integrador.prog2.config.ConexionBD;
import integrador.prog2.dao.CategoriaDAO;
import integrador.prog2.entities.Categoria;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDaoImpl implements CategoriaDAO {

    @Override
    public Categoria crear(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                categoria.setId(rs.getLong(1));
            }
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear categoría: " + e.getMessage());
        }
    }

    @Override
    public Categoria buscarPorId(Long id) {
        String sql = "SELECT * FROM categoria WHERE id = ? AND eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró categoría con ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar categoría: " + e.getMessage());
        }
    }

    @Override
    public List<Categoria> listar() {
        String sql = "SELECT * FROM categoria WHERE eliminado = false";
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar categorías: " + e.getMessage());
        }
    }

    @Override
    public Categoria editar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setLong(3, categoria.getId());
            ps.executeUpdate();
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar categoría: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "UPDATE categoria SET eliminado = true WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar categoría: " + e.getMessage());
        }
    }

    @Override
    public boolean existeNombre(String nombre, Long idExcluir) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ? AND eliminado = false AND id != ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setLong(2, idExcluir == null ? -1 : idExcluir);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar nombre: " + e.getMessage());
        }
    }

    // Mapea una fila del ResultSet a un objeto Categoria
    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("descripcion")
        );
        categoria.setEliminado(rs.getBoolean("eliminado"));
        return categoria;
    }
}

/*PreparedStatement → evita inyección SQL, los ? son parámetros seguros
ResultSet → el resultado de un SELECT, lo recorremos con while(rs.next())
mapear() → convierte una fila de la BD en un objeto Java
try-with-resources → cierra la conexión automáticamente al terminar
*/
