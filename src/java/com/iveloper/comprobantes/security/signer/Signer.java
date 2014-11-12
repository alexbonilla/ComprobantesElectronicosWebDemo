/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iveloper.comprobantes.security.signer;

/**
 *
 * @author Alex
 */
import es.mityc.firmaJava.libreria.xades.DataToSign;
import es.mityc.firmaJava.libreria.xades.EnumFormatoFirma;
import es.mityc.firmaJava.libreria.xades.XAdESSchemas;
import es.mityc.javasign.xml.refs.InternObjectToSign;
import es.mityc.javasign.xml.refs.ObjectToSign;
import org.w3c.dom.Document;

/**
 *
 * @author Rolando
 */
public class Signer extends GenericXMLSignature {

    private String rutaDocumentoAFirmar;
    private String rutaDocumentoFirmado;

    public Signer(String rutaDocumentoAFirmar, String rutaDocumentoFirmado, String claveFirma) {
        this.rutaDocumentoAFirmar = rutaDocumentoAFirmar;
        this.rutaDocumentoFirmado = rutaDocumentoFirmado;
        PASSWORD = claveFirma;
    }

    @Override
    protected DataToSign createDataToSign() {
        DataToSign dataToSign = new DataToSign();
        try {
            dataToSign.setXadesFormat(EnumFormatoFirma.XAdES_BES);
            dataToSign.setEsquema(XAdESSchemas.XAdES_132);
            dataToSign.setXMLEncoding("UTF-8");
            dataToSign.setEnveloped(true);
            dataToSign.addObject(new ObjectToSign(new InternObjectToSign("comprobante"), "Documento de ejemplo", null, "text/xml", null));
            Document docToSign = getDocument(getRutaDocumentoAFirmar());
            dataToSign.setDocument(docToSign);
        } catch (Exception ex) {
            dataToSign = null;
            System.out.println(ex.getMessage());
        } finally {
            return dataToSign;
        }
    }

    @Override
    protected String getSignatureFileName() {
        return rutaDocumentoFirmado;
    }

    public void firmar() {
        execute();
    }

    /**
     * @return the rutaDocumentoAFirmar
     */
    public String getRutaDocumentoAFirmar() {
        return rutaDocumentoAFirmar;
    }

    /**
     * @param rutaDocumentoAFirmar the rutaDocumentoAFirmar to set
     */
    public void setRutaDocumentoAFirmar(String rutaDocumentoAFirmar) {
        this.rutaDocumentoAFirmar = rutaDocumentoAFirmar;
    }

    /**
     * @return the rutaDocumentoFirmado
     */
    public String getRutaDocumentoFirmado() {
        return rutaDocumentoFirmado;
    }

    /**
     * @param rutaDocumentoFirmado the rutaDocumentoFirmado to set
     */
    public void setRutaDocumentoFirmado(String rutaDocumentoFirmado) {
        this.rutaDocumentoFirmado = rutaDocumentoFirmado;
    }

    public static void main(String... args) {
        String ruta_archivo_firmar="C:\\Users\\Alex\\Downloads\\test01_factura.xml";
        String ruta_archivo_firmado="C:\\Users\\Alex\\Downloads\\firmado\\test01_factura.xml";
//        if (args.length == 1) {
            //0. ruta del archivo a firmar
            //1. ruta de salida del archivo firmado (carpeta y nombre.xml)
            //2. contraseña
            Signer prueba = new Signer(ruta_archivo_firmar, ruta_archivo_firmado, "bgNILL1982$");
            prueba.firmar();
            System.out.println("E99TODO OK");
//        } else {
//            System.out.println("<ruta archivo a firmar> <ruta archivo firmado> <clave de archivo .p12>");
//        }
    }
}
