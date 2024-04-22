package com.example.a81c;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class chatActivity extends AppCompatActivity {
    private EditText editTextPrompt;
    private Button sendButton;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<String> messages = new ArrayList<>();

    private String stringToken = "LL-n7IhWNrPdXttzHN2GH3CgfuUaDNMpnHAGqBnc67uPQPPByO0A9VXq4ZskSES8cod";
    private String stringURLEndPoint = "https://api.llama-api.com/chat/completions";


    private void showLoadingAnimation() {
        LinearLayout layoutDots = findViewById(R.id.layout_loading_dots);
        layoutDots.setVisibility(View.VISIBLE);
        Animation dotPulse = AnimationUtils.loadAnimation(this, R.anim.dot_pule);
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            // Start animation with a slight delay between dots
            dot.startAnimation(dotPulse);
            dotPulse.setStartOffset(i * 200);
        }
    }

    private void hideLoadingAnimation() {
        LinearLayout layoutDots = findViewById(R.id.layout_loading_dots);
        layoutDots.setVisibility(View.GONE);
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            View dot = layoutDots.getChildAt(i);
            dot.clearAnimation();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        editTextPrompt = findViewById(R.id.et_prompt);
        sendButton = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.recyclerView);
        chatAdapter = new ChatAdapter(messages);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Retrieve the username passed from MainActivity
        String username = getIntent().getStringExtra("USERNAME");

        // Add a welcome message
        if (username != null && !username.isEmpty()) {
            messages.add("A: Hello " + username + ", how can I assist you?");
            chatAdapter.notifyDataSetChanged();
        }

        sendButton.setOnClickListener(v -> {
            final String stringInputText = editTextPrompt.getText().toString();

            if (!stringInputText.isEmpty()) {
                // Add the question to messages list
                messages.add("Q: " + stringInputText);
                chatAdapter.notifyDataSetChanged();
                editTextPrompt.setText(""); // Clear input field
                showLoadingAnimation();

                JSONObject jsonObject = new JSONObject();
                try {
                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObjectMessage = new JSONObject();
                    jsonObjectMessage.put("role", "user");
                    jsonObjectMessage.put("content", stringInputText);
                    jsonArray.put(jsonObjectMessage);
                    jsonObject.put("messages", jsonArray);

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            stringURLEndPoint, // Assume this variable holds your endpoint URL
                            jsonObject,
                            response -> {
                                try {
                                    // Extracting the response from the API
                                    String stringOutput = response.getJSONArray("choices")
                                            .getJSONObject(0)
                                            .getJSONObject("message")
                                            .getString("content");

                                    // Adding the chatbot's response to the messages list and updating RecyclerView
                                    messages.add("A: " + stringOutput);
                                    chatAdapter.notifyDataSetChanged();
                                    hideLoadingAnimation();

                                } catch (JSONException e) {
                                    Toast.makeText(chatActivity.this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(chatActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("Authorization", "Bearer " + stringToken); // Assume this variable holds your token
                            return headers;
                        }
                    };

                    jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                            60000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
                } catch (JSONException e) {
                    Toast.makeText(chatActivity.this, "Failed to create JSON object for request", Toast.LENGTH_SHORT).show();
                }

                editTextPrompt.setText(""); // Clear the input field
            } else {
                Toast.makeText(chatActivity.this, "Please enter a question", Toast.LENGTH_SHORT).show();
            }
        });
    }
}