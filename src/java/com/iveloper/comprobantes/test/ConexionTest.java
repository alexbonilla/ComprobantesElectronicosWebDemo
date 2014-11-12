/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.comprobantes.test;

import com.iveloper.db.Conexion;
import com.iveloper.entidades.TrcRUC;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alex
 */
public class ConexionTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Conexion c = new Conexion("192.168.20.20","1433","consolidado","sa","**useco01",1);
        try {
            c.conectar();

            ArrayList<TrcRUC> facturasvia = new <TrcRUC>ArrayList();

            facturasvia = c.consultarFacturasViaNoProc(1, 10);

            for (int i = 0; i < facturasvia.size(); i++) {
                TrcRUC facturavia = facturasvia.get(i);
                System.out.println("RUC del cliente " + facturavia.getRuc());
            }

            c.desconectar();

        } catch (Exception ex) {
            Logger.getLogger(ConexionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
