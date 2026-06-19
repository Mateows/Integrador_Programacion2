package integrador.prog2.menu;

import integrador.prog2.entities.Pedido;
import integrador.prog2.entities.Producto;
import integrador.prog2.entities.Usuario;
import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.exception.DatoInvalidoException;
import integrador.prog2.exception.EntidadNoEncontradaException;
import integrador.prog2.service.PedidoService;
import integrador.prog2.service.ProductoService;
import integrador.prog2.service.UsuarioService;

import java.util.List;
import java.util.Scanner;

public class PedidoMenu {

    private final Scanner scanner;
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    public PedidoMenu(Scanner scanner, PedidoService pedidoService,
                      UsuarioService usuarioService, ProductoService productoService) {
        this.scanner = scanner;
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    public void iniciar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n=== PEDIDOS ===");
            System.out.println("1. Listar todos");
            System.out.println("2. Listar por usuario");
            System.out.println("3. Crear pedido");
            System.out.println("4. Actualizar estado/forma de pago");
            System.out.println("5. Eliminar");
            System.out.println("0. Volver");
            System.out.print("Seleccione: ");

            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listar();
                case 2 -> listarPorUsuario();
                case 3 -> crear();
                case 4 -> actualizar();
                case 5 -> eliminar();
                case 0 -> System.out.println("Volviendo...");
                default -> System.out.println("Opción inválida.");
            }
        }
    }

    private void listar() {
        List<Pedido> lista = pedidoService.listar();
        if (lista.isEmpty()) {
            System.out.println("No hay pedidos cargados.");
            return;
        }
        System.out.println("\n--- LISTADO DE PEDIDOS ---");
        for (Pedido p : lista) {
            System.out.println(p);
        }
    }

    private void listarPorUsuario() {
        List<Usuario> usuarios =  usuarioService.listar();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios cargados.");
            return;
        }
        for (Usuario u : usuarios) {
            System.out.println(u);
        }
        try {
            System.out.print("Ingrese ID de usuario: ");
            Long id = (long) leerEntero();
            List<Pedido> lista = pedidoService.listarPorUsuario(id);
            if (lista.isEmpty()) {
                System.out.println("Este usuario no tiene pedidos.");
                return;
            }
            for (Pedido p : lista) {
                System.out.println(p);
            }
        } catch (EntidadNoEncontradaException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void crear() {
        System.out.println("\n--- CREAR PEDIDO ---");
        List<Usuario> usuarios = usuarioService.listar();
        if (usuarios.isEmpty()) {
            System.out.println("✗ Debe crear al menos un usuario primero.");
            return;
        }
        List<Producto> productos = productoService.listar();
        if (productos.isEmpty()) {
            System.out.println("✗ Debe crear al menos un producto primero.");
            return;
        }
        try {
            System.out.println("--- USUARIOS DISPONIBLES ---");
            for (Usuario u : usuarios) {
                System.out.println(u);
            }
            System.out.print("ID de usuario (0 para cancelar): ");
            Long usuarioId = (long) leerEntero();
            if (usuarioId == 0) { System.out.println("Operación cancelada."); return; }
            Usuario usuario = usuarioService.buscarPorId(usuarioId);

            System.out.println("Forma de pago:");
            System.out.println("1. TARJETA");
            System.out.println("2. TRANSFERENCIA");
            System.out.println("3. EFECTIVO");
            System.out.print("Seleccione: ");
            int fpOpcion = leerEntero();
            FormaPago formaPago = switch (fpOpcion) {
                case 1 -> FormaPago.TARJETA;
                case 2 -> FormaPago.TRANSFERENCIA;
                default -> FormaPago.EFECTIVO;
            };

            Pedido pedido = pedidoService.crear(usuario, formaPago);
            System.out.println("✓ Pedido creado con ID: " + pedido.getId());

            boolean agregarMas = true;
            while (agregarMas) {
                System.out.println("\n--- PRODUCTOS DISPONIBLES ---");
                for (Producto p : productos) {
                    System.out.println(p);
                }
                System.out.print("ID de producto (0 para finalizar): ");
                Long productoId = (long) leerEntero();

                if (productoId == 0) {
                    // Validar que tenga al menos un detalle antes de finalizar
                    if (pedido.getDetalles().isEmpty()) {
                        System.out.println("✗ El pedido debe tener al menos un producto. Agregue uno.");
                        continue;
                    }
                    agregarMas = false;
                    break;
                }

                Producto producto = productoService.buscarPorId(productoId);
                System.out.print("Cantidad: ");
                Integer cantidad = leerEntero();

                pedidoService.agregarDetalle(pedido, producto, cantidad);
                System.out.println("✓ Detalle agregado. Total actual: $" + pedido.getTotal());

                System.out.print("¿Agregar otro producto? (S/N): ");
                agregarMas = scanner.nextLine().trim().equalsIgnoreCase("S");
            }
            System.out.println("✓ Pedido finalizado. Total: $" + pedido.getTotal());
        } catch (EntidadNoEncontradaException | DatoInvalidoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void actualizar() {
        System.out.println("\n--- ACTUALIZAR PEDIDO ---");
        listar();
        try {
            System.out.print("Ingrese ID de pedido: ");
            Long id = (long) leerEntero();

            System.out.println("Nuevo estado:");
            System.out.println("1. PENDIENTE");
            System.out.println("2. CONFIRMADO");
            System.out.println("3. TERMINADO");
            System.out.println("4. CANCELADO");
            System.out.println("0. Mantener");
            System.out.print("Seleccione: ");
            int estadoOpcion = leerEntero();
            Estado estado = switch (estadoOpcion) {
                case 1 -> Estado.PENDIENTE;
                case 2 -> Estado.CONFIRMADO;
                case 3 -> Estado.TERMINADO;
                case 4 -> Estado.CANCELADO;
                default -> null;
            };

            System.out.println("Nueva forma de pago:");
            System.out.println("1. TARJETA");
            System.out.println("2. TRANSFERENCIA");
            System.out.println("3. EFECTIVO");
            System.out.println("0. Mantener");
            System.out.print("Seleccione: ");
            int fpOpcion = leerEntero();
            FormaPago formaPago = switch (fpOpcion) {
                case 1 -> FormaPago.TARJETA;
                case 2 -> FormaPago.TRANSFERENCIA;
                case 3 -> FormaPago.EFECTIVO;
                default -> null;
            };

            pedidoService.actualizar(id, estado, formaPago);
            System.out.println("✓ Pedido actualizado correctamente.");
        } catch (EntidadNoEncontradaException | DatoInvalidoException e) {
            System.out.println("✗ Error: " + e.getMessage());
        }
    }

    private void eliminar() {
        System.out.println("\n--- ELIMINAR PEDIDO ---");
        listar();
        try {
            System.out.print("Ingrese ID a eliminar: ");
            Long id = (long) leerEntero();

            System.out.print("¿Está seguro? (S/N): ");
            String confirmacion = scanner.nextLine().trim();

            if (confirmacion.equalsIgnoreCase("S")) {
                pedidoService.eliminar(id);
                System.out.println("✓ Pedido eliminado correctamente.");
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
}
