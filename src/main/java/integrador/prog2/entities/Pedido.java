package integrador.prog2.entities;

import integrador.prog2.enums.Estado;
import integrador.prog2.enums.FormaPago;
import integrador.prog2.interfaces.Calculable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido extends Base implements Calculable {

    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private FormaPago formaPago;
    private Usuario usuario;
    private List<DetallePedido> detalles;

    public Pedido(Long id, FormaPago formaPago, Usuario usuario) {
        super(id);
        this.fecha = LocalDate.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
        this.formaPago = formaPago;
        this.usuario = usuario;
        this.detalles = new ArrayList<>();
    }

    @Override
    public  void calcularTotal(){
        this.total = 0.0;
        for(DetallePedido detalle : detalles){
            this.total += detalle.getSubtotal();
        }
    }

    public void addDetallePedido(Integer cantidad, Double precio, Producto producto){
        if(cantidad <= 0 ){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if(producto.getStock() < cantidad){
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
        }
        DetallePedido detalle = new DetallePedido((long) (detalles.size() + 1 ), cantidad, producto);
        detalles.add(detalle);
        calcularTotal();
    }

    public DetallePedido findDetallePedidoByProducto(Producto producto) {
        for (DetallePedido detalle : detalles) {
            if (detalle.getProducto().getId().equals(producto.getId())) {
                return detalle;
            }
        }
        return null;
    }

    public void deleteDetallePedidoByProducto(Producto producto){
        detalles.removeIf(d -> d.getProducto().getId().equals(producto.getId()));
        calcularTotal();
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }

    @Override
    public String toString() {
        return "ID: " + getId() +
                " | Usuario: " + usuario.getNombre() + " " + usuario.getApellido() +
                " | Estado: " + estado +
                " | Forma de pago: " + formaPago +
                " | Total: $" + total +
                " | Fecha: " + fecha;
    }
}
