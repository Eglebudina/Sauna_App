package org.wit.sauna.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import org.wit.sauna.R
import org.wit.sauna.activities.map.Constants
import org.wit.sauna.activities.map.MYLocation1
import org.wit.sauna.activities.map.mapactivitys
import org.wit.sauna.activities.profile.ProfileActivity
import org.wit.sauna.adapters.SaunaListener
import org.wit.sauna.adapters.getpostdataforcategoriesforuser
import org.wit.sauna.databinding.ActivitySaunaListBinding
import org.wit.sauna.display.displayad
import org.wit.sauna.main.MainApp
import org.wit.sauna.models.SaunaModel
import org.wit.sauna.models.setdata
import org.wit.sauna.utils.Preferences

class SaunaListActivity : AppCompatActivity(), SaunaListener {

    lateinit var app: MainApp
    private lateinit var binding: ActivitySaunaListBinding
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>
    var drawerLayout: DrawerLayout? = null
    var btntogle: ImageView? = null
    private val PERMISSION_REQUEST_CODE = 1
    var recyclerView: RecyclerView? = null
    var ad: ArrayList<setdata> = ArrayList<setdata>()
    private var postadapter: getpostdataforcategoriesforuser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaunaListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = title
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView!!.layoutManager = LinearLayoutManager(this@SaunaListActivity)

        setSupportActionBar(binding.toolbar)
        //permissions
        drawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        drawerLayout!!.setScrimColor(Color.TRANSPARENT)
        //to open drawer
        btntogle = findViewById<ImageView>(R.id.btnToggle)
        btntogle!!.setOnClickListener(View.OnClickListener { drawerLayout!!.openDrawer(Gravity.LEFT) })
        // For Navigation click
        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val navMenuView = navigationView.getChildAt(0) as NavigationMenuView
        navMenuView.addItemDecoration(
            DividerItemDecoration(
                this@SaunaListActivity,
                DividerItemDecoration.VERTICAL
            )
        )
        navigationView.setNavigationItemSelectedListener { item: MenuItem? ->
            onNavigationItemSelected(
                item!!
            )
        }
        app = application as MainApp
        val layoutManager = LinearLayoutManager(this)
/*        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = SaunaAdapter(app.saunas.findAll(),this)*/
        val rep: String? = Preferences.readString(this@SaunaListActivity, "gmail")
        val rep2 = rep!!.replace(".", "")
        val cateDataa1 =
            FirebaseDatabase.getInstance().getReference("create").child("post").child(rep2)
        cateDataa1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ad.clear()
                for (petdatasnap in snapshot.children) {
                    val petData: setdata? = petdatasnap.getValue(setdata::class.java)
                    if (petData != null) {
                        ad.add(petData)
                    }
                }
                postadapter = getpostdataforcategoriesforuser(this@SaunaListActivity, ad)
                recyclerView!!.adapter = postadapter
                postadapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SaunaListActivity, "error", Toast.LENGTH_SHORT).show()
            }
        })

        registerRefreshCallback()
        checkInternet()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                val launcherIntent = Intent(this, SaunaActivity::class.java)
                refreshIntentLauncher.launch(launcherIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaunaClick(sauna: SaunaModel) {
        val launcherIntent = Intent(this, SaunaActivity::class.java)
        launcherIntent.putExtra("sauna_edit", sauna)
        refreshIntentLauncher.launch(launcherIntent)
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { binding.recyclerView.adapter?.notifyDataSetChanged() }
    }
    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this@SaunaListActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return if (result == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            false
        }
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_home) {
            val drawer = findViewById<View>(R.id.drawerLayout) as DrawerLayout
            drawer.closeDrawer(GravityCompat.START)
        }
        if (id == R.id.nav_maps) {
            val intent = Intent(this, mapactivitys::class.java)
            startActivity(intent)
        }
        if (id == R.id.nav_profile) {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
        if (id == R.id.nav_settings) {
            val intent = Intent(this, displayad::class.java)
            startActivity(intent)
        }
        val drawer = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkGPSStatus() {
        var locationManager: LocationManager? = null
        var gps_enabled = false
        var network_enabled = false
        if (locationManager == null) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }
        try {
            network_enabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }
        if (!gps_enabled && !network_enabled) {
            val dialog = AlertDialog.Builder(this@SaunaListActivity)
            dialog.setMessage("GPS not enabled")
            dialog.setPositiveButton(
                "Ok"
            ) { dialog, which -> //this will navigate user to the device location settings screen
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                this@SaunaListActivity.startActivityForResult(myIntent, 100)
            }
            val alert = dialog.create()
            alert.show()
        } else {
            getLocation1()
            Handler().postDelayed({
                //goButton.setVisibility(View.VISIBLE);
                //                    progressBar.setVisibility(View.GONE);
                val sharedPref: SharedPreferences =
                    this@SaunaListActivity.getSharedPreferences("location", 0)
                val lat = sharedPref.getString("lat", "") //0 is the default value
                val lng = sharedPref.getString("lng", "") //0 is the default value
                if (lat != "" && lng != "") {
                    //   Double lati =Double.valueOf(lat);
                    //   Double longi =Double.valueOf(lng);
                }
            }, 2500)
        }
    }

