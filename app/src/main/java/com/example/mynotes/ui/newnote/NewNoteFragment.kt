package com.example.mynotes.ui.newnote

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.R
import com.example.mynotes.databinding.ColorsBottomSheetBinding
import com.example.mynotes.databinding.FragmentNewNoteBinding
import com.example.mynotes.databinding.FragmentNewNoteOptionsBottomSheetBinding
import com.example.mynotes.databinding.ImageBottomSheetBinding
import com.example.mynotes.model.Note
import com.example.mynotes.ui.BaseFragment
import com.example.mynotes.ui.bottomsheet.BaseBottomSheet
import com.example.mynotes.ui.bottomsheet.ImageBottomSheet
import com.example.mynotes.ui.bottomsheet.NoteOptionModalBottomSheet
import com.example.mynotes.ui.bottomsheet.colors.ColorsBottomSheet
import com.example.mynotes.ui.enums.FragmentMode
import com.example.mynotes.ui.enums.Operation.CAMERA
import com.example.mynotes.ui.enums.Operation.GALLERY
import com.example.mynotes.ui.extensions.loadImage
import com.example.mynotes.ui.viewModel.NotesListViewModel
import com.example.mynotes.util.AppBarColorUtil
import com.example.mynotes.util.NoteItemAnimationUtil
import com.example.mynotes.util.ToastUtil
import com.example.mynotes.util.windowinsets.WindowUtil.Companion.setupEdgeToEdgeLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.IOException

//private const val TAG = "NewNoteFragment"

class NewNoteFragment : BaseFragment() {

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
                    setupNoteImage(uri)
                    viewModel.uri = uri

