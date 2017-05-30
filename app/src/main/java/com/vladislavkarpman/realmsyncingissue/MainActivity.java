package com.vladislavkarpman.realmsyncingissue;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vladislavkarpman.realmsyncingissue.RealmModel.Car;
import com.vladislavkarpman.realmsyncingissue.RealmModel.Manufacture;
import com.vladislavkarpman.realmsyncingissue.RealmModel.Module;
import com.vladislavkarpman.realmsyncingissue.RealmModel.Owner;

import io.realm.ObjectServerError;
import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncSession;
import io.realm.SyncUser;

/**
 * Main Activity performs following steps: <p>
 * 1. Login by {@link #SYNC_URL_AUTH} to Realm Server Object using {@link SyncCredentials} populated with {@link #USER_NAME} and {@link #USER_PASSWORD}.<p>
 * 2. Get Realm instance using {@link #SYNC_URL_DB}.<p>
 * 3. Query {@link #cars}, {@link #manufactures}, {@link #owners}<p>
 * 4. Set listeners {@link #globalChangeListener}, {@link #carChangeListener}, {@link #manufactureChangeListener}, {@link #ownerChangeListener}<p>
 *
 * <b> Explanation of the {@link Realm} Syncing problem:</b><p>
 * After i did previous steps i wanted to check if i correctly receive notifications about changing of realm DB which come from Server.
 * And i faced with the following problems: <p>
 * 1. If i only set {@link #globalChangeListener} or {@link #manufactureChangeListener} or both of them i get all notifications, everything works good. <p>
 * 2. If i set only {@link #carChangeListener} or {@link #ownerChangeListener} or both of them
 * or with mix of {@link #globalChangeListener} and {@link #manufactureChangeListener} i stop getting all notifications and even more i stop getting data itself.
 * Everything stops to work.<p>
 * 3. If delete data from {@link Car} or {@link Owner} tables, everything start working good.<p>
 *
 * So, i come to a conclusion that something goes wrong if i set ChangeListeners on tables which have inverse connections,
 * in my example it's {@link Owner} and {@link Car} and at the same time i have pretty much data in those tables Realm Syncing stop working at all.<p>
 *
 * The path to {@link Realm} DB i use to test, as below:
 * .../app/src/main/assets/RealmSyncIssueDB.realm
 * <p>
 *
 * P.S. I can not reveal you real {@link SyncCredentials} due to privacy limitations, so in order to test what i described
 * you have to create your own Realm Sync Object and put realm DB.
 * <p>
 *
 * Contact me: vladislavkarpman@gmail.com
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RealmSyncing";

    /**
     * User name. It is used to create SyncCredentials
     * Example: vladkarpman@gmail.com
     */
    private static final String USER_NAME = "";

    /**
     * User password. It is used to create SyncCredentials
     * Example: 123456789
     */
    private static final String USER_PASSWORD = "";

    /**
     * URL for authentication.
     * Example: http://realm.vladkarpman.com:9080/auth
     */
    private static final String SYNC_URL_AUTH = "";

    /**
     * Database url.
     * Example: realm://realm.vladkarpman.com:9080/~/RealmSyncIssueDB
     */
    private static final String SYNC_URL_DB = "";

    /**
     * Realm object we get
     */
    private Realm realm;

    // Queried Realm Data
    private RealmResults<Car> cars;
    private RealmResults<Manufacture> manufactures;
    private RealmResults<Owner> owners;

    // Change Listeners
    private GlobalChangeListener globalChangeListener = new GlobalChangeListener();
    private OrderedRealmChangeListener<Car> carChangeListener = new OrderedRealmChangeListener<>("Cars");
    private OrderedRealmChangeListener<Manufacture> manufactureChangeListener = new OrderedRealmChangeListener<>("Manufactures");
    private OrderedRealmChangeListener<Owner> ownerChangeListener = new OrderedRealmChangeListener<>("Owners");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);

        SyncCredentials syncCredentials = SyncCredentials.usernamePassword(USER_NAME, USER_PASSWORD);
        SyncUser.loginAsync(syncCredentials, SYNC_URL_AUTH, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                onLoginSuccess(user);
            }

            @Override
            public void onError(ObjectServerError error) {
                onLoginError(error);
            }
        });
    }

    private void onLoginSuccess(SyncUser user) {
        // Building syncing configuration using received authorized user
        SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, SYNC_URL_DB)
                .errorHandler(new SyncSession.ErrorHandler() {
                    @Override
                    public void onError(SyncSession session, ObjectServerError error) {
                        Log.d(TAG, "onSyncSessionError: " + error.toString());
                    }
                })
                // waiting for until remote data arrives
                .waitForInitialRemoteData()
                .modules(new Module())
                .build();

        Realm.getInstanceAsync(syncConfiguration, new Realm.Callback() {
            @Override
            public void onSuccess(Realm realm) {
                onRealmInstanceReceived(realm);
            }
        });
    }

    private void onRealmInstanceReceived(Realm realm) {
        this.realm = realm;
        queryRealmData();
        setChangeListeners();
    }

    private void queryRealmData() {
        this.cars = this.realm.where(Car.class).findAllAsync();
        this.manufactures = this.realm.where(Manufacture.class).findAllAsync();
        this.owners = this.realm.where(Owner.class).findAllAsync();
    }

    private void setChangeListeners() {
        this.realm.addChangeListener(globalChangeListener);
        this.cars.addChangeListener(carChangeListener);
        this.manufactures.addChangeListener(manufactureChangeListener);
        this.owners.addChangeListener(ownerChangeListener);
    }

    private static void onLoginError(ObjectServerError error) {
        Log.d(TAG, "onLoginError: " + error.toString());
    }

    private static class GlobalChangeListener implements RealmChangeListener<Realm> {
        @Override
        public void onChange(Realm realm) {
            Log.d(TAG, "onGlobalChange: ");
        }
    }

    private static class OrderedRealmChangeListener<T extends RealmModel> implements OrderedRealmCollectionChangeListener<RealmResults<T>> {

        private final String name;

        private OrderedRealmChangeListener(String name) {
            this.name = name;
        }

        @Override
        public void onChange(RealmResults<T> realmResults, OrderedCollectionChangeSet changeSet) {
            if (changeSet != null) {
                for (OrderedCollectionChangeSet.Range range : changeSet.getChangeRanges()) {
                    Log.d(TAG, name + " onChange. Range startIndex = " + range.startIndex + ". Range length = " + range.length);
                }

                for (OrderedCollectionChangeSet.Range range : changeSet.getDeletionRanges()) {
                    Log.d(TAG, name + " onDelete. Range startIndex = " + range.startIndex + ". Range length = " + range.length);
                }

                for (OrderedCollectionChangeSet.Range range : changeSet.getInsertionRanges()) {
                    Log.d(TAG, name + " onInsert. Range startIndex = " + range.startIndex + ". Range length = " + range.length);
                }
            }
        }
    }
}
