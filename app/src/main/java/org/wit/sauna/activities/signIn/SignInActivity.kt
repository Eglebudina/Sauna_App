package org.wit.sauna.activities.signIn

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import org.wit.sauna.R
import org.wit.sauna.activities.SaunaListActivity
import org.wit.sauna.activities.signUp.SignUpActivity
import org.wit.sauna.utils.Preferences

class SignInActivity : AppCompatActivity() {
    private var password: String? = null
    private var cnicc: String? = null
    var ref: DatabaseReference? = null
    var fRef: DatabaseReference? = null
    var chec: CheckBox? = null
    var vsref: String? = null
    var getemail: String? = null
    var pass: EditText? = null
    var cnic: EditText? = null
    var email: EditText? = null
    var login: TextView? = null
    var act: TextView? = null
    var zref: DatabaseReference? = null
    var str_email: String? = null
    var str_password: String? = null
    var status: String? = null
    var namew: String? = null
    var idnumberrrshare: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        cnic = findViewById(R.id.nic)
        chec = findViewById(R.id.checkBox)
        pass = findViewById(R.id.passw)
        email = findViewById(R.id.email)
        login = findViewById(R.id.loginbtn)
        act = findViewById(R.id.changeact)
        act!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@SignInActivity, SignUpActivity::class.java)
            startActivity(intent)
        })
        cnicc = cnic!!.getText().toString()
        requestPermissions()
        checkGPSStatus()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val sname = preferences.getString("name", "")
        val sstatus = preferences.getString("status", "")
        val checktrue = preferences.getBoolean("check", false)
        zref = FirebaseDatabase.getInstance().getReference("users")
        zref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot1 in snapshot.children) {
                    vsref = snapshot1.child("email").getValue(String::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        if (checktrue) {
            if (sstatus == "admin") {
     /*           val intent = Intent(this@SignInActivity, nav::class.java)
                intent.putExtra("getstatus", sstatus)
                intent.putExtra("getname", sname)
                startActivity(intent)
                finish()*/
            } else {
                val intent = Intent(this@SignInActivity, SaunaListActivity::class.java)
                intent.putExtra("getstatus", sstatus)
                intent.putExtra("getname", sname)
                startActivity(intent)
                finish()
            }
        } else {
            ref = FirebaseDatabase.getInstance().reference
            password = pass!!.getText().toString()
            login!!.setOnClickListener(View.OnClickListener {
                cnicc = cnic!!.getText().toString()
                password = pass!!.getText().toString()
                getemail = email!!.getText().toString()
                val a = getemail!!.replace(".", "")
                fRef = FirebaseDatabase.getInstance().getReference("users").child(a)
                fRef!!.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        str_email =
                            snapshot.child("email").getValue(
                                String::class.java
                            )
                        status =
                            snapshot.child("status").getValue(
                                String::class.java
                            )
                        str_password =
                            snapshot.child("pass").getValue(
                                String::class.java
                            )
                        idnumberrrshare =
                            snapshot.child("id").getValue(
                                String::class.java
                            )
                        namew = snapshot.child("name").getValue(
                            String::class.java
                        )


                        if (getemail == str_email && password == str_password) {
                            Preferences.writeString(this@SignInActivity, "gmail", getemail)
                            ///////////
                            if (chec!!.isChecked()) {
                                val preferences =
                                    PreferenceManager.getDefaultSharedPreferences(this@SignInActivity)
                                val editor = preferences.edit()
                                editor.putString("status", status)
                                editor.putString("name", namew)
                                editor.putString(
                                    "email",
                                  str_email
                                )
                                editor.putString(
                                    "idnumberrrshare",
                                    idnumberrrshare
                                )
                                editor.putBoolean("check", true)
                                //                                            Toast.makeText(signupactivity.this, "success", Toast.LENGTH_LONG).show();
                                editor.apply()
                            }
                            /*if (status == "admin") {
                                val intent = Intent(this@SignInActivity, nav::class.java)
                                intent.putExtra(
                                    "getemail",
                                    code.tar.iq.signIn.signupactivity.str_email
                                )
                                intent.putExtra(
                                    "getstatus",
                                    code.tar.iq.signIn.signupactivity.status
                                )
                                intent.putExtra("getname", code.tar.iq.signIn.signupactivity.namew)
                                intent.putExtra(
                                    "idnumberrrshare",
                                    code.tar.iq.signIn.signupactivity.idnumberrrshare
                                )
                                startActivity(intent)
                                Toast.makeText(
                                    this@signupactivity,
                                    "success admin",
                                    Toast.LENGTH_LONG
                                ).show()
                                finishAffinity()
                            } else {*/
                                val intent = Intent(this@SignInActivity, SaunaListActivity::class.java)
                                intent.putExtra(
                                    "getemail",
                                    str_email
                                )
                                intent.putExtra(
                                    "getstatus",
                                   status
                                )
                                intent.putExtra("getname", namew)
                                intent.putExtra(
                                    "idnumberrrshare",
                                    idnumberrrshare
                                )
                                startActivity(intent)
                                Toast.makeText(
                                    this@SignInActivity,
                                    "success user",
                                    Toast.LENGTH_LONG
                                ).show()
                                finishAffinity()

                        } else {
                            Toast.makeText(
                                this@SignInActivity,
                                "Invalid Credentials ",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            })
        }
    }
    private fun requestPermissions() {


        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            }).check()

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
            val dialog = AlertDialog.Builder(this@SignInActivity)
            dialog.setMessage("GPS not enabled")
            dialog.setPositiveButton(
                "Ok"
            ) { dialog, which -> //this will navigate user to the device location settings screen
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                this@SignInActivity.startActivityForResult(myIntent, 100)
            }
            val alert = dialog.create()
            alert.show()
        }
    }

}
