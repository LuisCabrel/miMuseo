package nos.nakisha.org.miMuseoNOS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_PARAMETROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_BALIZAS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_CUADROS;
import static nos.nakisha.org.miMuseoNOS.Estructura_BD.SQL_CREATE_TABLA_SIGMATABLE;

/**
 * Created by nakis on 14/12/2017.
 */

public class BBDD_Helper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "PrimeraBBDD.db";

    public BBDD_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLA_PARAMETROS);
        db.execSQL(SQL_CREATE_TABLA_BALIZAS);
        db.execSQL(SQL_CREATE_TABLA_CUADROS);
        db.execSQL(SQL_CREATE_TABLA_SIGMATABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_PARAMETROS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_BALIZAS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_CUADROS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_SIGMATABLE);
        db.execSQL(SQL_CREATE_TABLA_PARAMETROS);
        db.execSQL(SQL_CREATE_TABLA_BALIZAS);
        db.execSQL(SQL_CREATE_TABLA_CUADROS);
        db.execSQL(SQL_CREATE_TABLA_SIGMATABLE);
        //onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_PARAMETROS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_BALIZAS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_CUADROS);
        db.execSQL(Estructura_BD.SQL_DELETE_TABLA_SIGMATABLE);
        db.execSQL(SQL_CREATE_TABLA_PARAMETROS);
        db.execSQL(SQL_CREATE_TABLA_BALIZAS);
        db.execSQL(SQL_CREATE_TABLA_CUADROS);
        db.execSQL(SQL_CREATE_TABLA_SIGMATABLE);
    }

}