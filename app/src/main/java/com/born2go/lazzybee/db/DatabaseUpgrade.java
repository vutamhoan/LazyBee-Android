package com.born2go.lazzybee.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.born2go.lazzybee.shared.LazzyBeeShare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hue on 8/13/2015.
 */
public class DatabaseUpgrade extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseUpgrade";
    public static String DB_NAME = "update.db";
    private static final String TABLE_SYSTEM = "system";
    public static String DB_PATH = "/data/data/com.born2go.lazzybee/databases/";

    Context context;

    public DatabaseUpgrade(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }


    public List<Card> _getAllCard() {
        String selectQuery = "SELECT * FROM vocabulary";

        return _getListCardQueryString(selectQuery);
    }

    public int _getVersionDB() {
        try {
            int version = 0;
            String selectValueByKey = "SELECT value FROM " + TABLE_SYSTEM + " where key = '" + LazzyBeeShare.DB_VERSION + "'";
            SQLiteDatabase db = this.getReadableDatabase();
            //Todo query for cursor
            Cursor cursor = db.rawQuery(selectValueByKey, null);
            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0)
                    do {
                        //TODO:get data from sqlite
                        int value = cursor.getInt(0);
                        version = value;
                    } while (cursor.moveToNext());
            }
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }


    }

    private List<Card> _getListCardQueryString(String query) {
        List<Card> datas = new ArrayList<Card>();
        SQLiteDatabase db = this.getReadableDatabase();

        //query for cursor
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            if (cursor.getCount() > 0)
                do {
                    Card card = new Card();
                    card.setId(cursor.getInt(0));

                    card.setQuestion(cursor.getString(LazzyBeeShare.CARD_INDEX_QUESTION));
                    card.setAnswers(cursor.getString(LazzyBeeShare.CARD_INDEX_ANSWER));

                    card.setPackage(cursor.getString(LazzyBeeShare.CARD_INDEX_PACKAGE));
                    card.setLevel(cursor.getInt(LazzyBeeShare.CARD_INDEX_LEVEL));
                    card.setL_en(cursor.getString(LazzyBeeShare.CARD_INDEX_L_EN));
                    card.setL_vn(cursor.getString(LazzyBeeShare.CARD_INDEX_L_VN));
                    datas.add(card);

                } while (cursor.moveToNext());
        }
        Log.i(TAG, "Query String: " + query + " --Result card count:" + datas.size());
        return datas;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void copyDataBase(int type) throws IOException {

        //Open your local db as the input stream
        //InputStream myInput = myContext.getAssets().open("/" + SDCARD + "/" + DOWNLOAD + "/" + DB_NAME);

//        Open your local db as the input stream
        InputStream myInput;
        File source = null;

        if (type == 0) {
            myInput = context.getAssets().open(DB_NAME);
        } else {
            File sdCard_dir = Environment.getExternalStorageDirectory();
            File dlDir = new File(sdCard_dir.getAbsolutePath());
            dlDir.mkdirs();
            source = new File(dlDir, DB_NAME);
            myInput = new FileInputStream(source);
        }

//        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

        //delete file after update
        if (source != null)
            source.delete();


    }


    public void _createDataBase() {
        String myPath = DB_PATH + DB_NAME;
        boolean dbExist = checkDataBase(myPath);

        if (dbExist) {
            //do nothing - database already exist
            Log.i(TAG, "database in " + myPath + " already exist");
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
//            Log.e(TAG, "copyDataBase");

            try {

                copyDataBase(0);

            } catch (IOException e) {
                Log.e(TAG, "Error copying database:" + e.getMessage());
                throw new Error("Error copying database");

            }
        }
    }

    public boolean checkDataBase(String myPath) {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            //database does't exist yet.
            Log.e(TAG, "database in " + myPath + " does't exist yet:" + e.getMessage());
        }
        if (checkDB != null) {
            checkDB.close();
        }


        return checkDB != null ? true : false;
    }
}
