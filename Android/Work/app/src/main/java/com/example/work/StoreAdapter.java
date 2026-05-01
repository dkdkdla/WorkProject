package com.example.work;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * [코드 설계 설명]
 * 1. 유연한 URL 관리: AppConfig.API_MY_STORE_DELETE를 사용하여 서버 경로 변경에 유연하게 대응한다.
 * 2. 리소스 효율성: RecyclerView의 ViewHolder 패턴을 사용하여 뷰 재사용성을 극대화했다.
 * 3. 사용자 경험(UX): 삭제 성공 시 notifyItemRemoved를 호출하여 부드러운 삭제 애니메이션을 제공한다.
 */
public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {

    private Context context;
    private ArrayList<StoreData> list;
    private String currentUserId;

    // 🚨 수정 포인트: AppConfig 기반의 서블릿 경로 설정
    private final String deleteUrl = AppConfig.API_MY_STORE_DELETE;

    public StoreAdapter(Context context, ArrayList<StoreData> list, String currentUserId) {
        this.context = context;
        this.list = list;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_store, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // holder.getAdapterPosition()을 사용하여 정확한 위치 참조
        final int currentPos = holder.getAdapterPosition();
        final StoreData item = list.get(currentPos);

        String displayText = String.format("%s (%s)", item.getStoreName(), item.getStoreId());
        holder.tvStoreName.setText(displayText);

        // 삭제 버튼 클릭 시 서버 요청
        holder.btnDelete.setOnClickListener(v -> {
            requestDeleteStore(currentUserId, item.getStoreId(), currentPos);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * 뷰 홀더 클래스: 매장 이름과 삭제 버튼을 보관한다.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoreName;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    /**
     * 서버의 MyStoreDelete 서블릿으로 삭제 요청을 보낸다.
     */
    private void requestDeleteStore(final String userId, final String storeId, final int position) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // 🚨 팩트체크: 서버 서블릿 파라미터(id, storeId)와 일치시킴
                String query = "?id=" + userId + "&storeId=" + storeId;
                URL url = new URL(deleteUrl + query);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject json = new JSONObject(result.toString());
                    String status = json.optString("status", "fail");
                    String message = json.optString("message", "삭제에 실패했습니다.");

                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            if ("success".equals(status)) {
                                Toast.makeText(context, "목록에서 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                list.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, list.size());
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}