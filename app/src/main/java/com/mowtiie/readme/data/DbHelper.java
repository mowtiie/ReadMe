package com.mowtiie.readme.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "readme.db";
    public static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, email TEXT NOT NULL, password TEXT NOT NULL, name TEXT NOT NULL)");
        db.execSQL("CREATE TABLE books (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT NOT NULL, genre TEXT NOT NULL, summary TEXT NOT NULL, published TEXT NOT NULL, status TEXT NOT NULL)");
        db.execSQL("CREATE TABLE loans (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, book_id INTEGER NOT NULL, borrowed_on TEXT NOT NULL, due_on TEXT NOT NULL, returned_on TEXT NOT NULL)");
        seedDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS loans");
        db.execSQL("DROP TABLE IF EXISTS books");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seedDemo(SQLiteDatabase db) {
        insertUser(db, "member@demo.com", "demo1234", "Demo Member");
        long alexId = insertUser(db, "alex@demo.com", "demo1234", "Alex Reyes");

        // title, author, genre, summary, published
        String[][] books = {
                {"The Great Gatsby", "F. Scott Fitzgerald", "Fiction", "A young millionaire's obsession with his lost love unravels in the Jazz Age.", "1925"},
                {"To Kill a Mockingbird", "Harper Lee", "Fiction", "A lawyer defends an innocent man while his daughter learns about prejudice in the American South.", "1960"},
                {"Pride and Prejudice", "Jane Austen", "Fiction", "Elizabeth Bennet and Mr. Darcy navigate manners, marriage and misjudgment.", "1813"},
                {"The Hobbit", "J.R.R. Tolkien", "Fantasy", "Bilbo Baggins joins a company of dwarves to reclaim a mountain from a dragon.", "1937"},
                {"Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "Fantasy", "An orphan discovers he is a wizard and begins his first year at Hogwarts.", "1997"},
                {"A Game of Thrones", "George R.R. Martin", "Fantasy", "Noble families fight for the Iron Throne as a darker threat gathers in the north.", "1996"},
                {"A Brief History of Time", "Stephen Hawking", "Science", "A tour of black holes, the Big Bang and the nature of time for general readers.", "1988"},
                {"Cosmos", "Carl Sagan", "Science", "A journey through the universe and humanity's place within it.", "1980"},
                {"The Selfish Gene", "Richard Dawkins", "Science", "Evolution viewed from the perspective of genes rather than organisms.", "1976"},
                {"Sapiens", "Yuval Noah Harari", "History", "A sweeping history of humankind from the Stone Age to the present.", "2011"},
                {"Guns, Germs, and Steel", "Jared Diamond", "History", "Why some civilizations came to dominate others across human history.", "1997"},
                {"The Diary of a Young Girl", "Anne Frank", "History", "A teenager's diary written while hiding from the Nazis in Amsterdam.", "1947"},
                {"Clean Code", "Robert C. Martin", "Technology", "Practical principles for writing readable and maintainable software.", "2008"},
                {"The Pragmatic Programmer", "Andrew Hunt and David Thomas", "Technology", "Habits and techniques for becoming a more effective programmer.", "1999"},
                {"Introduction to Algorithms", "Thomas H. Cormen", "Technology", "A comprehensive reference on algorithms and data structures.", "1990"},
        };
        // Alex already holds these (indexes into books), with days since borrowed
        int[] borrowedIndexes = {3, 6, 12};
        int[] borrowedDaysAgo = {3, 7, 1};

        for (int i = 0; i < books.length; i++) {
            int slot = -1;
            for (int j = 0; j < borrowedIndexes.length; j++) {
                if (borrowedIndexes[j] == i) slot = j;
            }
            ContentValues cv = new ContentValues();
            cv.put("title", books[i][0]);
            cv.put("author", books[i][1]);
            cv.put("genre", books[i][2]);
            cv.put("summary", books[i][3]);
            cv.put("published", books[i][4]);
            cv.put("status", slot >= 0 ? Data.STATUS_BORROWED : Data.STATUS_AVAILABLE);
            long bookId = db.insert("books", null, cv);

            if (slot >= 0) {
                ContentValues loan = new ContentValues();
                loan.put("user_id", alexId);
                loan.put("book_id", bookId);
                loan.put("borrowed_on", Data.date(-borrowedDaysAgo[slot]));
                loan.put("due_on", Data.date(14 - borrowedDaysAgo[slot]));
                loan.put("returned_on", "");
                db.insert("loans", null, loan);
            }
        }
    }

    private long insertUser(SQLiteDatabase db, String email, String password, String name) {
        ContentValues cv = new ContentValues();
        cv.put("email", email);
        cv.put("password", password);
        cv.put("name", name);
        return db.insert("users", null, cv);
    }
}
