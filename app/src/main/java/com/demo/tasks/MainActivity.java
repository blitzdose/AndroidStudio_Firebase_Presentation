package com.demo.tasks;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button add;
    private EditText newTask;
    private LinearLayout taskBlock;
    private LinearLayout buttonBlock;
    private ScrollView scroll;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;

    private ProgressDialog progressDialog;

    private List<String> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Connect Java-activity with XML-layout
        setContentView(R.layout.activity_main);

        //Connect XML-buttons with Java-entitys
        add=findViewById(R.id.add);
        newTask=findViewById(R.id.newTask);
        taskBlock=findViewById(R.id.taskBlock);
        buttonBlock=findViewById(R.id.buttonBlock);
        scroll=findViewById(R.id.scroll);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Bitte warten...");
        progressDialog.show();

        //Authenticate with Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = mAuth.getCurrentUser();
                            database = FirebaseDatabase.getInstance();
                            showOldTasks();
                        } else {
                            Toast.makeText(MainActivity.this, "Anmeldung fehlgeschlagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //Configurate or add some usages of elemenst
        scroll.fullScroll((ScrollView.FOCUS_DOWN));

        newTask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                newTask.setText("");
                newTask.setTextColor(getResources().getColor(R.color.black));
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (newTask.getText()!=null){
                    //Save Tasks in Database
                    if (taskList == null) {
                        taskList = new ArrayList<>();
                    }
                    taskList.add(newTask.getText().toString());
                    DatabaseReference reference = database.getReference().child(user.getUid());
                    reference.setValue(taskList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showOldTasks();
                                Toast.makeText(MainActivity.this, "Neuen Task erfolgreich gepeichert", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Fehler beim speichern des Tasks", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

                newTask.setText("");
            }
        });

    }

    public void showOldTasks(){
        //Display tasks from old sessions
        taskBlock.removeAllViews();
        buttonBlock.removeAllViews();

        //Load data from database
        DatabaseReference reference = database.getReference();
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    taskList = (List<String>) task.getResult().getValue();
                    if (taskList != null) {
                        for (int i=0; i<taskList.size(); i++) {
                            addTask(taskList.get(i), i);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Keine Daten vorhanden", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(MainActivity.this, "Daten konnten nicht gelesen werden", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    public void addTask(String task, int taskNumber){


        if (!task.isEmpty()) {

            //Add and save new tasks, display it with icon and delete-button
            LinearLayout taskTextLayout = new LinearLayout(MainActivity.this);
            taskTextLayout.setOrientation(LinearLayout.HORIZONTAL);

            Button icon= new Button(MainActivity.this);
            icon.setLayoutParams(new ViewGroup.LayoutParams(130, 140));
            icon.setBackground(this.getResources().getDrawable(R.mipmap.pfeil_foreground));
            icon.setGravity(Gravity.CENTER_VERTICAL);
            taskTextLayout.addView(icon);

            TextView newTaskText = new TextView(MainActivity.this);
            newTaskText.setTextSize(19);
            newTaskText.setText(task);
            newTaskText.setGravity(Gravity.CENTER_VERTICAL);
            newTaskText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 168));
            taskTextLayout.addView(newTaskText);

            Button taskDelete = new Button(MainActivity.this);
            taskDelete.setText("Del");
            taskDelete.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 168));
            taskDelete.setTextSize(15);
            taskDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskList.remove(taskNumber);
                    DatabaseReference reference = database.getReference().child(user.getUid());
                    reference.setValue(taskList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                showOldTasks();
                                Toast.makeText(MainActivity.this, "Task gelöscht", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Task löschen gescheitert", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            taskBlock.addView(taskTextLayout);
            buttonBlock.addView(taskDelete);
            scroll.fullScroll((ScrollView.FOCUS_DOWN));
        }

    }
}