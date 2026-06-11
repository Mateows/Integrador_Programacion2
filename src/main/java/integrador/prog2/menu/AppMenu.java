package integrador.prog2.menu;

import integrador.prog2.service.*; //El * es un wildcard (comodín) — significa "importá todas las clases de ese paquete".
import java.util.Scanner;

public class AppMenu {

    private final Scanner scanner;
    private final CategoriaService categoriaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        this.categoriaService = new CategoriaService();
        this.productoService = new ProductoService();
        this.usuarioService = new UsuarioService();
        this.pedidoService = new PedidoService();
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== FOOD STORE - SISTEMA DE PEDIDOS ===");
            System.out.println("1. Categorías");
            System.out.println("2. Productos");
            System.out.println("3. Usuarios");
            System.out.println("4. Pedidos");
            System.out.println("0. Salir");
            System.out.print("Seleccione: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1 -> new CategoriaMenu(scanner, categoriaService).iniciar();
                case 2 -> new ProductoMenu(scanner, productoService, categoriaService).iniciar();
                case 3 -> new UsuarioMenu(scanner, usuarioService).iniciar();
                case 4 -> new PedidoMenu(scanner, pedidoService, usuarioService, productoService).iniciar();
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción inválida. Intente nuevamente.");
            }
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
}
