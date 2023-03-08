package dk.dbc.weekresolver.service;

import java.util.Date;

public class WeekDescription extends WeekResolverResult {

    // "DBF/DPF/BKM katalogkode"
    private String weekCodeShort;

    // "DBCKat ugekode start"
    private Date weekCodeFirst;

    // "DBCKat ugekode slut"
    private Date weekCodeLast;

    // "DBCKat ugeafslutning"
    private Date shiftDay;

    // "Bogvogn"
    private Date bookCart;

    // "Ugekorrektur"
    private Date proof;

    // "BKM-red."
    private Date bkm;

    // "Ugekorrekturen køres"
    private Date proofFrom;

    // "Slutredaktion (ugekorrektur)"
    private Date proofTo;

    // "Udgivelsesdato"
    private Date publish;

    public String getWeekCodeShort() {
        return weekCodeShort;
    }

    public void setWeekCodeShort(String weekCodeShort) {
        this.weekCodeShort = weekCodeShort;
    }

    public WeekDescription withWeekCodeShort(String weekCodeShort) {
        this.weekCodeShort = weekCodeShort;
        return this;
    }

    public Date getWeekCodeFirst() {
        return weekCodeFirst;
    }

    public void setWeekCodeFirst(Date weekCodeFirst) {
        this.weekCodeFirst = weekCodeFirst;
    }

    public WeekDescription withWeekCodeFirst(Date weekCodeFirst) {
        this.weekCodeFirst = weekCodeFirst;
        return this;
    }

    public Date getWeekCodeLast() {
        return weekCodeLast;
    }

    public void setWeekCodeLast(Date weekCodeLast) {
        this.weekCodeLast = weekCodeLast;
    }

    public WeekDescription withWeekCodeLast(Date weekCodeLast) {
        this.weekCodeLast = weekCodeLast;
        return this;
    }

    public Date getShiftDay() {
        return shiftDay;
    }

    public void setShiftDay(Date shiftDay) {
        this.shiftDay = shiftDay;
    }

    public WeekDescription withShiftDay(Date shiftDay) {
        this.shiftDay = shiftDay;
        return this;
    }

    public Date getBookCart() {
        return bookCart;
    }

    public void setBookCart(Date bookCart) {
        this.bookCart = bookCart;
    }

    public WeekDescription withBookCart(Date bookCart) {
        this.bookCart = bookCart;
        return this;
    }

    public Date getProof() {
        return proof;
    }

    public void setProof(Date proof) {
        this.proof = proof;
    }

    public WeekDescription withProof(Date proof) {
        this.proof = proof;
        return this;
    }

    public Date getBkm() {
        return bkm;
    }

    public void setBkm(Date bkm) {
        this.bkm = bkm;
    }

    public WeekDescription withBkm(Date bkm) {
        this.bkm = bkm;
        return this;
    }

    public Date getProofFrom() {
        return proofFrom;
    }

    public void setProofFrom(Date proofFrom) {
        this.proofFrom = proofFrom;
    }

    public WeekDescription withProofFrom(Date proofFrom) {
        this.proofFrom = proofFrom;
        return this;
    }

    public Date getProofTo() {
        return proofTo;
    }

    public void setProofTo(Date proofTo) {
        this.proofTo = proofTo;
    }

    public WeekDescription withProofTo(Date proofTo) {
        this.proofTo = proofTo;
        return this;
    }

    public Date getPublish() {
        return publish;
    }

    public void setPublish(Date publish) {
        this.publish = publish;
    }

    public WeekDescription withPublish(Date publish) {
        this.publish = publish;
        return this;
    }

    @Override
    public String toString() {
        return "WeekDescription{" +
                "weekCodeShort='" + weekCodeShort + '\'' +
                ", weekCodeFirst=" + weekCodeFirst +
                ", weekCodeLast=" + weekCodeLast +
                ", shiftDay=" + shiftDay +
                ", bookCart=" + bookCart +
                ", proof=" + proof +
                ", bkm=" + bkm +
                ", proofFrom=" + proofFrom +
                ", proofTo=" + proofTo +
                ", publish=" + publish +
                "} " + super.toString();
    }
}
