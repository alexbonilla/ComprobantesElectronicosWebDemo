/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.retencion.ComprobanteRetencion;
import com.iveloper.comprobantes.modelo.retencion.Impuesto;
import com.iveloper.comprobantes.modelo.retencion.InfoCompRetencion;
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
public class ComprobanteRetencionTest {

    public static void main(String[] args) {

        Date date = new Date();
        String tipoComprobante = "07";
        String ruc = "0913814455001";
        String ambiente = "1";
        String serie = "001001";
        String numeroComprobante = "000000300";
        String codigoNumerico = "19820420";
        String tipoEmision = "1";
        
        ComprobanteRetencion comp = new ComprobanteRetencion();

        comp.setVersion("1.1.0");
        comp.setId("comprobante");
        
        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente("1");
        infoTributaria.setRazonSocial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setNombreComercial("ALEX FERNANDO BONILLA GORDILLO");
        infoTributaria.setRuc(ruc);
        infoTributaria.setClaveAcceso(ClaveAcceso.generarClaveAcceso(date, tipoComprobante, ruc, ambiente, serie, numeroComprobante, codigoNumerico, tipoEmision));
        infoTributaria.setCodDoc(tipoComprobante);
        infoTributaria.setEstab("001");
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi("001");
        infoTributaria.setSecuencial(numeroComprobante);
        infoTributaria.setDirMatriz("CDLA NAVAL NORTE MZ 5 VILLA 12");

        comp.setInfoTributaria(infoTributaria);

        InfoCompRetencion infoCompRetencion = new InfoCompRetencion();
        infoCompRetencion.setFechaEmision("03/02/2014");
        infoCompRetencion.setDirEstablecimiento("CDLA NAVAL NORTE MZ 5 VILLA 12");
//        infoCompRetencion.setContribuyenteEspecial("5368");
        infoCompRetencion.setObligadoContabilidad("NO");
        infoCompRetencion.setTipoIdentificacionSujetoRetenido("04");
        infoCompRetencion.setRazonSocialSujetoRetenido("CONEXION TOTAL S.A.");
        infoCompRetencion.setIdentificacionSujetoRetenido("0992685491001");
        infoCompRetencion.setPeriodoFiscal("02/2014");

        comp.setInfoCompRetencion(infoCompRetencion);

        Impuesto impuesto_1 = new Impuesto();
        impuesto_1.setCodigo("2");
        impuesto_1.setCodigoRetencion("1");
        impuesto_1.setBaseImponible(new BigDecimal("101.94"));
        impuesto_1.setPorcentajeRetener(new BigDecimal("30"));
        impuesto_1.setValorRetenido(new BigDecimal("30.58"));
        impuesto_1.setCodDocSustento("01");
        impuesto_1.setNumDocSustento("002001000000001");
        impuesto_1.setFechaEmisionDocSustento("20/01/2012");

        List<Impuesto> impuestos = new ArrayList<>();
        impuestos.add(impuesto_1);
        
        comp.setImpuesto(impuestos);

        CampoAdicional campoAdicional = new CampoAdicional();
        campoAdicional.setNombre("ConvenioDobleTributacion");
        campoAdicional.setValor("MA123456");

        List<CampoAdicional> infoAdicional = new ArrayList<>();
        infoAdicional.add(campoAdicional);

        
        comp.setCampoAdicional(infoAdicional);

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{ComprobanteRetencion.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    "C:\\Users\\Alex\\Downloads\\test01_compretend.xml"), "UTF-8");
            marshaller.marshal(comp, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
