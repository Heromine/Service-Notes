package notes.service.com.servicenotes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.github.fabtransitionactivity.SheetLayout;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.lapism.searchview.view.SearchView;

import java.util.ArrayList;

import javax.inject.Inject;

import notes.service.com.servicenotes.data.Note;
import notes.service.com.servicenotes.data.dao.NoteDAO;
import notes.service.com.servicenotes.widget.NotesAdapter;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends RoboActionBarActivity
        implements NavigationView.OnNavigationItemSelectedListener, SheetLayout.OnFabAnimationEndListener {
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
    public static final int feedback = 1;
    private SheetLayout mSheetLayout;
    private FloatingActionButton fab;
    private static final int REQUEST_CODE = 4;
    Context context;
    private SearchView mSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServiceUtils.setSavedAnimations(this);
        ServiceUtils.setSavedLanguage(this);
        Window window = getWindow();
        ServiceUtils.setSavedTheme(this, window);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // set app version and type on the nav drawer
        NavigationView navigationView2 = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView2.inflateHeaderView(R.layout.nav_header_main);
        TextView versionname = (TextView) header.findViewById(R.id.version_number);
        // View inflatedView = getLayoutInflater().inflate(R.layout.nav_header_main, null);
        String versionName = BuildConfig.VERSION_NAME;
        String wordVersion = getString(R.string.version_format);
        versionname.setText(wordVersion + " " + versionName);

        AppUpdater appUpdater = new AppUpdater(this);
        if (BuildConfig.TYPE.equals("beta") || BuildConfig.TYPE.equals("debug")) {
            appUpdater.setGitHubUserAndRepo("Heromine", "Service-Notes");
            appUpdater.setUpdateFrom(UpdateFrom.GITHUB);
            appUpdater.setDisplay(Display.NOTIFICATION);
            appUpdater.setDisplay(Display.DIALOG);
            appUpdater.showAppUpdated(false);
        } else if (BuildConfig.TYPE.equals("release")) {
            appUpdater.setUpdateFrom(UpdateFrom.GOOGLE_PLAY);
            appUpdater.setDisplay(Display.NOTIFICATION);
            appUpdater.setDisplay(Display.DIALOG);
            appUpdater.showAppUpdated(false);
        }
        appUpdater.start();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        //To make the email sending working
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Set App version to the nav drawer
        mSheetLayout = (SheetLayout) findViewById(R.id.bottom_sheet);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSheetLayout.expandFab();
            }

        });
        mSheetLayout.setFab(fab);
        mSheetLayout.setFabAnimationEndListener(this);
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

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mSearchView.setStyle(0);
        mSearchView.setVersion(1);
        mSearchView.setTheme(0);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                listAdapter.getFilter().filter(query);
                //filter the adapter dataset
                updateView();
                listAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listAdapter.getFilter().filter(newText);
                updateView();
                listAdapter.notifyDataSetChanged();
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new SearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
                listAdapter.getFilter().filter("");
                listAdapter.notifyDataSetChanged();
                updateView();
                listAdapter.notifyDataSetChanged();
            }
        });

        SharedPreferences settings = getSharedPreferences("prefs", 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        boolean introview = settings.getBoolean("introview", false);
        if (firstRun) {  // here run your first-time instructions, for example :
            Intent intent = new Intent(this, MyIntro.class);
            startActivity(intent);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", false);
            editor.putBoolean("introview", true);
            editor.commit();

        }
        if (introview) {
            showCardViewIntro(fab);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("introview", false);
            editor.commit();

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void showCardViewIntro(View view) {
        new MaterialShowcaseView.Builder(this)
                .setDismissText(getResources().getString(R.string.gotit))
                .setDelay(1000) // optional but starting animations immediately in onCreate can make them choppy
                .setContentText(getResources().getString(R.string.introview))
                .setTarget(fab)
                .singleUse("intro_fab_button")
                .show();
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
                //Snackbar.make(this.findViewById(R.id.fab), getString(R.string.firstpopup), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                startActivity(new Intent("android.intent.action.DonateActivity"));
                return true;

            case R.id.action_search:
                mSearchView.show(true);
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
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, Info.class);
            startActivity(intent);
            return true;

        }/* else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, DisplayFileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_manage) {
        } */ else if (id == R.id.nav_feed) {
            showDialog(feedback);

        } else if (id == R.id.nav_share) {
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

        if (requestCode == REQUEST_CODE) {
            mSheetLayout.contractFab();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case feedback:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialoginputmessage, null);
                dialogBuilder.setView(dialogView);
                final EditText message = (EditText) dialogView.findViewById(R.id.name);

                dialogBuilder.setTitle("Feedback");
                dialogBuilder.setIcon(R.drawable.ic_tag_faces_black_48dp);
                dialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //nothing to show
                                if (findViewById(R.id.name) != null) {
                                    message.getText().clear();
                                }
                            }
                        }
                );
                dialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (message.getText().toString().trim().length() > 0) {
                                    GMailSender sender = new GMailSender("servicenotesapp@gmail.com", "servicenotes11");
                                    try {
                                        sender.sendMail("Service Notes feedback",
                                                message.getText().toString(),
                                                "servicenotesapp@gmail.com", //da...
                                                "jonny99dj@gmail.com"); //a...
                                        Snackbar.make(MainActivity.this.findViewById(R.id.fab), getString(R.string.feedbacksent), Snackbar.LENGTH_LONG).setAction("Action", null).show();

                                    } catch (Exception e) {
                                        Log.e("SendMail failed", e.getMessage(), e);
                                        Snackbar.make(MainActivity.this.findViewById(R.id.fab), getString(R.string.feedbacknotsent), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                    }
                                } else if (message.getText().toString().trim().length() == 0) {
                                    Snackbar.make(MainActivity.this.findViewById(R.id.fab), getString(R.string.feedbackempty), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                }
                            }
                        }
                );
                  /* alternative way with more than 1 receiver and with attachment
                                       Mail m = new Mail("my email ", "my password");
                                        String[] toArr = {"recivers email1", "recivers email2"};
                                        m.setTo(toArr); //inviare l'email a...
                                        m.setFrom("email sent from"); //l'email viene da...
                                        m.setSubject("Service Notes Subject");
                                        m.setBody("Email body.");
                                        try {
                                         //   m.addAttachment("/sdcard/filelocation");
                                            if(m.send()) {
                                                Toast.makeText(MainActivity.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                                            }
                                        } catch(Exception e) {
                                            Toast.makeText(MainActivity.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                                            Log.e("MailApp", "Could not send email", e);
                                        }
                                        */
                AlertDialog b = dialogBuilder.create();
                b.show();
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
        //ImageView imgView = (ImageView) findViewById(R.id.emptyimg);
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
            Snackbar.make(this.findViewById(R.id.fab), "Nota eliminata con successo", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
        // Y luego de la vista (no al mismo tiempo porque pierdo las posiciones que hay que borrar)
        for (NotesAdapter.NoteViewWrapper noteToRemove : toRemoveList)
            notesData.remove(noteToRemove);
        updateView();
        listAdapter.notifyDataSetChanged();
    }

    private void updateNote(Intent data) {
        Note updatedNote = ViewNoteActivity.getExtraUpdatedNote(data);
        noteDAO.update(updatedNote);
        for (NotesAdapter.NoteViewWrapper noteViewWrapper : notesData) {
            // Buscar la nota vieja para actulizarla en la vista
            if (noteViewWrapper.getNote().getId().equals(updatedNote.getId())) {
                noteViewWrapper.getNote().setTitle(updatedNote.getTitle());
                noteViewWrapper.getNote().setContent(updatedNote.getContent());
                noteViewWrapper.getNote().setContent2(updatedNote.getContent2());
                noteViewWrapper.getNote().setEmails(updatedNote.getEmails());
                noteViewWrapper.getNote().setPhone(updatedNote.getPhone());
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


    @Override
    public void onFabAnimationEnd() {
        //same as  NEW_NOTE_RESULT_CODE so it also save the note
        startActivityForResult(EditNoteActivity.buildIntent(MainActivity.this), REQUEST_CODE);
    }

}