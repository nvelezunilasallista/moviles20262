package com.example.proyecto1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Contact(val id: Long, val name: String, val phone: String, val email: String)

class ContactsDb(context: Context) : SQLiteOpenHelper(context, "contacts.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE contacts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT, email TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS contacts")
        onCreate(db)
    }

    fun addContact(name: String, phone: String, email: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
            put("email", email)
        }
        writableDatabase.insert("contacts", null, values)
    }

    fun updateContact(id: Long, name: String, phone: String, email: String) {
        val values = ContentValues().apply {
            put("name", name)
            put("phone", phone)
            put("email", email)
        }
        writableDatabase.update("contacts", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteContact(id: Long) {
        writableDatabase.delete("contacts", "id = ?", arrayOf(id.toString()))
    }

    fun getContacts(): List<Contact> {
        val list = mutableListOf<Contact>()
        val cursor = readableDatabase.query("contacts", null, null, null, null, null, "name ASC")
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"))
            val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
            list.add(Contact(id, name, phone, email))
        }
        cursor.close()
        return list
    }
}