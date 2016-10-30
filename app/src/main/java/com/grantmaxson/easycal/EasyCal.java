package com.grantmaxson.easycal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EasyCal extends Activity
{
    private DataBaseHelper dbNotes;
    private Calendar selectedDate = null, centerDate = null;
    private TextView lblSelectedDate;
    private Button btnReturnToday;
    private CustomEditText txtNotes;
    private LinearLayout navButtons;
    private InputMethodManager imm;
    private final TextView[] dayLabels = new TextView[7];
    private final Button[] dayButtons = new Button[7];
    private final String[] daysOfWeekShort = new String[]{"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private final SimpleDateFormat selectedDateFormatter = new SimpleDateFormat("EEEE MMMM d, yyyy");
    private final SimpleDateFormat sqlDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_cal);
        dbNotes = new DataBaseHelper(this.getApplicationContext());

        try
        {
            String GET_TABLES = "SELECT * FROM sqlite_master WHERE type='table' AND name='Notes'";
            String CREATE_DB = "CREATE TABLE Notes ( date DATE PRIMARY KEY, note VARCHAR )";
            Cursor tables = sqlQuery(GET_TABLES, new String[0]);
            if (tables.getCount() == 0)
            {
                sqlAction(CREATE_DB, new String[0]);
            }
            tables.close();
        }
        catch (SQLException e)
        {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage("There was an error building the tables needed");
            dlgAlert.setTitle("ERROR");
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
            finish();
        }

        dayLabels[0] = (TextView) findViewById(R.id.lblLess3);
        dayLabels[1] = (TextView) findViewById(R.id.lblLess2);
        dayLabels[2] = (TextView) findViewById(R.id.lblLess1);
        dayLabels[3] = (TextView) findViewById(R.id.lblDay0);
        dayLabels[4] = (TextView) findViewById(R.id.lblMore1);
        dayLabels[5] = (TextView) findViewById(R.id.lblMore2);
        dayLabels[6] = (TextView) findViewById(R.id.lblMore3);

        dayButtons[0] = (Button) findViewById(R.id.btnLess3);
        dayButtons[1] = (Button) findViewById(R.id.btnLess2);
        dayButtons[2] = (Button) findViewById(R.id.btnLess1);
        dayButtons[3] = (Button) findViewById(R.id.btnDay0);
        dayButtons[4] = (Button) findViewById(R.id.btnMore1);
        dayButtons[5] = (Button) findViewById(R.id.btnMore2);
        dayButtons[6] = (Button) findViewById(R.id.btnMore3);

        Button btnLeft = (Button) findViewById(R.id.btnLeft);
        Button btnRight = (Button) findViewById(R.id.btnRight);
        btnReturnToday = (Button) findViewById(R.id.btnReturnToday);
        txtNotes = (CustomEditText) findViewById(R.id.txtNotes);
        lblSelectedDate = (TextView) findViewById(R.id.lblSelectedDate);
        navButtons = (LinearLayout) findViewById(R.id.navButtons);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        txtNotes.setOnEditTextImeBackListener(new CustomEditText.OnEditTextImeBackListener()
        {
            public void onImeBack(CustomEditText ctrl)
            {
                imm.hideSoftInputFromWindow(ctrl.getWindowToken(), 0);
                delayedReveal(100);
            }
        });

        txtNotes.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                navButtons.setVisibility(LinearLayout.GONE);
                txtNotes.setCursorVisible(true);
                btnReturnToday.setText(R.string.save_btn);
            }
        });

        txtNotes.setOnLongClickListener(new OnLongClickListener()
        {
            public boolean onLongClick(View v)
            {
                navButtons.setVisibility(LinearLayout.GONE);
                txtNotes.setCursorVisible(true);
                btnReturnToday.setText(R.string.save_btn);
                imm.showSoftInput(txtNotes, 0);
                return false;
            }
        });

        for(int i = 0; i < dayButtons.length; i++)
        {
            final int offset = i-3;
            dayButtons[i].setOnClickListener(new OnClickListener()
            {
                public void onClick(View v)
                {
                    Calendar cal = (Calendar) centerDate.clone();
                    cal.add(Calendar.DATE, offset);
                    setNewDay(cal);
                }
            });
        }

        btnLeft.setOnClickListener(new OnClickListener()
        {
            public void onClick(View arg0)
            {
                centerDate.add(Calendar.DATE, -7);
                setNewDay(centerDate);
            }
        });

        btnRight.setOnClickListener(new OnClickListener()
        {
            public void onClick(View arg0)
            {
                centerDate.add(Calendar.DATE, 7);
                setNewDay(centerDate);
            }
        });

        btnReturnToday.setOnClickListener(new OnClickListener()
        {
            public void onClick(View arg0)
            {
                if (btnReturnToday.getText().equals("Save"))
                {
                    setNewDay(selectedDate);
                    Toast toast = Toast.makeText(getApplicationContext(), "Notes saved", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 225);
                    toast.show();
                }
                else
                {
                    centerDate = Calendar.getInstance();
                    setNewDay(centerDate);
                }
            }
        });
    }

    protected void onResume()
    {
        centerDate = Calendar.getInstance();
        setNewDay(centerDate);
        delayedReveal(50);
        super.onResume();
    }

    protected void onPause()
    {
        setNewDay(selectedDate);
        super.onPause();
    }

    protected void onDestroy()
    {
        dbNotes.close();
        super.onDestroy();
    }

    private void delayedReveal(long duration)
    {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                navButtons.setVisibility(LinearLayout.VISIBLE);
            }
        }, duration);
    }

    private void setNewDay(Calendar newDate)
    {
        if (selectedDate != null)
        {
            String text = txtNotes.getText().toString();
            if (!"".equals(text))
            {
                sqlUpdate(selectedDate, text);
            }
            else
            {
                sqlDelete(selectedDate);
            }
        }

        selectedDate = (Calendar) newDate.clone();

        if (centerDate == null)
        {
            centerDate = Calendar.getInstance();
        }

        Calendar tmpDate = (Calendar) centerDate.clone();
        tmpDate.add(Calendar.DATE, -3);

        lblSelectedDate.setText(selectedDateFormatter.format(selectedDate.getTime()));

        int noNotesColor = 0xFFBBBBBB;
        int hasNotesColor = 0xFF00AAFF;

        for(int i = 0; i < dayButtons.length; i++)
        {
            dayButtons[i].setText(String.format(Locale.getDefault(), "%d", tmpDate.get(Calendar.DAY_OF_MONTH)));
            dayLabels[i].setText(daysOfWeekShort[tmpDate.get(Calendar.DAY_OF_WEEK)]);
            if (!"".equals(sqlSelect(tmpDate)))
            {
                dayButtons[i].setTextColor(hasNotesColor);
            }
            else
            {
                dayButtons[i].setTextColor(noNotesColor);
            }
            tmpDate.add(Calendar.DATE, 1);
        }

        btnReturnToday.setText(R.string.today_btn);
        txtNotes.setCursorVisible(false);
        txtNotes.setText(sqlSelect(selectedDate));
    }

    private String sqlSelect(Calendar date)
    {
        String SELECT = "SELECT note FROM Notes WHERE date = ?";
        Cursor cu = sqlQuery(SELECT, new String[]{sqlDateFormatter.format(date.getTime())});
        String toReturn = cu.moveToFirst() ? cu.getString(0) : "";
        cu.close();
        return toReturn;
    }

    private void sqlUpdate(Calendar date, String notes)
    {
        String UPDATE = "UPDATE Notes SET note = ? WHERE date = ?";
        String GET_CHANGES = "SELECT changes()";
        String INSERT = "INSERT INTO Notes (date, note) VALUES (?,?)";
        sqlAction(UPDATE, new String[]{notes, sqlDateFormatter.format(date.getTime())});
        Cursor cu = sqlQuery(GET_CHANGES, new String[]{});
        if (cu.moveToFirst() && cu.getInt(0) == 0)
        {
            sqlAction(INSERT, new String[]{sqlDateFormatter.format(date.getTime()), notes});
        }
        cu.close();
    }

    private void sqlDelete(Calendar date)
    {
        String DELETE = "DELETE FROM Notes WHERE date = ?";
        sqlAction(DELETE, new String[]{sqlDateFormatter.format(date.getTime())});
    }

    protected Cursor sqlQuery(String query, String[] args)
    {
        try
        {
            return dbNotes.myDataBase.rawQuery(query, args);
        }
        catch (SQLException e)
        {
            return null;
        }
    }

    private void sqlAction(String query, String[] args)
    {
        dbNotes.myDataBase.execSQL(query, args);
    }
}