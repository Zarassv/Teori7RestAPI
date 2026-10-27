package com.example.teori7restapi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

private fun buatBaris(
    context: Context,
    convertView: View?,
    parent: ViewGroup,
    utama: String,
    kecil: String
): View {
    val view = convertView
        ?: LayoutInflater.from(context).inflate(R.layout.item_baris, parent, false)
    view.findViewById<TextView>(R.id.tvUtama).text = utama
    view.findViewById<TextView>(R.id.tvKecil).text = kecil
    return view
}

class UserAdapter(context: Context) : ArrayAdapter<User>(context, 0) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val user = getItem(position)!!
        return buatBaris(context, convertView, parent, user.name, "@${user.username}")
    }
}

class PostAdapter(context: Context) : ArrayAdapter<Post>(context, 0) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val post = getItem(position)!!
        return buatBaris(context, convertView, parent, post.title, post.body.replace("\n", " "))
    }
}