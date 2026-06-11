package integrador.prog2;

import integrador.prog2.entities.*;
import integrador.prog2.enums.*;
import integrador.prog2.service.*;
import integrador.prog2.exception.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("💀 INICIANDO SIMULACRO DE ATAQUE INTERNO Y VIOLACIÓN DE ROBUSTEZ 💀");
        System.out.println("==================================================================\n");

        CategoriaService categoriaService = new CategoriaService();
        ProductoService productoService = new ProductoService();
        UsuarioService usuarioService = new UsuarioService();
        PedidoService pedidoService = new PedidoService();

        // -------------------------------------------------------------------------
        // 🚨 ATAQUE 1: El ataque del ID Duplicado/Manual (Saltándose el contador)
        // -------------------------------------------------------------------------
        System.out.println("--- 💥 ATAQUE 1: Forzar IDs idénticos o negativos por Constructor ---");
        try {
            // El servicio usa un contador interno (arranca en 1L).
            // ¿Qué pasa si creamos entidades usando directamente el constructor con IDs clonados o inválidos?
            Categoria catHacker1 = new Categoria(-999L, "Hack", "Inyección");
            Categoria catHacker2 = new Categoria(1L, "Clon", "Duplicado"); // 1L va a colisionar con el contador del Service

            System.out.println("⚠️ ALERTA: Las entidades aceptaron IDs negativos (-999) y manuales (1) en sus constructores.");
            System.out.println("   Estado actual: Si el Service crea una categoría real, su contador dará 1L y colisionará en memoria.");
        } catch (Exception e) {
            System.out.println("✔ ESCUDO ACTIVADO: El constructor de la entidad frenó la creación: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 🚨 ATAQUE 2: Modificación Directa de ID vía Setter (Corrupción de Llaves)
        // -------------------------------------------------------------------------
        System.out.println("\n--- 💥 ATAQUE 2: Alterar el ID de un objeto ya registrado ---");
        try {
            Usuario juan = usuarioService.crear("Juan", "Perez", "juan@mail.com", "123", "123456", Rol.USUARIO);
            System.out.println("   Juan creado con ID legal del servicio: " + juan.getId());

            // Intentamos mutar su identidad en caliente
            juan.setId(-55L);
            System.out.println("⚠️ ALERTA: Se mutó el ID de Juan a " + juan.getId() + ". Rompiste la consistencia de la simulación.");

            // Intentamos buscarlo por su ID original
            try {
                usuarioService.buscarPorId(1L);
                System.out.println("   Service: Lo encontré igual (porque busca la referencia en la lista, no el campo ID).");
            } catch (EntidadNoEncontradaException e) {
                System.out.println("   Service: Perdió el rastro de Juan porque el ID cambió.");
            }
        } catch (Exception e) {
            System.out.println("✔ ESCUDO ACTIVADO: El setter de la Entidad bloqueó la alteración del ID: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 🚨 ATAQUE 3: Secuestro y Mutación Externa de Listas (Getter Vulnerable)
        // -------------------------------------------------------------------------
        System.out.println("\n--- 💥 ATAQUE 3: Hackear las listas internas de los objetos ---");
        try {
            Categoria pizzas = categoriaService.crear("Pizzas", "Especiales");
            Producto muzza = productoService.crear("Muzzarella", 4000.0, "Rica", 10, "m.jpg", true, pizzas);

            System.out.println("   Cantidad de productos en 'Pizzas' según el Service: " + pizzas.getProductos().size());

            // INTENTO DE SABOTAJE: Nos traemos la lista con el GETTER y le hacemos un clear() o un add() externo
            List<Producto> listaSecuestrada = pizzas.getProductos();
            listaSecuestrada.clear(); // Vaciamos la lista viva del objeto desde afuera de la clase

            System.out.print("   Resultado del sabotaje: ");
            if (pizzas.getProductos().isEmpty()) {
                System.out.println("❌ CRÍTICO: La lista fue vaciada desde afuera. Rompiste el encapsulamiento.");
            } else {
                System.out.println("✔ INMUTABLE: La lista original está protegida contra modificaciones externas.");
            }
        } catch (Exception e) {
            System.out.println("✔ BLOQUEADO: No se pudo alterar la lista: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 🚨 ATAQUE 4: Inyección Manual de Datos Basura por Setters
        // -------------------------------------------------------------------------
        System.out.println("\n--- 💥 ATAQUE 4: Puentear el Service usando Setters destructivos ---");
        try {
            Categoria burgerCat = categoriaService.crear("Burgers", "Fast food");
            Producto burger = productoService.crear("Clásica", 3500.0, "Simple", 5, "b.jpg", true, burgerCat);

            // El ProductoService jamás permitiría un precio negativo. ¿Pero qué pasa si usamos el setter directo?
            burger.setPrecio(-9999.99);
            burger.setStock(-50);

            System.out.println("❌ CRÍTICO: El objeto retiene valores imposibles en el mundo real.");
            System.out.println("   " + burger);
        } catch (Exception e) {
            System.out.println("✔ ESCUDO ACTIVADO: La entidad se defendió sola en su Setter: " + e.getMessage());
        }

        // -------------------------------------------------------------------------
        // 🚨 ATAQUE 5: Sabotaje Relacional Cruzado (Romper la coherencia del modelo)
        // -------------------------------------------------------------------------
        System.out.println("\n--- 💥 ATAQUE 5: Modificación de referencias cruzadas ---");
        try {
            Usuario cliente = usuarioService.crear("Carlos", "Gomez", "carlos@mail.com", "444", "123456", Rol.USUARIO);
            Pedido ped = pedidoService.crear(cliente, FormaPago.TARJETA);

            System.out.println("   El pedido " + ped.getId() + " le pertenece a: " + ped.getUsuario().getNombre());

            // Creamos un usuario impostor y se lo inyectamos al pedido directo por el Setter
            Usuario impostor = new Usuario(99L, "Hacker", "Malo", "h@m.com", "000", "xyz", Rol.ADMIN);
            ped.setUsuario(impostor);

            System.out.println("   Resultado: " + ped);
            System.out.println("   ⚠️ ALERTA: El pedido ahora dice que es del Impostor, pero en 'UsuarioService' el cliente Carlos sigue teniendo este pedido asignado en su lista de historial. Rompiste la integridad referencial.");
        } catch (Exception e) {
            System.out.println("✔ BLOQUEADO: El objeto impidió el cambio descontrolado de dueño: " + e.getMessage());
        }

        System.out.println("\n==================================================================");
        System.out.println("🏁 FIN DEL ANÁLISIS DE FALLAS DE DISEÑO INTERNO");
        System.out.println("==================================================================");
    }
}