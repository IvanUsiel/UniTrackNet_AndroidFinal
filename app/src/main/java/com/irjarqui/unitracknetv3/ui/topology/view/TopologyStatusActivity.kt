package com.irjarqui.unitracknetv3.ui.topology.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.application.UniTrackNetApp
import com.irjarqui.unitracknetv3.data.remote.repository.UserInfoRepository
import com.irjarqui.unitracknetv3.databinding.ActivityTopologyStatusBinding
import com.irjarqui.unitracknetv3.ui.alarms.model.AlarmStatus
import com.irjarqui.unitracknetv3.ui.alarms.view.AlarmsFragment
import com.irjarqui.unitracknetv3.ui.alarms.viewmodel.AlarmsViewModel
import com.irjarqui.unitracknetv3.ui.alarms.viewmodel.AlarmsViewModelFactory
import com.irjarqui.unitracknetv3.ui.profile.view.ProfileFragment
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModel
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModelFactory
import com.irjarqui.unitracknetv3.ui.telnet.view.TelnetConsoleFragment
import com.irjarqui.unitracknetv3.ui.topology.viewmodel.TopologyViewModel
import com.irjarqui.unitracknetv3.utils.RetrofitHelper

class TopologyStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopologyStatusBinding
    private lateinit var viewModel: TopologyViewModel
    private lateinit var alarmsVM: AlarmsViewModel
    private lateinit var topologyVM: TopologyViewModel
    private lateinit var userVM:     UserViewModel
    private lateinit var alarmsViewModel: AlarmsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityTopologyStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appl = application as UniTrackNetApp
        val factory = AlarmsViewModelFactory(appl.bgpRepository, appl.ospfRepository)
        alarmsViewModel = ViewModelProvider(this, factory)[AlarmsViewModel::class.java]

        alarmsViewModel.filteredAlarms.observe(this) { alarms ->
            val activeCount = alarms.count { it.status == AlarmStatus.ERROR }
            showAlarmBadge(activeCount)
        }

        val profileVM: UserViewModel by viewModels {
            UserViewModelFactory(UserInfoRepository(RetrofitHelper.getUserInfoService()))
        }

        topologyVM = ViewModelProvider(this)[TopologyViewModel::class.java]
        topologyVM.ensureDataLoaded()

        val app = application as UniTrackNetApp
        val alarmsFactory = AlarmsViewModelFactory(app.bgpRepository, app.ospfRepository)
        alarmsVM = ViewModelProvider(this, alarmsFactory)[AlarmsViewModel::class.java]
        alarmsVM.fetchData()

        initUserProfile()

        setupBottomNav(savedInstanceState)
    }

    private fun initUserProfile() {
        val prefs     = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val username  = prefs.getString("username", "") ?: ""

        if (username.isBlank()) {
            Toast.makeText(this,
                getString(R.string.usuario_no_encontrado_en_preferencias), Toast.LENGTH_SHORT).show()
            return
        }

        val repo     = UserInfoRepository(RetrofitHelper.getUserInfoService())
        val factory  = UserViewModelFactory(repo)
        userVM       = ViewModelProvider(this, factory)[UserViewModel::class.java]

        userVM.refresh(username)
    }

    private fun setupBottomNav(savedInstanceState: Bundle?) {
        val nav: BottomNavigationView = binding.bottomNav

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.statusFragmentContainer, TopologyFragment())
                .commit()
        }

        nav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_topology -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.statusFragmentContainer, TopologyFragment())
                        .commit()
                    true
                }
                R.id.nav_alarms -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.statusFragmentContainer, AlarmsFragment())
                        .commit()
                    true
                }
                R.id.navigation_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.statusFragmentContainer, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    fun showTelnetOverlay(args: Bundle) {
        val frag = TelnetConsoleFragment().apply { arguments = args }

        supportFragmentManager.beginTransaction()
            .replace(R.id.telnet_container, frag)
            .commit()

        binding.telnetContainer.visibility = View.VISIBLE
    }

    fun hideTelnetOverlay() {
        binding.telnetContainer.visibility = View.GONE
        supportFragmentManager.popBackStack()
    }

    private fun showAlarmBadge(count: Int) {
        val navView = binding.bottomNav
        val badge = navView.getOrCreateBadge(R.id.nav_alarms)
        badge.isVisible = count > 0
        badge.number = count
    }

}
