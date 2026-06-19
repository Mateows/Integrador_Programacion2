package integrador.prog2.service;

import integrador.prog2.dao.UsuarioDAO;
import integrador.prog2.dao.Impl.UsuarioDAOImpl;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EmailDuplicadoException;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario crear(String nombre, String apellido, String mail,
                         String celular, String contrasena, Rol rol) {
        validarNombre(nombre);
        validarNombreSoloLetras(nombre);
        validarApellido(apellido);
        validarApellidoSoloLetras(apellido);
        validarMail(mail);
        validarMailDominio(mail);
        validarMailUnico(mail, null);
        validarCelular(celular);
        validarCelularLongitud(celular);
        validarContrasena(contrasena);

        Usuario nuevo = new Usuario(null, nombre.trim(), apellido.trim(),
                mail.trim().toLowerCase(), celular.trim(),
                contrasena, rol);
        return usuarioDAO.crear(nuevo);
    }

    public Usuario editar(Long id, String nombre, String apellido,
                          String mail, String celular) {
        Usuario usuario = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            validarNombre(nombre);
            validarNombreSoloLetras(nombre);
            usuario.setNombre(nombre.trim());
        }
        if (apellido != null && !apellido.isBlank()) {
            validarApellido(apellido);
            validarApellidoSoloLetras(apellido);
            usuario.setApellido(apellido.trim());
        }
        if (mail != null && !mail.isBlank()) {
            validarMail(mail);
            validarMailDominio(mail);
            validarMailUnico(mail, id);
            usuario.setMail(mail.trim().toLowerCase());
        }
        if (celular != null && !celular.isBlank()) {
            validarCelular(celular);
            validarCelularLongitud(celular);
            usuario.setCelular(celular.trim());
        }
        return usuarioDAO.editar(usuario);
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioDAO.eliminar(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre no puede estar vacío");
        }
        if (nombre.trim().length() < 2) {
            throw new DatoInvalidoException("El nombre debe tener al menos 2 caracteres");
        }
    }

    private void validarApellido(String apellido) {
        if (apellido == null || apellido.isBlank()) {
            throw new DatoInvalidoException("El apellido no puede estar vacío");
        }
        if (apellido.trim().length() < 2) {
            throw new DatoInvalidoException("El apellido debe tener al menos 2 caracteres");
        }
    }

    private void validarMail(String mail) {
        if (mail == null || mail.isBlank()) {
            throw new DatoInvalidoException("El mail no puede estar vacío");
        }
        if (!mail.contains("@") || !mail.contains(".")) {
            throw new DatoInvalidoException("El mail no tiene un formato válido");
        }
    }

    private void validarMailUnico(String mail, Long idExcluir) {
        if (usuarioDAO.existeMail(mail.trim(), idExcluir)) {
            throw new EmailDuplicadoException("Ya existe un usuario con el mail: " + mail);
        }
    }

    private void validarCelular(String celular) {
        if (celular == null || celular.isBlank()) {
            throw new DatoInvalidoException("El celular no puede estar vacío");
        }
        if (!celular.trim().matches("\\d+")) {
            throw new DatoInvalidoException("El celular solo puede contener números");
        }
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new DatoInvalidoException("La contraseña no puede estar vacía");
        }
        if (contrasena.length() < 6) {
            throw new DatoInvalidoException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    public void validarMailUnico(String mail) {
        validarMailUnico(mail, null);
    }
    private void validarNombreSoloLetras(String nombre) {
        if (!nombre.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new DatoInvalidoException("El nombre solo puede contener letras y espacios");
        }
    }

    private void validarApellidoSoloLetras(String apellido) {
        if (!apellido.trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
            throw new DatoInvalidoException("El apellido solo puede contener letras y espacios");
        }
    }

    private void validarCelularLongitud(String celular) {
        if (celular.trim().length() < 8) {
            throw new DatoInvalidoException("El celular debe tener al menos 8 dígitos");
        }
    }

    private void validarMailDominio(String mail) {
        String[] partes = mail.split("@");
        if (partes.length != 2 || !partes[1].contains(".") || partes[1].endsWith(".")) {
            throw new DatoInvalidoException("El mail no tiene un dominio válido");
        }
        String dominio = partes[1].split("\\.")[1];
        if (dominio.length() < 2) {
            throw new DatoInvalidoException("El dominio del mail no es válido");
        }
    }
}