                    Log.d(TAG, "Selected URI: $uri")
                    Log.d(TAG, "Note: ${viewModel.uri}")
                } else {
                    Log.d(TAG, "No media selected")
                }
            }
    }

    private fun setupNoteImage(uri: Uri?) {
        binding.newNoteImageContainer.visibility = View.VISIBLE
        binding.newNoteImage.loadImage(uri)
        viewModel.updateViewModelNoteHasImage(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
//        (activity as AppCompatActivity).setSupportActionBar(binding.newNoteToolbar)
        setupAppBar(binding.newNoteToolbar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEdgeToEdgeLayout(
            window = requireActivity().window,
            toolbar = binding.newNoteToolbar,
            footer = binding.newNoteFooter
        )
        setupMenu()
        setupDeleteImageButton()
        loadNoteFromViewModel()
        setupBottomSheet()
    }

    private fun setupDeleteImageButton() {
        binding.newNoteImageDelete.setOnClickListener {
            viewModel.updateViewModelNoteHasImage(false)
        }
    }

    private fun setupBottomSheet() {
        binding.newNoteOptionsMenu.setOnClickListener {
            val bottomSheet = NoteOptionModalBottomSheet(
                backgroundColor = viewModel.note.value?.color,
                binding = FragmentNewNoteOptionsBottomSheetBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                ),
                listener = {
                    if (viewModel.noteIsTrash()) {
                        viewModel.deleteNote()
                        val message = resources.getQuantityString(
                            R.plurals.snackbar_message_notes_deleted,
                            1
                        )
                        Snackbar.make(
                            binding.root,
                            message,
                            Snackbar.LENGTH_LONG
                        ).show()

                        navigateToTrashFragment()
                    } else {
                        navigateToNoteListFragment(hasRemovedNote = true)
                    }
                }
            )
            openBottomSheet(bottomSheet, bottomSheet.TAG)
        }

        binding.newNoteOptionsColors.setOnClickListener {
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

        binding.newNoteOptionsPhoto.setOnClickListener {
            val bottomSheet = ImageBottomSheet(
                backgroundColor = viewModel.note.value?.color,
                binding = ImageBottomSheetBinding.inflate(
                    layoutInflater,
                    binding.root,
                    false
                )
            ) {
                when (it) {
                    GALLERY -> {
                        // Launch the photo picker and allow the user to choose only images.
                        pickMedia.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    CAMERA -> {
                        dispatchTakePictureIntent()
                    }
                }
            }

            openBottomSheet(bottomSheet, bottomSheet.TAG)
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            viewModel.note.value?.id?.let {
                viewModel.createTempFile(
                    requireContext(),
                    it
                )
            }
        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "Error creating file", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.android.fileprovider",
                it
            )
            takePictureActivityResultLauncher.launch(photoUri)
        }
    }

    private val takePictureActivityResultLauncher: ActivityResultLauncher<Uri> =
        registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) {
            if (it) {
                binding.newNoteImageContainer.visibility = View.VISIBLE
                binding.newNoteImage.loadImage(viewModel.currentPhotoPath)
                viewModel.updateViewModelNoteHasImage(true)

                Log.i(TAG, "PhotoPath: ${viewModel.currentPhotoPath}: ")
            }
        }

    private fun openBottomSheet(bottomSheet: BaseBottomSheet, tag: String) {
        bottomSheet.show(parentFragmentManager, tag)
    }

    private fun setThemeColors(color: Int?) {
        val colorId = color ?: ContextCompat.getColor(requireContext(), R.color.white)
        binding.root.setBackgroundColor(colorId)
        AppBarColorUtil.changeAppBarColor(activity as AppCompatActivity, colorId)
    }

/*    private fun setupAppBar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.newNoteToolbar.setupWithNavController(navController, appBarConfiguration)

        if (fragmentMode == FragmentMode.FRAGMENT_EDIT) {
            activity?.title = getString(R.string.app_bar_title_edit_note)
        }
    }*/

    private fun loadNoteFromViewModel() {
        viewModel.note.observe(viewLifecycleOwner) { note ->
            binding.newNoteTextInputEdittextTitle.setText(note?.title)
            binding.newNoteTextInputEdittextDescription.setText(note?.description)
            binding.newNoteDate.text = note?.modifiedDate
            setThemeColors(note?.color)

            if (note?.hasImage == true) {
                setupImage(note)
            }

            if (fragmentMode == FragmentMode.FRAGMENT_TRASH) {
                disableViews()
            }
        }

        viewModel.hasImage.observe(viewLifecycleOwner) {
            if (it) {
                binding.newNoteImageContainer.visibility = View.VISIBLE
            } else {
                binding.newNoteImageContainer.visibility = View.GONE
            }
        }
    }

    private fun setupImage(note: Note) {
        if (!note.imageUrl.isNullOrEmpty()) {
            binding.newNoteImage.loadImage(note.imageUrl)

            binding.newNoteImage.setOnClickListener {
                val navigatorExtras = FragmentNavigatorExtras(
                    binding.newNoteImage to "full_screen_image"
                )

                val color = note.color ?: R.color.white
                findNavController().navigate(
                    NewNoteFragmentDirections.actionNewNoteFragmentToImageFullScreenFragment2(
                        note.imageUrl.toString(),
                        color
                    ),
                    navigatorExtras
                )
            }
        }
    }

    private fun disableViews() {
        binding.apply {
            newNoteTextInputEdittextTitle.isEnabled = false
            newNoteTextInputEdittextDescription.isEnabled = false
            newNoteOptionsColors.isEnabled = false
            newNoteOptionsPhoto.isEnabled = false
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
                    navigateUp()
//                    navigateToNoteListFragment()
                }

                if (menuItem.itemId == R.id.fragment_new_note_trash_menu_restore) {
                    undoDeleteNote()
                    navigateToNoteListFragment()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveNewNote() {
        val title =
            binding.newNoteTextInputEdittextTitle.text.toString()
        val description =
            binding.newNoteTextInputEdittextDescription.text.toString()

        var bitmap: Bitmap? = null
        if (viewModel.hasImage.value!!) {
            bitmap = binding.newNoteImage.drawable.toBitmapOrNull(250, 250)
        }

        if (title.isEmpty() && description.isEmpty()) {
            ToastUtil.makeToast(
                context,
                getString(R.string.notes_list_fragment_toast_empty_note)
            )
        } else {
            viewModel.saveNewNote(
                title,
                description,
                bitmap,
                requireContext()
            )
        }
    }

    private fun undoDeleteNote() {
        viewModel.retrieveNoteFromTrash()
        viewModel.saveNote()
    }

    private fun navigateToNoteListFragment(hasRemovedNote: Boolean = false) {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToNotesListFragment(hasRemovedNote)
        )
    }

    private fun navigateToTrashFragment() {
        findNavController().navigate(
            NewNoteFragmentDirections.actionNewNoteFragmentToTrashFragment()
        )
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NewNoteFragment"
    }
}
