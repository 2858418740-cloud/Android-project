package com.example.projectw;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 注册页面
 * 功能：用户名密码注册、非空校验、密码一致性校验、重名检查
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etPwd, etConfirmPwd;
    private Button btnRegister, btnBack;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);

        // 初始化控件
        etUsername = findViewById(R.id.et_register_username);
        etPwd = findViewById(R.id.et_register_pwd);
        etConfirmPwd = findViewById(R.id.et_register_confirm_pwd);
        btnRegister = findViewById(R.id.btn_do_register);
        btnBack = findViewById(R.id.btn_back_to_login);

        // 注册按钮
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String pwd = etPwd.getText().toString().trim();
            String confirmPwd = etConfirmPwd.getText().toString().trim();

            // 1. 非空判断
            if (username.isEmpty() || pwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "用户名/密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 两次密码一致性判断
            if (!pwd.equals(confirmPwd)) {
                Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. 检查用户名是否已存在
            if (dbHelper.isUserExists(username)) {
                Toast.makeText(RegisterActivity.this, "用户名已被注册", Toast.LENGTH_SHORT).show();
                return;
            }

            // 4. 写入数据库
            boolean success = dbHelper.registerUser(username, pwd);
            if (success) {
                Toast.makeText(RegisterActivity.this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });

        // 返回登录按钮
        btnBack.setOnClickListener(v -> finish());
    }
}
