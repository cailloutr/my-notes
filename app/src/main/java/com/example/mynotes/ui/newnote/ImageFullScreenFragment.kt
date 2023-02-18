package com.example.mynotes.ui.newnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.databinding.FragmentImageFullScreenBinding
import com.example.mynotes.ui.extensions.loadImage
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.NoteItemAnimationUtil

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

//        WindowUtil.resetWindow(requireActivity() as AppCompatActivity)
//        WindowUtil.setNoLimitsWindow(requireActivity() as AppCompatActivity)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        implementsStatusBarInsets()
        setThemeColor()

        binding.fragmentImageFullScreenImage.loadImage(args.imageUrl)

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

    private fun implementsStatusBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.


            view.updatePadding(
                top = insets.top,
            )

            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
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
        AppBarColorUtil.resetSystemBarColor(requireActivity() as AppCompatActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}