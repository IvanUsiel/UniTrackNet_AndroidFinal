package com.irjarqui.unitracknetv3.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.application.UniTrackNetApp
import com.irjarqui.unitracknetv3.data.remote.model.auth.AddIpRequestDto
import com.irjarqui.unitracknetv3.data.remote.model.auth.LoginResponseDto
import com.irjarqui.unitracknetv3.databinding.ActivityAddIpBinding
import kotlinx.coroutines.*

class AddIpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIpBinding
    private val loginRepository by lazy { (application as UniTrackNetApp).loginRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddIpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = intent.getStringExtra(getString(R.string.username)) ?: return finish()
        val pass = intent.getStringExtra(getString(R.string.password)) ?: return finish()

        binding.etUsername.setText(user)
        binding.etPassword.setText(pass)

        binding.btnSendIp.setOnClickListener {
            sendAddIpRequest(user, pass)
        }
    }

    private fun sendAddIpRequest(user: String, pass: String) {
        val loading = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.solicitando_acceso))
            .setMessage(getString(R.string.por_favor_espera))
            .setCancelable(false)
            .show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = loginRepository.addIp(
                    AddIpRequestDto(user, pass, getString(R.string.acceso_vpn_m_vil))
                )

                val body = if (response.isSuccessful) {
                    response.body()
                } else {
                    val errorJson = response.errorBody()?.string()
                    try {
                        Gson().fromJson(errorJson, LoginResponseDto::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    loading.dismiss()

                    if (body != null) {
                        when (body.status) {
                            getString(R.string.success) -> showDialog(
                                getString(R.string.ip_agregada),
                                body.message
                            )

                            getString(R.string.error) -> {
                                val msg = when (body.error_type) {
                                    getString(R.string.invalid_credentials) -> getString(R.string.usuario_o_contrase_a_incorrectos)
                                    getString(R.string.unauthorized_ip) -> getString(R.string.no_tienes_permiso_desde_esta_red)
                                    getString(R.string.no_ise_available) -> getString(R.string.ning_n_servidor_ise_disponible)
                                    getString(R.string.missing_fields) -> getString(R.string.faltan_campos_obligatorios)
                                    getString(R.string.ip_missing) -> getString(R.string.no_se_pudo_detectar_tu_ip)
                                    getString(R.string.invalid_request) -> getString(R.string.la_solicitud_no_es_v_lida)
                                    else -> body.message
                                }
                                showDialog(getString(R.string.Error), msg)
                            }

                            else -> showDialog(getString(R.string.atenci_n), body.message)
                        }
                    } else {
                        showDialog(
                            getString(R.string.Error),
                            getString(R.string.no_se_pudo_procesar_la_respuesta_del_servidor)
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loading.dismiss()
                    showDialog(
                        getString(R.string.error_de_red),
                        getString(R.string.no_se_pudo_conectar_con_el_servidor)
                    )
                }
            }
        }
    }

    private fun showDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.aceptar)) { dialog, _ -> dialog.dismiss(); finish() }
            .show()
    }
}
