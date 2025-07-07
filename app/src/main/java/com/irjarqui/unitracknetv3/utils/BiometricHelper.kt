package com.irjarqui.unitracknetv3.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.pm.PackageManager
import android.util.Log

object BiometricHelper {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val result =
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)

        Log.d("BiometricHelper", "Biometric availability check result: $result")

        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun getBiometricType(context: Context): BiometricType {
        val pm = context.packageManager

        val hasFingerprint = pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
        val hasFace = pm.hasSystemFeature("android.hardware.biometrics.face")

        return when {
            hasFace -> BiometricType.FACE
            hasFingerprint -> BiometricType.FINGERPRINT
            else -> BiometricType.NONE
        }
    }

    fun showBiometricPrompt(
        context: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onAuthResult: (Boolean) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        val biometricPrompt = BiometricPrompt(
            context, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onAuthResult(false)
                }

                override fun onAuthenticationFailed() {
                    onAuthResult(false)
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    enum class BiometricType {
        FINGERPRINT,
        FACE,
        NONE
    }
}
