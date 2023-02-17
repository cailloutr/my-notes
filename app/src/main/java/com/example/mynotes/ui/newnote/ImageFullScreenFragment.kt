package com.example.mynotes.ui.newnote

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.databinding.FragmentImageFullScreenBinding
import com.example.mynotes.ui.extensions.loadImage
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.NoteItemAnimationUtil
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ImageFullScreenFragment"

class ImageFullScreenFragment
    : Fragment(),
    GestureDetector.OnGestureListener {

    private var _binding: FragmentImageFullScreenBinding? = null
    val binding get() = _binding!!

    private val args: ImageFullScreenFragmentArgs by navArgs()

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var FACTOR = 1.0f

    private lateinit var gestureDetector: GestureDetector

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

    // Given an action int, returns a string description
    fun actionToString(action: Int): String {
        return when (action) {
            MotionEvent.ACTION_DOWN -> "Down"
            MotionEvent.ACTION_MOVE -> "Move"
            MotionEvent.ACTION_POINTER_DOWN -> "Pointer Down"
            MotionEvent.ACTION_UP -> "Up"
            MotionEvent.ACTION_POINTER_UP -> "Pointer Up"
            MotionEvent.ACTION_OUTSIDE -> "Outside"
            MotionEvent.ACTION_CANCEL -> "Cancel"
            else -> ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        gestureDetector = GestureDetector(requireContext(), this)
//        WindowUtil.resetWindow(requireActivity() as AppCompatActivity)
//        WindowUtil.setNoLimitsWindow(requireActivity() as AppCompatActivity)

        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        implementsStatusBarInsets()
        setThemeColor()

        binding.fragmentImageFullScreenImage.loadImage(args.imageUrl)

        scaleGestureDetector = ScaleGestureDetector(
            requireContext(), ZoomListener()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hideAndShowSystemBars(binding.root)
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

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideAndShowSystemBars(targetView: View) {
        val window = requireActivity().window

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH

        // Add a listener to update the behavior of the toggle fullscreen button when
        // the system bars are hidden or revealed.
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            // You can hide the caption bar even when the other system bars are visible.
            // To account for this, explicitly check the visibility of navigationBars()
            // and statusBars() rather than checking the visibility of systemBars().
            if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())
                || windowInsets.isVisible(WindowInsetsCompat.Type.statusBars())
            ) {
                // Hide the status bar, the navigation bar and toolbar.
                targetView.setOnTouchListener { view, motionEvent ->
                    gestureDetector.onTouchEvent(motionEvent)
                    isSystemBaVisible = true
                    true
                }

            } else {
                // show the status bar, the navigation bar and toolbar.
                targetView.setOnTouchListener { view, motionEvent ->
                    gestureDetector.onTouchEvent(motionEvent)
                    isSystemBaVisible = false
                    true
                }
            }
            view.onApplyWindowInsets(windowInsets)
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

    inner class ZoomListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            FACTOR *= detector.scaleFactor
            FACTOR = max(0.1f, min(FACTOR, 10f))
            binding.fragmentImageFullScreenImage.scaleX = FACTOR
            binding.fragmentImageFullScreenImage.scaleY = FACTOR
            return true
        }
    }

    override fun onDown(p0: MotionEvent): Boolean {
        Log.i(TAG, "onDown: ")
        return false
    }

    override fun onShowPress(p0: MotionEvent) {
        Log.i(TAG, "onShowPress: ")
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        Log.i(TAG, "onSingleTapUp ")
        val window = requireActivity().window
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)

        if (isSystemBaVisible) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            (requireActivity() as AppCompatActivity).supportActionBar?.hide()
            Log.i(TAG, "hideAndShowSystemBars: hide")
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            (requireActivity() as AppCompatActivity).supportActionBar?.show()
            Log.i(TAG, "hideAndShowSystemBars: show")
        }
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.i(TAG, "onScroll: ")
        return false
    }

    override fun onLongPress(p0: MotionEvent) {
        Log.i(TAG, "onLongPress: ")
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.i(TAG, "onFling: ")
        return false
    }
}