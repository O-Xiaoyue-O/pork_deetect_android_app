package com.example.pork;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "history.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "history";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_VAL1 = "val1";
    public static final String COLUMN_VAL2 = "val2";
    public static final String COLUMN_AVERAGE = "average";
    public static final String COLUMN_RECIPE_TITLE = "recipe_title";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_RECIPE_CONTENT = "recipe_content";
    public static final String COLUMN_DATE = "date";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_VALUE + " TEXT, " +
                    COLUMN_VAL1 + " TEXT, " +
                    COLUMN_VAL2 + " INTEGER, " +
                    COLUMN_AVERAGE + " TEXT, " +
                    COLUMN_RECIPE_TITLE + " TEXT, " +
                    COLUMN_INGREDIENTS + " TEXT, " +
                    COLUMN_RECIPE_CONTENT + " TEXT, " +
                    COLUMN_DATE + " TEXT" +
                    ");";

    public HistoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_AVERAGE + " TEXT");
        }
    }
}
