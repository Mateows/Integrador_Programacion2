package integrador.prog2.dao;

import integrador.prog2.entities.DetallePedido;
import java.util.List;

public interface DetallePedidoDAO extends GenericDAO<DetallePedido> {
    List<DetallePedido> listarPorPedido(Long pedidoId);
}