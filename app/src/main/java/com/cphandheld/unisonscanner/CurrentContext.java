package com.cphandheld.unisonscanner;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Supernova on 2/16/2016.
 */
public class CurrentContext implements Serializable
{
    int organizationId;
    int locationId;
    String locationName;
    Vehicle vehicle;
    int binId;
    String binName;
    int pathId;
    boolean startPath;
    String pathName;
    String notes;
    VehicleTicket vehicleTicket;
    String Stock;

    CurrentContext() {
    }
}
