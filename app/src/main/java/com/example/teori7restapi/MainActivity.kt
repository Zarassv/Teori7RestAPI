package com.example.teori7restapi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private enum class Tampilan { LOADING, SUKSES, ERROR }

    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var tvPesanError: TextView
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus      = findViewById(R.id.tvStatus)
        listView      = findViewById(R.id.listUser)
        layoutLoading = findViewById(R.id.layoutLoading)
        layoutError   = findViewById(R.id.layoutError)
        tvPesanError  = findViewById(R.id.tvPesanError)

        adapter = UserAdapter(this)
        listView.adapter = adapter

        // Latihan Soal 5: klik pengguna -> lihat postingannya
        listView.setOnItemClickListener { _, _, posisi, _ ->
            val user = adapter.getItem(posisi) ?: return@setOnItemClickListener
            startActivity(
                Intent(this, PostActivity::class.java)
                    .putExtra(PostActivity.EXTRA_USER_ID, user.id)
                    .putExtra(PostActivity.EXTRA_USER_NAME, user.name)
            )
        }

        findViewById<Button>(R.id.btnCobaLagi).setOnClickListener { muatUsers() }
        findViewById<Button>(R.id.btnPost).setOnClickListener { kirimPostingan() }

        muatUsers()
    }

    // ================= GET: daftar pengguna =================
    private fun muatUsers() {
        tvStatus.text = "Memuat data..."
        atur(Tampilan.LOADING)

        // lifecycleScope: coroutine otomatis dibatalkan saat Activity ditutup
        lifecycleScope.launch {
            try {
                val daftar = RetrofitClient.api.getUsers()

                adapter.clear()
                adapter.addAll(daftar)
                tvStatus.text = "${daftar.size} pengguna dimuat"
                atur(Tampilan.SUKSES)

            } catch (e: HttpException) {
                // server menjawab, tetapi dengan kode error (4xx / 5xx)
                tampilkanError("Server error: HTTP ${e.code()}")
            } catch (e: IOException) {
                // tidak ada jawaban: internet mati, timeout, DNS gagal
                tampilkanError("Tidak ada koneksi internet")
            }
        }
    }

    // ================= POST: buat postingan =================
    private fun kirimPostingan() {
        lifecycleScope.launch {
            try {
                val baru = PostBaru(
                    title = "Belajar Retrofit",
                    body = "Contoh POST dari aplikasi",
                    userId = 1
                )
                val hasil = RetrofitClient.api.buatPost(baru)
                tampilkanStatus("Tersimpan dengan id ${hasil.id}")
            } catch (e: HttpException) {
                tampilkanStatus("Gagal menyimpan: HTTP ${e.code()}")
            } catch (e: IOException) {
                tampilkanStatus("Tidak ada koneksi internet")
            }
        }
    }

    // ================= bantu tampilan =================
    private fun atur(tampilan: Tampilan) {
        layoutLoading.visibility = if (tampilan == Tampilan.LOADING) View.VISIBLE else View.GONE
        listView.visibility      = if (tampilan == Tampilan.SUKSES) View.VISIBLE else View.GONE
        layoutError.visibility   = if (tampilan == Tampilan.ERROR) View.VISIBLE else View.GONE
        tvStatus.visibility      = if (tampilan == Tampilan.ERROR) View.GONE else View.VISIBLE
    }

    private fun tampilkanError(pesan: String) {
        tvPesanError.text = pesan
        atur(Tampilan.ERROR)
    }

    private fun tampilkanStatus(pesan: String) {
        tvStatus.visibility = View.VISIBLE
        tvStatus.text = pesan
    }
}