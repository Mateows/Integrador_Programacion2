package integrador.prog2.menu;

import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Rol;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EmailDuplicadoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class UsuarioMenu {

    private final Scanner scanner;
    private final UsuarioService usuarioService;

    public UsuarioMenu(Scanner scanner, UsuarioService usuarioService) {
        this.scanner = scanner;
        this.usuarioService = usuarioService;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== USUARIOS ===");
            System.out.println("1. Listar");
            System.out.println("2. Crear");
            System.out.println("3. Editar");
            System.out.println("4. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Seleccione: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listar();
                case 2 -> crear();
                case 3 -> editar();
                case 4 -> eliminar();
                case 0 -> System.out.println("Volviendo...");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void listar() {
        List<Usuario> lista = usuarioService.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay usuarios cargados.");
            return;
        }
        System.out.println("\n--- LISTADO DE USUARIOS ---");
        for (Usuario u : lista) {
            System.out.println(u);
        }
    }

    private void crear() {
        System.out.println("\n--- CREAR USUARIO ---");
        System.out.println("(Ingrese 0 en cualquier campo para cancelar)");

        String nombre = leerTextoValido("Nombre", 2);
        if (nombre.equals("0")) { System.out.println("Operación cancelada."); return; }

        String apellido = leerTextoValido("Apellido", 2);
        if (apellido.equals("0")) { System.out.println("Operación cancelada."); return; }

        String mail = leerMailValido();
        if (mail.equals("0")) { System.out.println("Operación cancelada."); return; }

        String celular = leerCelularValido();
        if (celular.equals("0")) { System.out.println("Operación cancelada."); return; }

        String contrasena = leerContrasenaValida();
        if (contrasena.equals("0")) { System.out.println("Operación cancelada."); return; }

        System.out.println("Rol: 1. ADMIN  2. USUARIO");
        System.out.print("Seleccione: ");
        int rolOpcion = leerEntero();
        Rol rol = rolOpcion == 1 ? Rol.ADMIN : Rol.USUARIO;

        Usuario nuevo = usuarioService.crear(nombre, apellido, mail, celular, contrasena, rol);
        System.out.println("✓ Usuario creado con ID: " + nuevo.getId());
    }

    private void editar() {
        System.out.println("\n--- EDITAR USUARIO ---");
        listar();
        if (usuarioService.listar().isEmpty()) return;
        try {
            System.out.print("Ingrese ID a editar (0 para cancelar): ");
            Long id = (long) leerEntero();

            if (id == 0) {
                System.out.println("Operación cancelada.");
                return;
            }

            System.out.print("Nuevo nombre (Enter para mantener): ");
            String nombre = scanner.nextLine();

            System.out.print("Nuevo apellido (Enter para mantener): ");
            String apellido = scanner.nextLine();

            System.out.print("Nuevo mail (Enter para mantener): ");
            String mail = scanner.nextLine();

            System.out.print("Nuevo celular (Enter para mantener): ");
            String celular = scanner.nextLine();

            usuarioService.editar(id, nombre, apellido, mail, celular);
            System.out.println("✓ Usuario actualizado correctamente.");
        } catch (EntidadNoEncontradaException | DatoInvalidoException | EmailDuplicadoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void eliminar() {
        System.out.println("\n--- ELIMINAR USUARIO ---");
        listar();
        try {
            System.out.print("Ingrese ID a eliminar: ");
            Long id = (long) leerEntero();

            System.out.print("¿Está seguro? (S/N): ");
            String confirmacion = scanner.nextLine().trim();

            if (confirmacion.equalsIgnoreCase("S")) {
                usuarioService.eliminar(id);
                System.out.println("✓ Usuario eliminado correctamente.");
            } else {
                System.out.println("Operación cancelada.");
            }
        } catch (EntidadNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private int leerEntero() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Ingrese un número válido: ");
            }
        }
    }

    private String leerTextoValido(String campo, int minCaracteres) {
        while (true) {
            System.out.print(campo + " (0 para cancelar): ");
            String valor = scanner.nextLine().trim();
            if (valor.equals("0")) return "0";
            if (valor.isBlank()) {
                System.out.println("✗ El " + campo.toLowerCase() + " no puede estar vacío.");
            } else if (valor.length() < minCaracteres) {
                System.out.println("✗ El " + campo.toLowerCase() +
                        " debe tener al menos " + minCaracteres + " caracteres.");
            } else {
                return valor;
            }
        }
    }

    private String leerMailValido() {
        while (true) {
            System.out.print("Mail (0 para cancelar): ");
            String mail = scanner.nextLine().trim();
            if (mail.equals("0")) return "0";
            if (mail.isBlank()) {
                System.out.println("✗ El mail no puede estar vacío.");
            } else if (!mail.contains("@") || !mail.contains(".")) {
                System.out.println("✗ El mail no tiene un formato válido (debe contener @ y .)");
            } else {
                try {
                    usuarioService.validarMailUnico(mail);
                    return mail;
                } catch (EmailDuplicadoException e) {
                    System.out.println("✗ " + e.getMessage());
                }
            }
        }
    }

    private String leerCelularValido() {
        while (true) {
            System.out.print("Celular (solo números, 0 para cancelar): ");
            String celular = scanner.nextLine().trim();
            if (celular.equals("0")) return "0";
            if (celular.isBlank()) {
                System.out.println("✗ El celular no puede estar vacío.");
            } else if (!celular.matches("\\d+")) {
                System.out.println("✗ El celular solo puede contener números.");
            } else {
                return celular;
            }
        }
    }

    private String leerContrasenaValida() {
        while (true) {
            System.out.print("Contraseña (mínimo 6 caracteres, 0 para cancelar): ");
            String contrasena = scanner.nextLine();
            if (contrasena.trim().equals("0")) return "0";
            if (contrasena.isBlank()) {
                System.out.println("✗ La contraseña no puede estar vacía.");
            } else if (contrasena.length() < 6) {
                System.out.println("✗ La contraseña debe tener al menos 6 caracteres.");
            } else {
                return contrasena;
            }
        }
    }
}
