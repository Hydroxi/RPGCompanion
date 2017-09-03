package com.mpvreeken.rpgcompanion;


import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mpvreeken.rpgcompanion.NPC.NPC;
import com.mpvreeken.rpgcompanion.NPC.NPCData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Activity to display randomly generated NPCs
 *
 * Reads in RAW json data then sends it to NPCData which parses it.
 *
 * Allows generation of random NPCs and displays them on screen
 *
 * @author Michael Vreeken
 * @version 2017.0727
 * @since 1.0
 */
public class RandomNPCActivity extends AppCompatActivity {

    NPCData npcData;
    TextView npc_tv;
    NPC currentNPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_npc);

        //Set up back button to appear in action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //set up variable to hold npc output text view
        npc_tv = findViewById(R.id.npc_body_tv);
        npc_tv.setMovementMethod(new ScrollingMovementMethod());

        //Get the json data from raw, then parse it into a nicer, more accessible object
        npcData = new NPCData(getApplicationContext());

        //Initialize the new activity with a freshly generated NPC
        generateNewNPC();

        //Set up button for generating new NPCs
        Button gen_btn = findViewById(R.id.npc_generate_btn);
        gen_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateNewNPC();
            }
        });
    }

    public void saveNPC() {
        Gson gson = new Gson();
        String json = gson.toJson(currentNPC);

        //TODO save to db
    }

    public void generateNewNPC() {
        //Generate new NPC
        currentNPC = npcData.getRandomNPC();
        //Show the new NPC in the output text view
        npc_tv.setText(currentNPC.output());
        npc_tv.scrollTo(0,0);
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