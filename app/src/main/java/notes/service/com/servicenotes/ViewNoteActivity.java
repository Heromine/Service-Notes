package notes.service.com.servicenotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.fabtransitionactivity.SheetLayout;

import java.text.DateFormat;

import notes.service.com.servicenotes.data.Note;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectView;

/**
 * Actividad para visualizar una nota. Adicionalmente, se puede editar la nota en otra actividad.
 *
 * @author Daniel Pedraza Arcega
 */
public class ViewNoteActivity extends RoboActionBarActivity implements SheetLayout.OnFabAnimationEndListener {

    private static final int EDIT_NOTE_RESULT_CODE = 8;
    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final String EXTRA_UPDATED_NOTE = "EXTRA_UPDATED_NOTE";
    private static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    @InjectView(R.id.scroll_view)
    private ScrollView scrollView;
    // @InjectView(R.id.edit_note_button)     private com.shamanland.fab.FloatingActionButton editNoteButton;
    @InjectView(R.id.note_title)
    private TextView noteTitleText;
    //@InjectView(R.id.note_book)
    //private TextView noteBookText;
    @InjectView(R.id.note_content)
    private TextView noteContentText;
    @InjectView(R.id.note_content2)
    private TextView noteContentText2;
    @InjectView(R.id.note_emails)
    private TextView noteEmails;
    @InjectView(R.id.note_phone)
    private TextView notePhone;
    @InjectView(R.id.note_created_at_date)
    private TextView noteCreatedAtDateText;
    @InjectView(R.id.note_updated_at_date)
    private TextView noteUpdatedAtDateText;
    private Note note;
    private SheetLayout mSheetLayout;
    private FloatingActionButton fab;
    private static final int REQUEST_CODE = 8;

    /**
     * Construye el Intent para llamar a esta actividad.
     *
     * @param context el contexto que la llama.
     * @param note    la nota a visualizar.
     * @return un Intent.
     */
    public static Intent buildIntent(Context context, Note note) {
        Intent intent = new Intent(context, ViewNoteActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    /**
     * Recupera la nota actualizada en la actividad de edición de notas.
     *
     * @param intent el Intent que vine en onActivityResult
     * @return la nota actualizada
     */
    public static Note getExtraUpdatedNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_UPDATED_NOTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ServiceUtils.setSavedAnimations(this);
        ServiceUtils.setSavedLanguage(this);
        Window window = getWindow();
        ServiceUtils.setSavedTheme(this, window);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        // Inicializa los componentes //////////////////////////////////////////////////////////////
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            setSupportActionBar(toolbar);
            ServiceUtils.setSavedAnimations(ViewNoteActivity.this);
        }
        //Get status bar color from the utils activity and set it

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Muestra la flecha hacia atrás
        //scrollView.setOnTouchListener(new ShowHideOnScroll(editNoteButton, getSupportActionBar())); // Esconde o muesta el FAB y la Action Bar
     /*   editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ir a la actividad de edición de notas
                startActivityForResult(EditNoteActivity.buildIntent(ViewNoteActivity.this, note), EDIT_NOTE_RESULT_CODE);
            }
        });

       */
        mSheetLayout = (SheetLayout) findViewById(R.id.bottom_sheet);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mSheetLayout.expandFab();
            }

        });
        mSheetLayout.setFab(fab);
        mSheetLayout.setFabAnimationEndListener(this);
        note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE); // Recuperar la nota del Intent
        // Mostrar la información de la nota en el layout
        noteTitleText.setText(note.getTitle());
        // noteBookText.setText(note.getBook());
        noteContentText.setText(note.getContent());
        noteContentText2.setText(note.getContent2());
        noteEmails.setText(note.getEmails());
        notePhone.setText(note.getPhone());
        noteCreatedAtDateText.setText(DATETIME_FORMAT.format(note.getCreatedAt()));
        noteUpdatedAtDateText.setText(DATETIME_FORMAT.format(note.getUpdatedAt()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // Cerrar esta actividad
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_NOTE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                // La nota fue editada correctamente y debe finalizar esta actividad con un resultado
                Intent resultIntent = new Intent();
                Note note = EditNoteActivity.getExtraNote(data);
                resultIntent.putExtra(EXTRA_UPDATED_NOTE, note);
                setResult(RESULT_OK, resultIntent);
                ServiceUtils.setSavedAnimations(this);
                finish();
            } else if (resultCode == RESULT_CANCELED)
                ServiceUtils.setSavedAnimations(this);
            onBackPressed();
        }

        if (requestCode == REQUEST_CODE) {
            mSheetLayout.contractFab();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        // No se edito la nota
        setResult(RESULT_CANCELED, new Intent());
        finish();
        ServiceUtils.setSavedAnimations(this);

    }


    @Override
    public void onFabAnimationEnd() {
        //same as +  EDIT_NOTE_RESULT_CODE so it also save the note
        //startActivityForResult(EditNoteActivity.buildIntent(ViewNoteActivity.this, note), EDIT_NOTE_RESULT_CODE);
        startActivityForResult(EditNoteActivity.buildIntent(ViewNoteActivity.this, note), REQUEST_CODE);
    }
}