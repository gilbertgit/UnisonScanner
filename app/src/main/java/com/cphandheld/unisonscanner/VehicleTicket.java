package com.cphandheld.unisonscanner;

import java.util.ArrayList;

/**
 * Created by titan on 3/23/16.
 */
public class VehicleTicket{

    int binId;
    String binName;
    boolean servicesCompleted;
    boolean allServicesStarted;
    String locationTitle;
    String moduleName;
    int ticketId;
    ArrayList ticketServices;

    VehicleTicket()
    {}

    @Override
    public String toString() {
        return this.binName;
    }
}
