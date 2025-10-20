package com.ro2santi.modul06

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME,
        null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Kafe.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "menu_kafe"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAMA = "nama"
        private const val COLUMN_HARGA = "harga"
        private const val COLUMN_URL_GAMBAR = "url_gambar"
    }

    // Dipanggil saat database dibuat pertama kali
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAMA TEXT," +
                "$COLUMN_HARGA INTEGER," +
                "$COLUMN_URL_GAMBAR TEXT)"
        db.execSQL(CREATE_TABLE)
    }

    // Dipanggil saat DATABASE_VERSION diubah
    override fun onUpgrade(
        db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // --- OPERASI CRUD (Create) ---
    fun insertMenu(menu: MenuModel) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, menu.nama)
            put(COLUMN_HARGA, menu.harga)
            put(COLUMN_URL_GAMBAR, menu.urlGambar)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // --- OPERASI CRUD (Read) ---
    fun getAllMenus(): List<MenuModel> {
        val menuList = mutableListOf<MenuModel>()
        val db = this.readableDatabase
        val cursor: Cursor? =
            db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        cursor?.use { // Menggunakan use untuk menutup cursor secara otomatis
            if (it.moveToFirst()) {
                do {
                    val id =
                        it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_ID))
                    val nama =
                        it.getString(
                            it.getColumnIndexOrThrow(COLUMN_NAMA))
                    val harga =
                        it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_HARGA))
                    val urlGambar =
                        it.getString(
                            it.getColumnIndexOrThrow(COLUMN_URL_GAMBAR))

                    val menu = MenuModel(id, nama, harga, urlGambar)
                    menuList.add(menu)
                } while (it.moveToNext())
            }
        }
        db.close()
        return menuList
    }

    // --- OPERASI CRUD (Update)
    fun updateMenu(menu: MenuModel) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAMA, menu.nama)
            put(COLUMN_HARGA, menu.harga)
            put(COLUMN_URL_GAMBAR, menu.urlGambar)
        }

        // Update data di tabel berdasarkan COLUMN_ID
        db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(menu.id.toString())
        )
        db.close()
    }

    // --- OPERASI CRUD (Delete)
    fun deleteMenu(id: Int) {
        val db = this.writableDatabase

        // Hapus baris dari tabel berdasarkan COLUMN_ID
        db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
    }

    // --- Operasi Tambahan (Reset untuk Inisialisasi) ---
    fun getMenuCount(): Int {
        val db = this.readableDatabase
        val cursor =
            db.rawQuery(
                "SELECT count(*) FROM $TABLE_NAME", null)
        cursor.use {
            if (it.moveToFirst()) {
                // Mengambil nilai integer dari kolom pertama (count)
                return it.getInt(0)
            }
        }
        return 0
    }
}