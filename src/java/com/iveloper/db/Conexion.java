package com.iveloper.db;

import com.iveloper.comprobantes.modelo.CampoAdicional;
import com.iveloper.comprobantes.modelo.DetAdicional;
import com.iveloper.comprobantes.modelo.Detalle;
import com.iveloper.comprobantes.modelo.Impuesto;
import com.iveloper.comprobantes.modelo.InfoTributaria;
import com.iveloper.comprobantes.modelo.TotalImpuesto;
import com.iveloper.comprobantes.modelo.factura.Factura;
import com.iveloper.comprobantes.modelo.factura.InfoFactura;
import com.iveloper.entidades.Cliente;
import com.iveloper.entidades.Cuenta;
import com.iveloper.entidades.Emisor;
import com.iveloper.entidades.TrcRUC;

import ec.gob.sri.comprobantes.ws.aut.Autorizacion;
import ec.gob.sri.comprobantes.ws.aut.Mensaje;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

public class Conexion {

    private Connection con;
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String PGSQL_DRIVER = "org.postgresql.Driver";
    private static final String MYSQL_DBMS = "mysql";
    private static final String MSSQL_DBMS = "sqlserver";
    private static final String PGSQL_DBMS = "postgresql";

    private String driver = MYSQL_DRIVER;
    private String dbms = MYSQL_DBMS;

    private String host = "localhost";
    private String port = "3306";
    private String database = "ce";
    private String user = "root";
    private String password = "fenetre";

