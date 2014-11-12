/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.utils;

import com.iveloper.comprobantes.modelo.ws.Proxy;
import com.iveloper.comprobantes.modelo.ws.TipoAmbienteEnum;

/**
 *
 * @author Alex
 */
public class WSUtil {

    public static String devuelveUrlWs(String ambiente, String nombreServicio) {
        StringBuilder url = new StringBuilder();
        String direccionIPServicio = null;
        Proxy configuracion = null;
        try {
            configuracion = obtenerProxy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (configuracion != null) {
            if (ambiente.equals(TipoAmbienteEnum.PRODUCCION.getCode())) {
                direccionIPServicio = configuracion.getWsProduccion();
                System.out.println("Se emite en produccion " + ambiente);
            } else if (ambiente.equals(TipoAmbienteEnum.PRUEBAS.getCode())) {
                direccionIPServicio = configuracion.getWsPruebas();
                System.out.println("Se emite en pruebas " + ambiente);
            }
            url.append(direccionIPServicio);
            url.append("/comprobantes-electronicos-ws/");
            url.append(nombreServicio);
            url.append("?wsdl");
        }
        System.out.println(url.toString());
        return url.toString();
    }

    private static Proxy obtenerProxy() {
        Proxy proxy = new Proxy();
        proxy.setWsProduccion("https://cel.sri.gob.ec");
        proxy.setWsPruebas("https://celcer.sri.gob.ec");
        return proxy;
    }

}
