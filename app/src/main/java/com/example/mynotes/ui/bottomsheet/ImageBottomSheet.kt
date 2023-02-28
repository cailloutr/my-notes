package com.example.mynotes.ui.bottomsheet

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mynotes.databinding.ImageBottomSheetBinding
import com.example.mynotes.ui.enums.Operation
import com.example.mynotes.ui.enums.Operation.*

class ImageBottomSheet(
    backgroundColor: Int?,
    private val binding: ImageBottomSheetBinding,
    private val operation: (Operation) -> Unit
) : BaseBottomSheet(backgroundColor, binding.root) {

    val TAG = "ImageBottomSheet"

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
        setupGalleryOption()
        setupCameraOption()
    }

    private fun setupCameraOption() {
        if (requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.menuBottomSheetCamera.isEnabled = true
            binding.menuBottomSheetCamera.setOnClickListener {
                operation(CAMERA)
                dismiss()
            }
        } else {
            binding.menuBottomSheetCamera.isEnabled = false
        }
    }

    private fun setupGalleryOption() {
        binding.optionMenuBottomSheetGallery.setOnClickListener {
            operation(GALLERY)
            dismiss()
        }
    }
}