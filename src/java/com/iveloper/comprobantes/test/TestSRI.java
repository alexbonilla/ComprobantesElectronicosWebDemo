/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.ws.AutorizacionComprobantesWs;
import com.iveloper.comprobantes.modelo.ws.EnvioComprobantesWs;
import com.iveloper.comprobantes.utils.ArchivoUtils;
import com.iveloper.comprobantes.utils.CertificadosSSL;
import com.iveloper.comprobantes.utils.WSUtil;
import com.iveloper.db.Conexion;

import ec.gob.sri.comprobantes.ws.RespuestaSolicitud;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import ec.gob.sri.comprobantes.ws.aut.Mensaje;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Alex
 */
public class TestSRI {

    public static void main(String[] args) {
        TestSRI e = new TestSRI();
        try {
            //
            System.out.println(e.enviarComprobante("C:\\Users\\Alex\\Downloads\\firmad<o\\test01_factura.xml", "2"));
//            Thread.sleep(2000);
//            System.out.println(e.autorizarComprobanteIndividual("1702201401091381445500120010010000000011982042015", "2", "C:\\Users\\Alex\\Downloads\\autorizado\\", "C:\\Users\\Alex\\Downloads\\no_autorizado\\"));
        } catch (Exception ex) {
            ex.printStackTrace();
            //Claves de contingencia
        }
    }

    public String enviarComprobante(String rutaArchivoFirmado, String ambiente) throws MalformedURLException, Exception {
        CertificadosSSL.instalarCertificados();
        EnvioComprobantesWs ec = new EnvioComprobantesWs(WSUtil.devuelveUrlWs(ambiente, "RecepcionComprobantes"));
        RespuestaSolicitud response = ec.enviarComprobante(new File(rutaArchivoFirmado));
        String respuesta = EnvioComprobantesWs.obtenerMensajeRespuesta(response);
        return respuesta;
    }

    public String autorizarComprobanteIndividual(String claveDeAcceso, String tipoAmbiente, String rutaArchivoAutorizado, String rutaArchivoNoAutorizado) throws JAXBException, IOException {
        StringBuilder msgAutorizacion = new StringBuilder();
        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;
//       RespuestaComprobante respuesta = null;
        respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
        List<Autorizacion> listaAutorizaciones = respuesta.getAutorizaciones().getAutorizacion();
        for (Autorizacion autorizacion : listaAutorizaciones) {
            String estado = autorizacion.getEstado().toUpperCase();
            if (estado.compareTo("AUTORIZADO") == 0) {
                msgAutorizacion.append(estado);
                /*System.out.println(autorizacion.getComprobante());
                 System.out.println("Número de autorización: " + autorizacion.getNumeroAutorizacion());
                 System.out.println("Fecha de autorización: " + autorizacion.getFechaAutorizacion());*/
                ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoAutorizado + claveDeAcceso + ".xml");
            } else {
                List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();
                for (Mensaje mensaje : mensajes) {
                    msgAutorizacion.append("NO AUTORIZADO").append(".").append(mensaje.getTipo()).append(".").append(mensaje.getIdentificador()).append(".").append(mensaje.getMensaje()).append(".").append(mensaje.getInformacionAdicional()).append("\n");
                    ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoNoAutorizado + claveDeAcceso + ".xml");
                }
            }
        }
        return msgAutorizacion.toString();
    }

    public String autorizarComprobanteIndividual(String numComprobante, String claveDeAcceso, String tipoAmbiente, String rutaArchivoAutorizado, String rutaArchivoNoAutorizado) throws JAXBException, IOException {
        StringBuilder msgAutorizacion = new StringBuilder();
        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;
//        RespuestaComprobante respuesta = null;
        respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
        List<Autorizacion> listaAutorizaciones = respuesta.getAutorizaciones().getAutorizacion();
        for (Autorizacion autorizacion : listaAutorizaciones) {
            String estado = autorizacion.getEstado().toUpperCase();
            if (estado.compareTo("AUTORIZADO") == 0) {
                msgAutorizacion.append(estado);
                ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoAutorizado + claveDeAcceso + ".xml");

            } else {
                List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();
                for (Mensaje mensaje : mensajes) {
                    msgAutorizacion.append("NO AUTORIZADO").append(".").append(mensaje.getTipo()).append(".").append(mensaje.getIdentificador()).append(".").append(mensaje.getMensaje()).append(".").append(mensaje.getInformacionAdicional()).append("\n");
                    ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoNoAutorizado + claveDeAcceso + ".xml");
                }
            }
            Conexion c = new Conexion();
            try {
                c.conectar();
                c.guardarFacturaAutorizada(claveDeAcceso, autorizacion);
            } catch (Exception ex) {
                Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c.estaConectado()) {
                    System.out.println("EVENTO: La conexión a la base de datos está siendo cerrada...");
                    c.desconectar();
                    System.out.println("EVENTO: La conexión a la base de datos ha sido cerrada");
                } else {
                    System.out.println("EVENTO: La conexión de base de datos ya estaba cerrada");
                }
            }
        }
        return msgAutorizacion.toString();
    }

    public Autorizacion obtenerAutorizacion(String numComprobante, String claveDeAcceso, String tipoAmbiente, String rutaArchivoAutorizado, String rutaArchivoNoAutorizado) throws JAXBException, IOException {
        Autorizacion autorizacionResp = new Autorizacion();
        StringBuilder msgAutorizacion = new StringBuilder();
        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;
//        RespuestaComprobante respuesta = null;
        respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
        List<Autorizacion> listaAutorizaciones = respuesta.getAutorizaciones().getAutorizacion();
        for (Autorizacion autorizacion : listaAutorizaciones) {
            autorizacionResp = autorizacion;
            String estado = autorizacion.getEstado().toUpperCase();
            if (estado.compareTo("AUTORIZADO") == 0) {
                msgAutorizacion.append(estado);
                ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoAutorizado + claveDeAcceso + ".xml");

            } else {
                List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();
                for (Mensaje mensaje : mensajes) {
                    msgAutorizacion.append("NO AUTORIZADO").append(".").append(mensaje.getTipo()).append(".").append(mensaje.getIdentificador()).append(".").append(mensaje.getMensaje()).append(".").append(mensaje.getInformacionAdicional()).append("\n");
                    ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoNoAutorizado + claveDeAcceso + ".xml");
                }
            }
            Conexion c = new Conexion();
            try {
                c.conectar();
                c.guardarFacturaAutorizada(claveDeAcceso, autorizacion);
            } catch (Exception ex) {
                Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c.estaConectado()) {
                    System.out.println("EVENTO: La conexión a la base de datos está siendo cerrada...");
                    c.desconectar();
                    System.out.println("EVENTO: La conexión a la base de datos ha sido cerrada");
                } else {
                    System.out.println("EVENTO: La conexión de base de datos ya estaba cerrada");
                }
            }
        }
        return autorizacionResp;
    }
