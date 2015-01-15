package pt.rikmartins.clubemg.clubemgandroid.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.PeriodicSync;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import pt.rikmartins.clubemg.clubemgandroid.accounts.GenericAccountService;
import pt.rikmartins.clubemg.clubemgandroid.provider.NoticiaContract;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static final String TAG = SyncUtils.class.getSimpleName();
    private static final String DEFAULT_SYNC_FREQUENCY = "86400";  // 1 dia (em segundos)
    private static final String CONTENT_AUTHORITY = NoticiaContract.CONTENT_AUTHORITY;
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    // Value below must match the account type specified in res/xml/syncadapter.xml
    public static final String ACCOUNT_TYPE = "pt.rikmartins.clubemg.clubemgandroid.account";

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void CreateSyncAccount(Context context) {
        Log.i(TAG, "CreateSyncAccount");
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        Account account = GenericAccountService.GetAccount(ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, CONTENT_AUTHORITY, 1);

            UpdateSyncPeriod(context, account, CONTENT_AUTHORITY);

            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            TriggerRefresh();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    public static boolean UpdateSyncPeriod(Context context){
        return UpdateSyncPeriod(context, GenericAccountService.GetAccount(ACCOUNT_TYPE), CONTENT_AUTHORITY);
    }

    private static boolean UpdateSyncPeriod(Context context, Account account, String authority){
        List<PeriodicSync> periodicSyncs = ContentResolver.getPeriodicSyncs(account, authority);

        long old_sync_frequency = periodicSyncs.isEmpty() ? -1 : periodicSyncs.get(0).period;
        long new_sync_frequency = Long.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString("sync_frequency", DEFAULT_SYNC_FREQUENCY));

        return UpdateSyncPeriod(account, authority, old_sync_frequency, new_sync_frequency);
    }

    private static boolean UpdateSyncPeriod(Account account, String authority, long old_sync_frequency, long new_sync_frequency){
        if (new_sync_frequency != old_sync_frequency) {
            if (new_sync_frequency != -1) {
                // Inform the system that this account is eligible for auto sync when the network is up
                ContentResolver.setSyncAutomatically(account, authority, true);
                // Recommend a schedule for automatic synchronization. The system may modify this based
                // on other scheduled syncs and network utilization.
                ContentResolver.addPeriodicSync(
                        account, authority, new Bundle(), new_sync_frequency);
            } else {
                // Inform the system that this account is not eligible for auto sync when the network is up
                ContentResolver.setSyncAutomatically(account, authority, false);
                ContentResolver.removePeriodicSync(account, authority, new Bundle());
            }
            return true;
        }
        return false;
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(ACCOUNT_TYPE), // Sync account
                NoticiaContract.CONTENT_AUTHORITY,                 // Content authority
                b);                                             // Extras
    }
}
