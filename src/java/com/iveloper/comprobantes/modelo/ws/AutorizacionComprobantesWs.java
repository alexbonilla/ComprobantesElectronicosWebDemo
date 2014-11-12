/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iveloper.comprobantes.modelo.ws;



import ec.gob.sri.comprobantes.ws.aut.AutorizacionComprobantes;
import ec.gob.sri.comprobantes.ws.aut.AutorizacionComprobantesService;
import ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante;
import java.net.URL;
import javax.xml.namespace.QName;

/**
 *
 * @author Alex
 */
public class AutorizacionComprobantesWs {
    
    private AutorizacionComprobantesService service;

    public AutorizacionComprobantesWs(String wsdlLocation) {
        try {
            service = new AutorizacionComprobantesService(new URL(wsdlLocation), new QName("http://ec.gob.sri.ws.autorizacion", "AutorizacionComprobantesService"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public RespuestaComprobante llamadaWSAutorizacionInd(String claveDeAcceso) {
        RespuestaComprobante response = null;
        try {
            AutorizacionComprobantes port = service.getAutorizacionComprobantesPort();
            java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider)port).getRequestContext();
            requestContext.put("set-jaxb-validation-event-handler", "false");            
            response = port.autorizacionComprobante(claveDeAcceso);
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        }
        return response;
    }
}
