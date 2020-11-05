package com.ptitfinal.covidtracing.model;

public class Location {
    private String beaconBLEid;
    private String address;

    public Location() {
    }

    public Location(String beaconBLEid, String address) {
        this.beaconBLEid = beaconBLEid;
        this.address = address;
    }

    public String getBeaconBLEid() {
        return beaconBLEid;
    }

    public void setBeaconBLEid(String beaconBLEid) {
        this.beaconBLEid = beaconBLEid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
