package com.agkminds.restra.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.agkminds.restra.R
import com.agkminds.restra.databinding.ActivityMainBinding
import com.agkminds.restra.fragment.*
import com.agkminds.restra.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var previousMenuItem: MenuItem? = null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager

    /*The action bar drawer toggle is used to handle the open and close events of the navigation drawer*/
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

//        This line will prevent the app from going in Night Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        sessionManager = SessionManager(this)
        sharedPreferences =
            this.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)

//        Setting up navigation bar
        setupBottomNavBar()

//        Setting up toolbar
        setupToolbar()

//        This will make HomeFragment as the default fragment as we launch the Restra
        transactFragmentTo(HomeFragment(), "Restaurants")

//        This will set Action Bar Toggle
        setupActionBarToggle()

        //  Below code makes the HamBurger icon functional. We are setting addDrawerListener.
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        //  Handling clicks on the menu items
        binding.navigationView.setNavigationItemSelectedListener {
            highlightMenuItems(it) // This function highlights the current selected menu items
            when (it.itemId) {
                R.id.faqs -> {
                    transactFragmentTo(FAQFragment(), "FAQ's")
                }
                R.id.logout -> {
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Logout") // Setting the Title for the Dialog Box
                    dialog.setMessage("Are you sure want to Logout?") // Setting message for the Dialog Box
                    dialog.setPositiveButton("Confirm") { _, _ ->
//                This will sign out the current user and Welcome Screen will appear
                        val editor: SharedPreferences.Editor =
                            sharedPreferences.edit()
                        editor.clear()
                        editor.apply()
                        startActivity(Intent(this, WelcomeScreen::class.java))
                        finishAffinity()
                    }
                    dialog.setNegativeButton("No") { _, _ ->
                        // Do Nothing
                    }
                    dialog.setCancelable(false)
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun setupBottomNavBar() {
        binding.bottomNavBar.setOnItemSelectedListener {
            when (it) {
                R.id.home -> {
                    transactFragmentTo(HomeFragment(), "Restaurants")
                }
                R.id.profile -> {
                    transactFragmentTo(ProfileFragment(), "Profile")
                }
                R.id.fav_restaurant -> {
                    transactFragmentTo(FavouritesFragment(), "Favourites")
                }
                R.id.order_history -> {
                    transactFragmentTo(OrderHistoryFragment(), "Order History")
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Restra"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupActionBarToggle() {
        actionBarDrawerToggle =
            object : ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer
            ) {
                override fun onDrawerStateChanged(newState: Int) {
                    super.onDrawerStateChanged(newState)
                    val pendingRunnable = Runnable {
                        val inputMethodManager =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    }
                    Handler(Looper.getMainLooper()).postDelayed(pendingRunnable, 50)
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    //    transactFragmentTo is custom function that is designed to minimize the code for fragment transaction
    private fun transactFragmentTo(passedFragment: Fragment, title: String = "Home") {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, passedFragment)
        transaction.commit()
        binding.drawerLayout.closeDrawers()
        supportActionBar?.title = title
        binding.navigationView.setCheckedItem(R.id.home) // This checks the dashboard as we open the app
    }

    //    We are overriding the onBackPressed() function & set the default behaviour to Dashboard Fragment
    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is HomeFragment -> transactFragmentTo(HomeFragment(), "All Restaurants")
            else -> super.onBackPressed()
        }
    }

    //    This is a custom function that highlights the selected menu item.
    private fun highlightMenuItems(item: MenuItem) {
        if (previousMenuItem != null) {
            previousMenuItem?.isChecked = false
        }

        item.isCheckable = true
        item.isChecked = true
        previousMenuItem = item
    }
}