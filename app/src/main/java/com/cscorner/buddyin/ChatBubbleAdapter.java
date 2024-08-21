package com.cscorner.buddyin;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatBubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int MSG_TYPE_RECEIVE = 0;
    private static final int MSG_TYPE_SEND = 1;
    private final Context context;
    private List<ChatModel> chatModelList;

    public ChatBubbleAdapter(Context context, List<ChatModel> chatModelList) {
        this.context = context;
        this.chatModelList = chatModelList;
    }

    @Override
    public int getItemViewType(int position) {
        String currentuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String senderid = chatModelList.get(position).getSender();
        if (senderid.equals(currentuid)) {
            return MSG_TYPE_SEND;
        } else {
            return MSG_TYPE_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RECEIVE) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_receive, parent, false);
            return new ReceiveViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_send, parent, false);
            return new SendViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel item = chatModelList.get(position);
        String message = item.getMessage();
        String type = item.getType();
        String timeStamp = formatTimestamp(item.getTimestamp());

        if (holder.getItemViewType() == MSG_TYPE_RECEIVE) {
            ReceiveViewHolder receiveViewHolder = (ReceiveViewHolder) holder;
            receiveViewHolder.receive_time.setText(timeStamp);

            if ("text".equals(type)) {
                receiveViewHolder.receive_text.setVisibility(View.VISIBLE);
                receiveViewHolder.receive_images.setVisibility(View.GONE);
                receiveViewHolder.receive_text.setText(message);
            } else {
                receiveViewHolder.receive_text.setVisibility(View.GONE);
                receiveViewHolder.receive_images.setVisibility(View.VISIBLE);
                Glide.with(context).load(message).into(receiveViewHolder.receive_images);

                ((ReceiveViewHolder) holder).receive_images.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewChatImageActivity.class);
                        intent.putExtra("imageUrl",item.getMessage() );
                        context.startActivity(intent);
                    }
                });
            }



        } else {
            SendViewHolder sendViewHolder = (SendViewHolder) holder;
            sendViewHolder.user_time.setText(timeStamp);

            if ("text".equals(type)) {
                sendViewHolder.user_text.setVisibility(View.VISIBLE);
                sendViewHolder.images.setVisibility(View.GONE);
                sendViewHolder.user_text.setText(message);
            } else {
                sendViewHolder.user_text.setVisibility(View.GONE);
                sendViewHolder.images.setVisibility(View.VISIBLE);
                Glide.with(context).load(message).into(sendViewHolder.images);

                ((SendViewHolder) holder).images.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ViewChatImageActivity.class);
                        intent.putExtra("imageUrl",item.getMessage() );
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public void setChatModelList(List<ChatModel> chatModelList) {
        this.chatModelList = chatModelList;
        notifyDataSetChanged();
    }

    private String formatTimestamp(String timeStamp) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        return DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
    }

    static class SendViewHolder extends RecyclerView.ViewHolder {
        TextView user_text, user_time;
        ImageView images;
        CardView user_card;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            user_card = itemView.findViewById(R.id.user_card);
            user_text = itemView.findViewById(R.id.user_text);
            user_time = itemView.findViewById(R.id.user_time);
            images = itemView.findViewById(R.id.images);
        }
    }

    static class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView receive_text, receive_time;
        ImageView receive_images;
        CardView receive_card;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            receive_card = itemView.findViewById(R.id.receive_card);
            receive_text = itemView.findViewById(R.id.receive_text);
            receive_time = itemView.findViewById(R.id.receive_time);
            receive_images = itemView.findViewById(R.id.receive_images);
        }
    }
}
