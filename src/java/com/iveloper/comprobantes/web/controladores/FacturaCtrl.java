/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.web.controladores;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.DetAdicional;
import com.iveloper.comprobantes.modelo.Detalle;
import com.iveloper.comprobantes.modelo.Impuesto;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.TotalImpuesto;
import com.iveloper.comprobantes.modelo.factura.Factura;
import com.iveloper.comprobantes.modelo.factura.InfoFactura;
import com.iveloper.comprobantes.security.signer.Signer;
import com.iveloper.comprobantes.test.ConexionTest;
import com.iveloper.comprobantes.test.TestSRI;
import com.iveloper.comprobantes.utils.ArchivoUtils;
import com.iveloper.comprobantes.utils.ClaveAcceso;
import com.iveloper.db.Conexion;
import com.iveloper.entidades.TrcRUC;
import com.iveloper.ihsuite.ws.ClientContactObject;
import com.iveloper.ihsuite.ws.LotType;
import com.iveloper.ihsuite.ws.Operations;
import com.iveloper.ihsuite.ws.Operations_Service;
import com.iveloper.ihsuite.ws.SriStatus;
import com.iveloper.ihsuite.ws.WsResponse;
import com.iveloper.ihsuite.ws.WsResponseData;
import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.WebServiceException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 *
 * @author Alex
 */
@WebServlet(name = "FacturaCtrl", urlPatterns = {"/FacturaCtrl"})
public class FacturaCtrl extends HttpServlet {

    final static String RAZON_SOCIAL = "ALEX FERNANDO BONILLA GORDILLO";
    final static String NOMBRE_COMERCIAL = "ALEX FERNANDO BONILLA GORDILLO";
    final static String RUC = "0913814455001";
    final static String DIRECCION_MATRIZ = "CDLA NAVAL NORTE MZ 5 VILLA 12";
    final static String TIPO_COMPROBANTE = "01"; //01 factura
    final static String AMBIENTE = "1"; //1 pruebas, 2 producción
    final static String CODIGO_NUMERICO = "12344321"; //codigo arbitrario
//    final static String RAZON_SOCIAL_COMPRADOR = "ITD INNOVACION TECNOLOGIA Y DESARROLLO S.A.";
//    final static String IDENTIFICACION_COMPRADOR = "0992761385001";
//    final static String TIPO_IDENTIFICACION_COMPRADOR = "04";

