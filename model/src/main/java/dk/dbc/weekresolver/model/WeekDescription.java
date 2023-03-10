package dk.dbc.weekresolver.model;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public class WeekDescription {

    public static List<String> Headers = List.of(
        "DBF/DPF/BKM katalogkode",
        "DBCKat ugekode start",
        "DBCKat ugekode slut",
        "DBCKat ugeafslutning"   ,
        "Bogvogn",
        "Ugekorrektur",
        "BKM-red.",
        "Ugekorrekturen køres",
        "Slutredaktion (ugekorrektur)",
        "Udgivelsesdato"
    );

    // DBF/DPF/BKM katalogkode
    private String weekCodeShort;

    // DBCKat ugekode start
    private Date weekCodeFirst;

    // DBCKat ugekode slut
    private Date weekCodeLast;

    // DBCKat ugeafslutning
    private Date shiftDay;

    // Bogvogn
    private Date bookCart;

    // Ugekorrektur
    private Date proof;

    // BKM-red.
    private Date bkm;

    // Ugekorrekturen køres
    private Date proofFrom;

    // Slutredaktion (ugekorrektur)
    private Date proofTo;

    // Udgivelsesdato
    private Date publish;

    private Boolean noProduction = false;

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

    public Boolean getNoProduction() {
        return noProduction;
    }

    public void setNoProduction(Boolean noProduction) {
        this.noProduction = noProduction;
    }

    public WeekDescription withNoProduction(Boolean noProduction) {
        this.noProduction = noProduction;
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
                ", noProduction=" + noProduction +
                '}';
    }
}
