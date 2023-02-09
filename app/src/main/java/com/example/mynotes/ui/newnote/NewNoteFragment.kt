package com.example.mynotes.ui.newnote

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotes.R
import com.example.mynotes.databinding.ColorsBottomSheetBinding
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding
import com.example.mynotes.databinding.ImageBottomSheetBinding
import com.example.mynotes.ui.bottomsheet.BaseBottomSheet
import com.example.mynotes.ui.bottomsheet.ImageBottomSheet
import com.example.mynotes.ui.bottomsheet.NoteOptionModalBottomSheet
import com.example.mynotes.ui.bottomsheet.colors.ColorsBottomSheet
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.extensions.loadEndImage
import com.example.mynotes.ui.extensions.loadImage
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.NoteItemAnimationUtil
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.util.WindowUtil
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

//private const val TAG = "NewNoteFragment"

class NewNoteFragment : Fragment() {

    private var _binding: FragmentNewNoteBinding? = null
    val binding get() = _binding!!

    private val args: NewNoteFragmentArgs by navArgs()

    lateinit var fragmentMode: FragmentMode

    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>

/*    private val viewModel: NotesListViewModel by activityViewModels {
        NotesListViewModelFactory(
            activity?.application as MyNotesApplication
        )
    }*/

    private val viewModel: NotesListViewModel by activityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentMode = args.fragmentMode
        setInOutAnimation()
        setupPicturesSelector()
    }

    private fun setInOutAnimation() {
        if (fragmentMode == FragmentMode.FRAGMENT_EDIT || fragmentMode == FragmentMode.FRAGMENT_TRASH) {
            val animation = NoteItemAnimationUtil.setMoveTransitionAnimation(requireContext())
            sharedElementEnterTransition = animation
            sharedElementReturnTransition = animation
        } else {
            enterTransition = NoteItemAnimationUtil.setSlideBottomTopTransition(requireContext())
            exitTransition = NoteItemAnimationUtil.setSlideFromTopBottomTransition(requireContext())
        }
    }

    private fun setupPicturesSelector() {
        // Registers a photo picker activity launcher in single-select mode.
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
                    binding.fragmentNewNoteImageContainer.visibility = View.VISIBLE
                    binding.fragmentNewNoteImage.loadImage(uri)

                    viewModel.updateViewModelNoteHasImage(true)

                    Log.d(TAG, "Selected URI: $uri")
                    Log.d(TAG, "Note: ${viewModel.note.value}")
                } else {
                    Log.d(TAG, "No media selected")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        setupAppBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        WindowUtil.setNoLimitsWindow(requireActivity() as AppCompatActivity)
        setupMenu()

/*        if (fragmentMode == FragmentMode.FRAGMENT_NEW) {
            viewModel.createEmptyNote()
        }*/

        setupDeleteImageButton()
        loadNoteFromViewModel()
        setupBottomSheet()
    }

    private fun setupDeleteImageButton() {
        binding.fragmentNewNoteImageDelete.setOnClickListener {
            viewModel.updateViewModelNoteHasImage(false)
        }
    }

    private fun setupBottomSheet() {
        binding.fragmentNewNoteOptionsMenu.setOnClickListener {
            val bottomSheet = NoteOptionModalBottomSheet(
                backgroundColor = viewModel.note.value?.color,
                binding = FragmentNewNoteOptionsBottomSheetBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                ),
                listener = {
                    val snackbar: Snackbar?
                    if (viewModel.note.value?.isTrash == true) {
                        viewModel.deleteNote()

                        snackbar = Snackbar.make(
                            binding.root,
                            resources.getQuantityString(
                                R.plurals.snackbar_message_notes_deleted,
                                1
                            ),
                            Snackbar.LENGTH_SHORT
                        )

                        navigateToTrashFragment()
                    } else {
                        viewModel.moveNoteToTrash()
                        viewModel.saveNote()

                        snackbar = Snackbar.make(
                            binding.root,
                            getString(R.string.note_list_fragment_move_to_trash_snackbar),
                            Snackbar.LENGTH_SHORT
                        )

                        navigateToNoteListFragment()
                    }
                    snackbar.show()
                }
            )
            openBottomSheet(bottomSheet, bottomSheet.TAG)
        }

        binding.fragmentNewNoteOptionsColors.setOnClickListener {
            val bottomSheet = ColorsBottomSheet(
                backgroundColor = viewModel.note.value?.color, {
                    setThemeColors(it)
                    viewModel.setNoteColor(it)
                },
                binding = ColorsBottomSheetBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                )
            )
            openBottomSheet(bottomSheet, bottomSheet.TAG)
        }

        binding.fragmentNewNoteOptionsPhoto.setOnClickListener {
            val bottomSheet = ImageBottomSheet(
                backgroundColor = viewModel.note.value?.color,
                binding = ImageBottomSheetBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                )
            ) {
                if (it == ImageBottomSheet.Operation.GALLERY) {
                    // Launch the photo picker and allow the user to choose only images.
                    pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }

            openBottomSheet(bottomSheet, bottomSheet.TAG)
        }
    }

    private fun openBottomSheet(bottomSheet: BaseBottomSheet, tag: String) {
        bottomSheet.show(parentFragmentManager, tag)
    }

    private fun navigateToTrashFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToTrashFragment()
        )
    }

    private fun setThemeColors(color: Int?) {
        val colorId = color ?: ContextCompat.getColor(requireContext(), R.color.white)
        binding.root.setBackgroundColor(colorId)
        AppBarColorUtil.changeAppBarColor(activity as AppCompatActivity, colorId)
        /*  (activity as AppCompatActivity).apply {
            window.statusBarColor = colorId
            window.navigationBarColor = colorId
        }*/
    }

    private fun setupAppBar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        if (fragmentMode == FragmentMode.FRAGMENT_EDIT) {
            activity?.title = getString(R.string.app_bar_title_edit_note)
        }
    }

    private fun loadNoteFromViewModel() {
        viewModel.note.observe(viewLifecycleOwner) {
            binding.fragmentNewNoteTextInputEdittextTitle.setText(it?.title)
            binding.fragmentNewNoteTextInputEdittextDescription.setText(it?.description)
            binding.fragmentNewNoteDate.text = it?.modifiedDate
            setThemeColors(it?.color)

            if (it?.hasImage == true) {
                if (!it.imageUrl.isNullOrEmpty()) {
                    binding.fragmentNewNoteImage.loadEndImage(it.imageUrl, it.imageUrl!!)
                }
            }

            if (fragmentMode == FragmentMode.FRAGMENT_TRASH) {
                binding.apply {
                    fragmentNewNoteTextInputEdittextTitle.isEnabled = false
                    fragmentNewNoteTextInputEdittextDescription.isEnabled = false
                    fragmentNewNoteOptionsColors.isEnabled = false
                    fragmentNewNoteOptionsPhoto.isEnabled = false
                }
            }
        }

        viewModel.hasImage.observe(viewLifecycleOwner) {
            if (it) {
                binding.fragmentNewNoteImageContainer.visibility = View.VISIBLE
            } else {
                binding.fragmentNewNoteImageContainer.visibility = View.GONE
            }
        }
    }


    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (args.fragmentMode == FragmentMode.FRAGMENT_TRASH) {
                    menuInflater.inflate(R.menu.fragment_new_note_trash_menu, menu)
                } else {
                    menuInflater.inflate(R.menu.fragment_new_note_menu_save_note, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.fragment_new_note_menu_item_save_note) {
                    saveNewNote()
                }

                if (menuItem.itemId == R.id.fragment_new_note_trash_menu_restore) {
                    undoDeleteNote()
                    navigateToNoteListFragment()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun undoDeleteNote() {
        viewModel.retrieveNoteFromTrash()
        viewModel.saveNote()
    }

    private fun saveNewNote() {
        val title = binding.fragmentNewNoteTextInputEdittextTitle.text
        val description = binding.fragmentNewNoteTextInputEdittextDescription.text

        if (title.isNullOrEmpty() && description.isNullOrEmpty()) {
            ToastUtil.makeToast(
                context,
                getString(R.string.notes_list_fragment_toast_empty_note)
            )
        } else {
            val imagePath = if (viewModel.hasImage.value == true) {
                val imageView = binding.fragmentNewNoteImage
                val bitmap = imageView.drawable.toBitmapOrNull(250, 250)
                viewModel.saveImageInAppSpecificAlbumStorageDir(bitmap, requireContext())
            } else {
                viewModel.deleteImageInAppSpecificAlbumStorageDir(requireContext())
                ""
            }

            viewModel.updateViewModelNote(
                title = title.toString(),
                description = description.toString(),
                imagePath = imagePath,
                hasImage = viewModel.hasImage.value
            )
            viewModel.saveNote()
        }
        findNavController().navigateUp()
    }

    private fun navigateToNoteListFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NewNoteFragment"
    }
}
