/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.notadebito.InfoNotaDebito;
import com.iveloper.comprobantes.modelo.notadebito.NotaDebito;
import com.iveloper.comprobantes.modelo.Impuesto;
import com.iveloper.comprobantes.modelo.notadebito.Motivo;
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
public class NotaDebitoTest {

    public static void main(String[] args) {

        NotaDebito nd = new NotaDebito();

        nd.setVersion("1.1.0");
        nd.setId("comprobante");

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente("1");
        infoTributaria.setRazonSocial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setNombreComercial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setRuc("1768153530001");
        infoTributaria.setClaveAcceso("0403201301179226110400110015010000000081234567816");
        infoTributaria.setCodDoc("05");
        infoTributaria.setEstab("001");
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi("001");
        infoTributaria.setSecuencial("000000008");
        infoTributaria.setDirMatriz("Alpallana");

        nd.setInfoTributaria(infoTributaria);

        InfoNotaDebito infoNotaDebito = new InfoNotaDebito();
        infoNotaDebito.setFechaEmision("02/01/2014");
        infoNotaDebito.setDirEstablecimiento("Alpallana");
        infoNotaDebito.setContribuyenteEspecial("5368");
        infoNotaDebito.setObligadoContabilidad("SI");
        infoNotaDebito.setTipoIdentificacionComprador("04");

        infoNotaDebito.setRazonSocialComprador("PRUEBAS SERVICIO DERENTAS INTERNAS");
        infoNotaDebito.setIdentificacionComprador("1760013210001");
        infoNotaDebito.setRise("Contribuyente Régimen Simplificado RISE");
        infoNotaDebito.setCodDocModificado("01");
        infoNotaDebito.setNumDocModificado("002-001-000000001");
        infoNotaDebito.setFechaEmisionDocSustento("21/10/2011");
        infoNotaDebito.setTotalSinImpuestos(new BigDecimal("1000.00"));

        Impuesto i1 = new Impuesto();
        i1.setCodigo("2");
        i1.setCodigoPorcentaje("2");
        i1.setTarifa(new BigDecimal("12.00"));
        i1.setBaseImponible(new BigDecimal("1000.00"));
        i1.setValor(new BigDecimal("120.00"));

        ArrayList<Impuesto> impuestos = new ArrayList<>();
        impuestos.add(i1);

        infoNotaDebito.setImpuesto(impuestos);

        infoNotaDebito.setValorTotal(new BigDecimal("1120.00"));

        nd.setInfoNotaDebito(infoNotaDebito);

        Motivo motiv = new Motivo();
        motiv.setRazon("Interés por mora");
        motiv.setValor(new BigDecimal("1000.00"));

        ArrayList<Motivo> motivos = new ArrayList<>();
        motivos.add(motiv);

        nd.setMotivo(motivos);

        CampoAdicional campoAdicional = new CampoAdicional();
        campoAdicional.setNombre("ConvenioDobleTributacion");
        campoAdicional.setValor("MA123456");

        List<CampoAdicional> infoAdicional = new ArrayList<>();
        infoAdicional.add(campoAdicional);

        nd.setCampoAdicional(infoAdicional);

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{NotaDebito.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    "C:\\Users\\Alex\\Downloads\\test01_notadebito.xml"), "UTF-8");
            marshaller.marshal(nd, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
