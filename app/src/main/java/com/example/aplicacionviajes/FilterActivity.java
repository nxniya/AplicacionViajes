package com.example.aplicacionviajes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import java.util.Calendar;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private EditText etCityFilter;
    private RangeSlider priceRangeSlider;
    private TextView tvPriceRange;
    private Button btnMinDate, btnMaxDate;
    private CheckBox cbSoloFavoritos;

    private long minTimestamp = 0;
    private long maxTimestamp = Long.MAX_VALUE;
    private float minPrice = 0;
    private float maxPrice = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        etCityFilter = findViewById(R.id.etCityFilter);
        priceRangeSlider = findViewById(R.id.priceRangeSlider);
        tvPriceRange = findViewById(R.id.tvPriceRange);
        btnMinDate = findViewById(R.id.btnMinDate);
        btnMaxDate = findViewById(R.id.btnMaxDate);
        cbSoloFavoritos = findViewById(R.id.cbSoloFavoritos);
        Button btnApplyFilter = findViewById(R.id.btnApplyFilter);

        // Recuperar valores previos si existen
        Intent intent = getIntent();
        if (intent.hasExtra("city")) etCityFilter.setText(intent.getStringExtra("city"));
        cbSoloFavoritos.setChecked(intent.getBooleanExtra("solo_favoritos", false));
        minTimestamp = intent.getLongExtra("min_date", 0);
        maxTimestamp = intent.getLongExtra("max_date", Long.MAX_VALUE);
        minPrice = intent.getFloatExtra("min_price", 0);
        maxPrice = intent.getFloatExtra("max_price", 2000);

        priceRangeSlider.setValues(minPrice, maxPrice);
        tvPriceRange.setText(String.format("%.0f€ - %.0f€", minPrice, maxPrice));

        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
            tvPriceRange.setText(String.format("%.0f€ - %.0f€", minPrice, maxPrice));
        });

        btnMinDate.setOnClickListener(v -> showDatePicker(true));
        btnMaxDate.setOnClickListener(v -> showDatePicker(false));

        btnApplyFilter.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("city", etCityFilter.getText().toString());
            resultIntent.putExtra("min_price", minPrice);
            resultIntent.putExtra("max_price", maxPrice);
            resultIntent.putExtra("min_date", minTimestamp);
            resultIntent.putExtra("max_date", maxTimestamp);
            resultIntent.putExtra("solo_favoritos", cbSoloFavoritos.isChecked());
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void showDatePicker(boolean isMin) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            if (isMin) {
                minTimestamp = selected.getTimeInMillis();
                btnMinDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            } else {
                selected.set(Calendar.HOUR_OF_DAY, 23);
                selected.set(Calendar.MINUTE, 59);
                selected.set(Calendar.SECOND, 59);
                maxTimestamp = selected.getTimeInMillis();
                btnMaxDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        if (isMin && maxTimestamp != Long.MAX_VALUE) {
            dialog.getDatePicker().setMaxDate(maxTimestamp);
        } else if (!isMin && minTimestamp != 0) {
            dialog.getDatePicker().setMinDate(minTimestamp);
        }

        dialog.show();
    }
}
