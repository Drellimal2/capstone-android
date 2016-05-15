package bluescreen1.vector.Game;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import bluescreen1.vector.Models.UserEntry;

/**
 * Created by Dane on 5/10/2016.
 */
public class GameDB extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "vector";
    private static final String USER_TABLE_NAME = "user";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + UserEntry.TABLE_NAME + "( "+
                    UserEntry._ID + " INTEGER PRIMARY KEY, " +
                    UserEntry.COLUMN_NAME_USER_ID + " INTEGER, " +
                    UserEntry.COLUMN_NAME_TOKEN + " TEXT, " +
                    UserEntry.COLUMN_NAME_FIRSTNAME + " TEXT, " +
                    UserEntry.COLUMN_NAME_LASTNAME + " TEXT, " +
                    UserEntry.COLUMN_NAME_TYPE + " TEXT);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    public GameDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}