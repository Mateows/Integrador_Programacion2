package integrador.prog2.menu;

import integrador.prog2.entities.Categoria;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.service.CategoriaService;

import java.util.List;
import java.util.Scanner;

public class CategoriaMenu {

    private final Scanner scanner;
    private final CategoriaService categoriaService;

    public CategoriaMenu(Scanner scanner, CategoriaService categoriaService) {
        this.scanner = scanner;
        this.categoriaService = categoriaService;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== CATEGORÍAS ===");
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
        List<Categoria> lista = categoriaService.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay categorías cargadas.");
            return;
        }
        System.out.println("\n--- LISTADO DE CATEGORÍAS ---");
        for (Categoria c : lista) {
            System.out.println(c);
        }
    }

    private void crear() {
        System.out.println("\n--- CREAR CATEGORÍA ---");
        System.out.println("(Ingrese 0 en cualquier campo para cancelar)");

        String nombre = leerNombreValido();
        if (nombre.equals("0")) {
            System.out.println("Operación cancelada.");
            return;
        }

        String descripcion = leerTextoValido("Descripción", 1);
        if (descripcion.equals("0")) {
            System.out.println("Operación cancelada.");
            return;
        }

        Categoria nueva = categoriaService.crear(nombre, descripcion);
        System.out.println("✓ Categoría creada con ID: " + nueva.getId());
    }

    private void editar() {
        System.out.println("\n--- EDITAR CATEGORÍA ---");
        listar();
        try {
            System.out.print("Ingrese ID a editar (0 para cancelar): ");
            Long id = (long) leerEntero();
            if (id == 0) {
                System.out.println("Operación cancelada.");
                return;
            }

            System.out.print("Nuevo nombre (Enter para mantener): ");
            String nombre = scanner.nextLine();

            System.out.print("Nueva descripción (Enter para mantener): ");
            String descripcion = scanner.nextLine();

            categoriaService.editar(id, nombre, descripcion);
            System.out.println("✓ Categoría actualizada correctamente.");
        } catch (EntidadNoEncontradaException | DatoInvalidoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void eliminar() {
        System.out.println("\n--- ELIMINAR CATEGORÍA ---");
        listar();
        try {
            System.out.print("Ingrese ID a eliminar: ");
            Long id = (long) leerEntero();

            System.out.print("¿Está seguro? (S/N): ");
            String confirmacion = scanner.nextLine().trim();

            if (confirmacion.equalsIgnoreCase("S")) {
                categoriaService.eliminar(id);
                System.out.println("✓ Categoría eliminada correctamente.");
            } else {
                System.out.println("Operación cancelada.");
            }
        } catch (EntidadNoEncontradaException | DatoInvalidoException e) {
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

    private String leerNombreValido() {
        while (true) {
            System.out.print("Nombre (0 para cancelar): ");
            String nombre = scanner.nextLine().trim();
            if (nombre.equals("0")) return "0";
            if (nombre.isBlank()) {
                System.out.println("✗ El nombre no puede estar vacío.");
            } else if (nombre.length() < 2) {
                System.out.println("✗ El nombre debe tener al menos 2 caracteres.");
            } else {
                try {
                    categoriaService.validarNombreUnico(nombre);
                    return nombre;
                } catch (DatoInvalidoException e) {
                    System.out.println("✗ " + e.getMessage());
                }
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
}
