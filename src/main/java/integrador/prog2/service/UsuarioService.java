package integrador.prog2.service;

import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EmailDuplicadoException;
import integrador.prog2.exception.EntidadNoEncontradaException;

import java.util.ArrayList;
import java.util.List;

public class UsuarioService {

    private List<Usuario> usuarios;
    private Long contadorId;

    public UsuarioService() {
        this.usuarios = new ArrayList<>();
        this.contadorId = 1L;
    }

    //Lista los no eliminados

    public List<Usuario> listar() {
        List<Usuario> activos = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (!u.isEliminado()) {
                activos.add(u);
            }
        }
        return activos;
    }

    // Busca por ID
    public Usuario buscarPorId(Long id) {
        for (Usuario u : usuarios) {
            if (u.getId().equals(id) && !u.isEliminado()) {
                return u;
            }
        }
        throw new EntidadNoEncontradaException("No se encontró usuario con el ID: " + id);
    }

    // Crear
    public Usuario crear(String nombre, String apellido, String mail, String celular, String contrasena, Rol rol) {

        validarNombre(nombre);
        validarApellido(apellido);
        validarMail(mail);
        validarMailUnico(mail, null);
        validarCelular(celular);
        validarContrasena(contrasena);

        Usuario nuevo = new Usuario(contadorId++, nombre.trim(), apellido.trim(), mail.trim().toLowerCase(), celular.trim(), contrasena, rol);
        usuarios.add(nuevo);
        return nuevo;
    }

    // Editar
    public Usuario editar(Long id, String nombre, String apellido, String mail, String celular) {

        Usuario usuario = buscarPorId(id);

        if (nombre != null && !nombre.isBlank()) {
            validarNombre(nombre);
            usuario.setNombre(nombre.trim());
        }
        if (apellido != null && !apellido.isBlank()) {
            validarApellido(apellido);
            usuario.setApellido(apellido.trim());
        }
        if (mail != null && !mail.isBlank()) {
            validarMail(mail);
            validarMailUnico(mail, id);
            usuario.setMail(mail.trim().toLowerCase());
        }
        if (celular != null && !celular.isBlank()) {
            validarCelular(celular);
            usuario.setCelular(celular.trim());
        }
        return usuario;
    }

    // Eliminar
    public void eliminar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEliminado(true);
    }

    // Validaciones privadas
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
        for (Usuario u : usuarios) {
            if (!u.isEliminado() &&
                    u.getMail().equalsIgnoreCase(mail.trim()) &&
                    !u.getId().equals(idExcluir)) {
                throw new EmailDuplicadoException("Ya existe un usuario con el mail: " + mail);
            }
        }
    }

    private void validarCelular(String celular) {
        if (celular == null || celular.isBlank()) {
            throw new DatoInvalidoException("El celular no puede estar vacío");
        }
        if (!celular.trim().matches("\\d+")) { //trim -> Elimina los espacios en blanco al principio y al final de un String
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
}
//trim -> Elimina los espacios en blanco al principio y al final de un String
//matches("\\d+")
//Verifica que el String cumpla un patrón. En este caso \\d+ significa "solo dígitos, al menos uno".
//El \\d significa "cualquier dígito del 0 al 9",
// y el + significa "uno o más". Eso se llama expresión regular (regex) — es un lenguaje para describir patrones de texto.