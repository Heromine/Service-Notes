package notes.service.com.servicenotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import notes.service.com.servicenotes.data.Note;
import notes.service.com.servicenotes.data.dao.NoteDAO;
import notes.service.com.servicenotes.widget.NotesAdapter;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActionBarActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int NEW_NOTE_RESULT_CODE = 4;
    private static final int EDIT_NOTE_RESULT_CODE = 5;
    @InjectView(android.R.id.empty)
    private TextView emptyListTextView;
    @InjectView(android.R.id.list)
    private ListView listView;
    @InjectView(R.id.fab)
    private FloatingActionButton addNoteButton;
    @Inject
    private NoteDAO noteDAO;


    public static final String TAG = "MainActivity";
    private ArrayList<Integer> selectedPositions;
    private ArrayList<NotesAdapter.NoteViewWrapper> notesData;
    private NotesAdapter listAdapter;
    private ActionMode.Callback actionModeCallback;
    private ActionMode actionMode;

    public static final int firstpopup = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServiceUtils.setSavedAnimations(this);
        super.onCreate(savedInstanceState);
        ServiceUtils.setSavedTheme(this);
        ServiceUtils.setSavedLanguage(this);
        // ServiceUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set App version to the nav drawer
       /* TextView textViewversionName = (TextView) findViewById(R.id.appversion);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textViewversionName.setText(packageInfo.versionName);

        }
        catch (PackageManager.NameNotFoundException e) {

        }*/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Nota in salvataggio...", Snackbar.LENGTH_LONG)
                //      .setAction("Action", null).show();

                // Crear una nota nueva

                startActivityForResult(EditNoteActivity.buildIntent(MainActivity.this), NEW_NOTE_RESULT_CODE);
            }

        });
        selectedPositions = new ArrayList<>();
        setupNotesAdapter();
        setupActionModeCallback();
        setListOnItemClickListenersWhenNoActionMode();
        updateView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences settings = getSharedPreferences("prefs", 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {  // here run your first-time instructions, for example :
            Intent intent = new Intent(this, MyIntro.class);
            startActivity(intent);

            showDialog(firstpopup);

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // put your code here... to get sharedpreferences

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                ServiceUtils.setSavedAnimations(this);
                startActivity(new Intent("android.intent.action.SettingsActivity"));
                return true;

            case R.id.action_donate:
                Snackbar.make(this.findViewById(R.id.fab), getString(R.string.firstpopup), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //startActivity(new Intent("android.intent.action.DonateActivity"));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            Intent intent = new Intent(this, Backup.class);
            ServiceUtils.setSavedAnimations(this);
            startActivity(intent);
            return true;
            // Handle the camera action
        }/* else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, NotesBackupActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, DisplayFileActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.nav_manage) {

        } */ else if (id == R.id.nav_share) {
            Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=notes.service.com.servicenotes"));
            startActivity(i2);

        } else if (id == R.id.action_bug) {
            Intent intent = new Intent(this, Gitty.class);
            ServiceUtils.setSavedAnimations(this);
            startActivity(intent);
            return true;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) addNote(data);
        }
        if (requestCode == EDIT_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) updateNote(data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case firstpopup:
                return new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Primo avvio!")
                        .setMessage("Questo è il tuo primo avvio: vuoi configuare le impostazioni?")
                        .setNegativeButton("Non  ora", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //nothing to show
                                    }
                                }
                        )
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                                        ServiceUtils.setSavedAnimations(MainActivity.this);
                                        startActivity(i);
                                    }
                                }
                        )
                        .create();
        }
        return super.onCreateDialog(id);
    }


    /**
     * Crea la llamada al modo contextual.
     */
    private void setupActionModeCallback() {
        actionModeCallback = new ActionMode.Callback() {

            /** {@inheritDoc} */
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                setListOnItemClickListenersWhenActionMode();
                // inflar menu contextual
                mode.getMenuInflater().inflate(R.menu.context_note, menu);
                return true;
            }

            /** {@inheritDoc} */
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Nada
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    // borrar notas solo si hay notas a borrar; sino se acaba el modo contextual.
                    case R.id.action_delete:
                        if (!selectedPositions.isEmpty()) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage(getString(R.string.delete_notes_alert, selectedPositions.size()))
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            deleteNotes(selectedPositions);
                                            mode.finish();
                                        }
                                    })
                                    .show();
                        } else mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            /** {@inheritDoc} */
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Regresar al modo normal
                setListOnItemClickListenersWhenNoActionMode();
                resetSelectedListItems();
            }
        };
    }

    /**
     * Inicializa el adaptador de notas.
     */
    private void setupNotesAdapter() {
        notesData = new ArrayList<>();
        for (Note note : noteDAO.fetchAll()) {
            NotesAdapter.NoteViewWrapper noteViewWrapper = new NotesAdapter.NoteViewWrapper(note);
            notesData.add(noteViewWrapper);
        }
        listAdapter = new NotesAdapter(notesData);
        listView.setAdapter(listAdapter);
    }

    /**
     * Actualiza la vista de esta actividad cuando hay notas o no hay notas.
     */
    private void updateView() {
        if (notesData.isEmpty()) { // Mostrar mensaje
            listView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        } else { // Mostrar lista
            listView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Agrega una nota a lista y la fuente de datos.
     *
     * @param data los datos de la actividad de edición de notas.
     */
    private void addNote(Intent data) {
        Note note = EditNoteActivity.getExtraNote(data);
        noteDAO.insert(note);
        NotesAdapter.NoteViewWrapper noteViewWrapper = new NotesAdapter.NoteViewWrapper(note);
        notesData.add(noteViewWrapper);
        updateView();
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Borra notas de la lista y de la fuente de datos.
     *
     * @param selectedPositions las posiciones de las notas en la lista.
     */
    private void deleteNotes(ArrayList<Integer> selectedPositions) {
        ArrayList<NotesAdapter.NoteViewWrapper> toRemoveList = new ArrayList<>(selectedPositions.size());
        // Primero borra de la base de datos
        for (int position : selectedPositions) {
            NotesAdapter.NoteViewWrapper noteViewWrapper = notesData.get(position);
            toRemoveList.add(noteViewWrapper);
            noteDAO.delete(noteViewWrapper.getNote());
            Snackbar.make(this.findViewById(android.R.id.content), "Nota eliminata con successo", Snackbar.LENGTH_LONG).setAction("Action", null).show();

        }
        // Y luego de la vista (no al mismo tiempo porque pierdo las posiciones que hay que borrar)
        for (NotesAdapter.NoteViewWrapper noteToRemove : toRemoveList)
            notesData.remove(noteToRemove);
        updateView();
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Actualiza una nota en la lista y la fuente de datos.
     *
     * @param data los datos de la actividad de edición de notas.
     */
    private void updateNote(Intent data) {
        Note updatedNote = ViewNoteActivity.getExtraUpdatedNote(data);
        noteDAO.update(updatedNote);
        for (NotesAdapter.NoteViewWrapper noteViewWrapper : notesData) {
            // Buscar la nota vieja para actulizarla en la vista
            if (noteViewWrapper.getNote().getId().equals(updatedNote.getId())) {
                noteViewWrapper.getNote().setTitle(updatedNote.getTitle());
               // noteViewWrapper.getNote().setBook(updatedNote.getBook());
                noteViewWrapper.getNote().setContent(updatedNote.getContent());
                noteViewWrapper.getNote().setUpdatedAt(updatedNote.getUpdatedAt());

            }
        }
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Reinicia las notas seleccionadas a no seleccionadas y limpia la lista de seleccionados.
     */
    private void resetSelectedListItems() {
        for (NotesAdapter.NoteViewWrapper noteViewWrapper : notesData)
            noteViewWrapper.setSelected(false);
        selectedPositions.clear();
        listAdapter.notifyDataSetChanged();
    }

    /**
     * Inicializa las acciones de la lista al hacer click en sus items cuando NO esta activo el
     * modo contextual.
     */
    private void setListOnItemClickListenersWhenNoActionMode() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ver la nota al hacer click
                startActivityForResult(ViewNoteActivity.buildIntent(MainActivity.this, notesData.get(position).getNote()), EDIT_NOTE_RESULT_CODE);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Iniciar modo contextual para selección de items
                notesData.get(position).setSelected(true);
                listAdapter.notifyDataSetChanged();
                selectedPositions.add(position);
                actionMode = startSupportActionMode(actionModeCallback);
                actionMode.setTitle(String.valueOf(selectedPositions.size()));
                return true;
            }
        });
    }

    /**
     * Inicializa las acciones de la lista al hacer click en sus items cuando esta activo el menu
     * contextual.
     */
    private void setListOnItemClickListenersWhenActionMode() {
        listView.setOnItemLongClickListener(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Agregar items a la lista de seleccionados y cambiarles el fondo.
                // Si se deseleccionan todos los items, se acaba el modo contextual
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove((Object) position); // no quiero el índice sino el objeto
                    if (selectedPositions.isEmpty()) actionMode.finish();
                    else {
                        actionMode.setTitle(String.valueOf(selectedPositions.size()));
                        notesData.get(position).setSelected(false);
                        listAdapter.notifyDataSetChanged();
                    }
                } else {
                    notesData.get(position).setSelected(true);
                    listAdapter.notifyDataSetChanged();
                    selectedPositions.add(position);
                    actionMode.setTitle(String.valueOf(selectedPositions.size()));
                }
            }
        });
    }


}