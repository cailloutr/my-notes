package com.example.mynotes.ui.bottomsheet.colors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.example.mynotes.databinding.ColorsOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class ColorsOptionBottomSheet(
    val onClickListener: (cor: Int) -> Unit,
) : BottomSheetDialogFragment() {

    private lateinit var binding: ColorsOptionsBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding = ColorsOptionsBottomSheetBinding.inflate(inflater, container, false)

//        binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black_ish))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val chipGroup = binding.chipGroupColors

        Colors.listOfColorsChip.forEach {
            chipGroup.addView(it.toChip(requireContext(), chipGroup))

            for (child in chipGroup.children) {
                val chip = child.findViewById<Chip>(child.id)

                chip.setOnClickListener {
                    val defaultColor = chip.chipBackgroundColor?.defaultColor

                    defaultColor?.let { color ->
                        onClickListener(color)
                        binding.root.setBackgroundColor(color)
                    }
                }
            }
        }

    }

    companion object {
        const val TAG = "ColorsOptionBottomSheet"
    }

}