package com.irjarqui.unitracknetv3.ui.intro

import android.R
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.irjarqui.unitracknetv3.databinding.ActivityAboutBinding
import com.irjarqui.unitracknetv3.ui.topology.view.TopologyStatusActivity

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSign.setOnClickListener {
            startActivity(Intent(this, TopologyStatusActivity::class.java))
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            finish()
        }
    }
}