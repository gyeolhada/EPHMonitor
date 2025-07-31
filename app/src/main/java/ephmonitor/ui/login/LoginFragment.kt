package com.example.ephmonitor.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.FragmentLoginBinding
import com.example.ephmonitor.room.entity.User

class LoginFragment : Fragment() {
    private lateinit var bind: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        bind = FragmentLoginBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.checkLogin { isLoggedIn ->
            if (isLoggedIn) {
                Navigation.findNavController(bind.root)
                    .navigate(R.id.action_loginFragment_to_navigation_main)
            }
        }

        bind.btnLogin.setOnClickListener {
            val username = bind.edtUsername.text.toString()
            val password = bind.edtPassword.text.toString()
            viewModel.login(username, password).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(bind.root)
                        .navigate(R.id.action_loginFragment_to_navigation_main)
                } else {
                    Toast.makeText(requireContext(), "登录失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bind.btnRegister.setOnClickListener {
            val username = bind.edtUsername.text.toString()
            val password = bind.edtPassword.text.toString()
            Log.d("LoginFragment", "User register in: ${username},${password}")
            val user = User(username = username, password = password)
            viewModel.register(user).observe(viewLifecycleOwner) { success ->
                if (success) {
                    // 注册成功，跳转到登录页面
                    viewModel.login(username,password)
                    Toast.makeText(requireContext(), "注册成功", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(bind.root)
                        .navigate(R.id.action_loginFragment_to_navigation_main)
                } else {
                    // 提示用户已存在
                    Toast.makeText(requireContext(), "用户已存在", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
