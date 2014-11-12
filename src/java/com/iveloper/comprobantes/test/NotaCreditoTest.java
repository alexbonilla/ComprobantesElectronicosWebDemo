/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.DetAdicional;
import com.iveloper.comprobantes.modelo.Detalle;
import com.iveloper.comprobantes.modelo.Impuesto;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.TotalImpuesto;
import com.iveloper.comprobantes.modelo.notacredito.InfoNotaCredito;
import com.iveloper.comprobantes.modelo.notacredito.NotaCredito;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Alex
 */
public class NotaCreditoTest {

    public static void main(String[] args) {
        // TODO code application logic here
        OutputStreamWriter out = null;
        
        NotaCredito f = new NotaCredito();
        f.setVersion("1.1.0");
        f.setId("comprobante");
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente("1");
        infoTributaria.setRazonSocial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setNombreComercial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setRuc("1768153530001");
        infoTributaria.setClaveAcceso("0403201301179226110400110015010000000081234567816");
        infoTributaria.setCodDoc("04");
        infoTributaria.setEstab("001");
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi("001");
        infoTributaria.setSecuencial("000000008");
        infoTributaria.setDirMatriz("Alpallana");
        
        f.setInfoTributaria(infoTributaria);
        
        TotalImpuesto totimp1 = new TotalImpuesto();
        totimp1.setBaseImponible(new BigDecimal("68.19"));
        totimp1.setCodigo("2");
        totimp1.setCodigoPorcentaje("2");
        totimp1.setValor(new BigDecimal("8.18"));
        
        TotalImpuesto totimp2 = new TotalImpuesto();
        totimp2.setBaseImponible(new BigDecimal("64.94"));
        totimp2.setCodigo("3");
        totimp2.setCodigoPorcentaje("3072");
        totimp2.setValor(new BigDecimal("3.25"));
        
        List<TotalImpuesto> totalConImpuestos = new ArrayList<>();
        
        totalConImpuestos.add(totimp1);
        totalConImpuestos.add(totimp2);
        
        InfoNotaCredito infoN = new InfoNotaCredito();
        infoN.setFechaEmision("02/01/2014");
        infoN.setDirEstablecimiento("Alpallana");
        infoN.setContribuyenteEspecial("5368");
        infoN.setObligadoContabilidad("SI");
        infoN.setTipoIdentificacionComprador("04");
        
        infoN.setRazonSocialComprador("PRUEBAS SERVICIO DERENTAS INTERNAS");
        infoN.setIdentificacionComprador("1760013210001");
        infoN.setRise("Contribuyente Régimen Simplificado RISE");
        infoN.setCodDocModificado("01");
        infoN.setNumDocModificado("002-001-000000001");
        infoN.setFechaEmisionDocSustento("21/10/2011");
        infoN.setTotalSinImpuestos(new BigDecimal("64.94"));
        infoN.setValorModificacion(new BigDecimal("346920.00"));
        
        String motivo = "Mal facturado";
        
        infoN.setMotivo(motivo);
        
        infoN.setTotalImpuesto(totalConImpuestos);
        
        infoN.setMoneda("DOLAR");
        
        f.setInfoNotaCredito(infoN);
        
        CampoAdicional campadc1 = new CampoAdicional();
        campadc1.setNombre("Codigo Impuesto ISD");
        campadc1.setValor("4580");
        
        CampoAdicional campadc2 = new CampoAdicional();
        campadc2.setNombre("Impuesto ISD");
        campadc2.setValor("15.42x");
        
        List<CampoAdicional> infoAdicional = new ArrayList<CampoAdicional>();
        infoAdicional.add(campadc1);
        infoAdicional.add(campadc2);
        
        f.setCampoAdicional(infoAdicional);
        
        List<Detalle> detalle = new ArrayList<Detalle>();
        Detalle d1 = new Detalle();
        //d1.setCodigoPrincipal("125BCJ-01");
        d1.setCodigoInterno("125BCJ-01");
        //d1.setCodigoAuxiliar("1234D56789-A");
        d1.setCodigoAdicional("1234D56789-A");
        d1.setDescripcion("CAMIONETA 4X4 DIESEL 3.7");
        d1.setCantidad(new BigDecimal("10.00"));
        d1.setPrecioUnitario(new BigDecimal("30000.00"));
        d1.setDescuento(new BigDecimal("5000.00"));
        d1.setPrecioTotalSinImpuesto(new BigDecimal("295000.00"));
        List<DetAdicional> da = new ArrayList<DetAdicional>();
        DetAdicional da1 = new DetAdicional();
        da1.setNombre("Marca Chevrolet");
        da1.setValor("Chevrolet");
        DetAdicional da2 = new DetAdicional();
        da2.setNombre("Modelo");
        da2.setValor("2012");
        DetAdicional da3 = new DetAdicional();
        da3.setNombre("Chasis");
        da3.setValor("8LDETA03V20003289");
        da.add(da1);
        da.add(da2);
        da.add(da3);
        d1.setDetAdicional(da);
        List<Impuesto> impuesto = new ArrayList<Impuesto>();
        Impuesto i1 = new Impuesto();
        i1.setCodigo("3");
        i1.setCodigoPorcentaje("3072");
        i1.setTarifa(new BigDecimal("5"));
        i1.setBaseImponible(new BigDecimal("295000.00"));
        i1.setValor(new BigDecimal("14750.00"));
        Impuesto i2 = new Impuesto();
        i2.setCodigo("2");
        i2.setCodigoPorcentaje("2");
        i2.setTarifa(new BigDecimal("12"));
        i2.setBaseImponible(new BigDecimal("309750.00"));
        i2.setValor(new BigDecimal("37170.00"));
        impuesto.add(i1);
        impuesto.add(i2);
        d1.setImpuesto(impuesto);
        detalle.add(d1);
        f.setDetalle(detalle);
        
        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{NotaCredito.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            out = new OutputStreamWriter(new FileOutputStream(
                    "C:\\Users\\Alex\\Downloads\\test01_notacredito.xml"), "UTF-8");
            marshaller.marshal(f, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
