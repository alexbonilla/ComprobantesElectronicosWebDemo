/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.Detalle;
import com.iveloper.comprobantes.modelo.Impuesto;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.TotalImpuesto;
import com.iveloper.comprobantes.modelo.factura.Factura;
import com.iveloper.comprobantes.modelo.factura.InfoFactura;
import com.iveloper.comprobantes.utils.ClaveAcceso;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Alex
 */
public class FacturaTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Date date = new Date();
        String tipoComprobante = "01";
        String ruc = "0913814455001";
        String ambiente = "2";
        String serie = "001001";
        String numeroComprobante = "000000001";
        String codigoNumerico = "19820420";
        String tipoEmision = "1";

        Factura f = new Factura();
        f.setVersion("1.1.0");
        f.setId("comprobante");
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente("2");
        infoTributaria.setRazonSocial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setNombreComercial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setRuc(ruc);
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(date, tipoComprobante, ruc, ambiente, serie, numeroComprobante, codigoNumerico, tipoEmision));
        infoTributaria.setCodDoc("01");
        infoTributaria.setEstab("001");
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi("001");
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz("CDLA NAVAL NORTE MZ 5 VILLA 12");

        f.setInfoTributaria(infoTributaria);

        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(new BigDecimal("1000.00"));
        totimp1.setCodigo("2"); //Este código indica que es IVA
        totimp1.setCodigoPorcentaje("2"); //Este código indica que el valor es 12% (0=0%;2=12%;6=No objeto de impuesto)
        totimp1.setValor(new BigDecimal("120.00"));

//        TotalImpuesto totimp2 = new TotalImpuesto();
//        totimp2.setBaseImponible(new BigDecimal("64.94"));
//        totimp2.setCodigo("3");//Este código indica que es ICE
//        totimp2.setCodigoPorcentaje("3620");//Este código indica que es una caja de Videojuego
//        totimp2.setValor(new BigDecimal("3.25"));
        List<TotalImpuesto> totalConImpuestos = new ArrayList<TotalImpuesto>();

        totalConImpuestos.add(totimp1);
//        totalConImpuestos.add(totimp2);

        InfoFactura infoFactura = new InfoFactura();
        infoFactura.setFechaEmision("17/02/2014");
        infoFactura.setDirEstablecimiento("CDLA NAVAL NORTE MZ 5 VILLA 12");
//        infoFactura.setContribuyenteEspecial("5368");
        infoFactura.setObligadoContabilidad("NO");
        infoFactura.setTipoIdentificacionComprador("04");
//        infoFactura.setGuiaRemision("001-001-000000001");
        infoFactura.setRazonSocialComprador("ADLIB S.A.");
        infoFactura.setIdentificacionComprador("0992836849001");
        infoFactura.setTotalSinImpuestos(new BigDecimal("1000.00"));
        infoFactura.setTotalDescuento(BigDecimal.ZERO);
        infoFactura.setTotalImpuesto(totalConImpuestos);
        infoFactura.setPropina(BigDecimal.ZERO);
        infoFactura.setImporteTotal(new BigDecimal("1120.00"));
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
        d1.setCodigoPrincipal("web-001-01");
        d1.setCodigoAuxiliar("impl-wrd-01");
        d1.setDescripcion("Implementacion Pagina Web");
        d1.setCantidad(new BigDecimal("1.00"));
        d1.setPrecioUnitario(new BigDecimal("750.00"));
        d1.setDescuento(new BigDecimal("0.00"));
        d1.setPrecioTotalSinImpuesto(new BigDecimal("750.00"));
//        List<DetAdicional> d1da = new ArrayList<DetAdicional>();
//        DetAdicional d1da1 = new DetAdicional();
//        d1da1.setNombre("CMS");
//        d1da1.setValor("Wordpress");
//        DetAdicional d1da2 = new DetAdicional();
//        d1da2.setNombre("Diseno");
//        d1da2.setValor("No");
//
//        d1da.add(d1da1);
//        d1da.add(d1da2);
//
//        d1.setDetAdicional(d1da);

        List<Impuesto> impuesto1 = new ArrayList<Impuesto>();
        Impuesto i11 = new Impuesto();
        i11.setCodigo("2");
        i11.setCodigoPorcentaje("2");
        i11.setTarifa(new BigDecimal("12"));
        i11.setBaseImponible(new BigDecimal("750.00"));
        i11.setValor(new BigDecimal("90.00"));
        impuesto1.add(i11);
        d1.setImpuesto(impuesto1);

        //Detalle 2
        Detalle d2 = new Detalle();
        d2.setCodigoPrincipal("web-001-20");
        d2.setCodigoAuxiliar("trdcc-wrd-01");
        d2.setDescripcion("Otro idioma pagina web existente");
        d2.setCantidad(new BigDecimal("1.00"));
        d2.setPrecioUnitario(new BigDecimal("150.00"));
        d2.setDescuento(new BigDecimal("0.00"));
        d2.setPrecioTotalSinImpuesto(new BigDecimal("150.00"));
