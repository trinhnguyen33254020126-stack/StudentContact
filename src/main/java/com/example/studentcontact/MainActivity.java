package com.example.studentcontact;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etId, etName, etEmail, etTel;
    private Button btnSave, btnLoad;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ View
        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etTel = findViewById(R.id.etTel);

        btnSave = findViewById(R.id.btnSave);
        btnLoad = findViewById(R.id.btnLoad);

        dbHelper = new DatabaseHelper(this);

        // Nút SAVE: Lưu vào cả SharedPreferences và SQLite
        btnSave.setOnClickListener(v -> {
            Student student = getStudentFromInput();

            if (student.getId().isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập ID!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Lưu vào SharedPreferences
            saveToSharedPreferences(student);

            // 2. Lưu vào SQLite
            boolean dbSuccess = dbHelper.saveStudent(student);

            if (dbSuccess) {
                Toast.makeText(this, "Đã lưu vào SharedPreferences & SQLite!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lưu vào SQLite thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        // Nút LOAD: Ưu tiên lấy từ SharedPreferences, nếu trống thì tìm trong SQLite theo ID
        btnLoad.setOnClickListener(v -> {
            String currentId = etId.getText().toString().trim();

            // Lần 1: Thử đọc từ SharedPreferences
            Student studentFromSP = loadFromSharedPreferences();

            if (!studentFromSP.getId().isEmpty()) {
                displayStudent(studentFromSP);
                Toast.makeText(this, "Tải dữ liệu từ SharedPreferences thành công!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lần 2: Nếu SharedPreferences trống, tìm trong SQLite theo ID đã nhập
            if (!currentId.isEmpty()) {
                Student studentFromDB = dbHelper.getStudent(currentId);
                if (studentFromDB != null) {
                    displayStudent(studentFromDB);
                    Toast.makeText(this, "Tải dữ liệu từ SQLite thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Không tìm thấy dữ liệu trong SQLite!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không có dữ liệu lưu tạm, hãy nhập ID để tìm trong SQLite!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Student getStudentFromInput() {
        return new Student(
                etId.getText().toString().trim(),
                etName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etTel.getText().toString().trim()
        );
    }

    private void displayStudent(Student student) {
        if (student != null) {
            etId.setText(student.getId());
            etName.setText(student.getName());
            etEmail.setText(student.getEmail());
            etTel.setText(student.getTel());
        }
    }

    private void saveToSharedPreferences(Student student) {
        SharedPreferences pref = getSharedPreferences("StudentPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id", student.getId());
        editor.putString("name", student.getName());
        editor.putString("email", student.getEmail());
        editor.putString("tel", student.getTel());
        editor.apply();
    }

    private Student loadFromSharedPreferences() {
        SharedPreferences pref = getSharedPreferences("StudentPref", MODE_PRIVATE);
        return new Student(
                pref.getString("id", ""),
                pref.getString("name", ""),
                pref.getString("email", ""),
                pref.getString("tel", "")
        );
    }
}