/*
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray
    ) {
        when (requestCode) {
            150 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission with request code 1 granted
                //   Toast.makeText(this, "Permission Granted" , Toast.LENGTH_LONG).show();
                checkInternet()
            } else {
                //permission with request code 1 was not granted
                Toast.makeText(this, "Permission was not Granted", Toast.LENGTH_LONG).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        }
    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkGPSStatus()
        // checkGPSStatus();
    }
    private fun getLocation1() {
        val locationResult: MYLocation1.LocationResult = object : MYLocation1.LocationResult() {
            override fun gotLocation(location: Location?) {
                if (location != null) {
                    runOnUiThread {
                        // change UI elements here
                        Constants.location = location
                        //progressBar.setVisibility(View.GONE);
                        val sharedPref = applicationContext.getSharedPreferences("location", 0)
                        val editor = sharedPref.edit()
                        editor.putString("lat", java.lang.Double.toString(location.latitude))
                        editor.putString("lng", java.lang.Double.toString(location.longitude))
                        editor.apply()


                        // goButton.setVisibility(View.VISIBLE);

                        //                            Log.e("-check","lat:"+Constants.location.getLatitude());
                        //                            Log.e("-check","lat:"+Constants.location.getLongitude());

                        //                     Intent i = new Intent(getApplicationContext(), AddNewPin.class);
                        //                     startActivity(i);

                        //  Toast.makeText(SplashActivity.this, "lat"+location.getLatitude(), Toast.LENGTH_SHORT).show();
                        //  Toast.makeText(SplashActivity.this, "lng: "+location.getLongitude(), Toast.LENGTH_SHORT).show();

                        //  btnStart.setVisibility(View.VISIBLE);
                        //  progressBarloading.setVisibility(View.INVISIBLE);
                    }
                    //Got the location!
                } else {
                    runOnUiThread {
                        // change UI elements here
                        Toast.makeText(this@SaunaListActivity, "Please try again", Toast.LENGTH_SHORT)
                            .show()
                        //     ShowDialogForDialog("start");
                    }
                }
            }
        }
        val myLocation = MYLocation1()
        myLocation.getLocation(this, locationResult)
    }
    private fun checkInternet() {
        if (isNetworkAvailable(this@SaunaListActivity)) {
            checkGPSStatus()
        } else {
            val alertDialog = AlertDialog.Builder(this@SaunaListActivity)
            alertDialog.setTitle("Internet Error")
            alertDialog.setMessage("Internet is not enabled! ")
            alertDialog.setPositiveButton(
                "Retry"
            ) { dialog, which ->
                dialog.cancel()
                checkInternet()
            }
            alertDialog.setNegativeButton(
                "Cancel"
            ) { dialog, which ->
                dialog.cancel()
                System.exit(0)
            }
            alertDialog.show()
        }
    }
    private fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        // if no network is availablgoButton_ide networkInfo will be null, otherwise check if we are connected
        try {
            @SuppressLint("MissingPermission") val activeNetworkInfo =
                connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        } catch (e: java.lang.Exception) {
            Log.e("UtilsClass", "isNetworkAvailable()::::" + e.message)
        }
        return false
    }


}