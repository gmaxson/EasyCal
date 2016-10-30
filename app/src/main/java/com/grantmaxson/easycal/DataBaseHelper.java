package com.grantmaxson.easycal;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

class DataBaseHelper extends SQLiteOpenHelper
{
	private static final String DB_PATH = "notes.sqlite";
	SQLiteDatabase myDataBase;

	DataBaseHelper(Context context)
	{
		super(context, DB_PATH, null, 1);
		boolean dbexist = checkdatabase();
		if (dbexist)
		{
			openDatabase();
		}
		else
		{
			createDatabase();
		}
	}

	private void createDatabase() throws SQLException
	{
		myDataBase = this.getWritableDatabase();
	}

	private boolean checkdatabase()
	{
		try
		{
			this.getWritableDatabase();
		}
		catch (SQLiteException e)
		{
			return false;
		}
		return true;
	}

	private void openDatabase() throws SQLException
	{
		myDataBase = this.getWritableDatabase();
	}

	public synchronized void close()
	{
		if (myDataBase != null)
		{
			myDataBase.close();
		}
		super.close();
	}

	public void onCreate(SQLiteDatabase arg0)
	{
	}

	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
	{
	}
}