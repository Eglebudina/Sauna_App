package org.wit.sauna.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import org.wit.sauna.R
import org.wit.sauna.databinding.ActivitySaunaBinding
import org.wit.sauna.main.MainApp
import org.wit.sauna.models.SaunaModel
import org.wit.sauna.showImagePicker
import timber.log.Timber.i

class SaunaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySaunaBinding
    var sauna = SaunaModel()
    lateinit var app: MainApp
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    val IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var edit = false

        binding = ActivitySaunaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbarAdd.title = title
        setSupportActionBar(binding.toolbarAdd)

        app = application as MainApp

        i("Sauna Activity started...")

        if (intent.hasExtra("sauna_edit")) {
            edit = true
            sauna = intent.extras?.getParcelable("sauna_edit")!!
            binding.saunaTitle.setText(sauna.title)
            binding.description.setText(sauna.description)
            binding.btnAdd.setText(R.string.save_sauna)
            Picasso.get()
                .load(sauna.image)
                .into(binding.saunaImage)
        }

        binding.btnAdd.setOnClickListener() {
            sauna.title = binding.saunaTitle.text.toString()
            sauna.description = binding.description.text.toString()
            if (sauna.title.isEmpty()) {
                Snackbar.make(it,R.string.enter_sauna_title, Snackbar.LENGTH_LONG)
                        .show()
            } else {
                if (edit) {
                    app.saunas.update(sauna.copy())
                } else {
                    app.saunas.create(sauna.copy())
                }
            }
            i("add Button Pressed: $sauna")
            setResult(RESULT_OK)
            finish()
        }

        binding.chooseImage.setOnClickListener {
            showImagePicker(imageIntentLauncher)
        }

        registerImagePickerCallback()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sauna, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        if (result.data != null) {
                            i("Got Result ${result.data!!.data}")
                            sauna.image = result.data!!.data!!
                            Picasso.get()
                                   .load(sauna.image)
                                   .into(binding.saunaImage)
                        } // end of if
                    }
                    RESULT_CANCELED -> { } else -> { }
                }
            }
    }
}