    public Conexion() {
        try {
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(System.getProperty("pwdCrpt"));
            Properties props = new EncryptableProperties(encryptor);
            props.load(new FileInputStream("C:\\Users\\Alex\\Documents\\NetBeansProjects\\ComprobantesElectronicosWebDemo\\web\\WEB-INF\\configuration.properties"));
            host = props.getProperty("datasource.hostname");
            port = props.getProperty("datasource.port");
            database = props.getProperty("datasource.database");
            user = props.getProperty("datasource.username");
            password = props.getProperty("datasource.password");

            String dbvendor_str = props.getProperty("datasource.dbvendor");
            setDBVendor(Integer.parseInt(dbvendor_str));
        } catch (IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Conexion(String ruta_configuracion_db) {
        try {
//            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
//            encryptor.setPassword(System.getProperty("pwdCrpt"));
            Properties props = new Properties();
            props.load(new FileInputStream(ruta_configuracion_db));
            host = props.getProperty("datasource.hostname");
            port = props.getProperty("datasource.port");
            database = props.getProperty("datasource.database");
            user = props.getProperty("datasource.username");
            password = props.getProperty("datasource.password");

            String dbvendor_str = props.getProperty("datasource.dbvendor");
            setDBVendor(Integer.parseInt(dbvendor_str));
        } catch (IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Conexion(String host, String port, String database, String user, String password, int dbvendor) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        setDBVendor(dbvendor);
    }

    private void setDBVendor(int dbvendor) {
        switch (dbvendor) {
            case 0:
                driver = MYSQL_DRIVER;
                dbms = MYSQL_DBMS;
                break;
            case 1:
                driver = MSSQL_DRIVER;
                dbms = MSSQL_DBMS;
                break;
            case 2:
                driver = PGSQL_DRIVER;
                dbms = PGSQL_DBMS;
                break;
            default:
                driver = MYSQL_DRIVER;
                dbms = MYSQL_DBMS;
                break;
        }
    }

    /*
     * METODO CONECTAR
     */
    public void conectar() throws Exception {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ce) {
            System.out.println("ERROR: No se encontró driver para la base de datos en Conexion.conectar: " + ce);
        }
        try {

            switch (dbms) {
                case MYSQL_DBMS:
                    this.con = DriverManager.getConnection("jdbc:" + dbms + "://" + host + ":" + port + "/" + database, user, password);
                    break;
                case MSSQL_DBMS:
                    this.con = DriverManager.getConnection("jdbc:" + dbms + "://" + host + ":" + port + ";"
                            + "databaseName=" + database + ";user=" + user + ";password=" + password + ";");
                    break;
                case PGSQL_DBMS:
                    this.con = DriverManager.getConnection("jdbc:" + dbms + "://" + host + ":" + port + "/" + database, user, password);
                    break;
                default:
                    this.con = DriverManager.getConnection("jdbc:" + dbms + "://" + host + ":" + port + "/" + database, user, password);
                    break;
            }

            System.out.println("EVENTO: Conexión exitosa con la base de datos");
        } catch (SQLException exception) {
            System.out.println("ERROR: No se pudo conectar con la base de datos en Conexion.conectar: " + exception);
        }

    }

    public boolean desconectar() {
        try {
            this.con.close();
            return (true);
        } catch (SQLException exception) {
            System.out.println("ERROR: No se pudo desconectar de la base de datos en Conexion.desconectar: " + exception);
            return (false);
        }
    }

    public boolean estaConectado() {
        try {
            return !(this.con.isClosed()) && this.con.isValid(2);
        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public Connection getCon() {
        return con;
    }

    public Cuenta obtenerCuentaPorId(int idcuenta) {

        Cuenta c = null;

        try {
            PreparedStatement st = con.prepareStatement("SELECT * FROM cuentas WHERE id = ?; ");
            st.setInt(1, idcuenta);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {
                String cuenta = rs.getString("cuenta");
                String fechacreacion = rs.getString("fechacreacion");
                String ultimoacceso = rs.getString("ultimoacceso");
                String roles = rs.getString("roles");
                int id = rs.getInt("id");
                byte[] pwd = rs.getBytes("pwd");
                byte[] salt = rs.getBytes("salt");
                String mail = rs.getString("mail");

                c = new Cuenta(id, cuenta, fechacreacion, ultimoacceso, roles, mail, pwd, salt);

            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return (c);
    }

    public boolean cuentaValida(String usuario, String rol) {
        boolean resultado = false;

        try {
            PreparedStatement st = con.prepareStatement("SELECT * FROM cuentas WHERE usuario = ? AND roles like concat('%',?,'%') ; ", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.setString(1, usuario);
            st.setString(2, rol);
            ResultSet rs = st.executeQuery();
            resultado = rs.first();
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return (resultado);
    }

    public ArrayList consultarCuentas() {
        ArrayList cuentas = new ArrayList();

        try {
            Statement st = this.con.createStatement();
            ResultSet rs = null;
            rs = st.executeQuery("SELECT * FROM cuentas;");

            while (rs.next()) {
                String cuenta = rs.getString("cuenta");
                String fechacreacion = rs.getString("fechacreacion");
                String ultimoacceso = rs.getString("ultimoacceso");
                String roles = rs.getString("roles");
                int id = rs.getInt("id");
                byte[] pwd = rs.getBytes("pwd");
                byte[] salt = rs.getBytes("salt");
                String mail = rs.getString("mail");

                Cuenta c = new Cuenta(id, cuenta, fechacreacion, ultimoacceso, roles, mail, pwd, salt);
                cuentas.add(c);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return (cuentas);
    }

    public Cuenta consultarCuenta(String usuario) {
        Cuenta cuenta = null;

        try {
            Statement st = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = null;
            rs = st.executeQuery("SELECT * FROM cuentas WHERE usuario = '" + usuario + "';");

            if (rs.first()) {

                String fechacreacion = rs.getString("fechacreacion");
                String ultimoacceso = rs.getString("ultimoacceso");
                String roles = rs.getString("roles");
                int id = rs.getInt("id");
                byte[] pwd = rs.getBytes("pwd");
                byte[] salt = rs.getBytes("salt");
                String mail = rs.getString("mail");
                System.out.println("Se obtuvo a memoria el registro de cuenta");
                cuenta = new Cuenta(id, usuario, fechacreacion, ultimoacceso, roles, mail, pwd, salt);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.out.println("ERROR: En el método Conexion.consultarCuenta " + e);
        }
        return (cuenta);
    }

    public boolean crearCuenta(Cuenta c) {
        try {
            PreparedStatement st = null;
            st = con.prepareStatement("INSERT INTO cuentas(usuario,roles,pwd,mail,salt) VALUES (?,?,?,?,?);");
            st.setString(1, c.usuario);
            st.setString(2, c.roles);
            st.setBytes(3, c.pwd);
            st.setString(4, c.mail);
            st.setBytes(5, c.salt);

            st.executeUpdate();
            st.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean modificarCuenta(Cuenta c) {
        try {
            PreparedStatement st = null;
            st = con.prepareStatement("CALL  sp_ModificarCuenta(?,?,?,?,?);");
            st.setString(1, c.usuario);
            st.setString(2, c.roles);
            st.setBytes(3, c.pwd);
            st.setString(4, c.mail);
            st.setBytes(5, c.salt);
            st.executeUpdate();
            st.close();

            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

    }

    public int obtenerSigNumFactura() {
        int numfactura = 0;
        try {
            Statement st = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = null;
            String query = "SELECT * FROM seriesfactura WHERE proce is NULL LIMIT 1;";

            rs = st.executeQuery(query);

            if (rs.first()) {
                numfactura = rs.getInt("numfactura");
                marcarNumFacturaParaUso(numfactura);
            } else {
                query = "INSERT INTO seriesfactura (SELECT MAX(numfactura)+1,null, null, null FROM seriesfactura);";
                int result = st.executeUpdate(query);
                if (result > 0) {
                    query = "SELECT * FROM seriesfactura WHERE proce is NULL LIMIT 1;";
                    rs = st.executeQuery(query);
                    if (rs.first()) {
                        numfactura = rs.getInt("numfactura");
                        marcarNumFacturaParaUso(numfactura);
                    }
                }
            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.obtenerSigNumFactura: " + exception);
        }
        return numfactura;
    }

    public int marcarNumFacturaParaUso(int numfactura) {
        int result = 0;
        try {
            Statement st = this.con.createStatement();

            String query = "UPDATE seriesfactura SET proce=1 WHERE numfactura=" + numfactura + ";";

            result = st.executeUpdate(query);

            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.marcarNumFacturaParaUso: " + exception);
        }
        return result;
    }

    public int liberarUsoFactura(int numfactura) {
        int result = 0;
        try {
            Statement st = this.con.createStatement();

            String query = "UPDATE seriesfactura SET proce=NULL WHERE numfactura=" + numfactura + ";";

            result = st.executeUpdate(query);

            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.marcarNumFacturaParaUso: " + exception);
        }
        return result;
    }

    public int guardarAutorizacion(Autorizacion autorizacion, Factura factura) {
        int result = 0;
        try {
            PreparedStatement st = null;
            String query = "INSERT INTO autorizaciones (claveacceso,autorizacion,fechaautorizacion,contenidoxml,estado,idcliente,tipodocumento) values (?,?,?,?,?,?,?);";
            st = con.prepareStatement(query);
            st.setString(1, factura.getInfoTributaria().getClaveAcceso());
            st.setString(2, autorizacion.getNumeroAutorizacion());
            XMLGregorianCalendar cal = autorizacion.getFechaAutorizacion();
            java.util.Date date = cal.toGregorianCalendar().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateString = formatter.format(date);
            st.setString(3, dateString);
            st.setString(4, autorizacion.getComprobante());
            st.setString(5, autorizacion.getEstado());
            st.setString(6, factura.getInfoFactura().getIdentificacionComprador());
            st.setString(7, factura.getInfoTributaria().getCodDoc());
            st.executeUpdate();
            this.guardarMensajesAutorizacion(autorizacion.getMensajes().getMensaje(), factura.getInfoTributaria().getClaveAcceso());
            this.guardarResumenFactura(factura, autorizacion.getEstado());

            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarAutorizacion: " + exception);
        }
        return result;
    }

    public int guardarMensajesAutorizacion(List<Mensaje> mensajes, String claveacceso) {
        int result = 0;
        Iterator<Mensaje> mensajesItr = mensajes.iterator();
        while (mensajesItr.hasNext()) {
            Mensaje mensaje = mensajesItr.next();
            result = guardarMensajeAutorizacion(mensaje, claveacceso);
        }
        return result;
    }

    public int guardarMensajeAutorizacion(Mensaje mensaje, String claveacceso) {
        int result = 0;
        try {
            PreparedStatement st = null;
            String update = "INSERT INTO autorizaciones_mensajes (claveacceso,identificador,tipo,mensaje,infoadicional) values (?,?,?,?,?);";
            st = con.prepareStatement(update);
            st.setString(1, claveacceso);
            st.setString(2, mensaje.getIdentificador());
            st.setString(3, mensaje.getTipo());
            st.setString(4, mensaje.getMensaje());
            st.setString(5, mensaje.getInformacionAdicional());

            result = st.executeUpdate();
            st.close();

        } catch (SQLException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public int guardarResumenFactura(Factura factura, String estado) {
        int result = 0;
        try {
            PreparedStatement st = null;
            String query = "INSERT INTO resumenes_facturas (claveacceso,numfactura,fechaemision,idcliente,importetotal,estado) values (?,?,?,?,?,?);";
            st = con.prepareStatement(query);
            st.setString(1, factura.getInfoTributaria().getClaveAcceso());
            st.setString(2, factura.getInfoTributaria().getEstab() + factura.getInfoTributaria().getPtoEmi() + factura.getInfoTributaria().getSecuencial());

            String string = factura.getInfoFactura().getFechaEmision();
            java.util.Date date = null;
            String dateString = null;
            try {
                date = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(string);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                dateString = formatter.format(date);
            } catch (ParseException ex) {
                Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
            }

            st.setString(3, dateString);
            st.setString(4, factura.getInfoFactura().getIdentificacionComprador());
            st.setBigDecimal(5, factura.getInfoFactura().getImporteTotal());
            st.setString(6, estado);
            st.executeUpdate();
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarResumenFactura: " + exception);
        }
        return result;
    }

    public int escribirAutorizacion(String numfactura, String claveacceso, String autorizacion) {
        int result = 0;
        try {
            PreparedStatement st = null;
            String query = "UPDATE seriesfactura SET claveacceso=?, autorizacion=? WHERE numfactura=?;";
            st = con.prepareStatement(query);
            st.setString(1, claveacceso);
            st.setString(2, autorizacion);
            st.setInt(3, Integer.parseInt(numfactura));

            st.executeUpdate();
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.escribirAutorizacion: " + exception);
        }
        return result;
    }

    public int escribirAutorizacionTRCRUC_PROCE(int trc_coest, int trc_ident, String claveacceso, Autorizacion autorizacion) {
        int result = 0;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fechaAutorizacion = df.format(autorizacion.getFechaAutorizacion().toGregorianCalendar().getTime());
        String query = "";
        try {
            PreparedStatement st = null;
            query = "UPDATE trcruc SET trc_proce='S' WHERE trc_coest=? AND trc_ident=?;";
            st = con.prepareStatement(query);
            st.setInt(1, trc_coest);
            st.setInt(2, trc_ident);
            st.executeUpdate();

            query = "INSERT INTO trcruc_proce(coest,ident,claveacceso,estado,autorizacion,fechaautorizacion,contenidoxml) values(?,?,?,?,?,?,?);";
            st = con.prepareStatement(query);
            st.setInt(1, trc_coest);
            st.setInt(2, trc_ident);
            st.setString(3, claveacceso);
            st.setString(4, autorizacion.getEstado());
            st.setString(5, autorizacion.getNumeroAutorizacion());
            st.setString(6, fechaAutorizacion);
            st.setString(7, autorizacion.getComprobante());
            st.executeUpdate();

            List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();

            for (int i = 0; i < mensajes.size(); i++) {
                query = "INSERT INTO trcruc_proce_mensajes(coest,ident,identificador,tipo,mensaje,infoadicional) values(?,?,?,?,?,?);";
                st = con.prepareStatement(query);
                st.setInt(1, trc_coest);
                st.setInt(2, trc_ident);
                st.setString(3, mensajes.get(i).getIdentificador());
                st.setString(4, mensajes.get(i).getTipo());
                st.setString(5, mensajes.get(i).getMensaje());
                st.setString(6, mensajes.get(i).getInformacionAdicional());
                st.executeUpdate();
            }

            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.escribirAutorizacionTRCRUC_PROCE: " + exception + " Query: " + query);
        }
        return result;
    }

    public int guardarFacturaAutorizada(String claveacceso, Autorizacion autorizacion) throws UnsupportedEncodingException, JAXBException, ParseException, IOException {
        int result = 0;

        InputStream in = new ByteArrayInputStream(autorizacion.getComprobante().getBytes("UTF-8"));

        JAXBContext jaxbContext = JAXBContext.newInstance(Factura.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Factura factura = (Factura) jaxbUnmarshaller.unmarshal(new InputStreamReader(in));

        InfoTributaria infoTributaria = factura.getInfoTributaria();
        InfoFactura infoFactura = factura.getInfoFactura();
        List<TotalImpuesto> totalconimpuestos = factura.getInfoFactura().getTotalImpuesto();
        List<Detalle> detalles = factura.getDetalle();
        List<CampoAdicional> infoadicional = factura.getCampoAdicional();

        guardarAutorizacion(claveacceso, autorizacion);
        guardarInfoTributaria(infoTributaria);
        guardarInfoFactura(claveacceso, infoFactura);

        if (totalconimpuestos != null) {
            guardarTotalConImpuestos(claveacceso, totalconimpuestos);
        } else {
            System.out.println("Total con impuestos es nulo.");
        }
        if (detalles != null) {
            guardarDetalles(claveacceso, detalles);
        } else {
            System.out.println("Detalles es nulo.");
        }
        if (infoadicional != null) {
            guardarInfoAdicional(claveacceso, infoadicional);
        } else {
            System.out.println("Infoadicional es nulo.");
        }

        return result;
    }

    private int guardarAutorizacion(String claveacceso, Autorizacion autorizacion) {
        int result = 0;

        String query = "";
        try {
            PreparedStatement st = null;

            query = "INSERT INTO ce_autorizaciones(claveacceso,estado,autorizacion,fechaautorizacion,contenidoxml) values(?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.setString(1, claveacceso);
            st.setString(2, autorizacion.getEstado());
            st.setString(3, autorizacion.getNumeroAutorizacion());
            st.setTimestamp(4, new java.sql.Timestamp(autorizacion.getFechaAutorizacion().toGregorianCalendar().getTimeInMillis()));
            st.setString(5, autorizacion.getComprobante());
            st.executeUpdate();

            List<Mensaje> mensajes = autorizacion.getMensajes().getMensaje();

            for (int i = 0; i < mensajes.size(); i++) {
                query = "INSERT INTO ce_autorizaciones_mensajes(claveacceso,identificador,tipo,mensaje,infoadicional) values(?,?,?,?,?);";
                st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                st.setString(1, claveacceso);
                st.setString(2, mensajes.get(i).getIdentificador());
                st.setString(3, mensajes.get(i).getTipo());
                st.setString(4, mensajes.get(i).getMensaje());
                st.setString(5, mensajes.get(i).getInformacionAdicional());
                st.executeUpdate();
            }

            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarAutorizacion: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarInfoTributaria(InfoTributaria infoTributaria) {
        int result = 0;

        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_infotributaria values(?,?,?,?,?,?,?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.setInt(1, Integer.parseInt(infoTributaria.getAmbiente()));
            st.setInt(2, Integer.parseInt(infoTributaria.getTipoEmision()));
            st.setString(3, infoTributaria.getRazonSocial());
            st.setString(4, infoTributaria.getNombreComercial());
            st.setString(5, infoTributaria.getRuc());
            st.setString(6, infoTributaria.getClaveAcceso());
            st.setString(7, infoTributaria.getCodDoc());
            st.setString(8, infoTributaria.getEstab());
            st.setString(9, infoTributaria.getPtoEmi());
            st.setString(10, infoTributaria.getSecuencial());
            st.setString(11, infoTributaria.getDirMatriz());
            st.executeUpdate();
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarInfoTributaria: " + exception + " Query: " + query);
        }

        return result;
    }

    private int guardarInfoFactura(String claveacceso, InfoFactura infoFactura) throws ParseException {
        int result = 0;
        SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy");

        java.util.Date fechaEmision = df.parse(infoFactura.getFechaEmision());

        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_infofactura values(?,?,?,?,?,?,?,?,?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            st.setTimestamp(1, new java.sql.Timestamp(fechaEmision.getTime()));
            st.setString(2, infoFactura.getDirEstablecimiento());
            st.setString(3, ((infoFactura.getContribuyenteEspecial() == null) ? null : infoFactura.getContribuyenteEspecial()));
            st.setString(4, infoFactura.getObligadoContabilidad());
            st.setString(5, infoFactura.getTipoIdentificacionComprador());
            st.setString(6, infoFactura.getRazonSocialComprador());
            st.setString(7, infoFactura.getIdentificacionComprador());
            st.setBigDecimal(8, infoFactura.getTotalSinImpuestos());
            st.setBigDecimal(9, infoFactura.getTotalDescuento());
            st.setString(10, claveacceso);
            st.setBigDecimal(11, infoFactura.getPropina());
            st.setBigDecimal(12, infoFactura.getImporteTotal());
            st.setString(13, infoFactura.getMoneda());
            st.executeUpdate();
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarInfoFactura: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarTotalConImpuestos(String claveacceso, List<TotalImpuesto> totalconimpuestos) throws ParseException {
        int result = 0;
        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_totalconimpuestos values(?,?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int counter = 0;
            for (TotalImpuesto totalimpuesto : totalconimpuestos) {
                counter++;
                st.setInt(1, Integer.parseInt(totalimpuesto.getCodigo()));
                st.setString(2, totalimpuesto.getCodigoPorcentaje());
                st.setBigDecimal(3, totalimpuesto.getBaseImponible());
                st.setBigDecimal(4, totalimpuesto.getValor());
                st.setInt(5, counter);
                st.setString(6, claveacceso);
                st.executeUpdate();
            }
            result = counter;
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarTotalConImpuestos: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarDetalles(String claveacceso, List<Detalle> detalles) throws ParseException {
        int result = 0;
        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_detalles values(?,?,?,?,?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int counter = 0;
            for (Detalle detalle : detalles) {
                counter++;
                st.setString(1, detalle.getCodigoPrincipal());
                st.setString(2, detalle.getCodigoAuxiliar());
                st.setString(3, detalle.getDescripcion());
                st.setBigDecimal(4, detalle.getCantidad());
                st.setBigDecimal(5, detalle.getPrecioUnitario());
                st.setBigDecimal(6, detalle.getDescuento());
                st.setBigDecimal(7, detalle.getPrecioTotalSinImpuesto());
                st.setInt(8, counter);
                st.setString(9, claveacceso);
                st.executeUpdate();

                List<Impuesto> impuestos = detalle.getImpuesto();
                if (impuestos != null) {
                    guardarImpuestosDetalle(claveacceso, counter, impuestos);
                } else {
                    System.out.println("Impuestos es nulo.");
                }

                List<DetAdicional> detallesadicionales = detalle.getDetAdicional();
                if (impuestos != null) {
                    guardarDetallesAdicionalesDetalle(claveacceso, counter, detallesadicionales);
                } else {
                    System.out.println("Detalles Adicionales es nulo.");
                }
            }
            result = counter;
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarDetalles: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarImpuestosDetalle(String claveacceso, int iddetalle, List<Impuesto> impuestos) throws ParseException {
        int result = 0;
        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_impuestos values(?,?,?,?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int counter = 0;
            for (Impuesto impuesto : impuestos) {
                counter++;
                st.setInt(1, Integer.parseInt(impuesto.getCodigo()));
                st.setString(2, impuesto.getCodigoPorcentaje());
                st.setBigDecimal(3, impuesto.getTarifa());
                st.setBigDecimal(4, impuesto.getBaseImponible());
                st.setBigDecimal(5, impuesto.getValor());
                st.setInt(6, counter);
                st.setInt(7, iddetalle);
                st.setString(8, claveacceso);
                st.executeUpdate();
            }
            result = counter;
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarImpuestosDetalle: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarDetallesAdicionalesDetalle(String claveacceso, int iddetalle, List<DetAdicional> detallesadicionales) throws ParseException {
        int result = 0;
        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_detallesadicionales values(?,?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int counter = 0;
            for (DetAdicional detalleadicional : detallesadicionales) {
                counter++;
                st.setString(1, detalleadicional.getNombre());
                st.setString(2, detalleadicional.getValor());
                st.setInt(3, counter);
                st.setInt(4, iddetalle);
                st.setString(5, claveacceso);
                st.executeUpdate();
            }
            result = counter;
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarDetallesAdicionalesDetalle: " + exception + " Query: " + query);
        }
        return result;
    }

    private int guardarInfoAdicional(String claveacceso, List<CampoAdicional> infoadicional) throws ParseException {
        int result = 0;
        String query = "";
        try {
            PreparedStatement st = null;
            query = "INSERT INTO ce_infoadicional values(?,?,?,?);";
            st = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int counter = 0;
            for (CampoAdicional campoadicional : infoadicional) {
                counter++;
                st.setString(1, campoadicional.getNombre());
                st.setString(2, campoadicional.getValor());
                st.setInt(3, counter);
                st.setString(4, claveacceso);
                st.executeUpdate();
            }
            result = counter;
            st.close();
        } catch (SQLException exception) {
            System.out.println((new java.util.Date()) + " ERROR:  Al ejecutar Conexion.guardarInfoAdicional: " + exception + " Query: " + query);
        }
        return result;
    }

    public ArrayList<TrcRUC> consultarFacturasViaNoProc(int primerafila, int cantidadfilas) {
        ArrayList facturasVia = new ArrayList();

        try {
            Statement st = this.con.createStatement();
            ResultSet rs = null;
            String query = "SELECT * FROM trcRUC WHERE trc_proce is NULL ";

            switch (dbms) {
                case MYSQL_DBMS:
                    query += " LIMIT %d,%d;";
                    break;
                case MSSQL_DBMS:
                    query += " ;"; //Debo investigar la forma más eficiente de limitar los resultados en SQL SERVER
                    break;
                default:
                    query += " LIMIT %d,%d;";
                    break;
            }

            query = String.format(query, primerafila, cantidadfilas);

            rs = st.executeQuery(query);

            while (rs.next()) {

                int coest = rs.getInt("trc_coest");
                int ident = rs.getInt("trc_ident");
                int numev = rs.getInt("trc_numev");
                int nuvia = rs.getInt("trc_nuvia");
                Date fecha = rs.getDate("trc_fecha");
                int parte = rs.getInt("trc_parte");
                int nturn = rs.getInt("trc_nturn");
                String ruc = rs.getString("trc_ruc");
                String nombr = rs.getString("trc_nombr");
                int ticke = rs.getInt("trc_ticke");
                int tarif = rs.getInt("trc_tarif");
                int titar = rs.getInt("trc_titar");
                BigDecimal impor = rs.getBigDecimal("trc_impor");
                BigDecimal iva = rs.getBigDecimal("trc_iva");
                String abort = rs.getString("trc_abort");
                int tidoc = rs.getInt("trc_tidoc");
                String proce = rs.getString("trc_proce");

                TrcRUC facturaVia = new TrcRUC(coest, ident, numev, nuvia, fecha, parte, nturn, ruc, nombr, ticke, tarif, titar, impor, iva, abort, tidoc, proce);
                facturasVia.add(facturaVia);
            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            System.out.println("ERROR: Al ejecutar Conexion.consultarFacturasVia: " + exception);
        }
        return (facturasVia);
    }

    public ArrayList<TrcRUC> consultarFacturasViaNoProc(int cantidadfilas) {
        ArrayList facturasVia = new ArrayList();

        try {
            Statement st = this.con.createStatement();
            ResultSet rs = null;
            String query = "SELECT TOP %d * FROM trcRUC";

            query = String.format(query, cantidadfilas);

            rs = st.executeQuery(query);

            while (rs.next()) {

                int coest = rs.getInt("trc_coest");
                int ident = rs.getInt("trc_ident");
                int numev = rs.getInt("trc_numev");
                int nuvia = rs.getInt("trc_nuvia");
                Date fecha = rs.getDate("trc_fecha");
                int parte = rs.getInt("trc_parte");
                int nturn = rs.getInt("trc_nturn");
                String ruc = rs.getString("trc_ruc");
                String nombr = rs.getString("trc_nombr");
                int ticke = rs.getInt("trc_ticke");
                int tarif = rs.getInt("trc_tarif");
                int titar = rs.getInt("trc_titar");
                BigDecimal impor = rs.getBigDecimal("trc_impor");
                BigDecimal iva = rs.getBigDecimal("trc_iva");
                String abort = rs.getString("trc_abort");
                int tidoc = rs.getInt("trc_tidoc");
//                String proce = rs.getString("trc_proce");

//                TrcRUC facturaVia = new TrcRUC(coest, ident, numev, nuvia, fecha, parte, nturn, ruc, nombr, ticke, tarif, titar, impor, iva, abort, tidoc, proce);
                TrcRUC facturaVia = new TrcRUC(coest, ident, numev, nuvia, fecha, parte, nturn, ruc, nombr, ticke, tarif, titar, impor, iva, abort, tidoc, null);
                facturasVia.add(facturaVia);
            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            System.out.println("ERROR: Al ejecutar Conexion.consultarFacturasVia: " + exception);
        }
        return (facturasVia);
    }

    public Emisor obtenerEmisorPorRUC(String ruc) {

        Emisor emisor = null;

        try {
            PreparedStatement st = con.prepareStatement("SELECT * FROM emisores WHERE ruc = ?; ");
            st.setString(1, ruc);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {

                String razonSocial = rs.getString("razonSocial");
                String nombreComercial = rs.getString("nombreComercial");
                String resolContribEsp = rs.getString("resolContribEsp");
                String obligadoContabilidad = rs.getString("obligadoContabilidad");

                emisor = new Emisor(ruc, razonSocial, nombreComercial, resolContribEsp, obligadoContabilidad);

            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            System.out.println("ERROR: Al ejecutar Conexion.obtenerEmisorPorRUC: " + exception);
        }
        return (emisor);
    }

    public ArrayList<Cliente> consultarClientes() {
        ArrayList clientes = new ArrayList();

        try {
            Statement st = this.con.createStatement();
            ResultSet rs = null;
            rs = st.executeQuery("SELECT * FROM clientes;");

            while (rs.next()) {

                String razonsocial = rs.getString("razonsocial");
//                System.out.println("DEBUG: Imprimiento en Conexion.consultarClientes: " + razonsocial);
                String identificacion = rs.getString("identificacion");
                String tipoidentificacion = rs.getString("tipoidentificacion");
                String tipocliente = rs.getString("tipocliente");
                String direccion = rs.getString("direccion");
                String telefonofijo = rs.getString("telefonofijo");
                String extensionfijo = rs.getString("extensionfijo");
                String telefonomovil = rs.getString("telefonomovil");
                String email = rs.getString("email");

                Cliente cliente = new Cliente(razonsocial, tipoidentificacion, identificacion, tipocliente, direccion, telefonofijo, extensionfijo, telefonomovil, email);
                clientes.add(cliente);
            }
            rs.close();
            st.close();
        } catch (SQLException exception) {
            System.out.println("ERROR: Al ejecutar Conexion.consultarClientes: " + exception);
        }
        return (clientes);
    }

    public boolean crearCliente(Cliente cliente) {
        try {
            PreparedStatement st = null;
            st = con.prepareStatement("CALL CrearCliente(?,?,?,?,?,?,?,?,?);");
            st.setString(1, cliente.getRazonsocial());
            st.setString(2, cliente.getTipoIdentificacion());
            st.setString(3, cliente.getIdentificacion());
            st.setString(4, cliente.getTipoCliente());
            st.setString(5, cliente.getDireccion());
            st.setString(6, cliente.getTelefonoFijo());
            st.setString(7, cliente.getExtensionFijo());
            st.setString(8, cliente.getTelefonoMovil());
            st.setString(9, cliente.getEmail());

            st.executeUpdate();
            st.close();
            return true;
        } catch (SQLException exception) {
            System.out.println("ERROR: Al ejecutar Conexion.crearCliente: " + exception);
            return false;
        }
    }

}
