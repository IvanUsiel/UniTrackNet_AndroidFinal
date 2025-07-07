package com.irjarqui.unitracknetv3.ui.intro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.databinding.ActivityIntroBinding
import com.irjarqui.unitracknetv3.ui.auth.LoginActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        splashScreen.setOnExitAnimationListener { splashView ->
            splashView.iconView.animate()
                .translationY(-200f)
                .setDuration(300L)
                .withEndAction { splashView.remove() }
                .start()
        }

        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSign.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            finish()
        }

        binding.TvAccountAction.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.deseas_registrarte))
                .setMessage(getString(R.string.se_generar_una_solicitud_y_se_abrir_tu_app_de_correo_para_enviarla_al_equipo_de_gsop))
                .setPositiveButton(getString(R.string.continuar_l)) { _, _ ->
                    sendStyledHtmlEmail()
                }
                .setNegativeButton(getString(R.string.cancelar_l), null)
                .show()
        }
    }

    private fun sendStyledHtmlEmail() {
        val htmlBody = getString(R.string.corre_html_gsop).trimIndent()

        val plainText = getString(R.string.correo_gsop_texto).trimIndent()

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = getString(R.string.text_html_)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.gsop_uninet_com_mx)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.solicitud_de_acceso_unitrack_net))
            putExtra(Intent.EXTRA_TEXT, plainText)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlBody)
        }

        startActivity(Intent.createChooser(emailIntent, getString(R.string.enviar_correo_)))
    }
}
