package com.example.kvitter.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.kvitter.DatabaseLogic;
import com.example.kvitter.R;
import com.example.kvitter.Util.CurrentReceipt;
import com.example.kvitter.Util.UserData;

public class AcceptDeleteActivity extends NavigationActivity {

    private Button accept;
    private Button decline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_delete);

        bindViews();
        addListiners();
        runNavigation(R.id.activityAcceptRemoval);
    }

    private void addListiners() {
       accept.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               UserData receipt = CurrentReceipt.getReceipt();
               DatabaseLogic logic = new DatabaseLogic();
               logic.deleteReceipt(receipt);
               Intent intent = new Intent(AcceptDeleteActivity.this, MyReceiptActivity.class);
               startActivity(intent);
           }
       });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AcceptDeleteActivity.this, MyReceiptActivity.class);
                startActivity(intent);
            }
        });

    }

    private void bindViews() {
        accept = findViewById(R.id.btn_yes_delete);
        decline = findViewById(R.id.btn_no_delete);
    }
}
