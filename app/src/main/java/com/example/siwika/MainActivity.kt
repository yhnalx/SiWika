package com.example.siwika

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.siwika.databinding.MainBinder

class MainActivity : AppCompatActivity() {

    private lateinit var binder: MainBinder
    private val viewModel: MainViewModel by viewModels()

    // Define top-level destinations
    private val topLevelDestinations = setOf(HomeFragment::class.java, CameraFragment::class.java, SettingsFragment::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { viewModel.keepSplashAlive }
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()

        binder = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binder.lifecycleOwner = this

        if (savedInstanceState == null) {
            val sharedPref = getSharedPreferences("SiWikaProgress", Context.MODE_PRIVATE)
            val userName = sharedPref.getString("USER_NAME", null)

            if (userName == null) {
                loadFragment(NameFragment(), addToBackStack = false)
            } else {
                loadFragment(HomeFragment(), addToBackStack = false)
            }
        }

        setupNavigation()
        setupFragmentVisibilityListener()
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
    }

    private fun setupNavigation() {
        binder.bottomNavigationView.setOnItemSelectedListener { item ->
            if (binder.bottomNavigationView.selectedItemId == item.itemId) return@setOnItemSelectedListener false
            
            val fragment = when (item.itemId) {
                R.id.home -> HomeFragment()
                R.id.camera -> CameraFragment()
                R.id.about -> SettingsFragment() // Changed to SettingsFragment
                else -> null
            }

            fragment?.let { loadFragment(it, false) }

            true
        }
    }

    private fun setupFragmentVisibilityListener() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                val isTopLevel = topLevelDestinations.any { it.isInstance(f) }
                binder.bottomNavigationView.visibility = if (isTopLevel) View.VISIBLE else View.GONE
            }
        }, true)
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadFragment(CameraFragment(), addToBackStack = false)
            binder.bottomNavigationView.selectedItemId = R.id.camera
        } else {
            Toast.makeText(this, "Please Approve Camera Permission!", Toast.LENGTH_LONG).show()
        }
    }
}