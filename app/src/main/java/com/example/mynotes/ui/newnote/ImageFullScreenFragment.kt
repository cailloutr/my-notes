package com.example.mynotes.ui.newnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.databinding.FragmentImageFullScreenBinding
import com.example.mynotes.ui.extensions.loadImage
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.NoteItemAnimationUtil
import com.example.mynotes.util.windowinsets.WindowUtil.Companion.implementsStatusBarInsets

//private const val TAG = "ImageFullScreenFragment"

class ImageFullScreenFragment : Fragment() {

    private var _binding: FragmentImageFullScreenBinding? = null
    val binding get() = _binding!!

    private val args: ImageFullScreenFragmentArgs by navArgs()

    private var isSystemBaVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInOutAnimation()
    }

    private fun setInOutAnimation() {
        val animation = NoteItemAnimationUtil.setMoveTransitionAnimation(requireContext())
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageFullScreenBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupEdgeToEdgeLayout()
        binding.fragmentImageFullScreenImage.loadImage(args.imageUrl)
        setupTapToHideOrShowSystemBars()
    }

    private fun setupTapToHideOrShowSystemBars() {
        binding.fragmentImageFullScreenImage.onSimpleTapListener = {
            val window = requireActivity().window
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)

            isSystemBaVisible = !isSystemBaVisible

            if (isSystemBaVisible) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                (requireActivity() as AppCompatActivity).supportActionBar?.hide()
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                (requireActivity() as AppCompatActivity).supportActionBar?.show()
            }
        }
    }

    private fun setupEdgeToEdgeLayout() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        implementsStatusBarInsets(binding.toolbar)
        setThemeColor()
    }

    private fun setThemeColor() {
        val colorId = args.color
        AppBarColorUtil.changeAppBarColor(activity as AppCompatActivity, colorId)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}