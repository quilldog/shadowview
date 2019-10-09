package com.pierce.demo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pierce.demo.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sv1.setOnClickListener { onClick(it) }
        sv2.setOnClickListener { onClick(it) }
        sv3.setOnClickListener { onClick(it) }
    }

    private fun onClick(it: View) {
        //test
        it.isSelected = !it.isSelected
    }
}
