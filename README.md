# MyNotes App (ongoing project)

<img src="Media/Gifs/new_note.gif" style="width: 390px; height: 844px; display: flex;"/> <img src="Media/Gifs/new_note_image_camera.gif" style="width: 390px; height: 844px;"/> <img src="Media/Gifs/new_note_image_gallery.gif" style="width: 390px; height: 844px;"/> <img src="Media/Gifs/full_screen_image.gif" style="width: 390px; height: 844px;"/>



Funcionalidades:
- Capacidade de criar uma nota rapidamente na tela de listagem de notas e/ou expandir e continuar a criação da nota na tela de NovasNotas
- Editar notas clicando no item na Listagem de Notas
- Apagar uma nota e envia-la para a lixeira
- Restaurar, todos ou individualmente, os itens da lixeira
- Apagar definitivamente, todos ou individualmente, os itens da lixeira
- As notas são salvas localmente no dispositivos usando o Room
- Segurar uma nota para selecionar uma ou mais notas e executar ações como deletar ou restaurar
- Criar notas com imagens da camera ou da galeria e salvar no arquivos específicos do app

Conteúdo do App em Edge-To-Edge   
Shared Elements Transitions nas Imagens e no conteúdo das notas ao clicar para editar   
Animações personalizadas de transição entre fragments.

Tecnologias:
- ViewModel, LiveData, Room, Flow, Coroutines, Contratos de atividade do Jetpack
- Navigation Component, Single Activivity with Fragments, Data Store, RecyclerView
- Injeção de Dependência com o Koin
- BottomSheetDialogFragment
