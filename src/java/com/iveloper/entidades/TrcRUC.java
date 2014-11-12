/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iveloper.entidades;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Alex
 */
public class TrcRUC {
    private int coest;
    private int ident;
    private int numev;
    private int nuvia;
    private Date fecha;
    private int parte;
    private int nturn;
    private String ruc;
    private String nombr;
    private int ticke;
    private int tarif;
    private int titar;
    private BigDecimal impor;
    private BigDecimal iva;
    private String abort;
    private int tidoc;
    private String proce;

    public TrcRUC() {
    }

    public TrcRUC(int coest, int ident, int numev, int nuvia, Date fecha, int parte, int nturn, String ruc, String nombr, int ticke, int tarif, int titar, BigDecimal impor, BigDecimal iva, String abort, int tidoc, String proce) {
        this.coest = coest;
        this.ident = ident;
        this.numev = numev;
        this.nuvia = nuvia;
        this.fecha = fecha;
        this.parte = parte;
        this.nturn = nturn;
        this.ruc = ruc;
        this.nombr = nombr;
        this.ticke = ticke;
        this.tarif = tarif;
        this.titar = titar;
        this.impor = impor;
        this.iva = iva;
        this.abort = abort;
        this.tidoc = tidoc;
        this.proce = proce;
    }

    public int getCoest() {
        return coest;
    }

    public void setCoest(int coest) {
        this.coest = coest;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }

    public int getNumev() {
        return numev;
    }

    public void setNumev(int numev) {
        this.numev = numev;
    }

    public int getNuvia() {
        return nuvia;
    }

    public void setNuvia(int nuvia) {
        this.nuvia = nuvia;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getParte() {
        return parte;
    }

    public void setParte(int parte) {
        this.parte = parte;
    }

    public int getNturn() {
        return nturn;
    }

    public void setNturn(int nturn) {
        this.nturn = nturn;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getNombr() {
        return nombr;
    }

    public void setNombr(String nombr) {
        this.nombr = nombr;
    }

    public int getTicke() {
        return ticke;
    }

    public void setTicke(int ticke) {
        this.ticke = ticke;
    }

    public int getTarif() {
        return tarif;
    }

    public void setTarif(int tarif) {
        this.tarif = tarif;
    }

    public int getTitar() {
        return titar;
    }

    public void setTitar(int titar) {
        this.titar = titar;
    }

    public BigDecimal getImpor() {
        return impor;
    }

    public void setImpor(BigDecimal impor) {
        this.impor = impor;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public String getAbort() {
        return abort;
    }

    public void setAbort(String abort) {
        this.abort = abort;
    }

    public int getTidoc() {
        return tidoc;
    }

    public void setTidoc(int tidoc) {
        this.tidoc = tidoc;
    }

    public String getProce() {
        return proce;
    }

    public void setProce(String proce) {
        this.proce = proce;
    }

    

}
