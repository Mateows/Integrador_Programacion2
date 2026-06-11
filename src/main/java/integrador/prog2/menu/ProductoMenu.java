package integrador.prog2.menu;

import integrador.prog2.entities.Categoria;
import integrador.prog2.entities.Producto;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.service.CategoriaService;
import integrador.prog2.service.ProductoService;

import java.util.List;
import java.util.Scanner;

public class ProductoMenu {

    private final Scanner scanner;
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public ProductoMenu(Scanner scanner, ProductoService productoService,
                        CategoriaService categoriaService) {
        this.scanner = scanner;
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== PRODUCTOS ===");
            System.out.println("1. Listar todos");
            System.out.println("2. Listar por categoría");
            System.out.println("3. Crear");
            System.out.println("4. Editar");
            System.out.println("5. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Seleccione: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listar();
                case 2 -> listarPorCategoria();
                case 3 -> crear();
                case 4 -> editar();
                case 5 -> eliminar();
                case 0 -> System.out.println("Volviendo...");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void listar() {
        List<Producto> lista = productoService.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay productos cargados.");
            return;
        }
        System.out.println("\n--- LISTADO DE PRODUCTOS ---");
        for (Producto p : lista) {
            System.out.println(p);
        }
    }

    private void listarPorCategoria() {
        List<Categoria> categorias = categoriaService.listar();
        if (categorias.isEmpty()) {
            System.out.println("No hay categorías cargadas.");
            return;
        }
        System.out.println("\n--- CATEGORÍAS ---");
        for (Categoria c : categorias) {
            System.out.println(c);
        }
        try {
            System.out.print("Ingrese ID de categoría: ");
            Long id = (long) leerEntero();
            List<Producto> lista = productoService.listarPorCategoria(id);
            if (lista.isEmpty()) {
                System.out.println("No hay productos en esta categoría.");
                return;
            }
            for (Producto p : lista) {
                System.out.println(p);
            }
        } catch (EntidadNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void crear() {
        System.out.println("\n--- CREAR PRODUCTO ---");
        System.out.println("(Ingrese 0 en cualquier campo para cancelar)");

        List<Categoria> categorias = categoriaService.listar();
        if (categorias.isEmpty()) {
            System.out.println("✗ Debe crear al menos una categoría primero.");
            return;
        }

        String nombre = leerTextoValido("Nombre", 2);
        if (nombre.equals("0")) { System.out.println("Operación cancelada."); return; }

        String descripcion = leerTextoValido("Descripción", 1);
        if (descripcion.equals("0")) { System.out.println("Operación cancelada."); return; }

        Double precio = leerPrecioValido();
        if (precio == -1.0) { System.out.println("Operación cancelada."); return; }

        Integer stock = leerStockValido();
        if (stock == -1) { System.out.println("Operación cancelada."); return; }

        System.out.print("Imagen (URL o nombre, 0 para cancelar): ");
        String imagen = scanner.nextLine().trim();
        if (imagen.equals("0")) { System.out.println("Operación cancelada."); return; }

        Boolean disponible = leerDisponible();
        if (disponible == null) { System.out.println("Operación cancelada."); return; }

        System.out.println("--- CATEGORÍAS DISPONIBLES ---");
        for (Categoria c : categorias) {
            System.out.println(c);
        }
        Categoria categoria = leerCategoriaValida(categorias);
        if (categoria == null) { System.out.println("Operación cancelada."); return; }

        Producto nuevo = productoService.crear(nombre, precio, descripcion,
                stock, imagen, disponible, categoria);
        System.out.println("✓ Producto creado con ID: " + nuevo.getId());
    }

    private void editar() {
        System.out.println("\n--- EDITAR PRODUCTO ---");
        listar();
        try {
            System.out.print("Ingrese ID a editar(0 para cancelar): ");
            Long id = (long) leerEntero();
            if(id == 0){
                System.out.println("Operación cancelada.");
                return;
            }
            System.out.print("Nuevo nombre (Enter para mantener): ");
            String nombre = scanner.nextLine();

            System.out.print("Nuevo precio (-1 para mantener): ");
            Double precio = leerDouble();
            if (precio == -1) precio = null;

            System.out.print("Nueva descripción (Enter para mantener): ");
            String descripcion = scanner.nextLine();

            System.out.print("Nuevo stock (-1 para mantener): ");
            Integer stock = leerEntero();
            if (stock == -1) stock = null;

            System.out.print("¿Disponible? (S/N/Enter para mantener): ");
            String dispStr = scanner.nextLine().trim();
            Boolean disponible = dispStr.isEmpty() ? null : dispStr.equalsIgnoreCase("S");

            productoService.editar(id, nombre, precio, descripcion, stock, disponible, null);
            System.out.println("✓ Producto actualizado correctamente.");
        } catch (EntidadNoEncontradaException | DatoInvalidoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void eliminar() {
        System.out.println("\n--- ELIMINAR PRODUCTO ---");
        listar();
        try {
            System.out.print("Ingrese ID a eliminar: ");
            Long id = (long) leerEntero();

            System.out.print("¿Está seguro? (S/N): ");
            String confirmacion = scanner.nextLine().trim();

            if (confirmacion.equalsIgnoreCase("S")) {
                productoService.eliminar(id);
                System.out.println("✓ Producto eliminado correctamente.");
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

    private double leerDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
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

    private Double leerPrecioValido() {
        while (true) {
            System.out.print("Precio (0 para cancelar): ");
            try {
                Double precio = Double.parseDouble(scanner.nextLine().trim());
                if (precio == 0) return -1.0;
                if (precio < 0) {
                    System.out.println("✗ El precio no puede ser negativo.");
                } else {
                    return precio;
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Ingrese un número válido.");
            }
        }
    }

    private Integer leerStockValido() {
        while (true) {
            System.out.print("Stock (0 para cancelar): ");
            try {
                Integer stock = Integer.parseInt(scanner.nextLine().trim());
                if (stock == 0) return -1;
                if (stock < 0) {
                    System.out.println("✗ El stock no puede ser negativo.");
                } else {
                    return stock;
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Ingrese un número entero válido.");
            }
        }
    }

    private Boolean leerDisponible() {
        while (true) {
            System.out.print("¿Disponible? (S/N, 0 para cancelar): ");
            String valor = scanner.nextLine().trim();
            if (valor.equals("0")) return null;
            if (valor.equalsIgnoreCase("S")) return true;
            if (valor.equalsIgnoreCase("N")) return false;
            System.out.println("✗ Ingrese S, N o 0.");
        }
    }

    private Categoria leerCategoriaValida(List<Categoria> categorias) {
        while (true) {
            System.out.print("ID de categoría (0 para cancelar): ");
            try {
                Long id = (long) Integer.parseInt(scanner.nextLine().trim());
                if (id == 0) return null;
                return categoriaService.buscarPorId(id);
            } catch (NumberFormatException e) {
                System.out.println("✗ Ingrese un número válido.");
            } catch (EntidadNoEncontradaException e) {
                System.out.println("✗ " + e.getMessage());
            }
        }
    }
}