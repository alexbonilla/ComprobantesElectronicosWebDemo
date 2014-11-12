/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iveloper.comprobantes.test;

import com.iveloper.comprobantes.utils.ArchivoUtils;

/**
 *
 * @author Alex
 */
public class TestValidacion {
    public static void main(String[] args){
        System.out.println(ArchivoUtils.validaArchivoXSD("01","C:\\Users\\Alex\\Downloads\\test01_factura.xml"));
//        System.out.println(ArchivoUtils.validaArchivoXSD("04","C:\\Users\\Alex\\Downloads\\test01_notacredito.xml"));
//        System.out.println(ArchivoUtils.validaArchivoXSD("05","C:\\Users\\Alex\\Downloads\\test01_notadebito.xml"));        
//        System.out.println(ArchivoUtils.validaArchivoXSD("06","C:\\Users\\Alex\\Downloads\\test01_guiaremision.xml"));        
//        System.out.println(ArchivoUtils.validaArchivoXSD("07","C:\\Users\\Alex\\Downloads\\test01_compretend.xml"));
    }
}
