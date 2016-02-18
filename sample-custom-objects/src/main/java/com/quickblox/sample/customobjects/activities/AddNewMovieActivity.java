package com.quickblox.sample.customobjects.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.customobjects.R;
import com.quickblox.sample.customobjects.helper.DataHolder;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddNewMovieActivity extends BaseActivity implements TextWatcher {

    private static String SPACE = "\u0020";
    private static String OBJ = "\uFFFC";
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Spinner yearSpinner;
    private RatingBar ratingBar;
    private Toast toast;
    private String title, description, year;
    private float rating;

    public static void start(Context context) {
        Intent intent = new Intent(context, AddNewMovieActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);
        initUI();
        initSpinner();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(this, R.string.error, Toast.LENGTH_LONG);

        titleEditText = _findViewById(R.id.add_movie_title_textview);
        titleEditText.addTextChangedListener(this);

        descriptionEditText = _findViewById(R.id.add_movie_description_textview);
        descriptionEditText.addTextChangedListener(this);

        yearSpinner = _findViewById(R.id.year_spinner);
        ratingBar = _findViewById(R.id.add_movie_ratingBar);
    }

    private void initSpinner() {
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int firstFilmYear = 1895;
        for (int i = currentYear; i >= firstFilmYear; i--) {
            years.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);

        yearSpinner.setAdapter(adapter);
    }

    private void createNewMovie() {
        title = titleEditText.getText().toString();
        description = descriptionEditText.getText().toString();
        year = yearSpinner.getSelectedItem().toString();
        rating = ratingBar.getRating();

        if (!isValidData()) {
            return;
        }
        progressDialog.show();

        QBCustomObject qbCustomObject = QBCustomObjectsUtils.createCustomObject(title, description, year, rating);

        QBCustomObjects.createObject(qbCustomObject, new QBEntityCallback<QBCustomObject>() {
            @Override
            public void onSuccess(QBCustomObject qbCustomObject, Bundle bundle) {
                progressDialog.dismiss();
                Toaster.shortToast(R.string.done);
                DataHolder.getInstance().addMovieToList(qbCustomObject);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                Toaster.longToast(e.getErrors().toString());
            }
        });
    }

    private boolean isValidData() {

        if (title.startsWith(SPACE) || title.endsWith(SPACE)) {
            title = cropSpace(title);
        }
        if (description.startsWith(SPACE) || description.endsWith(SPACE)) {
            description = cropSpace(description);
        }
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            toast.setText(R.string.error_fields_is_empty);
            toast.show();
            return false;
        }
        if (rating == 0) {
            toast.setText(R.string.error_rating_is_empty);
            toast.show();
            return false;
        }
        return true;
    }

    private String cropSpace(String field) {
        while (field.startsWith(SPACE)) {
            field = field.substring(1);
        }
        while (field.endsWith(SPACE)) {
            field = field.substring(0, field.length() - 1);
        }
        return field;
    }

    @Override
    public void onPause() {
        super.onPause();
        toast.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_movie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save_movie:
                createNewMovie();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //ignore
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //ignore
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() >= getResources().getInteger(R.integer.field_max_length)) {
            toast.setText(R.string.error_too_long_text);
            toast.show();
        }
        if (s.toString().contains(OBJ)) {
            int index = s.toString().indexOf(OBJ);
            s.replace(index, index + 1, "");
        }
    }
}