/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.modelo.ws;

/**
 *
 * @author Alex
 */
public class Proxy {

    public Proxy() {
    }

    public Proxy(String url, Integer puerto, String usuario, String clave, String wsProduccion, String wsPruebas) {
        this.url = url;
        this.puerto = puerto;
        this.usuario = usuario;
        this.clave = clave;
        this.wsProduccion = wsProduccion;
        this.wsPruebas = wsPruebas;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPuerto() {
        return puerto;
    }

    public void setPuerto(Integer puerto) {
        this.puerto = puerto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getWsProduccion() {
        return wsProduccion;
    }

    public void setWsProduccion(String wsProduccion) {
        this.wsProduccion = wsProduccion;
    }

    public String getWsPruebas() {
        return wsPruebas;
    }

    public void setWsPruebas(String wsPruebas) {
        this.wsPruebas = wsPruebas;
    }
    private String url;
    private Integer puerto;
    private String usuario;
    private String clave;
    private String wsProduccion;
    private String wsPruebas;

}
