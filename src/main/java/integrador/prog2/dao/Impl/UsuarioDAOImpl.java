package integrador.prog2.dao.Impl;

import integrador.prog2.config.ConexionBD;
import integrador.prog2.dao.UsuarioDAO;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario crear(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, apellido, mail, celular, contrasena, rol) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getMail());
            ps.setString(4, usuario.getCelular());
            ps.setString(5, usuario.getContrasena());
            ps.setString(6, usuario.getRol().name());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getLong(1));
            }
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear usuario: " + e.getMessage());
        }
    }

    @Override
    public Usuario buscarPorId(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ? AND eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró usuario con ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario: " + e.getMessage());
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql = "SELECT * FROM usuario WHERE eliminado = false";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios: " + e.getMessage());
        }
    }

    @Override
    public Usuario editar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, apellido = ?, mail = ?, celular = ? WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellido());
            ps.setString(3, usuario.getMail());
            ps.setString(4, usuario.getCelular());
            ps.setLong(5, usuario.getId());
            ps.executeUpdate();
            return usuario;
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar usuario: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(Long id) {
        String sql = "UPDATE usuario SET eliminado = true WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage());
        }
    }

    @Override
    public boolean existeMail(String mail, Long idExcluir) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE mail = ? AND eliminado = false AND id != ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mail);
            ps.setLong(2, idExcluir == null ? -1 : idExcluir);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar mail: " + e.getMessage());
        }
    }

    @Override
    public Usuario buscarPorMail(String mail) {
        String sql = "SELECT * FROM usuario WHERE mail = ? AND eliminado = false";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, mail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapear(rs);
            }
            throw new EntidadNoEncontradaException("No se encontró usuario con mail: " + mail);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por mail: " + e.getMessage());
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario(
                rs.getLong("id"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("mail"),
                rs.getString("celular"),
                rs.getString("contrasena"),
                Rol.valueOf(rs.getString("rol"))
        );
        usuario.setEliminado(rs.getBoolean("eliminado"));
        return usuario;
    }
}
