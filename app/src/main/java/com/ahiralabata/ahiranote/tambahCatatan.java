package com.ahiralabata.ahiranote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class tambahCatatan extends AppCompatActivity {
    public static final int REQUEST_CODE_STORAGE = 100;
    int eventID = 0;
    EditText name, konten;
    Button btnSimpan;
    String fileName = "";
    String tempCatatan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_catatan);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        name = findViewById(R.id.nama);
        konten = findViewById(R.id.konten);
        btnSimpan = findViewById(R.id.btnSimpan);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fileName = extras.getString("note");
            name.setText(fileName);
            getSupportActionBar().setTitle("Ubah Catatan");
        } else {
            getSupportActionBar().setTitle("Tambah Catatan");
        }
        eventID = 1;
        if (Build.VERSION.SDK_INT >= 23) {
            if (periksaIzinPenyimpanan()) {
                bacaFile();
            }
        } else {
            bacaFile();
        }

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventID = 2;
                if (!tempCatatan.equals(konten.getText().toString())) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (periksaIzinPenyimpanan()) {
                            tampilkanDialogKonfirmasiPenyimpanan();
                        }
                    } else {
                        tampilkanDialogKonfirmasiPenyimpanan();
                    }
                }
            }
        });
    }

    public boolean periksaIzinPenyimpanan() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (eventID == 1) {
                        bacaFile();
                    } else {
                        tampilkanDialogKonfirmasiPenyimpanan();
                    }
                }
                break;
        }
    }

    void bacaFile() {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ahiraNote");
        File file = new File(path, name.getText().toString());
        if (file.exists()) {
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                while (line != null) {
                    text.append(line);
                    line = br.readLine();
                }
                br.close();
            } catch (IOException e) {
                System.out.println("Error " + e.getMessage());
            }
            tempCatatan = text.toString();
            konten.setText(text.toString());
        }
    }

    void buatDanUbah() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return;
        }
        File path = Environment.getExternalStorageDirectory();
        File parent = new File(path.getAbsolutePath() + "/ahiraNote");
        if (parent.exists()) {
            File file = new File(parent, name.getText().toString());
            FileOutputStream outputStream = null;
            try {
                file.createNewFile();
                outputStream = new FileOutputStream(file);
                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream);
                streamWriter.append(konten.getText());
                streamWriter.flush();
                streamWriter.close();
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            parent.mkdir();
            File file = new File(parent, name.getText().toString());
            FileOutputStream outputStream = null;
            try {
                file.createNewFile();
                outputStream = new FileOutputStream(file, false);
                outputStream.write(konten.getText().toString().getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        finish();
    }

    void tampilkanDialogKonfirmasiPenyimpanan() {
        new AlertDialog.Builder(this)
                .setTitle("Simpan Catatan")
                .setMessage("Apakah Anda yakin ingin menyimpan Catatan ini?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        buatDanUbah();
                    }
                })
                .setNegativeButton("Tidak", null).show();
    }

    @Override
    public void onBackPressed() {
        if (!tempCatatan.equals(konten.getText().toString())) {
            tampilkanDialogKonfirmasiPenyimpanan();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

