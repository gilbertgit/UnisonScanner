package com.cphandheld.unisonscanner;

import java.io.Serializable;

/**
 * Created by titan on 9/6/16.
 */
public class CheckInPost implements Serializable {
    String ScannerSerialNumber;
    String Action;
    int LocationId;
    int BinId;
    int PathId;
    String Notes;
    int UserId;
    boolean StartPath;
    Vehicle Vehicle;
    String ScannedDate;
    double Latitude;
    double Longitude;

    CheckInPost() {
    }
}
