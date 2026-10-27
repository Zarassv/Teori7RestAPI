package com.example.teori7restapi

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class PostActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USER_NAME = "user_name"
    }

    private enum class Tampilan { LOADING, SUKSES, ERROR }

    private var userId = 0

    private lateinit var tvStatus: TextView
    private lateinit var listView: ListView
    private lateinit var layoutLoading: LinearLayout
    private lateinit var layoutError: LinearLayout
    private lateinit var tvPesanError: TextView
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        val nama = intent.getStringExtra(EXTRA_USER_NAME) ?: ""
        findViewById<TextView>(R.id.tvJudulPost).text = "Postingan: $nama"

        tvStatus      = findViewById(R.id.tvStatusPost)
        listView      = findViewById(R.id.listPost)
        layoutLoading = findViewById(R.id.layoutLoadingPost)
        layoutError   = findViewById(R.id.layoutErrorPost)
        tvPesanError  = findViewById(R.id.tvPesanErrorPost)

        adapter = PostAdapter(this)
        listView.adapter = adapter

        // Latihan Soal 4: tekan lama sebuah postingan untuk menghapus
        listView.setOnItemLongClickListener { _, _, posisi, _ ->
            adapter.getItem(posisi)?.let { konfirmasiHapus(it) }
            true
        }

        findViewById<Button>(R.id.btnCobaLagiPost).setOnClickListener { muatPosts() }

        muatPosts()
    }

    // ================= GET: /users/{id}/posts =================
    private fun muatPosts() {
        tvStatus.text = "Memuat postingan..."
        atur(Tampilan.LOADING)

        lifecycleScope.launch {
            try {
                val daftar = RetrofitClient.api.getPostsByUser(userId)

                adapter.clear()
                adapter.addAll(daftar)
                tvStatus.text = "${daftar.size} postingan dimuat (tekan lama untuk menghapus)"
                atur(Tampilan.SUKSES)

            } catch (e: HttpException) {
                tampilkanError("Server error: HTTP ${e.code()}")
            } catch (e: IOException) {
                tampilkanError("Tidak ada koneksi internet")
            }
        }
    }

    // ================= DELETE: /posts/{id} =================
    private fun konfirmasiHapus(post: Post) {
        AlertDialog.Builder(this)
            .setTitle("Hapus postingan?")
            .setMessage(post.title)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ -> hapusPost(post) }
            .show()
    }

    private fun hapusPost(post: Post) {
        lifecycleScope.launch {
            try {
                val respons = RetrofitClient.api.hapusPost(post.id)

                if (respons.code() == 200) {
                    adapter.remove(post)
                    tvStatus.text = "${adapter.count} postingan (tekan lama untuk menghapus)"
                    Toast.makeText(
                        this@PostActivity, "Postingan ${post.id} berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@PostActivity, "Gagal menghapus: HTTP ${respons.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: IOException) {
                Toast.makeText(
                    this@PostActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT
                ).show()
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
}