    final static int TIEMPO_ESPERA_SOLICITAR_AUTORIZACION = 3000;
    final static String RUTA_ARCHIVO_FIRMAR = "C:\\Users\\Alex\\Downloads\\sinfirmar\\";
    final static String RUTA_ARCHIVO_FIRMADO = "C:\\Users\\Alex\\Downloads\\firmado\\";
    final static String RUTA_ARCHIVO_AUTORIZADO = "C:\\Users\\Alex\\Downloads\\autorizado\\";
    final static String RUTA_ARCHIVO_NOAUTORIZADO = "C:\\Users\\Alex\\Downloads\\no_autorizado\\";

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        response.setContentType("text/xml;charset=UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        PrintWriter out = response.getWriter();
        String op = request.getParameter("op");

        if (op.equalsIgnoreCase("numFactura")) {
            out.print(obtenerSigNumFactura());
        } else if (op.equalsIgnoreCase("procesoCompleto")) {
            try {
                out.print(procesoCompletoFacturaWS(request, response));
            } catch (MalformedURLException | InterruptedException ex) {
                Logger.getLogger(FacturaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (op.equalsIgnoreCase("procesoBatch")) {
            out.print(construirFacturaElectronicaTrcRUCBatch(request, response));
        } else if (op.equalsIgnoreCase("pruebaSQLSERVER")) {
            out.print(pruebaSQL(request, response));
        }
        out.close();
    }

    private String pruebaSQL(HttpServletRequest request, HttpServletResponse response) {
        int resultados = Integer.parseInt(request.getParameter("cantidadregistros"));
        Conexion c = new Conexion("192.168.20.20", "1433", "consolidado", "sa", "**useco01", 1);
        StringBuilder b = new StringBuilder();
        try {
            c.conectar();

            ArrayList<TrcRUC> facturasvia = new <TrcRUC>ArrayList();

            facturasvia = c.consultarFacturasViaNoProc(resultados);

            for (int i = 0; i < facturasvia.size(); i++) {
                TrcRUC facturavia = facturasvia.get(i);
                b.append("RUC del cliente " + facturavia.getRuc());
            }

            c.desconectar();

        } catch (Exception ex) {
            Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b.toString();
    }

    private int obtenerSigNumFactura() {
        int signumfactura = 0;
        Conexion c = new Conexion();
        try {
            c.conectar();
//            signumfactura = c.obtenerSigNumFactura();
            signumfactura = 1980;
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
        return signumfactura;
    }

    private String procesoCompletoFacturaWS(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException, InterruptedException {
        String respuesta = null;
        String tipoid_comprador = request.getParameter("tipoidcomprador");
        String id_comprador = request.getParameter("idcomprador");
        String razonsocial_comprador = request.getParameter("razonsocial");
        String operador = request.getParameter("operador");
        String email = request.getParameter("email");
        String codproducto = request.getParameter("codproducto");
        String precio = request.getParameter("precio");
        String codestab = request.getParameter("codestab");
        String ptoemi = request.getParameter("ptoemi");
        String numerocomprobante = request.getParameter("numerocomprobante");
        String path = getServletContext().getRealPath("/WEB-INF/configuration.properties");
        String path_ihportal = getServletContext().getRealPath("/WEB-INF/configuration_portal.properties");
        Conexion c;
        c = new Conexion(path);

        String factura = construirFacturaAsString(tipoid_comprador, id_comprador, razonsocial_comprador, operador, email, codproducto, precio, codestab, ptoemi, numerocomprobante);
        String url_wsdl = "http://dev.iveloper.com:20004/ihsuite/Operations?WSDL";
        String user="root";
        String pass="1v3l0p3r$$_.";
        String entityId="8fedf2aea0694f43acc887ff6b2b9a60";
        Operations_Service operations_service = new Operations_Service(new URL(url_wsdl));
        Operations operation = operations_service.getOperationsPort();
        WsResponse lotRes = operation.createLot("Test lot", LotType.UNITARIO, user, pass, entityId, "alexfbonilla@hotmail.com");
        if(lotRes.isProcessed()){
            String lotId = lotRes.getLotId();
            ClientContactObject cc = new ClientContactObject();
            cc.setName(razonsocial_comprador);
            cc.setEmail1(email);
            WsResponse docRes = operation.addFilesWithApp(lotId, "PB", factura, "01", codestab+ptoemi+numerocomprobante, "Doc de prueba", new byte[0], true, cc, user, pass, entityId);
            if(docRes.isProcessed()){
                String docId = docRes.getDocumentId();
                WsResponseData dataRes = operation.getData(docId, user, pass, entityId);
                if(dataRes.getDocumentStatus().equals(SriStatus.NO_PROCESADA) || dataRes.getDocumentStatus().equals(SriStatus.RECIBIDA)){
                    //Esperar
                    Thread.sleep(TIEMPO_ESPERA_SOLICITAR_AUTORIZACION);
                    dataRes = operation.getData(docId, user, pass, entityId);
                    if(dataRes.isProcessed()){
                        return dataRes.getDocumentStatus().value();
                    }
                }
            }
        }
        return null;
    }

    private String procesoCompletoFactura(HttpServletRequest request, HttpServletResponse response) {
        String respuesta = null;
        String tipoid_comprador = request.getParameter("tipoidcomprador");
        String id_comprador = request.getParameter("idcomprador");
        String razonsocial_comprador = request.getParameter("razonsocial");
        String operador = request.getParameter("operador");
        String email = request.getParameter("email");
        String codproducto = request.getParameter("codproducto");
        String precio = request.getParameter("precio");
        String codestab = request.getParameter("codestab");
        String ptoemi = request.getParameter("ptoemi");
        String numerocomprobante = request.getParameter("numerocomprobante");
        String path = getServletContext().getRealPath("/WEB-INF/configuration.properties");
        String path_ihportal = getServletContext().getRealPath("/WEB-INF/configuration_portal.properties");
        Conexion c;
        c = new Conexion(path);

        Factura factura = construirFactura(tipoid_comprador, id_comprador, razonsocial_comprador, operador, email, codproducto, precio, codestab, ptoemi, numerocomprobante);
        String nombreArchivo = factura.getInfoTributaria().getClaveAcceso() + ".xml";
        String validacion = ArchivoUtils.validaArchivoXSD("01", RUTA_ARCHIVO_FIRMAR + nombreArchivo);
        System.out.println((new Date()) + " EVENTO: Comprobando la estructura de la factura " + factura.getInfoTributaria().getClaveAcceso() + " contra esquema");
        if (validacion == null) {
            System.out.println((new Date()) + " EVENTO: Se comprobó la estructura de la factura " + factura.getInfoTributaria().getClaveAcceso());
            Signer signer = new Signer(RUTA_ARCHIVO_FIRMAR + nombreArchivo, RUTA_ARCHIVO_FIRMADO + nombreArchivo, "bgNILL1982$");
            signer.firmar();
            System.out.println((new Date()) + "EVENTO: Se firmó la factura " + factura.getInfoTributaria().getClaveAcceso());
        } else {
            System.out.println((new Date()) + "ERROR: La factura con clave de acceso " + factura.getInfoTributaria().getClaveAcceso() + " no pasó la prueba de validación de esquema.");
            return null;
        }

        nombreArchivo = factura.getInfoTributaria().getClaveAcceso() + ".xml";
        TestSRI e = new TestSRI();
        boolean bandera_recepcion = false;
        try {
            Date dtFechaInicio = new Date();
            String resultadoRecepcion = e.enviarComprobante(RUTA_ARCHIVO_FIRMADO + nombreArchivo, AMBIENTE);
            Date dtFechaFin = new Date();
            long tiempoRespuesta = dtFechaFin.getTime() - dtFechaInicio.getTime();

            System.out.println((new Date()) + " EVENTO: Se envía factura " + factura.getInfoTributaria().getClaveAcceso() + " a WS de recepción: " + resultadoRecepcion);
            System.out.println((new Date()) + " INFO: Tiempo de respuesta para recepción: " + tiempoRespuesta + " milisegundos");

            if (resultadoRecepcion.equals("RECIBIDA")) {
                bandera_recepcion = true;
                Thread.sleep(TIEMPO_ESPERA_SOLICITAR_AUTORIZACION);
            } else {
                bandera_recepcion = false;
            }
        } catch (WebServiceException | ExceptionInInitializerError wex) {
            System.out.println((new Date()) + " ERROR: Durante comunicación con WS de SRI en método procesoCompletoFactura " + wex);
            return null;
            //Claves de contingencia
        } catch (Exception unidefex) {
            System.out.println((new Date()) + " ERROR: Durante comunicación con WS de SRI en método procesoCompletoFactura " + unidefex);
            return null;
            //Claves de contingencia
        }
        if (bandera_recepcion) {
            try {
                Date dtFechaInicioAut = new Date();
                Autorizacion resultadoAutorizacion = e.obtenerAutorizacion(factura.getInfoTributaria().getSecuencial(), factura.getInfoTributaria().getClaveAcceso(), "1", RUTA_ARCHIVO_AUTORIZADO, RUTA_ARCHIVO_NOAUTORIZADO);
                respuesta = resultadoAutorizacion.getEstado();
                Date dtFechaFinAut = new Date();
                long tiempoRespuestaAut = dtFechaFinAut.getTime() - dtFechaInicioAut.getTime();
                System.out.println((new Date()) + " EVENTO: Se solicita autorización para factura " + factura.getInfoTributaria().getClaveAcceso() + " a WS de autorización: " + resultadoAutorizacion);
                System.out.println((new Date()) + " INFO: Tiempo de respuesta para autorización: " + tiempoRespuestaAut + " milisegundos");
                String correodestinatario = null;
                for (int i = 0; i < factura.getCampoAdicional().size(); i++) {
                    if (factura.getCampoAdicional().get(i).getNombre().equals("correoCliente")) {
                        correodestinatario = factura.getCampoAdicional().get(i).getValor();
                    }
                }

                if (respuesta.equals("AUTORIZADO")) {
                    enviarCorreo(correodestinatario, RUTA_ARCHIVO_AUTORIZADO, factura.getInfoTributaria().getClaveAcceso());
                }
                if (respuesta != null) {
                    Conexion portal = new Conexion(path_ihportal);
                    portal.conectar();
                    portal.guardarAutorizacion(resultadoAutorizacion, factura);
                    portal.desconectar();
                }
            } catch (WebServiceException | ExceptionInInitializerError | IOException | JAXBException wex) {
                System.out.println((new Date()) + " ERROR: Durante comunicación con WS de SRI en método procesoCompletoFactura " + wex);
                respuesta = "No se pudo autorizar documento, revise su conectividad a internet y/o SRI.";
                //Claves de contingencia
            } catch (Exception ex) {
                Logger.getLogger(FacturaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            respuesta = "No se pudo enviar documento, revise su conectividad a internet y/o SRI.";
        }
        return respuesta;
    }

    private String construirFacturaAsString(String tipoid_comprador, String id_comprador, String razonsocial_comprador, String operador, String email, String codproducto, String precio, String codEstab, String ptoEmi, String numeroComprobante) {
        String tipoEmision = "1"; //1 normal, 2 contingencia
        String serie = String.format("%03d", Integer.parseInt(codEstab)) + String.format("%03d", Integer.parseInt(ptoEmi)); //serie arbitraria
        Date fecha = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDocumento = df.format(fecha);

        BigDecimal baseImponible = new BigDecimal(precio);
        baseImponible = baseImponible.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totSinImp = baseImponible;
        BigDecimal valorIva = baseImponible.multiply(new BigDecimal("0.12"));
        valorIva = valorIva.setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = baseImponible.add(valorIva);
        valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);

        System.out.println(baseImponible + " " + valorIva + " " + valorTotal);
        numeroComprobante = String.format("%09d", Integer.parseInt(numeroComprobante));

        System.out.println((new Date()) + " EVENTO: Se inicia la construcción de la factura no. " + serie + numeroComprobante + " ordenada por el operador " + operador);

        Factura f = new Factura();
        f.setVersion("1.1.0");
        f.setId("comprobante");

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente(AMBIENTE);
        infoTributaria.setRazonSocial(RAZON_SOCIAL);
        infoTributaria.setNombreComercial(NOMBRE_COMERCIAL);
        infoTributaria.setRuc(RUC);
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(fecha, TIPO_COMPROBANTE, RUC, AMBIENTE, serie, numeroComprobante, CODIGO_NUMERICO, tipoEmision));
        infoTributaria.setCodDoc(TIPO_COMPROBANTE);
        infoTributaria.setEstab(String.format("%03d", Integer.parseInt(codEstab)));
        infoTributaria.setTipoEmision(tipoEmision);
        infoTributaria.setPtoEmi(String.format("%03d", Integer.parseInt(ptoEmi)));
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz(DIRECCION_MATRIZ);
        f.setInfoTributaria(infoTributaria);

        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(baseImponible);
        totimp1.setCodigo("2"); //Este código indica que es IVA
        totimp1.setCodigoPorcentaje("2"); //Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp1.setValor(valorIva);

        List<TotalImpuesto> totalConImpuestos = new ArrayList<TotalImpuesto>();
        totalConImpuestos.add(totimp1);

        InfoFactura infoFactura = new InfoFactura();
        infoFactura.setFechaEmision(fechaDocumento);
        infoFactura.setDirEstablecimiento(DIRECCION_MATRIZ);
        infoFactura.setObligadoContabilidad("NO");

        infoFactura.setRazonSocialComprador(razonsocial_comprador);
        infoFactura.setTipoIdentificacionComprador(tipoid_comprador);
        infoFactura.setIdentificacionComprador(id_comprador);

        infoFactura.setTotalSinImpuestos(totSinImp);
        infoFactura.setTotalDescuento(BigDecimal.ZERO);
        infoFactura.setTotalImpuesto(totalConImpuestos);
        infoFactura.setPropina(BigDecimal.ZERO);
        infoFactura.setImporteTotal(valorTotal);
        infoFactura.setMoneda("DOLAR");

        f.setInfoFactura(infoFactura);

        List<Detalle> detalle = new ArrayList<Detalle>();

        //Detalle 1
        Detalle d1 = new Detalle();
        d1.setCodigoPrincipal(codproducto);
        d1.setCodigoAuxiliar(codproducto);
        d1.setDescripcion("Producto de prueba");
        d1.setCantidad(new BigDecimal("1.00"));
        d1.setPrecioUnitario(baseImponible);
        d1.setDescuento(BigDecimal.ZERO);
        d1.setPrecioTotalSinImpuesto(totSinImp);
        List<DetAdicional> d1da = new ArrayList<DetAdicional>();
        DetAdicional d1da1 = new DetAdicional();
        d1da1.setNombre("operador");
        d1da1.setValor(operador);
        d1da.add(d1da1);
        d1.setDetAdicional(d1da);
        List<Impuesto> impuesto1 = new ArrayList<Impuesto>();
        Impuesto i11 = new Impuesto();
        i11.setCodigo("2");
        i11.setCodigoPorcentaje("2");
        i11.setTarifa(new BigDecimal("12"));
        i11.setBaseImponible(baseImponible);
        i11.setValor(valorIva);
        impuesto1.add(i11);
        d1.setImpuesto(impuesto1);

        detalle.add(d1);

        f.setDetalle(detalle);

        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();
        CampoAdicional ca1 = new CampoAdicional();
        ca1.setNombre("correoCliente");
        ca1.setValor(email);
        infoAdicional.add(ca1);
        f.setCampoAdicional(infoAdicional);
        java.io.StringWriter sw = null;

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{Factura.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            sw = new StringWriter();

            marshaller.marshal(f, sw);

            System.out.println((new Date()) + " EVENTO: Se construyó de la factura no. " + serie + " ordenada por el operador " + operador);
        } catch (Exception ex) {
            System.out.println((new Date()) + " ERROR: Falló la construcción del archivo XML de Factura en el método construirFactura" + ex);
        }
        return sw.toString();
    }

    private Factura construirFactura(String tipoid_comprador, String id_comprador, String razonsocial_comprador, String operador, String email, String codproducto, String precio, String codEstab, String ptoEmi, String numeroComprobante) {
        String tipoEmision = "1"; //1 normal, 2 contingencia
        String serie = String.format("%03d", Integer.parseInt(codEstab)) + String.format("%03d", Integer.parseInt(ptoEmi)); //serie arbitraria
        Date fecha = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDocumento = df.format(fecha);

        BigDecimal baseImponible = new BigDecimal(precio);
        baseImponible = baseImponible.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totSinImp = baseImponible;
        BigDecimal valorIva = baseImponible.multiply(new BigDecimal("0.12"));
        valorIva = valorIva.setScale(2, RoundingMode.HALF_UP);
        BigDecimal valorTotal = baseImponible.add(valorIva);
        valorTotal = valorTotal.setScale(2, RoundingMode.HALF_UP);

        System.out.println(baseImponible + " " + valorIva + " " + valorTotal);
        numeroComprobante = String.format("%09d", Integer.parseInt(numeroComprobante));

        System.out.println((new Date()) + " EVENTO: Se inicia la construcción de la factura no. " + serie + numeroComprobante + " ordenada por el operador " + operador);

        Factura f = new Factura();
        f.setVersion("1.1.0");
        f.setId("comprobante");

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente(AMBIENTE);
        infoTributaria.setRazonSocial(RAZON_SOCIAL);
        infoTributaria.setNombreComercial(NOMBRE_COMERCIAL);
        infoTributaria.setRuc(RUC);
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(fecha, TIPO_COMPROBANTE, RUC, AMBIENTE, serie, numeroComprobante, CODIGO_NUMERICO, tipoEmision));
        infoTributaria.setCodDoc(TIPO_COMPROBANTE);
        infoTributaria.setEstab(String.format("%03d", Integer.parseInt(codEstab)));
        infoTributaria.setTipoEmision(tipoEmision);
        infoTributaria.setPtoEmi(String.format("%03d", Integer.parseInt(ptoEmi)));
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz(DIRECCION_MATRIZ);
        f.setInfoTributaria(infoTributaria);

        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(baseImponible);
        totimp1.setCodigo("2"); //Este código indica que es IVA
        totimp1.setCodigoPorcentaje("2"); //Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp1.setValor(valorIva);

        List<TotalImpuesto> totalConImpuestos = new ArrayList<TotalImpuesto>();
        totalConImpuestos.add(totimp1);

        InfoFactura infoFactura = new InfoFactura();
        infoFactura.setFechaEmision(fechaDocumento);
        infoFactura.setDirEstablecimiento(DIRECCION_MATRIZ);
        infoFactura.setObligadoContabilidad("NO");

        infoFactura.setRazonSocialComprador(razonsocial_comprador);
        infoFactura.setTipoIdentificacionComprador(tipoid_comprador);
        infoFactura.setIdentificacionComprador(id_comprador);

        infoFactura.setTotalSinImpuestos(totSinImp);
        infoFactura.setTotalDescuento(BigDecimal.ZERO);
        infoFactura.setTotalImpuesto(totalConImpuestos);
        infoFactura.setPropina(BigDecimal.ZERO);
        infoFactura.setImporteTotal(valorTotal);
        infoFactura.setMoneda("DOLAR");

        f.setInfoFactura(infoFactura);

        List<Detalle> detalle = new ArrayList<Detalle>();

        //Detalle 1
        Detalle d1 = new Detalle();
        d1.setCodigoPrincipal(codproducto);
        d1.setCodigoAuxiliar(codproducto);
        d1.setDescripcion("Producto de prueba");
        d1.setCantidad(new BigDecimal("1.00"));
        d1.setPrecioUnitario(baseImponible);
        d1.setDescuento(BigDecimal.ZERO);
        d1.setPrecioTotalSinImpuesto(totSinImp);
        List<DetAdicional> d1da = new ArrayList<DetAdicional>();
        DetAdicional d1da1 = new DetAdicional();
        d1da1.setNombre("operador");
        d1da1.setValor(operador);
        d1da.add(d1da1);
        d1.setDetAdicional(d1da);
        List<Impuesto> impuesto1 = new ArrayList<Impuesto>();
        Impuesto i11 = new Impuesto();
        i11.setCodigo("2");
        i11.setCodigoPorcentaje("2");
        i11.setTarifa(new BigDecimal("12"));
        i11.setBaseImponible(baseImponible);
        i11.setValor(valorIva);
        impuesto1.add(i11);
        d1.setImpuesto(impuesto1);

        detalle.add(d1);

        f.setDetalle(detalle);

        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();
        CampoAdicional ca1 = new CampoAdicional();
        ca1.setNombre("correoCliente");
        ca1.setValor(email);
        infoAdicional.add(ca1);
        f.setCampoAdicional(infoAdicional);

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{Factura.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));

            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    RUTA_ARCHIVO_FIRMAR + infoTributaria.getClaveAcceso() + ".xml"), "UTF-8");
            marshaller.marshal(f, out);

            out.close();
            System.out.println((new Date()) + " EVENTO: Se construyó de la factura no. " + serie + " ordenada por el operador " + operador);
        } catch (Exception ex) {
            System.out.println((new Date()) + " ERROR: Falló la construcción del archivo XML de Factura en el método construirFactura" + ex);
        }
        return f;
    }

    private void generarPDF(String ruta_archivo, String nombre_archivo) {
        try {
            String realPath = getServletContext().getRealPath("/factura.jrxml");
            System.out.println(realPath);

            File theFile = new File(realPath);
            JasperDesign jasperDesign = JRXmlLoader.load(theFile);

            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            Conexion c = new Conexion();
            c.conectar();

//            String imgPath = getServletContext().getRealPath("/img");
//            System.out.println(imgPath);
            Map parameters = new HashMap();
            parameters.put("paramclave", nombre_archivo);

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, c.getCon());

            JasperExportManager.exportReportToPdfFile(jasperPrint, ruta_archivo + nombre_archivo + ".pdf");

            System.out.println("Se genero pdf de factura " + nombre_archivo);
            c.desconectar();

        } catch (Exception e) {
            String error = "ERROR: No se pudo generar pdf de factura " + e;
            System.out.println(error);

        }

    }

