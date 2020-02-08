package com.wrig.truehb_ble_demo.modal;

public class TestDetailsModal {
    private  String deviceid,hbresult,date,time;

    public TestDetailsModal() {
    }

    public TestDetailsModal(String deviceid, String hbresult, String date, String time) {
        this.deviceid = deviceid;
        this.hbresult = hbresult;
        this.date = date;
        this.time = time;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getHbresult() {
        return hbresult;
    }

    public void setHbresult(String hbresult) {
        this.hbresult = hbresult;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
