package com.ptitfinal.covidtracing.model;

import java.util.List;

public class DeviceScan {
    private String personBLEid;
    private String dayInteraction;

    public DeviceScan() {
    }

    public DeviceScan(String personBLEid, String dayInteraction) {
        this.personBLEid = personBLEid;
        this.dayInteraction = dayInteraction;
    }

    public String getDayInteraction() {
        return dayInteraction;
    }

    public void setDayInteraction(String dayInteraction) {
        this.dayInteraction = dayInteraction;
    }

    public DeviceScan(String personBLEid ) {
        this.personBLEid = personBLEid;

    }
    public static boolean checkExist(List<DeviceScan> scanList,DeviceScan newScan){
        for(DeviceScan item:scanList){
            if(newScan.getPersonBLEid().equalsIgnoreCase(item.getPersonBLEid())){
                return true;
            }
        }
        return false;
    }
    public String getPersonBLEid() {
        return personBLEid;
    }

    public void setPersonBLEid(String personBLEid) {
        this.personBLEid = personBLEid;
    }

}
