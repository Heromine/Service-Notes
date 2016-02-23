package notes.service.com.servicenotes.config;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import notes.service.com.servicenotes.data.source.sqlite.NotesDatabaseHelper;


public class ConfigModule extends AbstractModule {

    private final Application context;

    public ConfigModule(Application context) {
        this.context = context;
    }

    /** Cablea las implementaciones. */
    @Override
    protected void configure() {
        bind(SQLiteOpenHelper.class)
                .annotatedWith(Names.named("NotesDbHelper"))
                .toInstance(new NotesDatabaseHelper(context));
    }
}