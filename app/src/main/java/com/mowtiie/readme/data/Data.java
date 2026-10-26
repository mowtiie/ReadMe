package com.mowtiie.readme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Data {

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_BORROWED = "BORROWED";
    public static final String DATE_PATTERN = "dd MMM yyyy";
    public static final int LOAN_DAYS = 14;

    // Logged-in user, copied from the users table on login / sign up
    public static long USER_ID = 0;
    public static String USER_EMAIL = "";
    public static String USER_NAME = "";

    public static SQLiteDatabase db(Context context) {
        return new DbHelper(context).getWritableDatabase();
    }

    // Display text for today plus an offset in days
    public static String date(int offsetDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offsetDays);
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(calendar.getTime());
    }

    public static boolean emailExists(Context context, String email) {
        Cursor cursor = db(context).rawQuery("SELECT id FROM users WHERE LOWER(email) = ?", new String[]{email.toLowerCase()});
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public static boolean login(Context context, String email, String password) {
        Cursor cursor = db(context).rawQuery("SELECT id, email, name FROM users WHERE LOWER(email) = ? AND password = ?",
                new String[]{email.toLowerCase(), password});
        boolean found = cursor.moveToFirst();
        if (found) {
            USER_ID = cursor.getLong(0);
            USER_EMAIL = cursor.getString(1);
            USER_NAME = cursor.getString(2);
        }
        cursor.close();
        return found;
    }

    public static void updateName(Context context, String name) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        db(context).update("users", cv, "id = ?", new String[]{String.valueOf(USER_ID)});
        USER_NAME = name;
    }

    public static void logout() {
        USER_ID = 0;
        USER_EMAIL = "";
        USER_NAME = "";
    }

    public static void signUp(Context context, String name, String email, String password) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);
        USER_ID = db(context).insert("users", null, cv);
        USER_EMAIL = email;
        USER_NAME = name;
    }

    // Rows of {id, title, author, genre, status}, sorted by title
    public static ArrayList<String[]> searchBooks(Context context, String query) {
        String like = "%" + query.trim().toLowerCase() + "%";
        Cursor cursor = db(context).rawQuery("SELECT id, title, author, genre, status FROM books"
                        + " WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(genre) LIKE ?"
                        + " ORDER BY title COLLATE NOCASE",
                new String[]{like, like, like});
        ArrayList<String[]> rows = new ArrayList<>();
        while (cursor.moveToNext()) {
            rows.add(new String[]{cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)});
        }
        cursor.close();
        return rows;
    }

    // {id, title, author, genre, summary, published, status}
    public static String[] getBook(Context context, String bookId) {
        Cursor cursor = db(context).rawQuery("SELECT id, title, author, genre, summary, published, status FROM books WHERE id = ?",
                new String[]{bookId});
        cursor.moveToFirst();
        String[] book = new String[7];
        for (int i = 0; i < 7; i++) book[i] = cursor.getString(i);
        cursor.close();
        return book;
    }

    // True if the logged-in user has this book out and not yet returned
    public static boolean holdsBook(Context context, String bookId) {
        Cursor cursor = db(context).rawQuery("SELECT id FROM loans WHERE user_id = ? AND book_id = ? AND returned_on = ''",
                new String[]{String.valueOf(USER_ID), bookId});
        boolean holds = cursor.moveToFirst();
        cursor.close();
        return holds;
    }

    // Re-reads the status inside the transaction; false if the book is no longer available
    public static boolean checkout(Context context, String bookId) {
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT status FROM books WHERE id = ?", new String[]{bookId});
            cursor.moveToFirst();
            boolean available = cursor.getString(0).equals(STATUS_AVAILABLE);
            cursor.close();
            if (!available) return false;

            ContentValues loan = new ContentValues();
            loan.put("user_id", USER_ID);
            loan.put("book_id", bookId);
            loan.put("borrowed_on", date(0));
            loan.put("due_on", date(LOAN_DAYS));
            loan.put("returned_on", "");
            db.insert("loans", null, loan);

            ContentValues book = new ContentValues();
            book.put("status", STATUS_BORROWED);
            db.update("books", book, "id = ?", new String[]{bookId});
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public static void returnBook(Context context, String bookId) {
        SQLiteDatabase db = db(context);
        db.beginTransaction();
        try {
            ContentValues loan = new ContentValues();
            loan.put("returned_on", date(0));
            db.update("loans", loan, "user_id = ? AND book_id = ? AND returned_on = ''",
                    new String[]{String.valueOf(USER_ID), bookId});

            ContentValues book = new ContentValues();
            book.put("status", STATUS_AVAILABLE);
            db.update("books", book, "id = ?", new String[]{bookId});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Rows of {loan id, book id, title, author, borrowed_on, due_on, returned_on}, newest first
    public static ArrayList<String[]> myLoans(Context context, boolean returned) {
        Cursor cursor = db(context).rawQuery("SELECT l.id, b.id, b.title, b.author, l.borrowed_on, l.due_on, l.returned_on"
                        + " FROM loans l JOIN books b ON b.id = l.book_id"
                        + " WHERE l.user_id = ? AND l.returned_on " + (returned ? "!=" : "=") + " ''"
                        + " ORDER BY l.id DESC",
                new String[]{String.valueOf(USER_ID)});
        ArrayList<String[]> rows = new ArrayList<>();
        while (cursor.moveToNext()) {
            String[] row = new String[7];
            for (int i = 0; i < 7; i++) row[i] = cursor.getString(i);
            rows.add(row);
        }
        cursor.close();
        return rows;
    }

    public static String statusText(String status) {
        return status.equals(STATUS_AVAILABLE) ? "Available" : "Borrowed";
    }

    // False when no account has that email
    public static boolean resetPassword(Context context, String email, String newPassword) {
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        return db(context).update("users", cv, "LOWER(email) = ?", new String[]{email.toLowerCase()}) > 0;
    }
}
