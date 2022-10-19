package org.wit.sauna.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.wit.sauna.R
import org.wit.sauna.adapters.SaunaAdapter
import org.wit.sauna.adapters.SaunaListener
import org.wit.sauna.databinding.ActivitySaunaListBinding
import org.wit.sauna.main.MainApp
import org.wit.sauna.models.SaunaModel

class SaunaListActivity : AppCompatActivity(), SaunaListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivitySaunaListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaunaListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        setSupportActionBar(binding.toolbar)

        app = application as MainApp

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = SaunaAdapter(app.saunas.findAll(),this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, SaunaActivity::class.java)
                startActivityForResult(launcherIntent,0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaunaClick(sauna: SaunaModel) {
        val launcherIntent = Intent(this, SaunaActivity::class.java)
        launcherIntent.putExtra("sauna_edit", sauna)
        startActivityForResult(launcherIntent,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        binding.recyclerView.adapter?.notifyDataSetChanged()
        super.onActivityResult(requestCode, resultCode, data)
    }
}