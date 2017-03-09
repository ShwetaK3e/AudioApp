package com.shwetak3e.audioapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shwetak3e.audioapp.supportClass.VokesAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VokesListActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;


    private ListView vokesList;
    private Button record;


    private List<String> allVokesURL=new ArrayList<>();
    private List<String> allVokesName=new ArrayList<>();

    private VokesAdapter vokesAdapter;

    private MediaPlayer mediaPlayer;
    private String fileName="";
    private int selectedPos=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vokes_list);

        record=(Button)findViewById(R.id.addAudio);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(VokesListActivity.this,RecordVoiceActivity.class);
                startActivity(intent);
            }
        });

        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("vokes");


        vokesList=(ListView)findViewById(R.id.vokesList);
        listVokes();
        vokesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               downloadURL(allVokesURL.get(position));
                selectedPos=position;
            }
        });



    }

    private void downloadURL(String s) {
        storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(s);
        File file=null;

        file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+allVokesName.get(selectedPos));
        fileName= file.getAbsolutePath();
        Log.i("TAG1",fileName);
        storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                playVoke();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    void listVokes(){

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String URL=dataSnapshot.getValue(String.class);
                 allVokesURL.add(URL);
                 int endPos=URL.indexOf('?');
                 int startPos=URL.indexOf('F')+1;
                 allVokesName.add(URL.substring(startPos,endPos));
                 vokesAdapter=new VokesAdapter(VokesListActivity.this,allVokesName);
                 vokesList.setAdapter(vokesAdapter);
                 Log.i("TAG",dataSnapshot.getValue(String.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
    
    private void playVoke() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            });
            Log.i("TAG", "play");
        } catch (IOException e) {
            Log.i("TAG", "not play");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }

    }
}
