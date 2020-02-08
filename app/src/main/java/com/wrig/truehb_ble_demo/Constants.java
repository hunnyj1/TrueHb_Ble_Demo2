package com.wrig.truehb_ble_demo;

import java.util.UUID;

public class Constants {


   //my device
   public static String SERVICE_STRING  = "D973F2E0-B19E-11E2-9E96-0800200C9A66";

    public static UUID SERVICE_UUID = UUID.fromString(SERVICE_STRING);


    //my device rx
    public static String CHARACTERISTIC_ECHO_STRING = "D973F2E2-B19E-11E2-9E96-0800200C9A66";
    public static UUID CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING);


    //my device tx
    public static String CHARACTERISTIC_TIME_STRING = "D973F2E1-B19E-11E2-9E96-0800200C9A66";
    public static UUID CHARACTERISTIC_TIME_UUID = UUID.fromString(CHARACTERISTIC_TIME_STRING);

    public static String CLIENT_CONFIGURATION_DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb";
    public static UUID CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CONFIGURATION_DESCRIPTOR_STRING);

    public static final String CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "2902";

    public static final long SCAN_PERIOD = 5000;
}
