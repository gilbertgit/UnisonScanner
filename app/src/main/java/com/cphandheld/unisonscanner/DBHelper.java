package com.cphandheld.unisonscanner;

/**
 * Created by titan on 3/14/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UnisonDB.db";

    public static final String VEHICLE_ENTRY_TABLE_NAME = "vehicleEntry";
    public static final String VEHICLE_ENTRY_COLUMN_ID = "id";
    public static final String VEHICLE_ENTRY_COLUMN_LOCATION = "locationId";
    public static final String VEHICLE_ENTRY_COLUMN_BIN = "binId";
    public static final String VEHICLE_ENTRY_COLUMN_PATH = "pathId";
    public static final String VEHICLE_ENTRY_COLUMN_NOTES = "notes";
    public static final String VEHICLE_ENTRY_COLUMN_USER = "userId";
    public static final String VEHICLE_ENTRY_COLUMN_VIN = "vin";
    public static final String VEHICLE_ENTRY_COLUMN_YEAR = "year";
    public static final String VEHICLE_ENTRY_COLUMN_MAKE = "make";
    public static final String VEHICLE_ENTRY_COLUMN_MODEL = "model";
    public static final String VEHICLE_ENTRY_COLUMN_COLOR = "color";
    public static final String VEHICLE_ENTRY_COLUMN_DATA = "data";

    public static final String VEHICLE_COLUMN_ID = "id";
    public static final String VEHICLE_TABLE_NAME = "vehicle";
    public static final String VEHICLE_COLUMN_VIN = "vin";
    public static final String VEHICLE_COLUMN_YEAR = "year";
    public static final String VEHICLE_COLUMN_MAKE = "make";
    public static final String VEHICLE_COLUMN_MODEL = "model";
    public static final String VEHICLE_COLUMN_COLOR = "color";
    public static final String VEHICLE_COLUMN_BIN_ID = "binId";

    public static final String BIN_COLUMN_ID = "id";
    public static final String BIN_TABLE_NAME = "bins";
    public static final String BIN_COLUMN_BIN_ID = "binId";
    public static final String BIN_COLUMN_NAME = "name";

    public static final String PATH_COLUMN_ID = "id";
    public static final String PATH_TABLE_NAME = "paths";
    public static final String PATH_COLUMN_PATH_ID = "pathId";
    public static final String PATH_COLUMN_NAME = "name";
    public static final String PATH_COLUMN_START_PATH = "startPath";
    public static final String PATH_COLUMN_LOCATION_ID = "locationId";

    public static final String USER_TABLE_NAME = "users";
    public static final String USER_COLUMN_ID = "id";
    public static final String USER_COLUMN_USER_ID = "userId";
    public static final String USER_COLUMN_PIN = "pin";
    public static final String USER_COLUMN_ORGANIZATION_ID = "organizationId";
    public static final String USER_COLUMN_NAME = "name";

    public static final String LOCATION_TABLE_NAME = "locations";
    public static final String LOCATION_COLUMN_LOCATION_ID = "locationId";
    public static final String LOCATION_COLUMN_NAME = "name";

    public static final String ORGANIZATION_TABLE_NAME = "organizations";
    public static final String ORGANIZATION_COLUMN_ORGANIZATION_ID = "organizationId";
    public static final String ORGANIZATION_COLUMN_NAME = "name";

    protected SQLiteDatabase sqdb;
    DBHelper dbHelper;


    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        this.dbHelper = this;
        this.sqdb = dbHelper.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + VEHICLE_ENTRY_TABLE_NAME + " (id integer primary key autoincrement, data text)");
        db.execSQL("create table " + VEHICLE_TABLE_NAME + " (id integer primary key, vin text, year text, make text, model text, color text)");
        db.execSQL("create table " + BIN_TABLE_NAME + " ( id INTEGER PRIMARY KEY autoincrement, binId TEXT, name TEXT)");
        db.execSQL("create table " + PATH_TABLE_NAME + " (id integer primary key, pathId text, name text, startPath text, locationId text)");
        db.execSQL("create table " + USER_TABLE_NAME + " (userId integer primary key, pin text, organizationId text, name text)");
        db.execSQL("create table " + LOCATION_TABLE_NAME + " ( id integer primary key autoincrement, locationId text, name text)");
        db.execSQL("create table " + ORGANIZATION_TABLE_NAME + " ( id integer primary key autoincrement, organizationId text, name text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_ENTRY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VEHICLE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BIN_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PATH_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ORGANIZATION_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertVehicleEntry(String data)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(VEHICLE_ENTRY_COLUMN_ID, id);
        contentValues.put(VEHICLE_ENTRY_COLUMN_DATA, data);

        db.insert(VEHICLE_ENTRY_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertVehicle(String vin, int year, String make, String model, String color)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(VEHICLE_ENTRY_COLUMN_VIN, vin);
        contentValues.put(VEHICLE_COLUMN_YEAR, year);
        contentValues.put(VEHICLE_COLUMN_MAKE, make);
        contentValues.put(VEHICLE_COLUMN_MODEL, model);
        contentValues.put(VEHICLE_COLUMN_COLOR, color);

        db.insert(VEHICLE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertBin(int binId, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BIN_COLUMN_BIN_ID, binId);
        contentValues.put(BIN_COLUMN_NAME, name);

        db.insert(BIN_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPath(int pathId, String name, int startPath, int locationId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PATH_COLUMN_PATH_ID, pathId);
        contentValues.put(PATH_COLUMN_NAME, name);
        contentValues.put(PATH_COLUMN_START_PATH, startPath);
        contentValues.put(PATH_COLUMN_LOCATION_ID, locationId);

        db.insert(PATH_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertUser(int userId, int pin, int orgId, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_USER_ID, userId);
        contentValues.put(USER_COLUMN_PIN, pin);
        contentValues.put(USER_COLUMN_ORGANIZATION_ID, orgId);
        contentValues.put(USER_COLUMN_NAME, name);

        // this is an insert/update
        db.insertWithOnConflict(USER_TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return true;
    }

    public boolean insertLocation(int locationId, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOCATION_COLUMN_LOCATION_ID, locationId);
        contentValues.put(LOCATION_COLUMN_NAME, name);

        db.insert(LOCATION_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertOrganization(int organizationId, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ORGANIZATION_COLUMN_ORGANIZATION_ID, organizationId);
        contentValues.put(ORGANIZATION_COLUMN_NAME, name);

        db.insert(ORGANIZATION_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean updateVehicleEntry(Integer id, int locationId, int binId, int pathId, String notes, int userId, String vin, int year, String make, String model, String color)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(VEHICLE_ENTRY_COLUMN_LOCATION, locationId);
        contentValues.put(VEHICLE_ENTRY_COLUMN_BIN, binId);
        contentValues.put(VEHICLE_ENTRY_COLUMN_PATH, pathId);
        contentValues.put(VEHICLE_ENTRY_COLUMN_NOTES, notes);
        contentValues.put(VEHICLE_ENTRY_COLUMN_USER, userId);
        contentValues.put(VEHICLE_ENTRY_COLUMN_VIN, vin);
        contentValues.put(VEHICLE_ENTRY_COLUMN_YEAR, year);
        contentValues.put(VEHICLE_ENTRY_COLUMN_MAKE, make);
        contentValues.put(VEHICLE_ENTRY_COLUMN_MODEL, model);
        contentValues.put(VEHICLE_ENTRY_COLUMN_COLOR, color);
        db.update(VEHICLE_ENTRY_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updateVehicles(Integer id, String vin, int year, String make, String model, String color)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(VEHICLE_ENTRY_COLUMN_VIN, vin);
        contentValues.put(VEHICLE_ENTRY_COLUMN_YEAR, year);
        contentValues.put(VEHICLE_ENTRY_COLUMN_MAKE, make);
        contentValues.put(VEHICLE_ENTRY_COLUMN_MODEL, model);
        contentValues.put(VEHICLE_ENTRY_COLUMN_COLOR, color);
        db.update(VEHICLE_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updateBins(Integer id, int binId, String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BIN_COLUMN_ID, binId);
        contentValues.put(BIN_COLUMN_NAME, name);

        db.update(BIN_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public boolean updatePaths(Integer id, int binId, String name, int startPath)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PATH_COLUMN_ID, binId);
        contentValues.put(PATH_COLUMN_NAME, name);
        contentValues.put(PATH_COLUMN_START_PATH, startPath);

        db.update(PATH_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Cursor getVehicleEntryById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from vehicleEntry where in="+id+"", null );
        return res;
    }

    public Cursor getVehicleEntries(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + VEHICLE_ENTRY_TABLE_NAME , null );
        return res;
    }

    public Cursor getVehicles(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + VEHICLE_TABLE_NAME , null );
        return res;
    }

    public Cursor getBins(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + BIN_TABLE_NAME , null );
        return res;
    }

    public Cursor getPaths(int locationId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + PATH_TABLE_NAME + " where locationId="+String.valueOf(locationId)+"", null );
        return res;
    }

    public Cursor getLocations(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + LOCATION_TABLE_NAME , null );
        return res;
    }

    public Cursor getOrganizations(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + ORGANIZATION_TABLE_NAME , null );
        return res;
    }

    public Cursor getUserByPin(int pin){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c =  db.rawQuery( "select * from " + USER_TABLE_NAME + " where pin="+pin+"", null );
        return c;
    }

    public boolean isUserStored(String pin) {
        Cursor c = null;
        SQLiteDatabase db = this.getReadableDatabase();
        boolean result = false;
        try {

            String query = "select count(*) from " + USER_TABLE_NAME + " where pin = ?";
            c = db.rawQuery(query, new String[] {pin});
            if (c.moveToFirst()) {
                if(c.getInt(0) != 0)
                    result = true;
            }
        }
        finally {
            if (c != null) {
                c.close();
            }
            if (db != null) {
                db.close();
            }

            return result;
        }
    }

    public Integer deleteVehicleEntry (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(VEHICLE_ENTRY_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer deleteVehicle (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(VEHICLE_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer deleteBin (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(BIN_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Integer deletePath (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(PATH_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public void clearVehicleEntryTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + VEHICLE_ENTRY_TABLE_NAME);
    }

    public void clearVehicleTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + VEHICLE_TABLE_NAME);
    }

    public void clearBinTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + BIN_TABLE_NAME);
    }

    public void clearLocationTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + LOCATION_TABLE_NAME);
    }

    public void clearOrganizationTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + ORGANIZATION_TABLE_NAME);
    }

    public void clearPathTable()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PATH_TABLE_NAME);
    }

}
