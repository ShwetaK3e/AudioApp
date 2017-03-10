package com.shwetak3e.audioapp;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shwetak3e.audioapp.supportClass.AudioRecordInfo;

import java.io.File;
import java.io.IOException;

public class RecordVoiceActivity extends AppCompatActivity {

    private ProgressBar progress;
    private ProgressBar uploadProgress;


    private final String audioDirName="myVoices";
    private final int audioCaptureDuration=15000;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    Button startRecord,stopRecord,startPlay, stopPlay,send;
    EditText myfileName;


    private StorageReference storageReferences;
    private FirebaseDatabase firebaseDatabase ;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        progress = (ProgressBar) findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);

        uploadProgress=(ProgressBar)findViewById(R.id.uploadProgress);
        uploadProgress.setVisibility(View.INVISIBLE);

        myfileName=(EditText)findViewById(R.id.fileName);

        send=(Button)findViewById(R.id.send);
        send.setEnabled(false);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });

        startRecord=(Button)findViewById(R.id.start);
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecorder();
            }
        });
        stopRecord=(Button)findViewById(R.id.stop);
        stopRecord.setEnabled(false);
        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 stopRecorder();
            }
        });

        startPlay=(Button)findViewById(R.id.startPlay);
        startPlay.setEnabled(false);
        startPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  startPlay();
            }
        });

        stopPlay=(Button)findViewById(R.id.stopPlay);
        stopPlay.setEnabled(false);
        stopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 stopPlay();
            }
        });



        AudioRecordInfo.filePath=getAudioFile();
        AudioRecordInfo.maxDuration = audioCaptureDuration;


        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setMaxDuration(AudioRecordInfo.maxDuration);
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    mr.stop();
                    mr.reset();
                    mr.release();
                    stopRecord.setEnabled(false);
                    startPlay.setEnabled(true);
                    send.setEnabled(true);
                    Toast.makeText(RecordVoiceActivity.this, "Recorder has reached maximum Duration", Toast.LENGTH_SHORT).show();
                }
            }
        });


        storageReferences = FirebaseStorage.getInstance().getReference();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("vokes");

    }

    private void stopPlay() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=null;

        progress.setVisibility(View.INVISIBLE);
        stopPlay.setEnabled(false);
        send.setEnabled(true);
    }

    private void startPlay() {
        try {
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setDataSource(AudioRecordInfo.filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            stopPlay.setEnabled(true);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlay.setEnabled(true);
                    progress.setVisibility(View.INVISIBLE);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        progress.setVisibility(View.VISIBLE);
        startPlay.setEnabled(false);

    }

    private void stopRecorder() {
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
        progress.setVisibility(View.INVISIBLE);
        stopRecord.setEnabled(false);
        startPlay.setEnabled(true);
        send.setEnabled(true);

    }


    private void startRecorder() {
        AudioRecordInfo.filePath=getAudioFile();
            try {
                mediaRecorder.setOutputFile(AudioRecordInfo.filePath);
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            progress.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopRecord.setEnabled(true);
                }
            }, 3000);
            startRecord.setEnabled(false);

    }


    private String getAudioFile() {
        String fileName=myfileName.getText().toString();
        if(fileName.equals(""))fileName="myaudio";
        File mediaStorageDir = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath(),audioDirName);
        File audioFile = null;
        try {
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs();
            }
            audioFile = new File(mediaStorageDir.getAbsolutePath(),fileName+".3gpp");
            audioFile.createNewFile();
            return audioFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("TAG", "Exception: " + e);
        }

        return null;
    }

    private void uploadFile(){
            Uri file = Uri.fromFile(new File(AudioRecordInfo.filePath));
            StorageReference riversRef = storageReferences.child("audios/" + file.getLastPathSegment());
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/*")
                    .build();
            uploadProgress.setVisibility(View.VISIBLE);
            riversRef.putFile(file, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            DatabaseReference newDatabaseReference = databaseReference.push();
                            newDatabaseReference.setValue(downloadUrl.toString());
                            Log.i("TAG", "success");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(RecordVoiceActivity.this,"Upload has Failed. Retry",Toast.LENGTH_LONG).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    uploadProgress.setProgress((int)progress);
                    Log.i("TAG","Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("TAG","Upload is paused");
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Toast.makeText(RecordVoiceActivity.this,"Your voke has been sent",Toast.LENGTH_LONG).show();
                }
            });


    }


}
