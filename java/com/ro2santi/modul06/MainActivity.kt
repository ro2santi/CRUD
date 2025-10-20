package com.ro2santi.modul06

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.GridLayoutManager

// 💡 IMPLEMENTASI OnItemClickListener dari MenuAdapter
class MainActivity : AppCompatActivity(), MenuAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper

    // Data inisialisasi
    private val initialMenus = listOf(
        MenuModel(1, "Espresso Klasik",20000,
            "https://ro2santi.com/cafe/menu/espresso_klasik.jpg"),
        MenuModel(2, "Latte Caramel",35000,
            "https://ro2santi.com/cafe/menu/latte_caramel.jpg"),
        MenuModel(3, "Teh Lychee Segar",25000,
            "https://ro2santi.com/cafe/menu/teh_lychee_segar.jpg")
    )

    private lateinit var fabAddMenu: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.menu_recycler_view)

        // Menggunakan GridLayoutManager dengan 2 kolom
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        dbHelper = DBHelper(this)

        fabAddMenu = findViewById(R.id.fab_add_menu)

        // Logika Coroutine untuk Memuat Data
        GlobalScope.launch(Dispatchers.IO) {
            initializeDataIfNeeded()
            loadAndDisplayMenus()
        }

        // Listener untuk Tombol Tambah Menu
        fabAddMenu.setOnClickListener {
            showAddMenuDialog()
        }
    }

    // --- IMPLEMENTASI INTERFACE CLICK LISTENER ---
    // 💡 Fungsi ini akan dipanggil dari MenuAdapter saat item di-tap
    override fun onItemClick(menu: MenuModel) {
        showEditMenuDialog(menu) // Lanjut ke dialog Edit/Delete
    }

    // --- FUNGSI UNTUK MUAT DAN TAMPILKAN MENU ---
    private suspend fun loadAndDisplayMenus() {
        val menuList = dbHelper.getAllMenus()
        withContext(Dispatchers.Main) {
            // 💡 Menggunakan 'this' (MainActivity) sebagai listener
            recyclerView.adapter = MenuAdapter(menuList, this@MainActivity)
        }
    }

    // --- FUNGSI TAMBAH MENU (Sama seperti sebelumnya) ---
    private fun showAddMenuDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_menu, null)
        val etNama = dialogView.findViewById<EditText>(R.id.et_nama_menu)
        val etHarga = dialogView.findViewById<EditText>(R.id.et_harga_menu)
        val etUrl = dialogView.findViewById<EditText>(R.id.et_url_gambar)

        AlertDialog.Builder(this)
            .setTitle("Tambah Menu Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val nama = etNama.text.toString().trim()
                val hargaStr = etHarga.text.toString().trim()
                val url = etUrl.text.toString().trim()

                if (nama.isEmpty() || hargaStr.isEmpty() || url.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val harga = try { hargaStr.toInt() } catch (e: Exception) { 0 }
                val newMenu = MenuModel(id = 0, nama = nama, harga = harga, urlGambar = url)

                GlobalScope.launch(Dispatchers.IO) {
                    dbHelper.insertMenu(newMenu)
                    loadAndDisplayMenus()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "${nama} berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // --- 💡 FUNGSI BARU: DIALOG UNTUK EDIT DAN DELETE ---
    private fun showEditMenuDialog(menu: MenuModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_menu, null)
        val etNama = dialogView.findViewById<EditText>(R.id.et_nama_menu)
        val etHarga = dialogView.findViewById<EditText>(R.id.et_harga_menu)
        val etUrl = dialogView.findViewById<EditText>(R.id.et_url_gambar)

        // Isi dengan data menu yang sudah ada
        etNama.setText(menu.nama)
        etHarga.setText(menu.harga.toString())
        etUrl.setText(menu.urlGambar)

        AlertDialog.Builder(this)
            .setTitle("Edit Menu: ${menu.nama}")
            .setView(dialogView)
            // Tombol "Update"
            .setPositiveButton("Update") { d, _ ->
                val namaBaru = etNama.text.toString().trim()
                val hargaStrBaru = etHarga.text.toString().trim()
                val urlBaru = etUrl.text.toString().trim()

                if (namaBaru.isEmpty() || hargaStrBaru.isEmpty() || urlBaru.isEmpty()) {
                    Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val hargaBaru = try { hargaStrBaru.toInt() } catch (e: Exception) { 0 }

                // Buat objek MenuModel yang diperbarui (ID menggunakan ID menu lama)
                val updatedMenu = MenuModel(
                    id = menu.id,
                    nama = namaBaru,
                    harga = hargaBaru,
                    urlGambar = urlBaru
                )

                // Update di Database
                GlobalScope.launch(Dispatchers.IO) {
                    dbHelper.updateMenu(updatedMenu)
                    loadAndDisplayMenus()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "${namaBaru} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                }
                d.dismiss()
            }
            // Tombol "Batal"
            .setNegativeButton("Batal", null)
            // Tombol "Hapus" (Neutral)
            .setNeutralButton("Hapus") { d, _ ->
                showDeleteConfirmationDialog(menu) // Panggil dialog konfirmasi hapus
                d.dismiss()
            }
            .show()
    }

    // --- 💡 FUNGSI BARU: KONFIRMASI HAPUS ---
    private fun showDeleteConfirmationDialog(menu: MenuModel) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Menu")
            .setMessage("Anda yakin ingin menghapus menu '${menu.nama}'?")
            .setPositiveButton("Hapus") { _, _ ->
                // Hapus di Database
                GlobalScope.launch(Dispatchers.IO) {
                    dbHelper.deleteMenu(menu.id) // Panggil fungsi delete di DBHelper
                    loadAndDisplayMenus() // Muat ulang data
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "${menu.nama} telah dihapus!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Fungsi untuk mengisi database jika masih kosong
    private fun initializeDataIfNeeded() {
        if (dbHelper.getMenuCount() == 0) {
            initialMenus.forEach { dbHelper.insertMenu(it) }
        }
    }
}