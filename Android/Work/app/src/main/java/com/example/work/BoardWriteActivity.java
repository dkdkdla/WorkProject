package com.example.work;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 설정: AppConfig.API_BOARD_WRITE 상수를 사용하여 서버 엔드포인트(/BoardWrite)를 관리합니다.
 * 2. 멀티파트(Multipart) 통신: DataOutputStream을 사용하여 텍스트 필드와 바이너리 파일을 한 번의 POST 요청으로 전송합니다.
 * 3. 리소스 관리: 파일 전송 후 InputStream 및 OutputStream을 명시적으로 닫아 메모리 누수를 방지합니다.
 */
public class BoardWriteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSelectFile, btnSubmit;
    private TextView tvFileName;

    private String userId, storeId;
    private Uri fileUri = null;
    private String fileName = null;

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String serverUrl = AppConfig.API_BOARD_WRITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_write);

        // 1. 뷰 초기화
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvFileName = findViewById(R.id.tvFileName);

        // 2. 인텐트 데이터 수신
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        storeId = intent.getStringExtra("storeId");

        // 3. 파일 선택 이벤트
        btnSelectFile.setOnClickListener(v -> {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("*/*"); // 모든 파일 형식 허용
            startActivityForResult(fileIntent, 1001);
        });

        // 4. 게시글 등록 이벤트
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 멀티파트 업로드 실행 (비동기)
            new Thread(() -> uploadFile(storeId, userId, title, content, fileUri)).start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            fileName = getFileName(fileUri);
            tvFileName.setText(fileName);
        }
    }

    /**
     * Uri로부터 실제 파일 이름을 추출합니다.
     */
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) result = cursor.getString(index);
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    /**
     * 서버의 BoardWrite 서블릿으로 멀티파트 데이터를 전송합니다.
     */
    private void uploadFile(String sId, String uId, String title, String content, Uri fileUri) {
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String twoHyphens = "--";
        String lineEnd = "\r\n";

        HttpURLConnection conn = null;
        DataOutputStream dos = null;

        try {
            URL url = new URL(serverUrl);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            dos = new DataOutputStream(conn.getOutputStream());

            // 🚨 팩트체크: BoardWrite.java 서블릿의 파라미터명과 일치시킴
            addFormField(dos, boundary, "storeId", sId);
            addFormField(dos, boundary, "writerId", uId);
            addFormField(dos, boundary, "title", title);
            addFormField(dos, boundary, "content", content);

            // 파일 데이터 처리
            if (fileUri != null && fileName != null) {
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                // 🚨 팩트체크: 서버는 'file'이라는 이름으로 파일을 수신함
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                try (InputStream fileInputStream = getContentResolver().openInputStream(fileUri)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                }
                dos.writeBytes(lineEnd);
            }

            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            dos.flush();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");
                String message = json.optString("message", "처리에 실패했습니다.");

                runOnUiThread(() -> {
                    Toast.makeText(BoardWriteActivity.this, message, Toast.LENGTH_SHORT).show();
                    if ("success".equals(status)) finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(BoardWriteActivity.this, "서버 응답 오류: " + responseCode, Toast.LENGTH_SHORT).show());
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(BoardWriteActivity.this, "전송 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            try { if (dos != null) dos.close(); } catch (Exception e) {}
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 멀티파트 폼 필드를 추가하는 헬퍼 메서드이다.
     */
    private void addFormField(DataOutputStream dos, String boundary, String name, String value) throws Exception {
        dos.writeBytes("--" + boundary + "\r\n");
        dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
        dos.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n");
        dos.writeBytes("\r\n");
        dos.write(value.getBytes("UTF-8"));
        dos.writeBytes("\r\n");
    }
}