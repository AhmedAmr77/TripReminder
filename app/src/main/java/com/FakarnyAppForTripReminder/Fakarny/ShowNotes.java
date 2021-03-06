package com.FakarnyAppForTripReminder.Fakarny;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.FakarnyAppForTripReminder.Fakarny.database.TripData;

public class ShowNotes extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ShowVoteAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notes);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerShow);
        layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        TripData tripData = (TripData) getIntent().getSerializableExtra("tripNotes");
        adapter = new ShowVoteAdapter(this, tripData.getNotes(),tripData.getState().equals("upcoming"));
        recyclerView.setAdapter(adapter);

    }
}