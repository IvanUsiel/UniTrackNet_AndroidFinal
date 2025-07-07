package com.irjarqui.unitracknetv3.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.application.UniTrackNetApp
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginResponseDto
import com.irjarqui.unitracknetv3.databinding.ActivityLoginBinding
import com.irjarqui.unitracknetv3.ui.alarms.AlarmPollingWorker
import com.irjarqui.unitracknetv3.ui.intro.AboutActivity
import com.irjarqui.unitracknetv3.utils.BiometricHelper
import com.irjarqui.unitracknetv3.utils.NotificationPermissionHelper
import com.irjarqui.unitracknetv3.utils.SecurePrefs
import kotlinx.coroutines.*
import java.io.IOException
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    companion object {
        var userIse: String = ""
        var providerSession: String = ""

        const val PREFS_NAME = "user_prefs"
        const val KEY_USERNAME = "username"
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginRepository by lazy { (application as UniTrackNetApp).loginRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.TvAccountAction.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.deseas_contactar_al_equipo))
                .setMessage(getString(R.string.se_abrir_tu_app_de_correo_con_un_mensaje_ya_preparado_para_solicitar_tus_credenciales_ise_a_sg_sord))
                .setPositiveButton(getString(R.string.continuar)) { _, _ ->
                    sendContactEmail()
                }
                .setNegativeButton(getString(R.string.cancelar), null)
                .show()

        }

        setupUI()
        checkRememberedCredentials()
        setupBiometricLogin()
        updateTvOrSign()
        NotificationPermissionHelper.requestPermissionIfNeeded(this)


        val hasSaved = SecurePrefs.isRememberMeEnabled(this)
        val canUse = BiometricHelper.isBiometricAvailable(this)
        if (hasSaved && canUse) {
            binding.biometricIcon.post { invokeBiometricFlow() }
        }
    }

    private fun scheduleAlarmPolling() {
        val request = PeriodicWorkRequestBuilder<AlarmPollingWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(AlarmPollingWorker.TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                AlarmPollingWorker.TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }


    private fun setupUI() {
        updateLoginState()
        binding.etUser.doOnTextChanged { _, _, _, _ ->
            updateLoginState()
            setupBiometricLogin()
        }
        binding.etPsw.doOnTextChanged { _, _, _, _ ->
            updateLoginState()
            setupBiometricLogin()
        }

        binding.btnLogin.setOnClickListener {
            val error = validateInputs()
            if (error != null) {
                showErrorDialog(getString(R.string.atencion_btn), error)
            } else {
                attemptLogin()
            }
        }
    }

    private fun updateLoginState() {
        val filled = binding.etUser.text.toString().isNotBlank() &&
                binding.etPsw.text.toString().isNotBlank()
        binding.btnLogin.setBackgroundColor(
            ContextCompat.getColor(this, if (filled) R.color.green_theme else R.color.black)
        )
        binding.btnLogin.setTextColor(
            ContextCompat.getColor(this, if (filled) R.color.black else R.color.white)
        )
    }

    private fun validateInputs(): String? {
        val user = binding.etUser.text.toString().trim()
        val pass = binding.etPsw.text.toString().trim()
        return when {
            user.isEmpty() && pass.isEmpty() ->
                getString(R.string.ingresa_usuario_y_contrase_a)

            user.isEmpty() ->
                getString(R.string.ingresa_usuario)

            pass.isEmpty() ->
                getString(R.string.ingresa_contrase_a)

            else -> null
        }
    }


    private fun checkRememberedCredentials() {
        if (SecurePrefs.isRememberMeEnabled(this)) {
            binding.etUser.setText(SecurePrefs.getUser(this))
            binding.etPsw.setText(SecurePrefs.getPassword(this))
            binding.rememberMeCheckbox.isChecked = true
            updateLoginState()
        }
    }

    private fun setupBiometricLogin() {
        val hasSaved = SecurePrefs.isRememberMeEnabled(this)
        val canUse = BiometricHelper.isBiometricAvailable(this)

        when {
            hasSaved && canUse -> {
                binding.biometricIcon.visibility = View.VISIBLE
                setBiometricIcon()
                binding.biometricIcon.isEnabled = true
                binding.biometricIcon.setOnClickListener { invokeBiometricFlow() }
            }

            !hasSaved && canUse -> {
                binding.biometricIcon.visibility = View.VISIBLE
                setBiometricIcon()
                val filled = binding.etUser.text.toString().isNotBlank() &&
                        binding.etPsw.text.toString().isNotBlank()
                binding.biometricIcon.isEnabled = filled

                if (filled) {
                    TooltipCompat.setTooltipText(
                        binding.biometricIcon,
                        getString(R.string.biometric_activate_prompt)
                    )
                    binding.biometricIcon.setOnClickListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle(getString(R.string.activar_biometr_a))
                            .setMessage(getString(R.string.quieres_guardar_tus_credenciales_y_usar_biometr_a_en_siguientes_accesos))
                            .setPositiveButton(getString(R.string.s)) { _, _ ->
                                val u = binding.etUser.text.toString().trim()
                                val p = binding.etPsw.text.toString().trim()
                                SecurePrefs.saveCredentials(this, u, p)
                                setupBiometricLogin()
                            }
                            .setNegativeButton(getString(R.string.ahora_no), null)
                            .show()
                    }
                } else {
                    binding.biometricIcon.setImageResource(R.drawable.image_disabled_biometric)
                    TooltipCompat.setTooltipText(
                        binding.biometricIcon,
                        getString(R.string.ingresa_usuario_y_contrase_a_primero_para_activar_biometr_a)
                    )
                    binding.biometricIcon.setOnClickListener {
                        MaterialAlertDialogBuilder(this)
                            .setTitle(getString(R.string.datos_incompletos))
                            .setMessage(getString(R.string.debes_ingresar_usuario_y_contrase_a_antes_de_activar_la_biometr_a))
                            .setPositiveButton(getString(R.string.entendido)) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }

                }
            }

            !canUse -> {
                binding.biometricIcon.visibility = View.VISIBLE
                binding.biometricIcon.isEnabled = false
                binding.biometricIcon.setImageResource(R.drawable.image_disabled_biometric)
                TooltipCompat.setTooltipText(
                    binding.biometricIcon,
                    getString(R.string.biometric_not_supported)
                )
            }

            else -> binding.biometricIcon.visibility = View.GONE
        }
    }

    private fun setBiometricIcon() {
        when (BiometricHelper.getBiometricType(this)) {
            BiometricHelper.BiometricType.FACE ->
                binding.biometricIcon.setImageResource(R.drawable.image_facelogin)

            BiometricHelper.BiometricType.FINGERPRINT ->
                binding.biometricIcon.setImageResource(R.drawable.image_fingerprint)

            else ->
                binding.biometricIcon.visibility = View.GONE
        }
    }

    private fun invokeBiometricFlow() {
        BiometricHelper.showBiometricPrompt(
            context = this,
            title = getString(R.string.autenticaci_n_biom_trica),
            subtitle = getString(R.string.usa_tu_huella_o_rostro_para_continuar),
            negativeButtonText = getString(R.string.cancelar_)
        ) { success ->
            if (success) {
                binding.etUser.setText(SecurePrefs.getUser(this))
                binding.etPsw.setText(SecurePrefs.getPassword(this))
                attemptLogin()
            } else {
                showErrorDialog(
                    getString(R.string.error_dialog),
                    getString(R.string.no_se_pudo_autenticar_con_biometr_a)
                )
            }
        }
    }

    private fun attemptLogin() {
        val rawInput = binding.etUser.text.toString().trim()
        val user = rawInput.substringBefore(getString(R.string.arroba))
        val psw = binding.etPsw.text.toString().trim()

        binding.etUser.setText(user)

        val loading = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.conectando_al_servidor_ise))
            .setMessage(getString(R.string.por_favor_espera_material))
            .setCancelable(false)
            .show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = loginRepository.login(LoginRequestDto(user, psw))
                val gson = Gson()
                val body = if (response.isSuccessful) {
                    response.body()
                } else {
                    response.errorBody()?.string()?.let {
                        try {
                            gson.fromJson(
                                it,
                                LoginResponseDto::class.java
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    loading.dismiss()
                    if (body?.status == getString(R.string.success_)) {
                        persistLoggedUser(user)

                        userIse = user
                        providerSession = psw

                        if (binding.rememberMeCheckbox.isChecked) {
                            SecurePrefs.saveCredentials(this@LoginActivity, user, psw)
                        } else {
                            SecurePrefs.clearCredentials(this@LoginActivity)
                        }
                        if (binding.rememberMeCheckbox.isChecked) {
                            SecurePrefs.saveCredentials(this@LoginActivity, user, psw)
                        } else {
                            SecurePrefs.clearCredentials(this@LoginActivity)
                        }
                        showSuccessDialog()
                    } else {
                        val msg = body?.let {
                            when (it.error_type) {
                                getString(R.string.unauthorized_ip_) -> {
                                    MaterialAlertDialogBuilder(this@LoginActivity)
                                        .setTitle(getString(R.string.ip_vpn_no_autorizada_))
                                        .setMessage(getString(R.string.tu_ip_vpn_no_est_permitida_quieres_realizar_la_solicitud_de_acceso_para_tu_ip_vpn))
                                        .setPositiveButton(getString(R.string.si)) { _, _ ->
                                            val intent = Intent(
                                                this@LoginActivity,
                                                AddIpActivity::class.java
                                            )
                                            intent.putExtra(getString(R.string.username_), user)
                                            intent.putExtra(getString(R.string.password_), psw)
                                            startActivity(intent)
                                        }
                                        .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                    return@withContext
                                }

                                getString(R.string.invalid_credentials_) -> getString(R.string.verifica_tu_usuario_y_contrase_a)
                                getString(R.string.no_ise_available_) -> getString(R.string.ning_n_servidor_ise_est_disponible)
                                getString(R.string.invalid_json) -> getString(R.string.el_formato_de_datos_es_inv_lido)
                                getString(R.string.missing_fields_) -> getString(R.string.debes_proporcionar_usuario_y_contrase_a)
                                else -> it.message
                            }
                        } ?: getString(R.string.no_se_pudo_procesar_la_respuesta_del_servidor_)
                        showErrorDialog(getString(R.string.error_), msg)
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    loading.dismiss()
                    showErrorDialog(
                        getString(R.string.error_de_red_),
                        getString(R.string.verifica_tu_conexi_n_o_vpn)
                    )
                }
            }
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.bienvenido))
            .setMessage(getString(R.string.acceso_concedido))
            .setPositiveButton(getString(R.string.continuar_)) { dialog, _ ->
                dialog.dismiss()
                val hasSaved = SecurePrefs.isRememberMeEnabled(this)
                val canUse = BiometricHelper.isBiometricAvailable(this)
                if (!hasSaved && canUse) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.activar_biometria))
                        .setMessage(getString(R.string.quieres_guardar_tus_credenciales_y_usar_biometria_en_siguientes_accesos))
                        .setPositiveButton(getString(R.string.si_)) { _, _ ->
                            SecurePrefs.saveCredentials(this, userIse, providerSession)
                            setupBiometricLogin()
                            navigateToAbout()
                        }
                        .setNegativeButton(getString(R.string.ahora_no_)) { _, _ ->
                            navigateToAbout()
                        }
                        .show()
                } else {
                    navigateToAbout()
                }
            }
            .show()
    }

    private fun showErrorDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.aceptar_)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateTvOrSign() {
        val hasSaved = SecurePrefs.isRememberMeEnabled(this)
        binding.TvOrSign.text =
            if (!hasSaved)
                getString(R.string.welcome)
            else
                getString(R.string.or_sign_in_with)
    }

    private fun navigateToAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
        finish()
    }

    private fun persistLoggedUser(username: String) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    private fun sendContactEmail() {

        val htmlBody = getString(R.string.html_de_correo_sg_sord).trimIndent()

        val plainText = getString(R.string.corre_sin_html_sg_sord).trimIndent()

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = getString(R.string.text_html)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.sg_sord_uninet_com_mx)))
            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.solicitud_de_credenciales_ise_unitrack_net)
            )
            putExtra(Intent.EXTRA_TEXT, plainText)
            putExtra(Intent.EXTRA_HTML_TEXT, htmlBody)
        }

        startActivity(Intent.createChooser(emailIntent, getString(R.string.enviar_correo)))
    }

}
