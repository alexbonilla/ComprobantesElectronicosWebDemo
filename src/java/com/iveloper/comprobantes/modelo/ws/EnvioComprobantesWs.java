/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iveloper.comprobantes.modelo.ws;

import com.iveloper.comprobantes.utils.ArchivoUtils;
import ec.gob.sri.comprobantes.ws.Comprobante;
import ec.gob.sri.comprobantes.ws.Mensaje;
import ec.gob.sri.comprobantes.ws.RecepcionComprobantes;
import ec.gob.sri.comprobantes.ws.RecepcionComprobantesService;
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Alex
 */
public class EnvioComprobantesWs {
    
    private static RecepcionComprobantesService service;
    
    public EnvioComprobantesWs(String wsdlLocation) throws MalformedURLException, WebServiceException {
        URL url = new URL(wsdlLocation);
        QName qname = new QName("http://ec.gob.sri.ws.recepcion", "RecepcionComprobantesService");
        service = new RecepcionComprobantesService(url, qname);
    }
    
    public static final Object webService(String wsdlLocation) {
        try {
            QName qname = new QName("http://ec.gob.sri.ws.recepcion", "RecepcionComprobantesService");
            URL url = new URL(wsdlLocation);
            service = new RecepcionComprobantesService(url, qname);
            return null;
        } catch (MalformedURLException ex) {
            return ex;
        } catch (WebServiceException ws) {
            return ws;
        }
    }
    
    public RespuestaSolicitud enviarComprobante(File xmlFile) throws Exception {
        RespuestaSolicitud response = null;
        RecepcionComprobantes port = service.getRecepcionComprobantesPort();
        byte[] archivoBytes = ArchivoUtils.convertirArchivoAByteArray(xmlFile);
        if (archivoBytes != null) {
            response = port.validarComprobante(archivoBytes);
        }
        return response;
    }

    public RespuestaSolicitud enviarComprobanteLotes(String ruc, byte xml[], String tipoComprobante, String versionXsd) {
        RespuestaSolicitud response = null;
        try {
            RecepcionComprobantes port = service.getRecepcionComprobantesPort();
            response = port.validarComprobante(xml);
        } catch (Exception e) {
            response = new RespuestaSolicitud();
            response.setEstado(e.getMessage());
            return response;
        }
        return response;
    }

    public RespuestaSolicitud enviarComprobanteLotes(String ruc, File xml, String tipoComprobante, String versionXsd) {
        RespuestaSolicitud response = null;
        try {
            RecepcionComprobantes port = service.getRecepcionComprobantesPort();
            response = port.validarComprobante(ArchivoUtils.convertirArchivoAByteArray(xml));
        } catch (Exception e) {
            response = new RespuestaSolicitud();
            response.setEstado(e.getMessage());
            return response;
        }
        return response;
    }
/*
    public static RespuestaSolicitud obtenerRespuestaEnvio(File archivo, String ruc, String tipoComprobante, String claveDeAcceso, String urlWsdl) throws Exception {
        RespuestaSolicitud respuesta = new RespuestaSolicitud();
        EnvioComprobantesWs cliente = null;
        try {
            cliente = new EnvioComprobantesWs(urlWsdl);
        } catch (Exception ex) {
            respuesta.setEstado(ex.getMessage());
            return respuesta;
        }
        respuesta = cliente.enviarComprobante(ruc, archivo, tipoComprobante, "1.0.0");
        return respuesta;
    }*/

    public static String obtenerMensajeRespuesta(RespuestaSolicitud respuesta) {
        StringBuilder mensajeDesplegable = new StringBuilder();
        if(respuesta==null) {
            return "";
        }
        if (respuesta.getEstado().equals("DEVUELTA")) {
            ec.gob.sri.comprobantes.ws.RespuestaSolicitud.Comprobantes comprobantes = respuesta.getComprobantes();
            for (Iterator i1 = comprobantes.getComprobante().iterator(); i1.hasNext(); mensajeDesplegable.append("\n")) {
                Comprobante comp = (Comprobante) i1.next();
                mensajeDesplegable.append(comp.getClaveAcceso());
                mensajeDesplegable.append("\n");
                for (Iterator i2 = comp.getMensajes().getMensaje().iterator(); i2.hasNext(); mensajeDesplegable.append("\n")) {
                    Mensaje m = (Mensaje) i2.next();
                    mensajeDesplegable.append(m.getMensaje()).append(" :\n");
                    mensajeDesplegable.append(m.getInformacionAdicional() == null ? "" : m.getInformacionAdicional());
                }

            }
        } else {
            mensajeDesplegable.append(respuesta.getEstado());
        }
        return mensajeDesplegable.toString();
    }
}
