package com.snowball.mangareader;

import java.util.ArrayList;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TabHost.OnTabChangeListener;

import com.snowball.mangareader.db_code.DownloadListAdapter;
import com.snowball.mangareader.interface_code.CustomMenu;
import com.snowball.mangareader.interface_code.CustomMenu.OnMenuItemSelectedListener;
import com.snowball.mangareader.interface_code.CustomMenuItem;
import com.snowball.mangareader.interface_code.ExpandableHeightGridView;

public class Main extends TabActivity implements OnMenuItemSelectedListener {
	
	/** Some global vars **/
	public static CustomMenu mMenu;
	private final static int MENU_1 = 1;
	private final static int MENU_2 = 2;
	private final static int MENU_3 = 3;
	private final static int MENU_4 = 4;	
	
	public static TabHost tabhost;
	private TabHost.TabSpec spec;
	private Intent intent;		// Universal intent for tabs
	private View indicator; 	// Universal indicator for tabs
	public static String LIBRARY_TAG;
	public static String SEARCH_TAG;
	
	public static DownloadListAdapter adapter;
	
	private ArrayList<CustomMenuItem> menuItems;
	
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        getWindow().setWindowAnimations(0);
        
        tabhost = getTabHost();
        
        /** Add library tab **/
        intent = new Intent().setClass(this, LibraryActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        indicator = getLayoutInflater().inflate(R.layout.library_tab_indicator, null);
        spec = tabhost.newTabSpec("library").setIndicator(indicator).setContent(intent);
        tabhost.addTab(spec);
        LIBRARY_TAG = spec.getTag();
        
        /** Add search tab **/
        intent = new Intent().setClass(this, SearchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        indicator = getLayoutInflater().inflate(R.layout.search_tab_indicator, null);
        spec = tabhost.newTabSpec("search").setIndicator(indicator).setContent(intent);
        tabhost.addTab(spec);
        SEARCH_TAG = spec.getTag();
        
        /** Add custom options menu **/
        mMenu = new CustomMenu(this, this, getLayoutInflater());
        mMenu.setHideOnSelect(true);
        mMenu.setItemsPerLineInLandscapeOrientation(4);
        mMenu.setItemsPerLineInPortraitOrientation(4);
        loadMenuItems();
        
        /** Add OnTabChangedListener **/
        tabhost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabTag) {
				if (mMenu.isShowing()) {
					mMenu.hide();
				}
				if (tabTag == LIBRARY_TAG) {
					if (StaticValues.numColumnsLibrary == 2) {
						menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_19_small);
					} else {
						menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_14_small);
					}
				} else if (tabTag == SEARCH_TAG) {
					if (StaticValues.numColumnsSearch == 2) {
						menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_19_small);
					} else {
						menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_14_small);
					}
				}
			}
		});
    }
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && !StaticValues.libraryDetailShow) {
			doMenu();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && mMenu.isShowing()) {
			mMenu.hide();
			StaticValues.isMenuShowing = false;
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && StaticValues.libraryDetailShow && !Main.mMenu.isShowing()) {
			/** Some strange bug, sometimes it just doesn't focus correctly **/
			Intent i = new Intent(this, Main.class);
			startActivity(i);
			finish();
			StaticValues.libraryDetailShow = false;
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && tabhost.getCurrentTabTag() == SEARCH_TAG && tabhost.getCurrentView().findViewById(R.id.main_search_advanced_outer).getVisibility() == View.VISIBLE) {
			tabhost.getCurrentView().findViewById(R.id.main_search_advanced_btn).performClick();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && tabhost.getCurrentTabTag() == SEARCH_TAG) {
			tabhost.setCurrentTabByTag(LIBRARY_TAG);
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	private void doMenu() {
		if (mMenu.isShowing()) {
			mMenu.hide();
			StaticValues.isMenuShowing = false;
		} else {
			mMenu.show(findViewById(R.id.main_layout));
			StaticValues.isMenuShowing = true;
		}		
	}

	private void loadMenuItems() {
		menuItems = new ArrayList<CustomMenuItem>();
		CustomMenuItem cmi = new CustomMenuItem();
		cmi.setImageResourceId(R.drawable.menu_buttons_19_small);
		cmi.setId(MENU_1);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setImageResourceId(R.drawable.menu_buttons_15_small);
		cmi.setId(MENU_2);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setImageResourceId(R.drawable.menu_buttons_16_small);
		cmi.setId(MENU_3);
		menuItems.add(cmi);
		cmi = new CustomMenuItem();
		cmi.setImageResourceId(R.drawable.menu_buttons_17_small);
		cmi.setId(MENU_4);
		menuItems.add(cmi);
		if (!mMenu.isShowing()) {
			try {
				mMenu.setMenuItems(menuItems);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void MenuItemSelectedEvent(CustomMenuItem selection) {
		if (selection.getId() == MENU_1) {
			if (tabhost.getCurrentTabTag() == LIBRARY_TAG) {
				ExpandableHeightGridView grid = (ExpandableHeightGridView) getCurrentActivity().findViewById(R.id.main_library_book_grid);
				if (StaticValues.numColumnsLibrary == 2) {
					grid.setNumColumns(1);
					StaticValues.numColumnsLibrary = 1;
					menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_14_small);
				} else {
					grid.setNumColumns(2);
					StaticValues.numColumnsLibrary = 2;
					menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_19_small);
				}
			} else if (tabhost.getCurrentTabTag() == SEARCH_TAG) {
				ExpandableHeightGridView grid = (ExpandableHeightGridView) tabhost.getCurrentView().findViewById(R.id.main_search_list);
				if (StaticValues.numColumnsSearch == 2) {
					grid.setNumColumns(1);
					StaticValues.numColumnsSearch = 1;
					menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_14_small);
				} else {
					grid.setNumColumns(2);
					StaticValues.numColumnsSearch = 2;
					menuItems.get(0).setImageResourceId(R.drawable.menu_buttons_19_small);
				}
			}
		} else if (selection.getId() == MENU_3) {
			makeDownloadPopup();
			StaticValues.download_popup = true;
		}
	}

	private void makeDownloadPopup() {
		// Grab GUI
		LayoutInflater popupInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		View popupView = popupInflater.inflate(R.layout.download_screen, null);
		ExpandableHeightGridView mGrid = (ExpandableHeightGridView) popupView.findViewById(R.id.download_screen_list);
		final ScrollView mScroll = (ScrollView) popupView.findViewById(R.id.download_screen_popup_shadow);
		
		// Populate GUI
		MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchAllDownloads();
		if (MangaReader.mDownloadsCursor.moveToFirst()) {
			adapter = new DownloadListAdapter(this, MangaReader.mDownloadsCursor);
			mGrid.setAdapter(adapter);
			mGrid.setExpanded(true);
		}
		// Make window
		final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				StaticValues.download_popup = false;
			}
		});
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAtLocation(popupView, Gravity.LEFT | Gravity.TOP, 0, 0);
		// ScrollView to top (some strange bug)
		mScroll.post(new Runnable() {
			@Override
			public void run() {
				mScroll.scrollTo(0, 0);
			}
		});
	}
	
	public static void notifyAdapters(String bookTable) {
		if (LibraryActivity.adapter != null) {
			MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchLibraryBase();
			MangaReader.mBaseCursor.moveToFirst();
			LibraryActivity.adapter.setCursor(MangaReader.mBaseCursor);
			LibraryActivity.adapter.notifyDataSetChanged();
		}
		if (SearchActivity.adapter != null) {
			MangaReader.mBaseCursor = MangaReader.mDbHelper.fetchSearchBase();
			MangaReader.mBaseCursor.moveToFirst();
			SearchActivity.adapter.setCursor(MangaReader.mBaseCursor);
			SearchActivity.adapter.notifyDataSetChanged();
		}
		if (SearchActivity.adapter_popup != null) {
			MangaReader.mChapterCursor = MangaReader.mDbHelper
					.fetchChapters(bookTable);
			SearchActivity.adapter_popup.setCursor(MangaReader.mChapterCursor);
			SearchActivity.adapter_popup.notifyDataSetChanged();
		}
		if (BookActivity.adapter != null) {
			MangaReader.mChapterCursor = MangaReader.mDbHelper
					.fetchChapters(bookTable);
			BookActivity.adapter.setCursor(MangaReader.mChapterCursor);
			BookActivity.adapter.notifyDataSetChanged();
		}
		if (Main.adapter != null) {
			MangaReader.mDownloadsCursor = MangaReader.mDbHelper.fetchAllDownloads();
			Main.adapter.setCursor(MangaReader.mDownloadsCursor);
			Main.adapter.notifyDataSetChanged();
		}
	}
}