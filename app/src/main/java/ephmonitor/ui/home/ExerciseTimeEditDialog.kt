package com.example.ephmonitor.ui.home

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.example.ephmonitor.R
import com.example.ephmonitor.databinding.DialogExerciseTimeEditBinding
import com.example.ephmonitor.utils.ViewUtil


class ExerciseTimeEditDialog(
    val preContext: Context,
    private var content: String, //内容
    private val callback: (String) -> Unit//回调
) :
    Dialog(preContext, R.style.myDialog) {
    private lateinit var bind: DialogExerciseTimeEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DialogExerciseTimeEditBinding.inflate(layoutInflater)
        setContentView(bind.root)
        initView()
        bind.etDescription.setText(content)
        bind.tvCancel.setOnClickListener {
            dismiss()
        }
        bind.tvSubmit.setOnClickListener{
            content = bind.etDescription.text.toString()
            callback(content)
            dismiss()
            Toast.makeText(preContext, "设置成功", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initView() {
        window?.attributes = window?.attributes?.apply {
            height = WindowManager.LayoutParams.WRAP_CONTENT
            width = ViewUtil.getScreenWidth(context) - ViewUtil.dpToPx(context, 40f)
        }

    }
}