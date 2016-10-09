package com.udacity.classroom.victorpinto.portfolio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_filmes).setOnClickListener(new ButtonAction(getString(R.string.filmes_populares)));
        findViewById(R.id.button_stock).setOnClickListener(new ButtonAction(getString(R.string.stock_hawk)));
        findViewById(R.id.button_maior).setOnClickListener(new ButtonAction(getString(R.string.faca_maior)));
        findViewById(R.id.button_material).setOnClickListener(new ButtonAction(getString(R.string.aplicativo_material)));
        findViewById(R.id.button_onipresente).setOnClickListener(new ButtonAction(getString(R.string.seja_onipresente)));
        findViewById(R.id.button_capstone).setOnClickListener(new ButtonAction(getString(R.string.capstone)));
    }
    
    class ButtonAction implements View.OnClickListener{
    
        private String nome;
    
        public ButtonAction(String nome){
        
            this.nome = nome;
        }
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, getString(R.string.abrir_, nome), Toast.LENGTH_SHORT).show();
        }
    }
}
