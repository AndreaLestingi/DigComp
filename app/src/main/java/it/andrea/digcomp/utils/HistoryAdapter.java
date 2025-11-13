package it.andrea.digcomp.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import it.andrea.digcomp.R;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<JSONObject> historyList;

    public HistoryAdapter(List<JSONObject> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject item = historyList.get(position);
            String type = item.getString("type");
            String timestamp = item.getString("timestamp");
            String formattedDate = formatTimestamp(timestamp);

            if (type.equals("exam")) {
                int level = item.getInt("level");
                holder.title.setText("Esito Esame");
                holder.description.setText("Hai superato l'esame col livello " + level + "\n" + formattedDate);
                holder.image.setImageResource(R.mipmap.ic_launcher_foreground);
            } else if (type.equals("lesson")) {
                int lezione = item.getInt("lezione");
                holder.title.setText("Esito Lezione");
                holder.description.setText("Hai completato la lezione " + lezione + "\n" + formattedDate);
                holder.image.setImageResource(R.mipmap.ic_launcher_foreground);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            description = itemView.findViewById(R.id.item_description);
            image = itemView.findViewById(R.id.item_image);
        }
    }
}