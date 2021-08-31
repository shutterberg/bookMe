package com.aqua.bookmeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.aqua.bookmeapp.Adapter.BookAdapter;
import com.aqua.bookmeapp.Model.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FindBookActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private DatabaseReference userRef;
    private List<Book> bookList;
    private BookAdapter bookAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_book);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        bookList = new ArrayList<>();

        bookAdapter = new BookAdapter(FindBookActivity.this, bookList);

        recyclerView.setAdapter(bookAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("books").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String type = snapshot.child("books").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }


        });

    }

}
