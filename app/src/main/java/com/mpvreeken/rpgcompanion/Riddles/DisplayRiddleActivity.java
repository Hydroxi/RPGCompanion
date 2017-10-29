package com.mpvreeken.rpgcompanion.Riddles;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mpvreeken.rpgcompanion.CommentActivity;
import com.mpvreeken.rpgcompanion.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DisplayRiddleActivity extends AppCompatActivity {

    ArrayList<RiddleComment> commentsArray = new ArrayList<>();
    RiddleCommentsArrayAdapter commentArrayAdapter;
    LinearLayout comments_layout;
    Riddle riddle;
    Context context;
    ConstraintLayout loading_screen;
    ProgressBar loading_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_riddle);

        //Set up back button to appear in action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        context = this.getBaseContext();

        Intent intent = getIntent();
        final Bundle riddleBundle = intent.getExtras();
        riddle = (Riddle) riddleBundle.getSerializable("RIDDLE_OBJ");

        loading_screen = findViewById(R.id.riddle_loading_screen);
        loading_progressBar = findViewById(R.id.riddle_loading_progressBar);

        Button upvote_btn = findViewById(R.id.riddle_details_vote_up_btn);
        Button downvote_btn = findViewById(R.id.riddle_details_vote_down_btn);
        Button comment_btn = findViewById(R.id.riddle_details_comment_btn);
        View.OnClickListener buttonHandler = new View.OnClickListener() {
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.riddle_details_vote_up_btn:
                        upvote();
                        break;
                    case R.id.riddle_details_vote_down_btn:
                        downvote();
                        break;
                    case R.id.riddle_details_comment_btn:
                        displayCommentInput();
                        break;
                    default:
                }
            }
        };

        upvote_btn.setOnClickListener(buttonHandler);
        downvote_btn.setOnClickListener(buttonHandler);
        comment_btn.setOnClickListener(buttonHandler);




        //Fetch riddles from db
        this.commentsArray = new ArrayList<>();
        this.commentArrayAdapter = new RiddleCommentsArrayAdapter(this, commentsArray);
        //this.comments_lv = findViewById(R.id.riddle_comments_lv);

        showLoadingScreen();

        this.comments_layout = findViewById(R.id.riddle_comments_linear_layout);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(getResources().getString(R.string.url_get_riddle)+riddle.getId())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                displayError("Could not connect to server. Please try again");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    displayError("An unknown error occurred. Please try again");
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    try {
                        JSONObject all = new JSONObject(response.body().string());
                        /*
                        JSONObject h = all.getJSONObject("riddle");
                        riddle = new Riddle(h.getString("id"),
                                h.getString("title"),
                                h.getString("username"),
                                h.getString("description"),
                                h.getString("votes"),
                                h.getString("created_at")
                        );
                        */
                        JSONArray cs = all.getJSONArray("comments");
                        for (int i=0; i<cs.length(); i++) {
                            commentsArray.add(
                                new RiddleComment(cs.getJSONObject(i).getString("id"),
                                    cs.getJSONObject(i).getString("riddle_id"),
                                    cs.getJSONObject(i).getString("username"),
                                    cs.getJSONObject(i).getString("comment"),
                                    cs.getJSONObject(i).getString("votes"),
                                    cs.getJSONObject(i).getString("created_at")
                                )
                            );
                        }


                        //We can't update the UI on a background thread, so run on the UI thread
                        DisplayRiddleActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupUI();
                            }
                        });
                    }
                    catch (JSONException e) {
                        displayError("An unknown error occurred. Please try again");
                        Log.e("DisplayRiddleActivity", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setupUI() {
        TextView votes_tv = findViewById(R.id.riddle_details_votes_tv);
        votes_tv.setText(String.valueOf(riddle.getCalculatedVotes()));
        TextView riddle_tv = findViewById(R.id.riddle_details_riddle_tv);
        riddle_tv.setText(String.valueOf(riddle.getRiddle()));
        TextView answer_tv = findViewById(R.id.riddle_details_answer_tv);
        answer_tv.setText(String.valueOf(riddle.getAnswer()));

        //comments_lv.setAdapter(commentArrayAdapter);

        for (int i=0; i<commentsArray.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.comment_layout, null);
            TextView comment_tv = (TextView) view.findViewById(R.id.comment_layout_comment_tv);


            comment_tv.setText(commentsArray.get(i).getComment());
            comments_layout.addView(view);
        }

        hideLoadingScreen();

        /*
        comments_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(parent.getContext(), DisplayRiddleActivity.class);

                Bundle bundle = new Bundle();
                //bundle.putSerializable("MOVIE_OBJ", (Serializable) moviesArray.get(position));

                //intent.putExtras(bundle);
                intent.putExtra("riddle_id", riddlesArray.get(position).getId());
                startActivity(intent);
            }
        });
        */
    }

    private void showLoadingScreen() {
        loading_progressBar.setVisibility(View.VISIBLE);
        loading_screen.setVisibility(View.VISIBLE);
    }
    private void hideLoadingScreen() {
        loading_progressBar.setVisibility(View.GONE);
        loading_screen.setVisibility(View.GONE);
    }

    private void downvote() {
        String url = getResources().getString(R.string.url_downvote)+riddle.getId();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                displayError("Could not connect to server. Please try again");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    displayError("An unknown error occurred. Please try again");
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    try {
                        JSONObject all = new JSONObject(response.body().string());

                        if (all.has("error")) {
                            //error
                            Log.e("DisplayRiddleActivity", "Error: "+ all.getString("error"));
                            displayError("Error: "+all.getString("error"));
                        }
                        else if (all.has("msg")) {
                            //success
                            Log.e("DisplayRiddleActivity", "Success");
                            downvoteUI();
                        }
                        else {
                            //unknown error
                            Log.e("DisplayRiddleActivity", "Unknown Error: "+ all.toString());
                            displayError("An unknown error occurred. Please try again");
                        }
                    }
                    catch (JSONException e) {
                        Log.e("err", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void downvoteUI() {
        DisplayRiddleActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void upvote() {
        String url = getResources().getString(R.string.url_upvote)+riddle.getId();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                displayError("Could not connect to server. Please try again");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    displayError("An unknown error occurred. Please try again");
                    throw new IOException("Unexpected code " + response);
                }
                else {
                    try {
                        JSONObject all = new JSONObject(response.body().string());

                        if (all.has("error")) {
                            //error
                            Log.e("DisplayRiddleActivity", "Error: "+ all.getString("error"));
                            displayError("Error: "+all.getString("error"));
                        }
                        else if (all.has("msg")) {
                            //success
                            Log.e("DisplayRiddleActivity", "Success");
                            //We can't update the UI on a background thread, so run on the UI thread
                            upvoteUI();
                        }
                        else {
                            //unknown error
                            Log.e("DisplayRiddleActivity", "Unknown Error: "+ all.toString());
                            displayError("An unknown error occurred. Please try again");
                        }


                    }
                    catch (JSONException e) {
                        Log.e("err", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void upvoteUI() {
        DisplayRiddleActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void displayError(final String s) {
        DisplayRiddleActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayCommentInput() {
        Intent intent = new Intent(context, CommentActivity.class);

        intent.putExtra("id", riddle.getId());
        intent.putExtra("commentType", "riddle");

        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Should only happen after comment is submitted or cancelled
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                //String result=data.getStringExtra("result");
                //TODO Add user's new comment to the list of comments
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //user pressed back btn
            }
        }
    }


    //Set click listener for back button in action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