//        List<DetAdicional> d2da = new ArrayList<DetAdicional>();
//        DetAdicional d2da1 = new DetAdicional();
//        d2da1.setNombre("CMS");
//        d2da1.setValor("Wordpress");
//        DetAdicional d2da2 = new DetAdicional();
//        d2da2.setNombre("Diseno");
//        d2da2.setValor("No");
//
//        d2da.add(d2da1);
//        d2da.add(d2da2);
//
//        d2.setDetAdicional(d2da);

        List<Impuesto> impuesto2 = new ArrayList<Impuesto>();
        Impuesto i21 = new Impuesto();
        i21.setCodigo("2");
        i21.setCodigoPorcentaje("2");
        i21.setTarifa(new BigDecimal("12"));
        i21.setBaseImponible(new BigDecimal("150.00"));
        i21.setValor(new BigDecimal("18.00"));
        impuesto2.add(i21);
        d2.setImpuesto(impuesto2);

        //Detalle 3
        Detalle d3 = new Detalle();
        d3.setCodigoPrincipal("web-002-01");
        d3.setCodigoAuxiliar("hostg-hatching-01");
        d3.setDescripcion("Web Hosting 50GB para 1 Dominio");
        d3.setCantidad(new BigDecimal("1.00"));
        d3.setPrecioUnitario(new BigDecimal("100.00"));
        d3.setDescuento(new BigDecimal("0.00"));
        d3.setPrecioTotalSinImpuesto(new BigDecimal("100.00"));
//        List<DetAdicional> d3da = new ArrayList<DetAdicional>();
//        DetAdicional d3da1 = new DetAdicional();
//        d3da1.setNombre("Capacidad");
//        d3da1.setValor("50 GB");
//        DetAdicional d3da2 = new DetAdicional();
//        d3da2.setNombre("Dominios alojables");
//        d3da2.setValor("1");
//        DetAdicional d3da3 = new DetAdicional();
//        d3da3.setNombre("Periodo");
//        d3da3.setValor("1 anio");
//
//        d3da.add(d3da1);
//        d3da.add(d3da2);
//
//        d3.setDetAdicional(d3da);

        List<Impuesto> impuesto3 = new ArrayList<Impuesto>();
        Impuesto i31 = new Impuesto();
        i31.setCodigo("2");
        i31.setCodigoPorcentaje("2");
        i31.setTarifa(new BigDecimal("12"));
        i31.setBaseImponible(new BigDecimal("100.00"));
        i31.setValor(new BigDecimal("12.00"));
        impuesto3.add(i31);
        d3.setImpuesto(impuesto3);

//        List<Impuesto> impuesto = new ArrayList<Impuesto>();
////        Impuesto i1 = new Impuesto();
////        i1.setCodigo("3");
////        i1.setCodigoPorcentaje("3072");
////        i1.setTarifa(new BigDecimal("5"));
////        i1.setBaseImponible(new BigDecimal("295000.00"));
////        i1.setValor(new BigDecimal("14750.00"));
//        Impuesto i2 = new Impuesto();
//        i2.setCodigo("2");
//        i2.setCodigoPorcentaje("2");
//        i2.setTarifa(new BigDecimal("12"));
//        i2.setBaseImponible(new BigDecimal("68.19"));
//        i2.setValor(new BigDecimal("8.18"));
////        impuesto.add(i1);
//        impuesto.add(i2);
//        d1.setImpuesto(impuesto);
        detalle.add(d1);
        detalle.add(d2);
        detalle.add(d3);

        f.setDetalle(detalle);

        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();

        CampoAdicional ca1 = new CampoAdicional();
        ca1.setNombre("direccionCliente");
        ca1.setValor("ORELLANA 211 Y PANAMA");
        
        CampoAdicional ca2 = new CampoAdicional();
        ca2.setNombre("telefonoCliente");
        ca2.setValor("042300185");        
        
        infoAdicional.add(ca1);
        infoAdicional.add(ca2);
        
        f.setCampoAdicional(infoAdicional);

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{Factura.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    "C:\\Users\\Alex\\Downloads\\test01_factura.xml"), "UTF-8");
            marshaller.marshal(f, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
