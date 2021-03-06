package com.example.a.alcoholapp.Activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.a.alcoholapp.Database.Entity.Drink;
import com.example.a.alcoholapp.Listeners.RecyclerItemClickListener;
import com.example.a.alcoholapp.ViewModel.DrinkViewModel;
import com.example.a.alcoholapp.R;
import com.example.a.alcoholapp.ViewModel.ViewModelFactory;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ShowDrinksActivity extends AppCompatActivity {

    public static final int NEW_DRINK_ACTIVITY_REQUEST_CODE = 1;
    public static final int MODIFY_DRINK_ACTIVITY_REQUEST_CODE = 2;

    private DrinkViewModel mDrinkViewModel;

    @Inject
    ViewModelFactory factory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showdrinks);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final DrinkListAdapter adapter = new DrinkListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Add onItemTouchListener for recyclerview
        recyclerView.addOnItemTouchListener(
            new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //User clicked on a drink element
                //switch to NewDrinkActivity
                Intent intent = new Intent(ShowDrinksActivity.this, NewDrinkActivity.class);
                Drink drink = adapter.getAtPosition(position);

                //Set drink information to the intent
                intent.putExtra(NewDrinkActivity.EXTRA_DRINK_ID, adapter.getItemId(position));
                intent.putExtra(NewDrinkActivity.EXTRA_DRINK_NAME, drink.getName());
                intent.putExtra(NewDrinkActivity.EXTRA_DRINK_CL, drink.getCl());
                intent.putExtra(NewDrinkActivity.EXTRA_DRINK_CALORIES, drink.getCalories());
                intent.putExtra(NewDrinkActivity.EXTRA_DRINK_ALCOHOLPERCENTAGE, drink.getAlcoholPercentage());

                startActivityForResult(intent, MODIFY_DRINK_ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //There is no need for longClick method yet...
            }
        }));
        mDrinkViewModel = ViewModelProviders.of(this, factory).get(DrinkViewModel.class);

        mDrinkViewModel.getAllDrinks().observe(this, new Observer<List<Drink>>() {
            @Override
            public void onChanged(@Nullable final List<Drink> drinks) {
                adapter.setDrinks(drinks);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowDrinksActivity.this, NewDrinkActivity.class);
                startActivityForResult(intent, NEW_DRINK_ACTIVITY_REQUEST_CODE);
            }
        });

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowDrinksActivity.this, UserInfoActivity.class);
                startActivity(intent);
            }
        });
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_DRINK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Drink drink = new Drink(data.getStringExtra(NewDrinkActivity.EXTRA_DRINK_NAME),data.getIntExtra(NewDrinkActivity.EXTRA_DRINK_CL, 0),
                    data.getIntExtra(NewDrinkActivity.EXTRA_DRINK_CALORIES, 0), data.getDoubleExtra(NewDrinkActivity.EXTRA_DRINK_ALCOHOLPERCENTAGE, 0));
            mDrinkViewModel.insert(drink);
            Context context = getApplicationContext();
            CharSequence text = "Drink saved";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        } else if(requestCode == MODIFY_DRINK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            //Check for DELETE flag
            if(data.getBooleanExtra("DELETE", false)){
                mDrinkViewModel.delete(data.getLongExtra(NewDrinkActivity.EXTRA_DRINK_ID, 0));
                Toast.makeText(getApplicationContext(), "DRINK DELETED", Toast.LENGTH_SHORT).show();
            }else{
                //User modified the drink. Simply insert it to database.
                //The new drink will be replaced by the old one in the database.
                Drink drink = new Drink(data.getStringExtra(NewDrinkActivity.EXTRA_DRINK_NAME),data.getIntExtra(NewDrinkActivity.EXTRA_DRINK_CL, 0),
                        data.getIntExtra(NewDrinkActivity.EXTRA_DRINK_CALORIES, 0), data.getDoubleExtra(NewDrinkActivity.EXTRA_DRINK_ALCOHOLPERCENTAGE, 0));
                drink.setId(data.getLongExtra(NewDrinkActivity.EXTRA_DRINK_ID, 0));
                mDrinkViewModel.insert(drink);
                Toast.makeText(getApplicationContext(), "DRINK UPDATED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
