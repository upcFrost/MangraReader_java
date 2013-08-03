package com.snowball.mangareader;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.snowball.mangareader.db_code.DbAdapter;
import com.snowball.mangareader.db_code.LibraryGridAdapter;
import com.snowball.mangareader.interface_code.ExpandableHeightGridView;

public class LibraryActivity extends ActivityGroup {
	
	public static ExpandableHeightGridView gridview;
	public static LibraryGridAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_library_tab);
		
		/** Get interface **/
		gridview = (ExpandableHeightGridView) findViewById(R.id.main_library_book_grid);
		gridview.setExpanded(true);
		gridview.setNumColumns(StaticValues.numColumnsLibrary);
		
		MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchLibraryBase();
		MangaReader.mBaseCursor.moveToFirst();
		
		adapter = new LibraryGridAdapter(this, MangaReader.mBaseCursor);
		gridview.setAdapter(adapter);
		
		gridview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/** Close menu if showing **/
				if (Main.mMenu.isShowing()) {
					Main.mMenu.hide();
				}
				/** Load book screen **/
				Intent i = new Intent(getBaseContext(), BookActivity.class);
				i.putExtra("id", id);
				replaceContentView("Details", i);
				StaticValues.libraryDetailShow = true;
			}
		});
		
		registerForContextMenu(gridview);
		
		getWindow().setWindowAnimations(0);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.main_library_book_grid) {
		    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    MangaReader.mBaseCursor.moveToPosition(info.position);
		    menu.setHeaderTitle(MangaReader.mBaseCursor.getString(MangaReader.mBaseCursor.getColumnIndex(DbAdapter.KEY_TITLE)));
		    menu.add(Menu.NONE, 0, 0, "Show info");
		    menu.add(Menu.NONE, 1, 1, "Delete");
		  }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 0) {
			gridview.performItemClick(gridview, info.position, info.position);
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && StaticValues.libraryDetailShow && !Main.mMenu.isShowing()) {
			/** Here need to restart the main Main class - or focus, stack, etc. will be lost **/
			Intent i = new Intent(this, Main.class);
			startActivity(i);
			finish();
			StaticValues.libraryDetailShow = false;
			return true;
		} else {
			return this.getParent().onKeyDown(keyCode, event);
		}
	}
	
	public void replaceContentView(String id, Intent intent) {
		View view = getLocalActivityManager().startActivity(id, intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)).getDecorView();
		this.setContentView(view);
	}
	
}
