package com.android.gudana.BootNavigation;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottomnavigation.FloatingActionButtonBehavior;

import static android.util.Log.INFO;
import static android.util.Log.VERBOSE;
import static it.sephiroth.android.library.bottomnavigation.MiscUtils.log;

import com.android.gudana.BuildConfig;
import com.android.gudana.R;
import com.android.gudana.GuDFeed.GuDFeed_Fragment;
import com.android.gudana.linphone.HistoryListFragment;
// import com.android.rivchat.MainActivity_NavDrawer;


@TargetApi (Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends BaseActivity implements BottomNavigation.OnMenuItemSelectionListener {

    static final String TAG = MainActivity.class.getSimpleName();

    public static final int MENU_TYPE_3_ITEMS = 0;
    public static final int MENU_TYPE_3_ITEMS_NO_BACKGROUND = 1;

    public static final int MENU_TYPE_4_ITEMS = 2;
    public static final int MENU_TYPE_4_ITEMS_NO_BACKGROUND = 3;

    public static final int MENU_TYPE_5_ITEMS = 4;
    public static final int MENU_TYPE_5_ITEMS_NO_BACKGROUND = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BottomNavigation.DEBUG = BuildConfig.DEBUG;

        try{

            setContentView(getActivityLayoutResId());

        }catch(Exception ex){
            ex.printStackTrace();
        }
        final ViewGroup root = (ViewGroup) findViewById(R.id.CoordinatorLayout01);
        final CoordinatorLayout coordinatorLayout;
        if (root instanceof CoordinatorLayout) {
            coordinatorLayout = (CoordinatorLayout) root;
        } else {
            coordinatorLayout = null;
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        final int statusbarHeight = getStatusBarHeight();
        final boolean translucentStatus = hasTranslucentStatusBar();
        final boolean translucentNavigation = hasTranslucentNavigation();

        log(TAG, VERBOSE, "translucentStatus: %b", translucentStatus);

        if (translucentStatus) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
            params.topMargin = -statusbarHeight;

            params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = statusbarHeight;
        }

        if (translucentNavigation) {
            final ViewPager viewPager = getViewPager();
            if (null != viewPager) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
                params.bottomMargin = -getNavigationBarHeight();
            }
        }

        initializeBottomNavigation(savedInstanceState);
        initializeUI(savedInstanceState);
    }

    protected int getActivityLayoutResId() {return R.layout.bn_activity_main;}

    protected void initializeBottomNavigation(final Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            getBottomNavigation().setDefaultSelectedIndex(0);
            final BadgeProvider provider = getBottomNavigation().getBadgeProvider();
            provider.show(R.id.bbn_item3);
            provider.show(R.id.bbn_item4);
        }
    }

    protected void initializeUI(final Bundle savedInstanceState) {
        final FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (null != floatingActionButton) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final View root = findViewById(R.id.CoordinatorLayout01);
                    Snackbar snackbar = Snackbar.make(root, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction(
                            "Action",
                            null
                        );
                    snackbar.show();
                }
            });

            if (hasTranslucentNavigation()) {
                final ViewGroup.LayoutParams params = floatingActionButton.getLayoutParams();
                if (CoordinatorLayout.LayoutParams.class.isInstance(params)) {
                    CoordinatorLayout.LayoutParams params1 = (CoordinatorLayout.LayoutParams) params;
                    if (FloatingActionButtonBehavior.class.isInstance(params1.getBehavior())) {
                        ((FloatingActionButtonBehavior) params1.getBehavior()).setNavigationBarHeight(getNavigationBarHeight());
                    }
                }
            }
        }

        final ViewPager viewPager = getViewPager();
        if (null != viewPager) {

            getBottomNavigation().setOnMenuChangedListener(new BottomNavigation.OnMenuChangedListener() {
                @Override
                public void onMenuChanged(final BottomNavigation parent) {

                    viewPager.setAdapter(new ViewPagerAdapter(MainActivity.this, parent.getMenuItemCount()));
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(
                            final int position, final float positionOffset, final int positionOffsetPixels) { }

                        @Override
                        public void onPageSelected(final int position) {
                            if (getBottomNavigation().getSelectedIndex() != position) {
                                getBottomNavigation().setSelectedIndex(position, false);
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(final int state) { }
                    });
                }
            });

        }


        //   inflate menu   to 5 items   ... and change background color  ..
        BottomNavigation navigation = getBottomNavigation();
        if (null == navigation) {
            // return 0; ... do something to avoid  Exception   ...
        }else{
            navigation.inflateMenu(R.menu.bottombar_menu_5items);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bn_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.item1:
                return setMenuType(MENU_TYPE_3_ITEMS);
            case R.id.item2:
                return setMenuType(MENU_TYPE_3_ITEMS_NO_BACKGROUND);
            case R.id.item3:
                return setMenuType(MENU_TYPE_4_ITEMS);
            case R.id.item4:
                return setMenuType(MENU_TYPE_4_ITEMS_NO_BACKGROUND);
            case R.id.item5:
                return setMenuType(MENU_TYPE_5_ITEMS);
            case R.id.item6:
                return setMenuType(MENU_TYPE_5_ITEMS_NO_BACKGROUND);
            case R.id.item7:
                // start  Linphone Activity ...
                //startActivity(new Intent(this, MainActivity_NavDrawer.class));
                // startActivity(new Intent(this, MainActivityTablet.class));
                return true;
            case R.id.item8:
                startActivity(new Intent(this, MainActivityTabletCollapsedToolbar.class));
                return true;
            case R.id.item9:
                startActivity(new Intent(this, MainActivityCustomBehavior.class));
                return true;
            case R.id.item10:
                startActivity(new Intent(this, MainActivityCustomBadge.class));
                return true;
            case R.id.item11:
                startActivity(new Intent(this, MainActivityNoHide.class));
                return true;
            case R.id.item12:
                startActivity(new Intent(this, EnableDisableItemsActivity.class));
                return true;
            case R.id.item13:
                startActivity(new Intent(this, MainActivityNoCoordinator.class));
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public boolean setMenuType(final int type) {
        BottomNavigation navigation = getBottomNavigation();
        if (null == navigation) {
            return false;
        }

        switch (type) {
            case MENU_TYPE_3_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_3items);
                break;

            case MENU_TYPE_3_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_3items_no_background);
                break;

            case MENU_TYPE_4_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_4items);
                break;

            case MENU_TYPE_4_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_4items_no_background);
                break;

            case MENU_TYPE_5_ITEMS:
                navigation.inflateMenu(R.menu.bottombar_menu_5items);
                break;

            case MENU_TYPE_5_ITEMS_NO_BACKGROUND:
                navigation.inflateMenu(R.menu.bottombar_menu_5items_no_background);
                break;
        }

        return true;
    }

    @Override
    public void onMenuItemSelect(final int itemId, final int position, final boolean fromUser) {
        log(TAG, INFO, "onMenuItemSelect(" + itemId + ", " + position + ", " + fromUser + ")");
        if (fromUser) {
            getBottomNavigation().getBadgeProvider().remove(itemId);
            if (null != getViewPager()) {
                getViewPager().setCurrentItem(position);
            }
        }
    }

    @Override
    public void onMenuItemReselect(@IdRes final int itemId, final int position, final boolean fromUser) {
        log(TAG, INFO, "onMenuItemReselect(" + itemId + ", " + position + ", " + fromUser + ")");

        if (fromUser) {
            final FragmentManager manager = getSupportFragmentManager();
            GuDFeed_Fragment fragment = (GuDFeed_Fragment) manager.findFragmentById(R.id.fragment);
            if (null != fragment) {
                // fragment.scrollToTop();
            }
        }

    }

    public static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mCount;

        public ViewPagerAdapter(final AppCompatActivity activity, int count) {
            super(activity.getSupportFragmentManager());
            this.mCount = count;
        }

        @Override
        public Fragment getItem(final int position) {

            Log.d( "position ", String.valueOf(position));

            //fragments[3] = new HistoryListFragment();
            //fragments[4] = new ContactsListFragment();
            //fragments[5] = new DialerFragment();


            switch (position) {
                case 0:
                    //  chat fragment
                    return new GuDFeed_Fragment();

                case 1:
                    // problaby  call fragment
                    return new GuDFeed_Fragment();

                case 2:
                    // status share fragment
                    return new HistoryListFragment();

                case 3:
                    // VOIP frgament  real call and real SMS  ......  not free  domaine
                    //return new ContactsListFragment();

                case 4:
                    // user profil fragment
                    //return  new  DialerFragment();

            }

            return null;
        }

        @Override
        public int getCount() {
            return mCount;
        }
    }

}
