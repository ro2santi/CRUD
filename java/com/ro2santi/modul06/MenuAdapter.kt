package com.ro2santi.modul06

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.util.Locale

class MenuAdapter(
    private val menuList: List<MenuModel>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(menu: MenuModel)
    }

    class MenuViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val nama: TextView =
            itemView.findViewById(R.id.menu_nama)
        val harga: TextView =
            itemView.findViewById(R.id.menu_harga)
        val gambar: ImageView =
            itemView.findViewById(R.id.menu_gambar)

        fun bind(menu: MenuModel) {
            nama.text = menu.nama
            // Format harga ke Rupiah
            harga.text = "Rp ${menu.harga.toRupiahFormat()}"

            // Menggunakan Picasso
            Picasso.get()
                .load(menu.urlGambar)
                .centerCrop()
                .resize(100,100)
                .into(gambar)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_menu,
                parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MenuViewHolder, position: Int) {
        val item = menuList[position]

        holder.bind(item)

        // 💡 Logika Klik: Menambahkan listener pada seluruh item
        holder.itemView.setOnClickListener {
            // Panggil onItemClick pada listener, kirim objek MenuModel
            itemClickListener.onItemClick(item)
        }
    }

    override fun getItemCount() = menuList.size
}

// Extension function untuk format Rupiah yang lebih baik
fun Int.toRupiahFormat(): String {
    // Menggunakan Locale default untuk format angka
    return String.format(Locale.getDefault(),
        "%,d", this).replace(
        ',', '.') // Mengganti koma (separator ribuan) dengan titik
}