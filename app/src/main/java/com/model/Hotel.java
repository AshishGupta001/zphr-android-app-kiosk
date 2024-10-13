package com.model;

/**
 * Created by Admin on 17-10-17.
 */
public class Hotel {
    String iVisitId;
    String vSourceLatitude;
    String vSourceLongitude;
    String vDestLatitude;
    String vDestLongitude;
    String vSourceAddresss;
    String tDestName;
    String tDestAddress;

    public String getiVisitId() {
        return iVisitId;
    }

    public void setiVisitId(String iVisitId) {
        this.iVisitId = iVisitId;
    }

    public String getvSourceLatitude() {
        return vSourceLatitude;
    }

    public void setvSourceLatitude(String vSourceLatitude) {
        this.vSourceLatitude = vSourceLatitude;
    }

    public String getvSourceLongitude() {
        return vSourceLongitude;
    }

    public void setvSourceLongitude(String vSourceLongitude) {
        this.vSourceLongitude = vSourceLongitude;
    }

    public String getvDestLatitude() {
        return vDestLatitude;
    }

    public void setvDestLatitude(String vDestLatitude) {
        this.vDestLatitude = vDestLatitude;
    }

    public String getvDestLongitude() {
        return vDestLongitude;
    }

    public void setvDestLongitude(String vDestLongitude) {
        this.vDestLongitude = vDestLongitude;
    }

    public String getvSourceAddresss() {
        return vSourceAddresss;
    }

    public void setvSourceAddresss(String vSourceAddresss) {
        this.vSourceAddresss = vSourceAddresss;
    }

    public String gettDestName() {
        return tDestName;
    }

    public void settDestName(String tDestName) {
        this.tDestName = tDestName;
    }

    public String gettDestAddress() {
        return tDestAddress;
    }

    public void settDestAddress(String tDestAddress) {
        this.tDestAddress = tDestAddress;
    }
}

