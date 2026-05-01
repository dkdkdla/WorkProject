package com.example.work;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * [코드 설계 설명]
 * 1. ViewHolder 패턴: findViewById의 반복 호출을 줄여 리스트 스크롤 성능을 최적화했습니다.
 * 2. 실시간 데이터 동기화: 서버에서 삭제 성공 응답을 받으면 리스트(ArrayList)에서 즉시 제거하고 notifyDataSetChanged를 호출합니다.
 * 3. 중앙 집중형 URL: AppConfig.BASE_URL을 참조하여 서버 경로 잔해를 제거했습니다.
 */
public class MemberAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<MemberDTO> items;
    private final String deleteUrl = AppConfig.BASE_URL + "AdminMemberDelete";

    public MemberAdapter(Context context, ArrayList<MemberDTO> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvMemberName);
            holder.tvId = convertView.findViewById(R.id.tvMemberId);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteMember);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MemberDTO item = items.get(position);
        holder.tvName.setText(item.getName());

        DecimalFormat df = new DecimalFormat("###,###");
        String formattedWage = df.format(item.getHourlyWage());
        holder.tvId.setText(String.format("%s | 시급: %s원", item.getId(), formattedWage));

        // 삭제(매장 제외) 버튼 이벤트
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("직원 제외")
                    .setMessage(String.format("'%s' 직원을 현재 매장에서 제외하시겠습니까?\n(계정은 삭제되지 않습니다)", item.getName()))
                    .setPositiveButton("제외", (dialog, which) -> {
                        // 서버에 삭제 요청 (비동기)
                        new Thread(() -> requestDeleteMember(item.getId(), position)).start();
                    })
                    .setNegativeButton("취on", null)
                    .show();
        });

        // 버튼 클릭 시 리스트 아이템 클릭 이벤트 방해 금지
        holder.btnDelete.setFocusable(false);
        holder.btnDelete.setFocusableInTouchMode(false);

        return convertView;
    }

    /**
     * 서버의 AdminMemberDelete 서블릿으로 삭제 요청을 보냅니다.
     */
    private void requestDeleteMember(String memId, int position) {
        try {
            // 🚨 팩트체크: AdminMemberDelete.java 서블릿은 GET 방식으로 'id' 파라미터를 받음
            URL url = new URL(deleteUrl + "?id=" + memId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                String result = sb.toString();

                ((Activity)context).runOnUiThread(() -> {
                    // 서버 응답에 "성공" 혹은 알림창 코드가 포함되어 있는지 확인
                    if (result.contains("성공") || result.contains("success")) {
                        Toast.makeText(context, "매장에서 제외되었습니다.", Toast.LENGTH_SHORT).show();
                        items.remove(position);
                        notifyDataSetChanged();
                    } else {
                        Toast.makeText(context, "처리에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            ((Activity)context).runOnUiThread(() ->
                    Toast.makeText(context, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
        }
    }

    // 뷰 홀더 클래스 (성능 최적화용)
    static class ViewHolder {
        TextView tvName, tvId;
        Button btnDelete;
    }
}