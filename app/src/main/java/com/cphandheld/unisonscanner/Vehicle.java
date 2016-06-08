package com.cphandheld.unisonscanner;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Supernova on 2/24/2016.
 */
public class Vehicle implements Serializable
{
    String VIN;
    int Year;
    String Make;
    String Model;
    String Color;
    String Stock;
    String ScannedDate;


    Vehicle() {
    }

}
