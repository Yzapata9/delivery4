package com.venta_productos.delivery.adapter;

/**
 * Created by windows hdrp on 21/03/2017.
 */

public class adaptador_de_recyclerview {

    private String nombre;
    private String imagen;
    private String rubro="";
    private String precio="";
    private String tiene_detalles="";
    private String disponible="";
    private String estado_negocio="";
    private String horario="";
//aca poner string date
    public adaptador_de_recyclerview(){

    }

    public adaptador_de_recyclerview(String nombre, String imagen, String rubro, String tiene_detalles, String disponible, String estado_negocio, String horario) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.horario = horario;
        this.disponible = disponible;
        this.estado_negocio = estado_negocio;
        this.rubro = rubro;
        this.tiene_detalles = tiene_detalles;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTiene_detalles() {
        return tiene_detalles;
    }

    public void setTiene_detalles(String tiene_detalles) {
        this.tiene_detalles = tiene_detalles;
    }

    public String getDisponible() {
        return disponible;
    }

    public void setDisponible(String disponible) {
        this.disponible= disponible;
    }

   public String getEstado_negocio() {
        return estado_negocio;
    }

    public void setEstado_negocio(String estado_negocio) {
        this.estado_negocio= estado_negocio;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario= horario;
    }
}
