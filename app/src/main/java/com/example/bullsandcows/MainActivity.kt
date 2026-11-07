package com.example.bullsandcows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.random.Random

data class Attempt(val number: String, val bulls: Int, val cows: Int)

class MainActivity : AppCompatActivity() {

    private var secret = ""
    private val history = mutableListOf<Attempt>()
    private lateinit var adapter: HistoryAdapter
    private lateinit var input: EditText
    private lateinit var hintText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.attemptInput)
        hintText = findViewById(R.id.hintText)
        val list = findViewById<RecyclerView>(R.id.historyList)

        adapter = HistoryAdapter()
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        findViewById<Button>(R.id.attemptButton).setOnClickListener { checkAttempt() }
        findViewById<Button>(R.id.newGameButton).setOnClickListener { startNewGame() }

        startNewGame()
    }

    private fun startNewGame() {
        secret = generateSecret()
        history.clear()
        adapter.replace(history)
        input.setText("")
        hintText.text = "Угадайте 4-значное число из разных цифр (первая — не ноль)"
    }

    private fun generateSecret(): String {
        val digits = ('0'..'9').toMutableList()
        val sb = StringBuilder()
        // первая цифра — не ноль
        while (true) {
            val d = digits[Random.nextInt(digits.size)]
            if (d != '0') {
                sb.append(d)
                digits.remove(d)
                break
            }
        }
        repeat(3) {
            val d = digits[Random.nextInt(digits.size)]
            sb.append(d)
            digits.remove(d)
        }
        return sb.toString()
    }

    private fun checkAttempt() {
        val text = input.text.toString().trim()

        if (text.length != 4 || !text.all { it.isDigit() } ||
            text[0] == '0' || text.toSet().size != 4
        ) {
            Toast.makeText(
                this,
                "Нужно 4 разные цифры, первая — не ноль",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        var bulls = 0
        var cows = 0
        for (i in 0 until 4) {
            if (text[i] == secret[i]) {
                bulls++
            } else if (text[i] in secret) {
                cows++
            }
        }

        history.add(Attempt(text, bulls, cows))
        adapter.replace(history)
        input.setText("")

        if (bulls == 4) {
            AlertDialog.Builder(this)
                .setTitle("Победа!")
                .setMessage("Число $secret угадано за ${history.size} попыток!")
                .setPositiveButton("Новая игра") { _, _ -> startNewGame() }
                .setNegativeButton("Выйти") { _, _ -> finish() }
                .setCancelable(false)
                .show()
        }
    }

    class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {
        private val data = mutableListOf<Attempt>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun getItemCount() = data.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val a = data[position]
            holder.text.text =
                "${position + 1}. ${a.number}  —  быки: ${a.bulls}, коровы: ${a.cows}"
        }

        fun replace(list: List<Attempt>) {
            data.clear()
            data.addAll(list)
            notifyDataSetChanged()
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(android.R.id.text1)
        }
    }
}
