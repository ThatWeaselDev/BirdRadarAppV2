package com.example.loginpage

import FilterFragment
import MapFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity3 : AppCompatActivity(), FilterFragment.FilterListener {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var filterFragment: FilterFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Initialize FilterFragment
        filterFragment = FilterFragment()

        // Set the initial fragment
        replaceFragment(MapFragment())

        // Set up a listener for bottom navigation item selection
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_map -> replaceFragment(MapFragment())
                R.id.menu_filter -> replaceFragment(FilterFragment())
                R.id.menu_bird -> replaceFragment(BirdFragment())
                R.id.menu_journal -> replaceFragment(JournalFragment())
            }
            true
        }

        // Set FilterListener for FilterFragment
        filterFragment.setFilterListener(this)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, fragment)
            .commit()
    }

    // Implement FilterListener methods
    override fun onRadiusSelected(radius: Int) {
        // Handle radius selection, e.g., apply filter to birds
        // You may call a method in the MapFragment to filter birds based on the radius
        // For now, let's just log the selected radius
        println("Selected Radius: $radius")
    }

    override fun onFilterChanged(radius: Int) {
        // Handle filter changes if needed
    }
}
