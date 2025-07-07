package com.irjarqui.unitracknetv3.utils

import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseFragment(@LayoutRes layoutId: Int) : Fragment(layoutId) {

    private val overlay: View by lazy {
        requireView().findViewById<View>(com.irjarqui.unitracknetv3.R.id.loadingOverlay)
    }

    open fun showLoading(show: Boolean) {
        val target = if (show) 1f else 0f
        overlay.animate()
            .alpha(target)
            .setDuration(250)
            .withStartAction { if (show) overlay.visibility = View.VISIBLE }
            .withEndAction { if (!show) overlay.visibility = View.GONE }
            .start()
    }

    private var lastActionTs = 0L
    private val debounceMillis = 1_500L

    protected fun safeLaunch(
        showLoader: Boolean = true,
        block: suspend () -> Unit
    ): Job = viewLifecycleOwner.lifecycleScope.launch {
        if (System.currentTimeMillis() - lastActionTs < debounceMillis) return@launch
        lastActionTs = System.currentTimeMillis()

        try {
            if (showLoader) showLoading(true)
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (showLoader) showLoading(false)
        }
    }

    protected fun safeLaunch(
        showLoader: Boolean = true,
        onError: (String) -> Unit = {},
        onFinally: () -> Unit = {},
        block: suspend () -> Unit
    ): Job = viewLifecycleOwner.lifecycleScope.launch {
        if (System.currentTimeMillis() - lastActionTs < debounceMillis) return@launch
        lastActionTs = System.currentTimeMillis()

        try {
            if (showLoader) showLoading(true)
            block()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e.message ?: "Error inesperado")
        } finally {
            if (showLoader) showLoading(false)
            onFinally()
        }
    }
}
