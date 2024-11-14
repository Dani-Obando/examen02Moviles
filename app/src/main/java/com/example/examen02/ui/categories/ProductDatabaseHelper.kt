package com.example.examen02.ui.categories

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProductDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "products.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PRODUCTS = "products"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_PRODUCTS (id INTEGER PRIMARY KEY, name TEXT, price REAL, quantity INTEGER, discount REAL)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }

    fun deleteAllProducts() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_PRODUCTS")
        db.close()
    }
}
