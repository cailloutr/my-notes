package com.example.mynotes.ui.bottomsheet.colors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.example.mynotes.databinding.ColorsBottomSheetBinding
import com.example.mynotes.ui.bottomsheet.BaseBottomSheet
import com.google.android.material.chip.Chip

class ColorsBottomSheet(
    private val backgroundColor: Int?,
    val onClickListener: (cor: Int) -> Unit,
    var binding: ColorsBottomSheetBinding
) : BaseBottomSheet(backgroundColor, binding.root) {

    val TAG = "ColorsBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBackgroundColor()

        val chipGroup = binding.chipGroupColors

        Colors.listOfColorsChip.forEach { chipItem ->
            chipGroup.addView(chipItem.toChip(requireContext(), chipGroup))
        }

        chipGroup.children.forEach { child ->
            val chip = child.findViewById<Chip>(child.id)

            val chipDefaultColor = chip.chipBackgroundColor?.defaultColor

            if (chipDefaultColor == backgroundColor) {
                chipGroup.check(child.id)
            }

            chip.setOnClickListener {
                chipDefaultColor?.let { color ->
                    onClickListener(color)
                    setBottomSheetBackgroundColor(color)
                }
            }
        }

    }

    private fun setBottomSheetBackgroundColor(color: Int) {
        binding.root.setBackgroundColor(color)
    }

}