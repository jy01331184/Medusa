package com.bundle2;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Created by tianyang on 18/3/7.
 */
public class Bundle2Provider extends ContentProvider {

    private final static int CONTACT = 1;

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI("com.bundle2", "contact", CONTACT);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"c1", "c2"});
        if (uriMatcher.match(uri) == CONTACT) {
            cursor.addRow(new Object[]{"r1 value1", "r1 value2"});
            cursor.addRow(new Object[]{"r2 value1", "r2 value2"});
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
