package com.infinityinfoway.sfsampleapp.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.text.TextUtils;

import com.infinityinfoway.sfsampleapp.config.Config;
import com.infinityinfoway.sfsampleapp.model.GPSMasterBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DBConnector extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = Config.db_version;
    private static final String DATABASE_NAME = Config.db_name;


    //========================================================
    // store offline gps data
    //=====================================================
    private static final String TBL_GPS_DATA = "GPS_Master";
    private static final String GPS_Master_Id = "GPS_Master_Id";
    private static final String GPS_Location_Name = "GPS_Location_Name";
    private static final String GPS_Latitude = "GPS_Latitude";
    private static final String GPS_Longitude = "GPS_Longitude";
    private static final String GPS_Address = "GPS_Address";
    private static final String GPS_Battery_Percentage = "GPS_Battery_Percentage";
    private static final String GPS_Internet_Status = "GPS_Internet_Status";
    private static final String GPS_Status = "GPS_Status";
    private static final String GPS_DateTime = "GPS_DateTime";


    String createGPSMaster = "CREATE TABLE "
            + TBL_GPS_DATA + " ( "
            + GPS_Master_Id + " INTEGER PRIMARY KEY,"
            + GPS_Location_Name + " TEXT,"
            + GPS_Latitude + " TEXT , "
            + GPS_Longitude + " TEXT , "
            + GPS_Address + " TEXT, "
            + GPS_Battery_Percentage + " TEXT, "
            + GPS_Internet_Status + " TEXT, "
            + GPS_Status + " TEXT, "
            + GPS_DateTime + " TEXT " + ");";


    // =====================================================================================
// Database Class constructor
// =====================================================================================
    public DBConnector(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // =====================================================================================
// On Create Method
// =====================================================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(createGPSMaster);
        } catch (Exception ex) {
        }
    }

    // =====================================================================================
// On Upgrade Method
// =====================================================================================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_GPS_DATA);
        this.onCreate(db);
    }

    // =====================================================================================
// Insert Into GPSMaster
// =====================================================================================
    public boolean addGPSData(GPSMasterBean data) {

        int gpsCounter = 0;
        try {
            String sql = "INSERT OR REPLACE INTO "
                    + TBL_GPS_DATA
                    + " ( GPS_Master_Id, GPS_Location_Name,GPS_Latitude,GPS_Longitude,GPS_Address,GPS_Battery_Percentage,GPS_Internet_Status,GPS_Status,GPS_DateTime) VALUES (?,?,?,?,?,?,?,?,?)";
            SQLiteDatabase db = this.getWritableDatabase();

            gpsCounter = fetchLastRecordAutoId(db);


            db.beginTransaction();

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/SfSampleApp/TextFile/");

            myDir.mkdirs();

            String filename = "gpsCounter.txt";
            File file = new File(myDir, filename);

            try {
                OutputStream output = new FileOutputStream(file);
                output.write(String.valueOf(gpsCounter).getBytes());
                output.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            SQLiteStatement stmt = db.compileStatement(sql);


            stmt.bindLong(1, gpsCounter + 1); // id

            if (!TextUtils.isEmpty(data.getGPS_Location_Name())) {
                stmt.bindString(2, data.getGPS_Location_Name());
            } else {
                stmt.bindString(2, " ");
            }

            if (!TextUtils.isEmpty(data.getGPS_Latitude())) {
                stmt.bindString(3, data.getGPS_Latitude());
            } else {
                stmt.bindString(3, "0.0");
            }

            if (!TextUtils.isEmpty(data.getGPS_Longitude())) {
                stmt.bindString(4, data.getGPS_Longitude());
            } else {
                stmt.bindString(4, "0.0");
            }

            if (!TextUtils.isEmpty(data.getGPS_Address())) {
                stmt.bindString(5, data.getGPS_Address());
            } else {
                stmt.bindString(5, "");
            }

            if (!TextUtils.isEmpty(data.getGPS_Battery_Percentage())) {
                stmt.bindString(6, data.getGPS_Battery_Percentage());
            } else {
                stmt.bindString(6, " ");
            }

            if (!TextUtils.isEmpty(data.getGPS_Internet_Status())) {
                stmt.bindString(7, data.getGPS_Internet_Status());
            } else {
                stmt.bindString(7, " ");
            }

            if (!TextUtils.isEmpty(data.getGPS_Status())) {
                stmt.bindString(8, data.getGPS_Status());
            } else {
                stmt.bindString(8, " ");
            }

            if (!TextUtils.isEmpty(data.getGPS_DateTime())) {
                stmt.bindString(9, data.getGPS_DateTime());
            } else {
                stmt.bindString(9, " ");
            }


            stmt.execute();
            stmt.clearBindings();

            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }

    }

    //=====================================================================================
    // Get GPSMaster Data
    // =====================================================================================

    public List<GPSMasterBean> getGPSMasterData() {
        //" ORDER BY Notification_Master_Id DESC LIMIT 30 "; ;

        String sql = "SELECT * FROM " + TBL_GPS_DATA + " ORDER BY " + GPS_Master_Id + " LIMIT 50 ";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        List<GPSMasterBean> listGPSData = new ArrayList<>();

        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    GPSMasterBean data = new GPSMasterBean();
                    data.setGPS_Master_Id(cursor.getString(0));
                    data.setGPS_Location_Name(cursor.getString(1));
                    data.setGPS_Latitude(cursor.getString(2));
                    data.setGPS_Longitude(cursor.getString(3));
                    data.setGPS_Address(cursor.getString(4));
                    data.setGPS_Battery_Percentage(cursor.getString(5));
                    data.setGPS_Internet_Status(cursor.getString(6));
                    data.setGPS_Status(cursor.getString(7));
                    data.setGPS_DateTime(cursor.getString(8));

                    if (data != null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("1") &&
                            (!TextUtils.isEmpty(data.getGPS_Latitude())) && (!TextUtils.isEmpty(data.getGPS_Longitude())) &&
                            Double.parseDouble(data.getGPS_Latitude()) > 0 && Double.parseDouble(data.getGPS_Longitude()) > 0) {
                        listGPSData.add(data);
                    } else if (data != null && (!TextUtils.isEmpty(data.getGPS_Status())) && data.getGPS_Status().trim().equals("0")) {
                        listGPSData.add(data);
                    }
                    cursor.moveToNext();
                }
            }
        } catch (Exception ignored) {

        }
        cursor.close();
        db.close();
        return listGPSData;
    }

    //=====================================================================================
    // Delete sales order data
// =====================================================================================

    public void deleteGPSData() {
        String sql = "DELETE FROM " + TBL_GPS_DATA;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public void deleteGPSRangeData(Integer min_gps_id, Integer max_gps_id) {
        String sql = "DELETE FROM " + TBL_GPS_DATA + " WHERE " + GPS_Master_Id + " >= \""
                + min_gps_id + "\"" + " and " + GPS_Master_Id + " <= \""
                + max_gps_id + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public int fetchLastRecordAutoId(SQLiteDatabase db) {
        int autoId = 0;
        try {
            String sql = "SELECT GPS_Master_Id FROM " + TBL_GPS_DATA + " ORDER BY GPS_Master_Id DESC LIMIT 1 ";
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                autoId = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return autoId;
    }

    public int countGPSData() {

        SQLiteDatabase db = this.getWritableDatabase();

        int totalRecords = 0;
        try {
            String sql = "SELECT count(*) FROM " + TBL_GPS_DATA ;
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                totalRecords = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            db.close();
        }
        return totalRecords;
    }


}

