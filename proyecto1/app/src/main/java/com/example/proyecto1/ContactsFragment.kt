package com.example.proyecto1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContactsFragment : Fragment() {

    private lateinit var db: ContactsDb
    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = ContactsDb(requireContext())
        container = view.findViewById(R.id.contactsContainer)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddContact)
        fabAdd.setOnClickListener { showAddContactDialog() }

        loadContacts()
    }

    private fun loadContacts() {
        container.removeAllViews()
        val contacts = db.getContacts()
        for (contact in contacts) {
            val itemView = layoutInflater.inflate(R.layout.item_contact, container, false)
            itemView.findViewById<TextView>(R.id.tvName).text = contact.name
            itemView.findViewById<TextView>(R.id.tvPhone).text = contact.phone
            itemView.findViewById<TextView>(R.id.tvEmail).text = contact.email

            itemView.findViewById<Button>(R.id.btnEdit).setOnClickListener {
                showEditContactDialog(contact)
            }

            itemView.findViewById<Button>(R.id.btnDelete).setOnClickListener {
                showDeleteConfirmationDialog(contact)
            }

            container.addView(itemView)
        }
    }

    private fun showAddContactDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)
        val etEmail = dialogView.findViewById<EditText>(R.id.etContactEmail)

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Contacto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty()) {
                    db.addContact(name, phone, email)
                    loadContacts()
                } else {
                    Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditContactDialog(contact: Contact) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)
        val etEmail = dialogView.findViewById<EditText>(R.id.etContactEmail)

        // Pre-llenar los campos con la información actual del contacto
        etName.setText(contact.name)
        etPhone.setText(contact.phone)
        etEmail.setText(contact.email)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Contacto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()
                if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty()) {
                    db.updateContact(contact.id, name, phone, email)
                    loadContacts()
                } else {
                    Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Contacto")
            .setMessage("¿Está seguro de que desea eliminar a ${contact.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.deleteContact(contact.id)
                loadContacts()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}