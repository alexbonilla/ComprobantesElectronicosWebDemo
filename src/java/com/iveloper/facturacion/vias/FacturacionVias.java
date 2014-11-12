/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.facturacion.vias;

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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Alex
 */
public class FacturacionVias {

    final static String ruta_archivo_firmar = "C:\\Users\\Alex\\Downloads\\sinfirmar\\";
    final static String ruta_archivo_firmado = "C:\\Users\\Alex\\Downloads\\firmado\\";
    final static String ruta_archivo_autorizado = "C:\\Users\\Alex\\Downloads\\autorizado\\";
    final static String ruta_archivo_noautorizado = "C:\\Users\\Alex\\Downloads\\no_autorizado\\";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String nombreArchivo = ".xml";
        Conexion c = new Conexion();
        try {
            c.conectar();

            ArrayList<TrcRUC> facturasvia = new <TrcRUC>ArrayList();

            facturasvia = c.consultarFacturasViaNoProc(1, 5);

            for (int i = 0; i < facturasvia.size(); i++) {
                TrcRUC facturavia = facturasvia.get(i);
                Factura factura = construirFacturaElectronica(facturavia);
                nombreArchivo = factura.getInfoTributaria().getClaveAcceso() + ".xml";
                String validacion = ArchivoUtils.validaArchivoXSD("01", ruta_archivo_firmar + nombreArchivo);
                System.out.println("EVENTO: se ejecutó la validación de la factura " + factura.getInfoTributaria().getClaveAcceso() + " con resultado " + validacion);
                if (validacion == null) {
                    System.out.println("EVENTO: Se comprobó la factura con Clave de acceso " + factura.getInfoTributaria().getClaveAcceso());
                    Signer signer = new Signer(ruta_archivo_firmar + nombreArchivo, ruta_archivo_firmado + nombreArchivo, "bgNILL1982$");
                    signer.firmar();
                    System.out.println("EVENTO: Se firmó la factura con Clave de acceso " + factura.getInfoTributaria().getClaveAcceso());
                    Factura facturaFirmada = factura;
                    nombreArchivo = facturaFirmada.getInfoTributaria().getClaveAcceso() + ".xml";
                    TestSRI e = new TestSRI();
                    try {
                        System.out.println(e.enviarComprobante(ruta_archivo_firmado + nombreArchivo, "1"));
                        Thread.sleep(3000);
                        System.out.println(e.autorizarComprobanteIndividualTRCRUC_PROCE(facturavia.getCoest(), facturavia.getIdent(), facturaFirmada.getInfoTributaria().getClaveAcceso(), "1", ruta_archivo_autorizado, ruta_archivo_noautorizado));
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
    }

    public static Factura construirFacturaElectronica(TrcRUC facturavia) {

        String tipoComprobante = "01";
        String ruc = "0913814455001";
        String ambiente = "1"; //1 pruebas, 2 producción
        String serie = String.format("%03d", facturavia.getCoest()) + String.format("%03d", facturavia.getNuvia()); //serie arbitraria
        String numeroComprobante = String.format("%09d", facturavia.getTicke()); //inicio arbitrario
        String codigoNumerico = "12344321";
        String tipoEmision = "1"; //1 normal, 2 contingencia

        Factura f = new Factura();
        f.setVersion("1.1.0");
        f.setId("comprobante");
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente(ambiente);
        infoTributaria.setRazonSocial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setNombreComercial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setRuc(ruc);
//        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(date, tipoComprobante, ruc, ambiente, serie, numeroComprobante, codigoNumerico, tipoEmision));
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(facturavia.getFecha(), tipoComprobante, ruc, ambiente, serie, numeroComprobante, codigoNumerico, tipoEmision));

        infoTributaria.setCodDoc("01");
        infoTributaria.setEstab(String.format("%03d", facturavia.getCoest()));
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi(String.format("%03d", facturavia.getNuvia()));
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz("CDLA NAVAL NORTE MZ 5 VILLA 12");

        f.setInfoTributaria(infoTributaria);

        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(facturavia.getImpor());
        totimp1.setCodigo("2"); //Este código indica que es IVA
        totimp1.setCodigoPorcentaje("0"); //Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp1.setValor(new BigDecimal("0.00")); //CORREGIR Y PONER EL RESULTADO DE LA OPERACION DE PORCENTAJE IVA CALCULADO PARA BASE IMPONIBLE

        TotalImpuesto totimp2 = new TotalImpuesto();
        totimp2.setBaseImponible(new BigDecimal("0.00"));
        totimp2.setCodigo("2"); //Este código indica que es IVA
        totimp2.setCodigoPorcentaje("2");//Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp2.setValor(new BigDecimal("0.00"));

        List<TotalImpuesto> totalConImpuestos = new ArrayList<TotalImpuesto>();

        totalConImpuestos.add(totimp1);
        totalConImpuestos.add(totimp2);

        InfoFactura infoFactura = new InfoFactura();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDocumento = df.format(facturavia.getFecha());

        infoFactura.setFechaEmision(fechaDocumento);
        infoFactura.setDirEstablecimiento("CDLA NAVAL NORTE MZ 5 VILLA 12");
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

//        CampoAdicional campadc1 = new CampoAdicional();
//        campadc1.setNombre("Codigo Impuesto ISD");
//        campadc1.setValor("4580");
//
//        CampoAdicional campadc2 = new CampoAdicional();
//        campadc2.setNombre("Impuesto ISD");
//        campadc2.setValor("15.42x");
//
//        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();
//        infoAdicional.add(campadc1);
//        infoAdicional.add(campadc2);
//        f.setCampoAdicional(infoAdicional);
        List<Detalle> detalle = new ArrayList<Detalle>();

        //Detalle 1
        Detalle d1 = new Detalle();
        d1.setCodigoPrincipal("pea-" + String.format("%03d", facturavia.getCoest()) + "-" + String.format("%02d", facturavia.getTarif()));
        d1.setCodigoAuxiliar("tar-" + String.format("%03d", facturavia.getCoest()) + "-" + String.format("%02d", facturavia.getTarif()));
        d1.setDescripcion("Paso por peaje estacion no. " + String.format("%03d", facturavia.getCoest()) + ", categoria " + String.format("%02d", facturavia.getTarif()));
        d1.setCantidad(new BigDecimal("1.00"));
        d1.setPrecioUnitario(facturavia.getImpor());
        d1.setDescuento(new BigDecimal("0.00"));
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
        i11.setTarifa(new BigDecimal("0"));
        i11.setBaseImponible(facturavia.getImpor());
        i11.setValor(new BigDecimal("0.00"));

        Impuesto i12 = new Impuesto();
        i12.setCodigo("2");
        i12.setCodigoPorcentaje("2");
        i12.setTarifa(new BigDecimal("12.00"));
        i12.setBaseImponible(new BigDecimal("0.00"));
        i12.setValor(new BigDecimal("0.00"));

        impuesto1.add(i11);
        impuesto1.add(i12);

        d1.setImpuesto(impuesto1);

        detalle.add(d1);

        f.setDetalle(detalle);

//        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();
//
//        CampoAdicional ca1 = new CampoAdicional();
//        ca1.setNombre("direccionCliente");
//        ca1.setValor("ORELLANA 211 Y PANAMA");
//
//        CampoAdicional ca2 = new CampoAdicional();
//        ca2.setNombre("telefonoCliente");
//        ca2.setValor("042300185");
//
//        infoAdicional.add(ca1);
//        infoAdicional.add(ca2);
//
//        f.setCampoAdicional(infoAdicional);
        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{Factura.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    ruta_archivo_firmar + infoTributaria.getClaveAcceso() + ".xml"), "UTF-8");
            marshaller.marshal(f, out);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return f;
    }

}
