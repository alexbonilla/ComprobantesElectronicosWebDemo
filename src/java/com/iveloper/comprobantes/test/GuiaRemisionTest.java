/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.DetAdicional;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.guiaremision.Destinatario;
import com.iveloper.comprobantes.modelo.guiaremision.GuiaDetalle;
import com.iveloper.comprobantes.modelo.guiaremision.GuiaRemision;
import com.iveloper.comprobantes.modelo.guiaremision.InfoGuiaRemision;
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
public class GuiaRemisionTest {

    public static void main(String[] args) {

        GuiaRemision gr = new GuiaRemision();

        gr.setVersion("1.1.0");
        gr.setId("comprobante");

        InfoTributaria infoTributaria = new InfoTributaria();
        infoTributaria.setAmbiente("1");
        infoTributaria.setRazonSocial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setNombreComercial("EMPRESA PUBLICA DE HIDROCARBUROS DEL ECUADOR EP PETROECUADOR");
        infoTributaria.setRuc("1768153530001");
        infoTributaria.setClaveAcceso("0403201301179226110400110015010000000081234567816");
        infoTributaria.setCodDoc("01");
        infoTributaria.setEstab("001");
        infoTributaria.setTipoEmision("1");
        infoTributaria.setPtoEmi("001");
        infoTributaria.setSecuencial("000000008");
        infoTributaria.setDirMatriz("Alpallana");

        gr.setInfoTributaria(infoTributaria);

        InfoGuiaRemision infoGR = new InfoGuiaRemision();

        infoGR.setDirEstablecimiento("Rodrigo Moreno S/N Francisco García");
        infoGR.setDirPartida("Av. Eloy Alfaro 34 y Av. Libertad Esq.");
        infoGR.setRazonSocialTransportista("Transportes S.A.");
        infoGR.setTipoIdentificacionTransportista("04");
        infoGR.setRucTransportista("1796875790001");
        infoGR.setRise("Contribuyente Regimen Simplificado RISE");
        infoGR.setContribuyenteEspecial("5368");
        infoGR.setObligadoContabilidad("SI");
        infoGR.setFechaIniTransporte("21/10/2011");
        infoGR.setFechaFinTransporte("22/10/2011");
        infoGR.setPlaca("MCL0827");

        gr.setInfoGuiaRemision(infoGR);

        Destinatario destinatario = new Destinatario();
        destinatario.setIdentificacionDestinatario("1716849140001");
        destinatario.setRazonSocialDestinatario("Alvarez Mina John Henry");
        destinatario.setDirDestinatario("Av. Simón Bolívar S/N Intercambiador");
        destinatario.setMotivoTraslado("Venta de Maquinaria de Impresión");
        destinatario.setDocAduaneroUnico("0041324846887");
        destinatario.setCodEstabDestino("001");
        destinatario.setRuta("Quito – Cayambe - Otavalo");
        destinatario.setCodDocSustento("01");
        destinatario.setNumDocSustento("002-001-000000001");
        destinatario.setNumAutDocSustento("2110201116302517921467390011234567891");
        destinatario.setFechaEmisionDocSustento("21/10/2011");

        GuiaDetalle detalle = new GuiaDetalle();
        detalle.setCodigoInterno("125BJC-01");
        detalle.setCodigoAdicional("1234D56789-A");
        detalle.setDescripcion("CAMIONETA 4X4 DIESEL 3.7");
        detalle.setCantidad(new BigDecimal("10.00"));

            DetAdicional detalleadicional = new DetAdicional();
            detalleadicional.setNombre("Marca");
            detalleadicional.setValor("Chevrolet");

            ArrayList<DetAdicional> detallesadicionales = new ArrayList();
            detallesadicionales.add(detalleadicional);

        detalle.setDetAdicional(detallesadicionales);
        
        ArrayList<GuiaDetalle> detalles = new ArrayList();
        detalles.add(detalle);
        
        destinatario.setDetalle(detalles);
        
        ArrayList<Destinatario> destinatarios = new ArrayList<>();
        destinatarios.add(destinatario);

        gr.setDestinatarios(destinatarios);                

        CampoAdicional campoAdicional = new CampoAdicional();
        campoAdicional.setNombre("ConvenioDobleTributacion");
        campoAdicional.setValor("MA123456");

        List<CampoAdicional> infoAdicional = new ArrayList<>();
        infoAdicional.add(campoAdicional);

        gr.setCampoAdicional(infoAdicional);

        try {
            JAXBContext context = JAXBContext.newInstance(
                    new Class[]{GuiaRemision.class});
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.encoding", "UTF-8");
            marshaller.setProperty("jaxb.formatted.output",
                    Boolean.valueOf(true));
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(
                    "C:\\Users\\Alex\\Downloads\\test01_guiaremision.xml"), "UTF-8");
            marshaller.marshal(gr, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
