package com.iveloper.controllers;

import com.iveloper.comprobantes.modelo.factura.Factura;
import com.iveloper.comprobantes.modelo.ws.AutorizacionComprobantesWs;
import com.iveloper.comprobantes.modelo.ws.TipoAmbienteEnum;
import com.iveloper.comprobantes.utils.WSUtil;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante.Autorizaciones;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alex
 */
@WebServlet(name = "DocumentCtrl", urlPatterns = {"/DocumentCtrl"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10, // 10 MB 
        maxFileSize = 1024 * 1024 * 50, // 50 MB
        maxRequestSize = 1024 * 1024 * 100)
public class DocumentCtrl extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        try (PrintWriter pw = response.getWriter()) {
            String op = request.getParameter("op");
            if (op.equalsIgnoreCase("verifyDocumentFactura")) {
                System.out.println("Ingresando a: verifyDocumentFactura");
                pw.println(verifyDocumentFactura(request, response));
            } else if (op.equalsIgnoreCase("verifyDocumentFacturaInjectResult")) {
                System.out.println("Ingresando a: verifyDocumentFacturaInjectResult");
                pw.println(verifyDocumentFacturaInjectResult(request, response));
            }
        }
    }

    public String verifyDocumentFacturaInjectResult(HttpServletRequest request, HttpServletResponse response) {
        String html = "";
        String result = "";

        try {

            Part filePart = request.getPart("docfile");
            String filename = getFilename(filePart);
            InputStream filecontent = filePart.getInputStream();

            ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = verificarDocumento(filecontent);

            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Nombre de archivo recibido: {0}", filename);
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Clave de acceso: {0}", respuesta.getClaveAccesoConsultada());
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Número de comprobantes: {0}", respuesta.getNumeroComprobantes());
            Autorizaciones autorizaciones = respuesta.getAutorizaciones();
            ArrayList<Autorizacion> autorizacionesList = new ArrayList(autorizaciones.getAutorizacion());
            System.out.println("Número de autorizaciones: " + autorizacionesList.size());
            if (!(autorizacionesList.size() > 0)) {
                html += "<script type=\"text/javascript\" src=\"js/ajax.js\" ></script>";
                html += "<script type=\"text/javascript\" src=\"js/funciones.js\" ></script>";
                html += "<script type=\"text/javascript\"> resultado_upload('No existen autorizaciones para este archivo') </script>";
            }

            Iterator<Autorizacion> autorizacionesListItr = autorizacionesList.iterator();
            while (autorizacionesListItr.hasNext()) {
                Autorizacion autorizacion = autorizacionesListItr.next();
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Estado: {0}", autorizacion.getEstado());
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Fecha de Autorización: {0}", autorizacion.getFechaAutorizacion());
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Número de Autorización: {0}", autorizacion.getNumeroAutorizacion());
                JAXBContext jaxbContext = JAXBContext.newInstance(Factura.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                StringReader reader = new StringReader(autorizacion.getComprobante());
                Factura factura = (Factura) jaxbUnmarshaller.unmarshal(reader);
                result += "<h4>Respuesta desde el SRI:</h4>";
                result += "<ul>";
                result += "<li>Estado: " + autorizacion.getEstado() + "</li>";
                result += "<li>Fecha de autorización: " + autorizacion.getFechaAutorizacion() + "</li>";
                result += "<li>Número de autorización: " + autorizacion.getNumeroAutorizacion() + "</li>";
                result += "<li>Clave de Acceso: " + factura.getInfoTributaria().getClaveAcceso() + "</li>";
                result += "<li>Emisor: " + factura.getInfoTributaria().getRazonSocial() + "</li>";
                result += "<li>RUC Emisor: " + factura.getInfoTributaria().getRuc() + "</li>";
                result += "<li>Cliente: " + factura.getInfoFactura().getRazonSocialComprador() + "</li>";
                result += "<li>ID Cliente: " + factura.getInfoFactura().getIdentificacionComprador() + "</li>";
                result += "<li>Importe Total: $" + factura.getInfoFactura().getImporteTotal() + "</li>";
                result += "</ul>";
                html += "<script type=\"text/javascript\" src=\"js/ajax.js\" ></script>";
                html += "<script type=\"text/javascript\" src=\"js/funciones.js\" ></script>";
                html += "<script type=\"text/javascript\"> resultado_upload('" + result + "') </script>";
            }

        } catch (IOException | JAXBException | ServletException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.SEVERE, null, ex);
            html += "<script type=\"text/javascript\" src=\"js/ajax.js\" ></script>";
            html += "<script type=\"text/javascript\" src=\"js/funciones.js\" ></script>";
            html += "<script type=\"text/javascript\"> resultado_upload('No existen autorizaciones para este archivo') </script>";
        }
        return html;
    }

    public String verifyDocumentFactura(HttpServletRequest request, HttpServletResponse response) {
        String xml = "";

        try {

            Part filePart = request.getPart("docfile");
            String filename = getFilename(filePart);
            InputStream filecontent = filePart.getInputStream();

            ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = verificarDocumento(filecontent);

            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Nombre de archivo recibido: {0}", filename);
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Clave de acceso: {0}", respuesta.getClaveAccesoConsultada());
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Número de comprobantes: {0}", respuesta.getNumeroComprobantes());
            Autorizaciones autorizaciones = respuesta.getAutorizaciones();
            ArrayList<Autorizacion> autorizacionesList = new ArrayList(autorizaciones.getAutorizacion());
            System.out.println("Número de autorizaciones: " + autorizacionesList.size());
            Iterator<Autorizacion> autorizacionesListItr = autorizacionesList.iterator();
            xml = "<?xml version='1.0' encoding='UTF-8' ?>";
            xml += "<autorizaciones>";
            while (autorizacionesListItr.hasNext()) {
                Autorizacion autorizacion = autorizacionesListItr.next();
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Estado: {0}", autorizacion.getEstado());
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Fecha de Autorización: {0}", autorizacion.getFechaAutorizacion());
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.INFO, "Número de Autorización: {0}", autorizacion.getNumeroAutorizacion());
                JAXBContext jaxbContext = JAXBContext.newInstance(Factura.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                StringReader reader = new StringReader(autorizacion.getComprobante());
                Factura factura = (Factura) jaxbUnmarshaller.unmarshal(reader);
                xml += "<autorizacion>";
                xml += "<estado>" + autorizacion.getEstado() + "</estado>";
                xml += "<fecha>" + autorizacion.getFechaAutorizacion() + "</fecha>";
                xml += "<numautorizacion>" + autorizacion.getNumeroAutorizacion() + "</numautorizacion>";
                xml += "<claveacceso>" + factura.getInfoTributaria().getClaveAcceso() + "</claveacceso>";
                xml += "<emisor>" + factura.getInfoTributaria().getRazonSocial() + "</emisor>";
                xml += "<ruc>" + factura.getInfoTributaria().getRuc() + "</ruc>";
                xml += "<cliente>" + factura.getInfoFactura().getRazonSocialComprador() + "</cliente>";
                xml += "<total>" + factura.getInfoFactura().getImporteTotal() + "</total>";
                xml += "</autorizacion>";
            }
            xml += "</autorizaciones>";
        } catch (IOException | JAXBException | ServletException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return xml;
    }

    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
            }
        }
        return null;
    }

    public ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante verificarDocumento(InputStream autorizacionXML) throws JAXBException, IOException, ParserConfigurationException, SAXException {

        System.setProperty("javax.net.ssl.keyStore", "/etc/ssl/certs/java/cacerts");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStore", "/etc/ssl/certs/java/cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
//        System.setProperty("javax.net.ssl.keyStore", "C:\\Program Files (x86)\\Java\\jre7\\lib\\security\\cacerts");
//        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
//        System.setProperty("javax.net.ssl.trustStore", "C:\\Program Files (x86)\\Java\\jre7\\lib\\security\\cacerts");
//        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        ec.gob.sri.comprobantes.ws.aut.RespuestaComprobante respuesta = null;

        try {
            JAXBContext jaxbContextAutorizacion = JAXBContext.newInstance(com.iveloper.comprobantes.autorizacion.Autorizacion.class);

            Unmarshaller jaxbUnmarshallerAutorizacion = jaxbContextAutorizacion.createUnmarshaller();

            com.iveloper.comprobantes.autorizacion.Autorizacion autorizacion = (com.iveloper.comprobantes.autorizacion.Autorizacion) jaxbUnmarshallerAutorizacion.unmarshal(autorizacionXML);

            String xml = autorizacion.getComprobante();
            try {
                Document doc = convertStringToDocument(xml);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String claveDeAcceso = xPath.compile("/factura/infoTributaria/claveAcceso").evaluate(doc);
                System.out.println("Clave de acceso: " + claveDeAcceso);
                respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(TipoAmbienteEnum.PRODUCCION.getCode(), "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
            } catch (XPathExpressionException | DOMException ex) {
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (JAXBException e) {
            try {
                Document doc = readXml(autorizacionXML);
                XPath xPath = XPathFactory.newInstance().newXPath();
                String claveDeAcceso = xPath.compile("/factura/infoTributaria/claveAcceso").evaluate(doc);
                System.out.println("Clave de acceso: " + claveDeAcceso);
                respuesta = (new AutorizacionComprobantesWs(WSUtil.devuelveUrlWs(TipoAmbienteEnum.PRODUCCION.getCode(), "AutorizacionComprobantes"))).llamadaWSAutorizacionInd(claveDeAcceso);
            } catch (XPathExpressionException | DOMException ex) {
                Logger.getLogger(DocumentCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return respuesta;

    }

    private Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(DocumentCtrl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Document readXml(InputStream is) throws SAXException, IOException,
            ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setIgnoringComments(false);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setNamespaceAware(true);
        // dbf.setCoalescing(true);
        // dbf.setExpandEntityReferences(true);

        DocumentBuilder db = null;
        db = dbf.newDocumentBuilder();
        db.setEntityResolver(new NullResolver());

        // db.setErrorHandler( new MyErrorHandler());        
        return db.parse(is);
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}

class NullResolver implements EntityResolver {
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
            IOException {
        return new InputSource(new StringReader(""));
    }
}