    private void enviarCorreo(String destinatario, String ruta_archivo, String nombre_archivo) {
        generarPDF(ruta_archivo, nombre_archivo);
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
        final String username = "afbonilla@gmail.com";
        final String password = "centos0420";

        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("afbonilla@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(destinatario));
            message.setSubject("Su factura electronica (demo)");
            message.setText("PFA");

            MimeBodyPart messageBodyPartXML = new MimeBodyPart();
            MimeBodyPart messageBodyPartPDF = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            System.out.println("Correo destinatario: " + destinatario);
            System.out.println("Ruta de Archivo Autorizado: " + ruta_archivo);
            System.out.println("Nombre de Archivo Autorizado: " + nombre_archivo);

            String fileXML = ruta_archivo + nombre_archivo + ".xml";
            String fileXMLName = nombre_archivo + ".xml";
            DataSource sourceXML = new FileDataSource(fileXML);
            messageBodyPartXML.setDataHandler(new DataHandler(sourceXML));
            messageBodyPartXML.setFileName(fileXMLName);
            multipart.addBodyPart(messageBodyPartXML);

            String filePDF = ruta_archivo + nombre_archivo + ".pdf";
            String filePDFName = nombre_archivo + ".pdf";
            DataSource sourcePDF = new FileDataSource(filePDF);
            messageBodyPartPDF.setDataHandler(new DataHandler(sourcePDF));
            messageBodyPartPDF.setFileName(filePDFName);
            multipart.addBodyPart(messageBodyPartPDF);

            message.setContent(multipart);

            System.out.println("Sending");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String construirFacturaElectronicaTrcRUCBatch(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msgAutorizacionBatch = new StringBuilder();

        String registro_resultado_inicial = request.getParameter("registro_resultado_inicial");
        String numero_registros_procesar = request.getParameter("numero_registros_procesar");

        String nombreArchivo;
        Conexion c = new Conexion();
        try {
            c.conectar();

            ArrayList<TrcRUC> facturasvia = new <TrcRUC>ArrayList();

            facturasvia = c.consultarFacturasViaNoProc(Integer.parseInt(registro_resultado_inicial), Integer.parseInt(numero_registros_procesar));

            for (int i = 0; i < facturasvia.size(); i++) {
                TrcRUC facturavia = facturasvia.get(i);
                Factura factura = construirFacturaElectronicaTrcRUC(facturavia);
                nombreArchivo = factura.getInfoTributaria().getClaveAcceso() + ".xml";
                String validacion = ArchivoUtils.validaArchivoXSD("01", RUTA_ARCHIVO_FIRMAR + nombreArchivo);
                System.out.println("EVENTO: se ejecutó la validación de la factura " + factura.getInfoTributaria().getClaveAcceso() + " con resultado " + validacion);
                if (validacion == null) {
                    System.out.println("EVENTO: Se comprobó la factura con Clave de acceso " + factura.getInfoTributaria().getClaveAcceso());
                    Signer signer = new Signer(RUTA_ARCHIVO_FIRMAR + nombreArchivo, RUTA_ARCHIVO_FIRMADO + nombreArchivo, "bgNILL1982$");
                    signer.firmar();
                    System.out.println("EVENTO: Se firmó la factura con Clave de acceso " + factura.getInfoTributaria().getClaveAcceso());
                    Factura facturaFirmada = factura;
                    nombreArchivo = facturaFirmada.getInfoTributaria().getClaveAcceso() + ".xml";
                    TestSRI e = new TestSRI();
                    try {
                        System.out.println(e.enviarComprobante(RUTA_ARCHIVO_FIRMADO + nombreArchivo, "1"));
                        Thread.sleep(3000);
                        msgAutorizacionBatch.append(facturaFirmada.getInfoTributaria().getClaveAcceso() + ":" + e.autorizarComprobanteIndividualTRCRUC_PROCE(facturavia.getCoest(), facturavia.getIdent(), facturaFirmada.getInfoTributaria().getClaveAcceso(), "1", RUTA_ARCHIVO_AUTORIZADO, RUTA_ARCHIVO_NOAUTORIZADO) + "\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        //Claves de contingencia
                    }
                } else {
                    System.out.println("ERROR: La factura con clave de acceso " + factura.getInfoTributaria().getClaveAcceso() + " no pasó la prueba de validación de esquema.");
                }
            }
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
        return msgAutorizacionBatch.toString();
    }

    public static Factura construirFacturaElectronicaTrcRUC(TrcRUC facturavia) {

        String serie = String.format("%03d", facturavia.getCoest()) + String.format("%03d", facturavia.getNuvia()); //serie arbitraria
        String numeroComprobante = String.format("%09d", facturavia.getTicke()); //inicio arbitrario

        String tipoEmision = "1"; //1 normal, 2 contingencia

        Factura f = new Factura();
        f.setVersion("1.1.0");
        f.setId("comprobante");
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente(AMBIENTE);
        infoTributaria.setRazonSocial(RAZON_SOCIAL);
        infoTributaria.setNombreComercial(RAZON_SOCIAL);
        infoTributaria.setRuc(RUC);
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(facturavia.getFecha(), TIPO_COMPROBANTE, RUC, AMBIENTE, serie, numeroComprobante, CODIGO_NUMERICO, tipoEmision));

        infoTributaria.setCodDoc(TIPO_COMPROBANTE);
        infoTributaria.setEstab(String.format("%03d", facturavia.getCoest()));
        infoTributaria.setTipoEmision(tipoEmision);
        infoTributaria.setPtoEmi(String.format("%03d", facturavia.getNuvia()));
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz(DIRECCION_MATRIZ);

        f.setInfoTributaria(infoTributaria);

        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(facturavia.getImpor());
        totimp1.setCodigo("2"); //Este código indica que es IVA
        totimp1.setCodigoPorcentaje("0"); //Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp1.setValor(BigDecimal.ZERO); //CORREGIR Y PONER EL RESULTADO DE LA OPERACION DE PORCENTAJE IVA CALCULADO PARA BASE IMPONIBLE

        TotalImpuesto totimp2 = new TotalImpuesto();
        totimp2.setBaseImponible(BigDecimal.ZERO);
        totimp2.setCodigo("2"); //Este código indica que es IVA
        totimp2.setCodigoPorcentaje("2");//Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp2.setValor(BigDecimal.ZERO);

        List<TotalImpuesto> totalConImpuestos = new ArrayList<TotalImpuesto>();

        totalConImpuestos.add(totimp1);
        totalConImpuestos.add(totimp2);

        InfoFactura infoFactura = new InfoFactura();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDocumento = df.format(facturavia.getFecha());

        infoFactura.setFechaEmision(fechaDocumento);
        infoFactura.setDirEstablecimiento(DIRECCION_MATRIZ);
//        infoFactura.setContribuyenteEspecial("5368");
        infoFactura.setObligadoContabilidad("NO");

        switch (facturavia.getTidoc()) {
            case 1:
                infoFactura.setTipoIdentificacionComprador("04");
                break;
            case 2:
                infoFactura.setTipoIdentificacionComprador("05");
                break;
            case 3:
                infoFactura.setTipoIdentificacionComprador("06");
                break;
            case 4:
                infoFactura.setTipoIdentificacionComprador("07");
                break;
            default:
                infoFactura.setTipoIdentificacionComprador("07");
                break;
        }

        infoFactura.setRazonSocialComprador(facturavia.getNombr());
        infoFactura.setIdentificacionComprador(facturavia.getRuc());
        infoFactura.setTotalSinImpuestos(facturavia.getImpor());
        infoFactura.setTotalDescuento(BigDecimal.ZERO);
        infoFactura.setTotalImpuesto(totalConImpuestos);
        infoFactura.setPropina(BigDecimal.ZERO);
        infoFactura.setImporteTotal(facturavia.getImpor()); //CORREGIR Y COLOCAR SUMA DE TOTAL SIN IMPUESTOS MAS TOTAL CON IMPUESTOS
        infoFactura.setMoneda("DOLAR");

        f.setInfoFactura(infoFactura);

        List<Detalle> detalle = new ArrayList<Detalle>();

        //Detalle 1
        Detalle d1 = new Detalle();
        d1.setCodigoPrincipal("pea-" + String.format("%03d", facturavia.getCoest()) + "-" + String.format("%02d", facturavia.getTarif()));
        d1.setCodigoAuxiliar("tar-" + String.format("%03d", facturavia.getCoest()) + "-" + String.format("%02d", facturavia.getTarif()));
        d1.setDescripcion("Paso por peaje estacion no. " + String.format("%03d", facturavia.getCoest()) + ", categoria " + String.format("%02d", facturavia.getTarif()));
        d1.setCantidad(new BigDecimal("1.00"));
        d1.setPrecioUnitario(facturavia.getImpor());
        d1.setDescuento(BigDecimal.ZERO);
        d1.setPrecioTotalSinImpuesto(facturavia.getImpor());
        List<DetAdicional> d1da = new ArrayList<DetAdicional>();
        DetAdicional d1da1 = new DetAdicional();
        d1da1.setNombre("turno");
        d1da1.setValor("" + facturavia.getNturn());
        DetAdicional d1da2 = new DetAdicional();
        d1da2.setNombre("evento");
        d1da2.setValor("" + facturavia.getNumev());
        DetAdicional d1da3 = new DetAdicional();
        d1da3.setNombre("ident");
        d1da3.setValor("" + facturavia.getIdent());

        d1da.add(d1da1);
        d1da.add(d1da2);
        d1da.add(d1da3);

        d1.setDetAdicional(d1da);

        List<Impuesto> impuesto1 = new ArrayList<Impuesto>();
        Impuesto i11 = new Impuesto();
        i11.setCodigo("2");
        i11.setCodigoPorcentaje("0");
        i11.setTarifa(BigDecimal.ZERO);
        i11.setBaseImponible(facturavia.getImpor());
        i11.setValor(BigDecimal.ZERO);

        Impuesto i12 = new Impuesto();
        i12.setCodigo("2");
        i12.setCodigoPorcentaje("2");
        i12.setTarifa(new BigDecimal("12.00"));
        i12.setBaseImponible(BigDecimal.ZERO);
        i12.setValor(BigDecimal.ZERO);

        impuesto1.add(i11);
        impuesto1.add(i12);

        d1.setImpuesto(impuesto1);

        detalle.add(d1);

        f.setDetalle(detalle);

        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();

        CampoAdicional ca1 = new CampoAdicional();
        ca1.setNombre("idRegistro");
        ca1.setValor(facturavia.getCoest() + "-" + facturavia.getIdent());

        infoAdicional.add(ca1);

        f.setCampoAdicional(infoAdicional);
        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{Factura.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    RUTA_ARCHIVO_FIRMAR + infoTributaria.getClaveAcceso() + ".xml"), "UTF-8");
            marshaller.marshal(f, out);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return f;
    }

    public String generarRIDE(Factura factura, String url_logo, String numautorizacion, String fechaautorizacion) {
        StringBuilder ride = new StringBuilder();
        ride.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>");
        ride.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
        ride.append("<title>Imprimir Factura</title>");
        ride.append("<link href=\"css/factura.css\" rel=\"stylesheet\"></head>");
        ride.append("<body>");
        ride.append("<table width=\"900\" border=\"0\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">");
        ride.append("<tbody><tr>");
        ride.append("<td width=\"450\" height=\"109\" align=\"center\" valign=\"middle\"><img src=\"").append(url_logo).append("\"></td>");
        ride.append("<td width=\"450\" rowspan=\"2\" align=\"center\" valign=\"middle\">");
        ride.append("<table width=\"373\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:thin dashed #333;\">");
        ride.append("<tbody><tr>");
        ride.append("<td width=\"363\" height=\"229\" align=\"left\" valign=\"top\" style=\"padding:5px; padding-left:25px\"><p class=\"sub-tit\">RUC: <span id=\"ruc\" class=\"prueba_txt2\" style=\"padding-left:10px\">").append(factura.getInfoTributaria().getRuc()).append("</span><br>");
        ride.append("FACTURA<br>");
        ride.append("No.: <span id=\"numfactura\" class=\"prueba_txt1\">").append(factura.getInfoTributaria().getEstab()).append(factura.getInfoTributaria().getPtoEmi()).append(factura.getInfoTributaria().getSecuencial()).append("</span><br>");
        ride.append("Número de Autorización:<br>");
        ride.append("<span id=\"numautorizacion\" class=\"txt1\">").append(numautorizacion).append("</span><br>");
        ride.append("Fecha y hora de aut.:<span id=\"fechaautorizacion\" class=\"txt1\">").append(fechaautorizacion).append("</span> <br>");
        ride.append("Ambiente: <span id=\"ambiente\" class=\"txt1\">").append(getAmbiente(factura.getInfoTributaria().getAmbiente())).append("</span><br>");
        ride.append("Ambiente: <span id=\"ambiente\" class=\"txt1\">").append(getAmbiente(factura.getInfoTributaria().getAmbiente())).append("</span><br>");
        ride.append("Emisión: <span id=\"tipoemision\" class=\"txt1\">Normal<br>");
        ride.append("Clave de Acceso:<br>");
        ride.append("<img id=\"imgclaveacceso\" src=\"barcodeimage.php?&amp;text=").append(factura.getInfoTributaria().getClaveAcceso()).append("\" alt=\"Clave de acceso\">");
        ride.append("<span id=\"claveacceso\" class=\"txt2\" style=\"text-align:center;\">").append(factura.getInfoTributaria().getClaveAcceso()).append("</span><br>");
        ride.append("</span></p></td>");
        ride.append("</tr>");
        ride.append("</tbody></table></td>");
        ride.append("</tr>");
        ride.append("<tr>");
        ride.append("<td height=\"135\" align=\"center\" valign=\"middle\">");
        ride.append("<table width=\"369\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:thin dashed #333;\">");
        ride.append("<tbody><tr>");
        ride.append("<td width=\"361\" align=\"left\" valign=\"top\" style=\"padding:5px\"><p id=\"razonsocial_emisor\" class=\"tit\">").append(factura.getInfoTributaria().getRazonSocial()).append("</p>");
        ride.append("<p class=\"sub-tit\">Dir Matriz: <span id=\"direccion_matriz\" class=\"txt1\">").append(factura.getInfoTributaria().getDirMatriz()).append("</span><br>");
        ride.append("Dir Sucursal: <span id=\"direccion_sucursal\" class=\"txt1\">").append(factura.getInfoTributaria().getDirMatriz()).append("</span><br>");
        if (factura.getInfoFactura().getContribuyenteEspecial() != null) {
            ride.append("Contribuyente Especial Nro.: <span id=\"contibespecial\" class=\"txt1\">").append(factura.getInfoFactura().getContribuyenteEspecial()).append("</span><br>");
        }
        ride.append("Obligado a llevar contabilidad: <span id=\"obligadocontabilidad\" class=\"txt1\">").append(factura.getInfoFactura().getObligadoContabilidad()).append("</span></p></td>");
        ride.append("</tr>");
        ride.append("</tbody></table></td>");
        ride.append("</tr>");
        ride.append("<tr>");
        ride.append("<td height=\"91\" colspan=\"2\" align=\"center\" valign=\"middle\"><table width=\"820\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border:thin dashed #333;\">");
        ride.append("<tbody><tr>");
        ride.append("<td width=\"460\" height=\"22\" style=\"padding:5px\" align=\"left\"><span class=\"sub-tit\">Razón Social / Nombres y Apellidos: ");
        ride.append("</span><span id=\"razonsocial_comprador\" class=\"txt1\">").append(factura.getInfoFactura().getRazonSocialComprador()).append("</span></td>");
        ride.append("<td width=\"356\" style=\"padding:5px\" align=\"left\"><span class=\"sub-tit\">RUC/CI: </span><span id=\"identificacion_comprador\" class=\"txt1\">").append(factura.getInfoFactura().getIdentificacionComprador()).append("</span></td>");
        ride.append("</tr>");
        ride.append("<tr>");
        ride.append("<td height=\"22\" style=\"padding:5px\" align=\"left\"><span class=\"sub-tit\">Fecha Emisión: </span><span id=\"fechaemision\" class=\"txt1\">").append(factura.getInfoFactura().getFechaEmision()).append("</span></td>");
        ride.append("<td width=\"356\" style=\"padding:5px\" align=\"left\"><span class=\"sub-tit\">Guía Remisión: </span></td>");
        ride.append("</tr>");
        ride.append("</tbody></table></td>");
        ride.append("</tr>");
        ride.append("<tr>");
        ride.append("<td height=\"48\" colspan=\"2\" align=\"center\" valign=\"top\"><table width=\"820\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
        ride.append("<tbody>");
        ride.append("<tr>");
        ride.append("<td width=\"71\" height=\"22\" align=\"center\" valign=\"middle\" style=\"padding:5px; border:thin dashed #333;\"><span class=\"sub-tit\">Cod<br>Principal</span></td>");
        ride.append("<td width=\"70\" align=\"center\" valign=\"middle\" style=\"padding:5px; border:thin dashed #333;\"><span class=\"sub-tit\">Cod<br>Auxiliar</span></td>");
        ride.append("<td width=\"55\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Cant</td>");
        ride.append("<td width=\"152\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Descripción</td>");
        ride.append("<td width=\"91\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Detalle<br>Adicional</td>");
        ride.append("<td width=\"88\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Detalle<br>Adicional</td>");
        ride.append("<td width=\"93\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Detalle<br>Adicional</td>");
        ride.append("<td width=\"63\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Precio<br>Unitario</td>");
        ride.append("<td width=\"79\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Descuento</td>");
        ride.append("<td width=\"54\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">Precio<br>Total</td>");
        ride.append("</tr>");
        List<Detalle> detalles = factura.getDetalle();
        for (Detalle detalle : detalles) {
            ride.append("<tr>");
            ride.append("<td height=\"22\" align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getCodigoPrincipal()).append("</td>");
            ride.append("<td align=\"center\" valign=\"middle\" style=\"padding:5px; border:thin dashed #333;\">&nbsp;</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getCantidad()).append("</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getDescripcion()).append("</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">&nbsp;</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">&nbsp;</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">&nbsp;</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getPrecioUnitario()).append("</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getDescuento()).append("</td>");
            ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(detalle.getPrecioTotalSinImpuesto()).append("</td>");
            ride.append("</tr>");
        }
//        ride.append("<tr>");
//        ride.append("<td height=\"22\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
//        ride.append("<td colspan=\"2\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\" align=\"left\">SUBTOTAL 12%</td>");
//        ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(factura.getInfoFactura().).append("</td>");
//        ride.append("</tr>");
        ride.append("<tr>");
        ride.append("<td height=\"22\" style=\"padding:5px;\">&nbsp;</td>");
        ride.append("<td class=\"sub-tit\" style=\"padding:5px;\">&nbsp;</td>");
        ride.append("<td colspan=\"2\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\" align=\"left\">VALOR TOTAL</td>");
        ride.append("<td align=\"center\" valign=\"middle\" class=\"sub-tit\" style=\"padding:5px; border:thin dashed #333;\">").append(factura.getInfoFactura().getImporteTotal()).append("</td>");
        ride.append("</tr>");
        ride.append("</tbody></table></td>");
        ride.append("</tr>");
        ride.append("</tbody></table>");
        ride.append("<br>");
        ride.append("</body>");
        ride.append("</html>");
        return ride.toString();
    }

    private String getAmbiente(String ambiente) {
        if (ambiente.equals("1")) {
            return "PRUEBAS";
        } else {
            return "PRODUCCION";
        }
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
