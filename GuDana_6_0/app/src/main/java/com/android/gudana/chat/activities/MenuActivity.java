package com.android.gudana.chat.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.gudana.R;
import com.android.gudana.chat.ChatApplication;
import com.android.gudana.chat.fragments.FriendsListFragment;
import com.android.gudana.chat.fragments.RoomsFragment;
import com.android.gudana.chat.model.User;
import com.android.gudana.chat.network.Logout;
import com.android.gudana.chat.utilities.CenteredImageSpan;


public class MenuActivity extends AppCompatActivity {
    private String username, session;
    private int user_id = -1;
    Fragment roomsFragment, friendsListFragment;
    private Intent intent;
    public CoordinatorLayout coordinatorLayout;
    ChatApplication chatApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_menu);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("GuDTalk");

        intent = getIntent();
        chatApplication = (ChatApplication) MenuActivity.this.getApplication();
        User user = chatApplication.getUser();
        username = user.getUsername();
        user_id = user.getUserID();
        session = user.getSession();

        roomsFragment = new RoomsFragment();
        friendsListFragment = new FriendsListFragment();
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt("user_id", user_id);
        fragmentArguments.putString("username", username);
        fragmentArguments.putString("session", session);
        roomsFragment.setArguments(fragmentArguments);
        friendsListFragment.setArguments(fragmentArguments);

        CollectionPagerAdapter mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_menu);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager_menu);
        mViewPager.setAdapter(mCollectionPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        if(chatApplication.isLoggedIn() && intent.getBooleanExtra("returning user", false)) {

            Snackbar.make(coordinatorLayout, "Signed in as " + username, Snackbar.LENGTH_LONG)
                    .setAction("Not you?", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Logout(MenuActivity.this, user_id, username, session);
                        }
                    }).show();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent ContactList = new Intent(MenuActivity.this, ContactListActivity.class);
                //startActivity(ContactList);
                showCreateRoomDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_menu_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            // NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_user_profile) {
            Intent userProfileIntent = new Intent(MenuActivity.this, UserProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(userProfileIntent);
        } else if(id == R.id.action_create_room) {
            showCreateRoomDialog();

            return true;
        } else if(id == R.id.action_logout) {
            new Logout(MenuActivity.this, user_id, username, session);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        // if (keyCode == KeyEvent.KEYCODE_BACK ) { new Logout(MenuActivity.this, user_id, username, session);return true; }

        return super.onKeyDown(keyCode, event);
    }

    public void showCreateRoomDialog() {
        AlertDialog.Builder createRoomDialog = new AlertDialog.Builder(MenuActivity.this);
        createRoomDialog.setTitle("Create room");

        final EditText input = new EditText(MenuActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        input.setLayoutParams(lp);
        input.setHint("Must be between 1-20 characters");

        createRoomDialog.setView(input);

        createRoomDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String room = input.getText().toString().trim();
                String fake_url_pic =  "https://image.flaticon.com/sprites/new_packs/145841-avatar-set.png";
                if(!room.isEmpty() && room.length() <= 20) {
                    //(new CreateRoomAsyncTask(MenuActivity.this, user_id, username, session, room , fake_url_pic)).execute();
                } else {
                    Toast.makeText(MenuActivity.this, "Room name must be between 1-20 characters long.", Toast.LENGTH_LONG).show();
                }
            }
        });

        createRoomDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        createRoomDialog.show();
    }

    public void unableToConnectSnackBar() {
        Snackbar.make(coordinatorLayout, "Unable to connect to server", Snackbar.LENGTH_LONG).show();
    }

    class CollectionPagerAdapter extends FragmentPagerAdapter {
        private static final int FRAGMENT_ROOMS = 0, FRAGMENT_FRIENDS = 1;

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case FRAGMENT_ROOMS:
                    return roomsFragment;
                case FRAGMENT_FRIENDS:
                    return friendsListFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable image;
            SpannableString sb;
            CenteredImageSpan imageSpan;

            // https://guides.codepath.com/android/google-play-style-tabs-using-tablayout
            try {
                switch (position) {
                    case FRAGMENT_ROOMS:
                        image = MenuActivity.this.getResources().getDrawable(R.mipmap.ic_room_black);
                        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                        sb = new SpannableString("  Rooms");
                        imageSpan = new CenteredImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return sb;
                    case FRAGMENT_FRIENDS:
                        image = MenuActivity.this.getResources().getDrawable(R.mipmap.ic_friend_black);
                        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                        sb = new SpannableString("  Friends");
                        CenteredImageSpan imageSpan2 = new CenteredImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                        sb.setSpan(imageSpan2, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return sb;
                    default:
                        return "Null";
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return "Null";
            }
        }
    }
}