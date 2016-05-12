package bluescreen1.vector.Models;

import android.provider.BaseColumns;

/**
 * Created by Dane on 5/10/2016.
 */
public class UserEntry implements BaseColumns {

    public static final String TABLE_NAME = "user";
    public static final String COLUMN_NAME_USER_ID = "userid";
    public static final String COLUMN_NAME_TOKEN = "token";
    public static final String COLUMN_NAME_FIRSTNAME = "fname";
    public static final String COLUMN_NAME_LASTNAME = "lname";
    public static final String COLUMN_NAME_TYPE = "type";

    public static final String[] COLUMNS = new String[]{
            COLUMN_NAME_USER_ID,
            COLUMN_NAME_TOKEN,
            COLUMN_NAME_FIRSTNAME,
            COLUMN_NAME_LASTNAME,
            COLUMN_NAME_TYPE
    };
}
