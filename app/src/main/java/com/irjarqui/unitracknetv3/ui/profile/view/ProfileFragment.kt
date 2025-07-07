package com.irjarqui.unitracknetv3.ui.profile.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.irjarqui.unitracknetv3.R
import com.irjarqui.unitracknetv3.data.remote.repository.UserInfoRepository
import com.irjarqui.unitracknetv3.databinding.FragmentProfileBinding
import com.irjarqui.unitracknetv3.ui.alarms.AlarmPollingWorker
import com.irjarqui.unitracknetv3.ui.alarms.scheduleAlarmPolling
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModel
import com.irjarqui.unitracknetv3.ui.profile.viewmodel.UserViewModelFactory
import com.irjarqui.unitracknetv3.utils.RetrofitHelper
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(UserInfoRepository(RetrofitHelper.getUserInfoService()))
    }

    private val prefs by lazy { requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    private val avatarFile by lazy { File(requireContext().filesDir, getString(R.string.avatar_jpg)) }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.imgProfile.setImageURI(it)
            saveImageToInternalStorage(it)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImageLauncher.launch(getString(R.string.image))
        } else {
            Toast.makeText(requireContext(),
                getString(R.string.permiso_denegado_para_acceder_a_im_genes), Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageFromGallery()
            else Toast.makeText(requireContext(),
                getString(R.string.permiso_denegado_no_se_puede_elegir_imagen), Toast.LENGTH_LONG).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.switchAlarmas.isChecked = loadPollingState()

        binding.switchAlarmas.setOnCheckedChangeListener { _, isChecked ->
            savePollingState(isChecked)
            if (isChecked) {
                requireContext().scheduleAlarmPolling()
            } else {
                WorkManager.getInstance(requireContext())
                    .cancelUniqueWork(AlarmPollingWorker.TAG)
            }
        }

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)
        if (username != null) {
            viewModel.refresh(username)
        }

        binding.imgProfile.setOnClickListener { checkPermissionAndPick() }
        binding.btnUpdatePhoto.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                android.Manifest.permission.READ_MEDIA_IMAGES
            } else {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }

            when {
                ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                    pickImageLauncher.launch(getString(R.string.image_))
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(requireContext(),
                        getString(R.string.se_necesita_permiso_para_acceder_a_tus_im_genes), Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        }

        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.txtFullname.text = user.fullname
            binding.txtEmail.text    = user.email
            binding.txtUsername.text = getString(R.string.usuario_perf, user.username)
            binding.txtRole.text     = getString(R.string.rol, user.role)

            val profileFile = File(requireContext().filesDir, "profile.jpg")
            if (profileFile.exists()) {
                Glide.with(this)
                    .load(profileFile)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding.imgProfile)
            } else {
                Glide.with(this)
                    .load(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(binding.imgProfile)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { it?.let { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }}

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefreshProfile.isRefreshing = refreshing
            binding.loadingOverlay.root.visibility =
                if (refreshing) View.VISIBLE else View.GONE
        }

        binding.switchAlarmas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) requireContext().scheduleAlarmPolling()
            else WorkManager.getInstance(requireContext())
                .cancelUniqueWork(AlarmPollingWorker.TAG)
        }

        val savedPath = prefs.getString(KEY_AVATAR_PATH, null)
        if (savedPath != null && File(savedPath).exists()) {
            Glide.with(this).load(File(savedPath)).into(binding.imgProfile)
        }
    }

    private fun checkPermissionAndPick() {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission)
                    == PackageManager.PERMISSION_GRANTED -> pickImageFromGallery()
            shouldShowRequestPermissionRationale(permission) ->
                Toast.makeText(requireContext(),
                    getString(R.string.se_necesita_permiso_para_elegir_una_imagen), Toast.LENGTH_LONG).show()
            else -> permissionLauncher.launch(permission)
        }
    }

    private fun pickImageFromGallery() {
        pickImageLauncher.launch(getString(R.string.image_pick))
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, getString(R.string.profile_jpg))
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            Toast.makeText(requireContext(),
                getString(R.string.imagen_actualizada), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(),
                getString(R.string.error_al_guardar_imagen), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val PREFS = "profile_prefs"
        private const val KEY_AVATAR_PATH = "avatar_path"
    }

    private fun savePollingState(enabled: Boolean) {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("polling_enabled", enabled).apply()
    }

    private fun loadPollingState(): Boolean {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("polling_enabled", false)
    }
}
