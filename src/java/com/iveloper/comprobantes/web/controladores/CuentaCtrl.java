/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.web.controladores;

import com.iveloper.db.Conexion;
import com.iveloper.db.PasswordEncryptionService;
import com.iveloper.entidades.Cuenta;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Alex
 */
@WebServlet(name = "ctrl_cuenta", urlPatterns = {"/CuentaCtrl"})
public class CuentaCtrl extends HttpServlet {

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

        response.setContentType("text/xml;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        PrintWriter out = response.getWriter();
        String op = request.getParameter("op");

        if (op.equalsIgnoreCase("validar")) {
            System.out.println("Entrando a validar usuario");
            validarCuenta(request, response);
        } else if (op.equalsIgnoreCase("consultar")) {
            out.print(toJSON(op_consultar_cuentas(request, response)));
            out.close();
        } else if (op.equalsIgnoreCase("editar")) {
            op_obtener_cuenta(request, response, request.getParameter("usuario"));
        } else if (op.equalsIgnoreCase("modificar")) {
            try {
                modificarCuenta(request, response);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CuentaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(CuentaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (op.equalsIgnoreCase("cerrar_sesion")) {
            op_cerrar_sesion(request, response);
        } else if (op.equalsIgnoreCase("crear")) {
            System.out.println("Entrando a crear usuario");
            try {
                crearCuenta(request, response);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CuentaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(CuentaCtrl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**/

    public void validarCuenta(HttpServletRequest request, HttpServletResponse response) {

        String usuario = request.getParameter("usuario");
        System.out.println("Usuario: " + usuario);
        String clave = request.getParameter("clave");
        String rol = request.getParameter("rol");
        System.out.println("Rol: " + rol);
        Conexion c = new Conexion();
        String url = "";

        try {
            c.conectar();
            PasswordEncryptionService pes = new PasswordEncryptionService();
            Cuenta cuentaIngreso = c.consultarCuenta(usuario);            
            boolean claveValida = false;
            if (cuentaIngreso != null) {
                claveValida = pes.authenticate(clave, cuentaIngreso.getPwd(), cuentaIngreso.getSalt());
            }

            if (cuentaIngreso != null && claveValida && c.cuentaValida(usuario, rol)) {
                url = "index.html";
                HttpSession session = request.getSession(true);

                session.setAttribute("cuentaSesion", cuentaIngreso);

            } else {
//                String error = "Usuario, clave y/o perfil incorrectos";
                url = "registration.html";
                if (cuentaIngreso == null) {
                    System.out.println("Usuario no existe");
                } else if (!claveValida) {
                    System.out.println("Clave incorrecta");
                } else {
                    System.out.println("Rol no admitido para usuario " + usuario);
                }
            }

        } catch (Exception e) {
//            String error = "OCURRIO UN ERROR EN LA CONEXION";
            url = "registration.html";
        }

        c.desconectar();

        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            System.out.println("ERROR: " + e);
        }

    }

    public List op_consultar_cuentas(HttpServletRequest request, HttpServletResponse response) {

        Conexion c = new Conexion();
        String xml = "";
        List cuentas = new ArrayList();

        try {
            c.conectar();
            cuentas = c.consultarCuentas();
        } catch (Exception e) {
            String error = "OCURRIO UN ERROR EN LA CONEXION";
        }

        c.desconectar();
        return cuentas;

    }

    public void op_obtener_cuenta(HttpServletRequest request, HttpServletResponse response, String usuario) {

        Conexion c = new Conexion();
        String xml = "";
        PrintWriter pw = null;

        try {
            pw = response.getWriter();

            c.conectar();
            Cuenta cuenta = c.consultarCuenta(usuario);

            xml = "<?xml version='1.0' encoding='UTF-8' ?>";
            xml += "<cuentas>";

            xml += "<registro>";
            xml += "<cuenta>" + cuenta.getUsuario() + "</cuenta>";
            xml += "<pwd>" + cuenta.getPwd() + "</pwd>";
            xml += "<mail>" + cuenta.getMail() + "</mail>";
            xml += "<tipo>" + cuenta.getRoles() + "</tipo>";
            xml += "</registro>";

            xml += "</cuentas>";
            pw.println(xml);

        } catch (Exception e) {
            String error = "OCURRIO UN ERROR EN LA CONEXION";
        }

        c.desconectar();

    }

    public void modificarCuenta(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Conexion c = new Conexion();
        PasswordEncryptionService pes = new PasswordEncryptionService();
        byte[] salt = pes.generateSalt();
        byte[] clave = pes.getEncryptedPassword(request.getParameter("password"), salt);
        Cuenta cuenta = new Cuenta(request.getParameter("username"), request.getParameter("rol"), request.getParameter("email"), clave, salt);
        PrintWriter pw = null;
        String xml = "";

        try {
            pw = response.getWriter();
            c.conectar();
            xml = "<?xml version='1.0' encoding='UTF-8' ?>";
            xml += "<cuentas>";
            xml += "<registro>";
            xml += "<respuesta>" + c.modificarCuenta(cuenta) + "</respuesta>";
            xml += "</registro>";
            xml += "</cuentas>";
            pw.println(xml);

        } catch (Exception e) {
            String error = "OCURRIO UN ERROR EN LA CONEXION";
        }

        c.desconectar();

    }

    public void crearCuenta(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, InvalidKeySpecException {

        Conexion c = new Conexion();
        PasswordEncryptionService pes = new PasswordEncryptionService();
        byte[] salt = pes.generateSalt();

        byte[] clave = pes.getEncryptedPassword(request.getParameter("password"), salt);
        
        System.out.println("salt: " + new String(salt));
        System.out.println("clave: " + new String(clave));

        Cuenta cuenta = new Cuenta(request.getParameter("username"), request.getParameter("rol"), request.getParameter("email"), clave, salt);
        PrintWriter pw = null;
        String xml = "";

        try {
            pw = response.getWriter();
            c.conectar();
            xml = "<?xml version='1.0' encoding='UTF-8' ?>";
            xml += "<cuentas>";
            xml += "<registro>";
            xml += "<respuesta>" + c.crearCuenta(cuenta) + "</respuesta>";
            xml += "</registro>";
            xml += "</cuentas>";
            pw.println(xml);

        } catch (Exception e) {
            String error = "OCURRIO UN ERROR EN LA CONEXION";
        }

        c.desconectar();

    }

    public void op_cerrar_sesion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.invalidate();
        response.sendRedirect("index.jsp");
    }

    public static JSONObject toJSON(List elementos) {
        JSONObject json = new JSONObject();
        try {
            JSONArray jsonItems = new JSONArray();
            Iterator it = elementos.iterator();
            do {
                if (!it.hasNext()) {
                    break;
                }
                Cuenta c = (Cuenta) it.next();

                if (c != null) {
                    jsonItems.put(c.toJSONObject());
                }
            } while (true);
            json.put("items", jsonItems);
        } catch (JSONException ex) {
        }
        return json;
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
