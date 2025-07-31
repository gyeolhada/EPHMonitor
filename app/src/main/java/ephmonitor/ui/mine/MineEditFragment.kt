package com.example.ephmonitor.ui.mine

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentMineEditBinding
import com.example.ephmonitor.ui.login.LoginViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MineEditFragment : Fragment() {
    private lateinit var bind: FragmentMineEditBinding
    private lateinit var viewModel: MineEditViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var pickMultipleMedia: ActivityResultLauncher<PickVisualMediaRequest>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentMineEditBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(MineEditViewModel::class.java)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        viewModel.getPerson(loginViewModel.getCurrentUserId()) {
            it?.let {
                bind.edtUserRealName.setText(it.pname)
                bind.edtHeight.setText(it.pheight)
                bind.edtWeight.setText(it.pweight)
                bind.edtBMI.setText(it.pBMI)
                bind.edtAge.setText(it.page)
                bind.tvSex.text = it.psex
                bind.tvBirth.text = it.pbirth
                loadAvatar(it.avatar)
            }
        }
        return bind.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickMultipleMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) {
                if (it.size > 1) {
                    Toast.makeText(requireContext(), "只能选择一张图片", Toast.LENGTH_SHORT).show()
                }
                //申请永久访问权限
                requireActivity().contentResolver.takePersistableUriPermission(
                    it[0], Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.changeAvatar(it[0].toString())
                loadAvatar(it[0].toString())
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isChangeSex = false
        var birthDate: String? = null

        bind.tvUsername.text = loginViewModel.getCurrentUserAccount()

        bind.ivExit.setOnClickListener {
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_mineEditFragment_to_navigation_mine)
        }

        bind.btnDelete.setOnClickListener {
            loginViewModel.logout()
            Navigation.findNavController(bind.root)
                .navigate(R.id.action_mineEditFragment_to_loginFragment)
        }

        bind.ivExchangeTime.setOnClickListener {
            clearFocus()
            //禁用0.5秒
            bind.ivExchangeTime.isEnabled = false
            bind.ivExchangeTime.postDelayed({ bind.ivExchangeTime.isEnabled = true }, 500)
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setTitleText("修改生日")
                    .build()
            datePicker.show(childFragmentManager, "datePicker")
            datePicker.addOnPositiveButtonClickListener {
                val date = Date(it)
                birthDate = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(date)
                bind.tvBirth.text = birthDate
            }
        }

        bind.ivExchange.setOnClickListener {
            clearFocus()
            isChangeSex = true
            bind.tvSex.text = if (bind.tvSex.text == "男") "女" else "男"
        }

        bind.tvSave.setOnClickListener {
            clearFocus()
            val name = bind.edtUserRealName.text.toString()
            if (name.isNotEmpty()) viewModel.changeName(name)
            val height = bind.edtHeight.text.toString()
            if (height.isNotEmpty()) viewModel.changeHeight(height)
            val weight = bind.edtWeight.text.toString()
            if (weight.isNotEmpty()) viewModel.changeWeight(weight)
            val bmi = bind.edtBMI.text.toString()
            if (bmi.isNotEmpty()) viewModel.changeBMI(bmi)
            val age = bind.edtAge.text.toString()
            if (age.isNotEmpty()) viewModel.changeAge(age)
            if (birthDate!=null) viewModel.changeBirth(birthDate!!)
            if (isChangeSex) viewModel.changeSex()
            viewModel.saveChanges()
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show()
        }
        bind.ivAvatar.setOnClickListener {
            clearFocus()
            //禁用0.5秒
            bind.ivAvatar.isEnabled = false
            bind.ivAvatar.postDelayed({ bind.ivAvatar.isEnabled = true }, 500)
            //调用系统相册
            pickMultipleMedia.launch(PickVisualMediaRequest())
        }
    }

    private fun clearFocus() {
        bind.edtUserRealName.clearFocus()
        bind.edtHeight.clearFocus()
        bind.edtWeight.clearFocus()
        bind.edtBMI.clearFocus()
        bind.edtAge.clearFocus()
    }

    private fun loadAvatar(uri: String?) {
        if (!uri.isNullOrEmpty()) {
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.brain)
                .into(bind.ivAvatar)
        }
    }
}