/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.utils;

import com.sun.xml.bind.marshaller.DataWriter;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Alex
 */
public class ArchivoUtils {

    public static byte[] convertirArchivoAByteArray(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        InputStream ios = null;
        try {
            ios = new FileInputStream(file);
            if (ios.read(buffer) == -1) {
                throw new IOException("EOF reached while trying to read the whole file");
            }
        } catch (Exception ex) {
            buffer = null;
        } finally {
            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {
            }
        }
        return buffer;
    }

    private static String seleccionaXsd(String tipo) {
        String nombreXsd = null;
        if (tipo.equals(TipoComprobanteEnum.FACTURA.getCode())) {
            nombreXsd = TipoComprobanteEnum.FACTURA.getXsd();
        } else if (tipo.equals(TipoComprobanteEnum.COMPROBANTE_DE_RETENCION.getCode())) {
            nombreXsd = TipoComprobanteEnum.COMPROBANTE_DE_RETENCION.getXsd();
        } else if (tipo.equals(TipoComprobanteEnum.GUIA_DE_REMISION.getCode())) {
            nombreXsd = TipoComprobanteEnum.GUIA_DE_REMISION.getXsd();
        } else if (tipo.equals(TipoComprobanteEnum.NOTA_DE_CREDITO.getCode())) {
            nombreXsd = TipoComprobanteEnum.NOTA_DE_CREDITO.getXsd();
        } else if (tipo.equals(TipoComprobanteEnum.NOTA_DE_DEBITO.getCode())) {
            nombreXsd = TipoComprobanteEnum.NOTA_DE_DEBITO.getXsd();
        } else if (tipo.equals(TipoComprobanteEnum.LOTE.getCode())) {
            nombreXsd = TipoComprobanteEnum.LOTE.getXsd();
        }
        return nombreXsd;
    }

    public static String validaArchivoXSD(String tipoComprobante, String pathArchivoXML) {
        String respuestaValidacion = null;
        try {
            String nombreXsd = seleccionaXsd(tipoComprobante);
            String pathArchivoXSD = (new StringBuilder()).append("C:\\Users\\Alex\\Documents\\comprobantes_electronicos\\validadores\\").append(nombreXsd).toString();
            ValidadorEstructuraDocumento validador = new ValidadorEstructuraDocumento(pathArchivoXSD, pathArchivoXML);
            if (pathArchivoXML != null) {
                respuestaValidacion = validador.validacion();
            }
        } catch (Exception ex) {
            respuestaValidacion = ExceptionUtils.getStackTrace(ex);
        }
        return respuestaValidacion;
    }
    
    public static void convertirCadenaAArchivo(String ruta, String contenido) {
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(ruta);
            fop = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = contenido.getBytes();
            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    

//    public static void guardarDocumento(Autorizacion autorizacion, String ruta) throws JAXBException, IOException {
//        String comprobante = autorizacion.getComprobante().replaceAll("&lt;", "<");
//        comprobante = comprobante.replaceAll("&gt;", ">");
//        System.out.println(comprobante);
//        autorizacion.setComprobante((new StringBuilder()).append("<![CDATA[").append(comprobante).append("]]>").toString());
//
//        JAXBContext jc = JAXBContext.newInstance(Autorizacion.class);
//        Marshaller marshaller = jc.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
//        Writer writer = new FileWriter(ruta);
//        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//        marshaller.marshal(new JAXBElement<Autorizacion>(new QName("autorizacion"), Autorizacion.class, autorizacion), writer);
//        writer.close();
//    }

    public static void guardarDocumento(Autorizacion autorizacion, String ruta) throws JAXBException, IOException {

        //autorizacion.setComprobante((new StringBuilder()).append("<![CDATA[").append(autorizacion.getComprobante()).append("]]>").toString());
        /*
        JAXBContext jc = JAXBContext.newInstance(Autorizacion.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        Writer writer = new FileWriter(ruta);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        marshaller.marshal(new JAXBElement<Autorizacion>(new QName("autorizacion"), Autorizacion.class, autorizacion), writer);
        marshaller.marshal(autorizacion, writer);
        writer.close();*/
        com.iveloper.comprobantes.autorizacion.Autorizacion objAutorizacion = new com.iveloper.comprobantes.autorizacion.Autorizacion();
//        objAutorizacion.setComprobante(autorizacion.getComprobante());
        objAutorizacion.setComprobante((new StringBuilder()).append("<![CDATA[").append(autorizacion.getComprobante()).append("]]>").toString());
        objAutorizacion.setEstado(autorizacion.getEstado());
        objAutorizacion.setFechaAutorizacion(autorizacion.getFechaAutorizacion());
        objAutorizacion.setMensajes(autorizacion.getMensajes());
        objAutorizacion.setNumeroAutorizacion(autorizacion.getNumeroAutorizacion());

        //String packageName = objAutorizacion.getClass().getPackage().getName(); // Provide the package name of the generated 
        //JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
        JAXBContext jaxbContext = JAXBContext.newInstance(com.iveloper.comprobantes.autorizacion.Autorizacion.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        // Set UTF-8 Encoding
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        //StringWriter stringWriter = new StringWriter();
        //PrintWriter printWriter = new PrintWriter(stringWriter);
        Writer writer = new FileWriter(ruta);
        DataWriter dataWriter = new DataWriter(writer, "UTF-8", new JaxbCharacterEscapeHandler());

        // Perform Marshalling operation
        marshaller.marshal(objAutorizacion, dataWriter);
        writer.close();
        //System.out.println(stringWriter.toString());
    }
}
