package com.example.work;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * [코드 설계 설명]
 * 1. 중앙 집중식 설정: AppConfig.API_BOARD_LIST를 사용하여 서버의 /BoardList 서블릿과 통신합니다.
 * 2. 효율적인 목록 갱신: onResume()에서 loadBoardList()를 호출하여 사용자가 글을 쓰고 돌아왔을 때 즉시 목록이 갱신되도록 했습니다.
 * 3. 데이터 캡슐화: BoardItem 내부 클래스를 통해 게시글 정보를 객체화하여 관리합니다.
 */
public class BoardListActivity extends AppCompatActivity {

    private ListView listView;
    private Button btnWrite;
    private BoardAdapter adapter;
    private ArrayList<BoardItem> boardList = new ArrayList<>();

    private String userId, storeId, role;

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String listUrl = AppConfig.API_BOARD_LIST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);

        // 1. 뷰 및 데이터 초기화
        listView = findViewById(R.id.listView);
        btnWrite = findViewById(R.id.btnWrite);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        storeId = intent.getStringExtra("storeId");
        role = intent.getStringExtra("role");

        adapter = new BoardAdapter();
        listView.setAdapter(adapter);

        // 2. 글쓰기 버튼 이벤트
        btnWrite.setOnClickListener(v -> {
            Intent writeIntent = new Intent(BoardListActivity.this, BoardWriteActivity.class);
            writeIntent.putExtra("userId", userId);
            writeIntent.putExtra("storeId", storeId);
            startActivity(writeIntent);
        });

        // 3. 리스트 아이템 클릭 (게시글 상세 보기 이동)
        listView.setOnItemClickListener((parent, view, position, id) -> {
            BoardItem item = boardList.get(position);
            Intent readIntent = new Intent(BoardListActivity.this, BoardReadActivity.class);

            // 🚨 팩트체크: 서버(BoardView.java) 호출을 위해 게시글 ID(seq)를 넘김
            readIntent.putExtra("board_seq", item.id);
            readIntent.putExtra("userId", userId);
            readIntent.putExtra("storeId", storeId); // 매장 컨텍스트 유지용
            startActivity(readIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 화면이 보일 때마다 목록 최신화 (비동기 처리)
        new Thread(this::loadBoardList).start();
    }

    /**
     * 서버의 BoardList 서블릿으로부터 JSON 데이터를 가져와 리스트뷰를 갱신합니다.
     */
    private void loadBoardList() {
        HttpURLConnection conn = null;
        try {
            // GET 방식으로 storeId 전달
            URL url = new URL(listUrl + "?storeId=" + storeId);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String status = json.optString("status", "fail");

                if ("success".equals(status)) {
                    JSONArray data = json.getJSONArray("data");

                    // UI 스레드 작업 전 데이터 가공
                    ArrayList<BoardItem> tempList = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject obj = data.getJSONObject(i);
                        tempList.add(new BoardItem(
                                obj.getString("id"),
                                obj.getString("title"),
                                obj.getString("writer"),
                                obj.getString("date")
                        ));
                    }

                    // 리스트뷰 갱신
                    runOnUiThread(() -> {
                        boardList.clear();
                        boardList.addAll(tempList);
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "목록을 불러오는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 게시글 데이터를 담는 데이터 모델 클래스이다.
     */
    static class BoardItem {
        String id, title, writer, date;
        public BoardItem(String id, String title, String writer, String date) {
            this.id = id;
            this.title = title;
            this.writer = writer;
            this.date = date;
        }
    }

    /**
     * 리스트뷰와 데이터를 연결해 주는 어댑터 클래스이다.
     */
    class BoardAdapter extends BaseAdapter {
        @Override
        public int getCount() { return boardList.size(); }
        @Override
        public Object getItem(int position) { return boardList.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(BoardListActivity.this).inflate(R.layout.item_board, parent, false);
            }

            BoardItem item = boardList.get(position);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvWriter = convertView.findViewById(R.id.tvWriter);
            TextView tvDate = convertView.findViewById(R.id.tvDate);

            if (tvTitle != null) tvTitle.setText(item.title);
            if (tvWriter != null) tvWriter.setText(item.writer);
            if (tvDate != null) tvDate.setText(item.date);

            return convertView;
        }
    }
}