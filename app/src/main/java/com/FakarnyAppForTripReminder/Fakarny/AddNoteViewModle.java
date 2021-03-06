package com.FakarnyAppForTripReminder.Fakarny;

import android.app.Application;

import androidx.lifecycle.ViewModel;

import com.FakarnyAppForTripReminder.Fakarny.database.NoteData;
import com.FakarnyAppForTripReminder.Fakarny.database.Repository;
import com.FakarnyAppForTripReminder.Fakarny.database.TripData;

import java.util.ArrayList;
import java.util.List;

public class AddNoteViewModle extends ViewModel {
    private List<NoteData> notes;
    private Repository repository;
    private TripData tripData;

    public void intiData(TripData tripData, Application application) {
        if (this.tripData == null) {
            this.tripData = tripData;
            this.notes = tripData.getNotes();
            repository = new Repository(application);
        }
    }

    public List<NoteData> getNotes() {
        if (notes == null) {
            notes = new ArrayList<>();
        }
        return notes;
    }

    public void addNote(String note) {
        NoteData noteData = new NoteData();
        noteData.setNote(note);
        noteData.setStatus(false);
        notes.add(noteData);
    }

    public void remove(int pos) {
        notes.remove(pos);
    }

    public String getNote(int pos) {
        return notes.get(pos).getNote();
    }

    public void save() {
        tripData.setNotes(notes);
        repository.update(tripData);
    }
}
