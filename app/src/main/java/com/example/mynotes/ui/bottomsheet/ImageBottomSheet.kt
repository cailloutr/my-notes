package com.example.mynotes.ui.bottomsheet

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mynotes.databinding.ImageBottomSheetBinding

class ImageBottomSheet(
    backgroundColor: Int?,
    private val binding: ImageBottomSheetBinding,
    private val operation: (Operation) -> Unit
) : BaseBottomSheet(backgroundColor, binding.root) {

    val TAG = "ImageBottomSheet"

    enum class Operation {
        CAMERA, GALLERY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor()

        binding.optionMenuBottomSheetGallery.setOnClickListener {
            operation(Operation.GALLERY)
            dismiss()
        }

        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.menuBottomSheetCamera.isEnabled = true
            binding.menuBottomSheetCamera.setOnClickListener {
                operation(Operation.CAMERA)
                dismiss()
            }
        } else {
            binding.menuBottomSheetCamera.isEnabled = false
        }

    }
}