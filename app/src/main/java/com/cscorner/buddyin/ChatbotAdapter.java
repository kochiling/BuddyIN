package com.cscorner.buddyin;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatbotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int MSG_TYPE_USER= 0;
    private static final int MSG_TYPE_BOT = 1;
    private final Context context;
    private List<ChatbotModel> chatbotModelList;

    public ChatbotAdapter(Context context, List<ChatbotModel> chatbotModelList) {
        this.context = context;
        this.chatbotModelList = chatbotModelList;
    }

    @Override
    public int getItemViewType(int position) {
        ChatbotModel message = chatbotModelList.get(position);
        String type = message.getType(); // Assuming your ChatbotModel has a getType() method

        if (type.equals("user")) {
            return MSG_TYPE_USER;
        } else {
            return MSG_TYPE_BOT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_BOT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chatbot, parent, false);
            return new ChatbotAdapter.ChatbotViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);
            return new ChatbotAdapter.UserViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatbotModel message = chatbotModelList.get(position);
        String formattedTime = formatTimestamp(message.getTimestamp());

        if (holder.getItemViewType() == MSG_TYPE_USER) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.user_text.setText(message.getMessage());
            userHolder.user_time.setText(formattedTime);
        } else  {
            ChatbotViewHolder botHolder = (ChatbotViewHolder) holder;
            botHolder.bot_text.setText(message.getMessage());
            botHolder.bot_time.setText(formattedTime);
        }
    }


    @Override
    public int getItemCount() {
        return chatbotModelList.size();
    }

    public void setChatbotModelList(List<ChatbotModel> chatbotModelList) {
        this.chatbotModelList = chatbotModelList;
        notifyDataSetChanged();
    }



    private String formatTimestamp(String timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        return DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView user_text, user_time;
        CardView user_card;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            user_card = itemView.findViewById(R.id.user_card);
            user_text = itemView.findViewById(R.id.user_text);
            user_time = itemView.findViewById(R.id.user_time);
        }
    }

    static class ChatbotViewHolder extends RecyclerView.ViewHolder {
        TextView bot_text, bot_time;
        CardView bot_card;

        public ChatbotViewHolder(@NonNull View itemView) {
            super(itemView);
            bot_card = itemView.findViewById(R.id.chatbot_card);
            bot_text = itemView.findViewById(R.id.bot_text);
            bot_time = itemView.findViewById(R.id.bot_time);
        }
    }
}
