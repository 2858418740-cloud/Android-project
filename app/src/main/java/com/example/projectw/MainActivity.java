package com.example.projectw;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 登录页面
 * 功能：用户名密码登录、记住密码、跳转注册
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private CheckBox cbRememberPwd;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnForgetPwd;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        dbHelper = new DBHelper(this);

        // 检查是否有记住密码的用户，自动回填
        autoFillRememberedUser();

        // 登录按钮
        btnLogin.setOnClickListener(v -> {
            String name = etUsername.getText().toString().trim();
            String pwd = etPassword.getText().toString().trim();

            // 非空校验
            if (name.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            // 数据库校验
            if (dbHelper.loginUser(name, pwd)) {
                // 处理记住密码
                dbHelper.updateRememberStatus(name, cbRememberPwd.isChecked());

                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                // 跳转到店铺首页，携带用户名
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                intent.putExtra("username", name);
                startActivity(intent);
                finish(); // 关闭登录页，避免按返回键回来
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        // 注册按钮
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        // 找回密码（待开发）
        btnForgetPwd.setOnClickListener(v ->
                Toast.makeText(this, "找回密码功能待开发", Toast.LENGTH_SHORT).show());
    }

    private void initView() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        cbRememberPwd = findViewById(R.id.cb_remember_pwd);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        btnForgetPwd = findViewById(R.id.btn_forget_pwd);
    }

    /** 自动回填记住密码的用户信息 */
    private void autoFillRememberedUser() {
        String[] user = dbHelper.getRememberedUser();
        if (user != null) {
            etUsername.setText(user[0]);
            etPassword.setText(user[1]);
            cbRememberPwd.setChecked(true);
        }
    }
}