//    public Autorizacion autorizarComprobanteIndividual(String numComprobante, String claveDeAcceso, String tipoAmbiente, String rutaArchivoAutorizado, String rutaArchivoNoAutorizado) throws JAXBException, IOException {
//        StringBuilder msgAutorizacion = new StringBuilder();
//        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;
//        respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
//        List<Autorizacion> listaAutorizaciones = respuesta.getAutorizaciones().getAutorizacion();
//        Autorizacion autorizacionResp = null;
//        for (Autorizacion autorizacion : listaAutorizaciones) {
//            autorizacionResp = autorizacion;
//            String estado = autorizacion.getEstado().toUpperCase();
//            if (estado.compareTo("AUTORIZADO") == 0) {
//                msgAutorizacion.append(estado);
//                ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoAutorizado + claveDeAcceso + ".xml");
//            } else {
//                List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();
//                for (Mensaje mensaje : mensajes) {
//                    msgAutorizacion.append("NO AUTORIZADO").append(".").append(mensaje.getTipo()).append(".").append(mensaje.getIdentificador()).append(".").append(mensaje.getMensaje()).append(".").append(mensaje.getInformacionAdicional()).append("\n");
//                    ArchivoUtils.guardarDocumento(autorizacion, rutaArchivoNoAutorizado + claveDeAcceso + ".xml");
//                }
//            }
//        }
//        return autorizacionResp;
//    }

    public String autorizarComprobanteIndividualTRCRUC_PROCE(int coest, int ident, String claveDeAcceso, String tipoAmbiente, String rutaArchivoAutorizado, String rutaArchivoNoAutorizado) throws JAXBException, IOException {
        StringBuilder msgAutorizacion = new StringBuilder();
        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;
//        RespuestaComprobante respuesta = null;
        respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(tipoAmbiente, "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
        List<Autorizacion> listaAutorizaciones = respuesta.getAutorizaciones().getAutorizacion();
        for (Autorizacion autorizacion : listaAutorizaciones) {
            String estado = autorizacion.getEstado().toUpperCase();
            msgAutorizacion.append(estado);
            Conexion c = new Conexion();
            try {
                c.conectar();
                c.escribirAutorizacionTRCRUC_PROCE(coest, ident, claveDeAcceso, autorizacion);
            } catch (Exception ex) {
                Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (c.estaConectado()) {
                    System.out.println("EVENTO: La conexión a la base de datos está siendo cerrada...");
                    c.desconectar();
                    System.out.println("EVENTO: La conexión a la base de datos ha sido cerrada");
                } else {
                    System.out.println("EVENTO: La conexión de base de datos ya estaba cerrada");
                }
            }
        }
        return msgAutorizacion.toString();
    }
}
