package integrador.prog2.dao;

import integrador.prog2.entities.Pedido;
import java.util.List;

public interface PedidoDAO extends GenericDAO<Pedido> {
    List<Pedido> listarPorUsuario(Long usuarioId);
}
