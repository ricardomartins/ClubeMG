package pt.rikmartins.android.utilitarios.recyclerview;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
* Created by ricardo on 21-03-2015.
*/
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
//    private Cursor mCursor;
//
//    private boolean mDataValid;
//
//    private Context mContext;
//
//    private int mRowIDColumn;
//
//    private ChangeObserver mChangeObserver;
//
//    private MyDataSetObserver mDataSetObserver;
//
//    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;
//
//    public CursorRecyclerAdapter(Context context, Cursor c, int flags) {
//        init(context, c, flags);
//    }
//
//    void init(Context context, Cursor c, int flags) {
//        boolean cursorPresent = c != null;
//        mCursor = c;
//        mDataValid = cursorPresent;
//        mContext = context;
//        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
//        if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
//            mChangeObserver = new ChangeObserver();
//            mDataSetObserver = new MyDataSetObserver();
//        } else {
//            mChangeObserver = null;
//            mDataSetObserver = null;
//        }
//
//        if (cursorPresent) {
//            if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
//            if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
//        }
//    }
//
//    public void changeCursor(Cursor cursor) {
//        Cursor old = swapCursor(cursor);
//        if (old != null) {
//            old.close();
//        }
//    }
//
//    public Cursor swapCursor(Cursor newCursor) {
//        if (newCursor == mCursor) {
//            return null;
//        }
//        Cursor oldCursor = mCursor;
//        if (oldCursor != null) {
//            if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
//            if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
//        }
//        mCursor = newCursor;
//        if (newCursor != null) {
//            if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
//            if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
//            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
//            mDataValid = true;
//            // notify the observers about the new cursor
//            notifyDataSetChanged();
//        } else {
//            mRowIDColumn = -1;
//            mDataValid = false;
//            // notify the observers about the lack of a data set
//            notifyDataSetInvalidated();
//        }
//        return oldCursor;
//    }






    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

//    private class ChangeObserver extends ContentObserver {
//        public ChangeObserver() {
//            super(new Handler());
//        }
//
//        @Override
//        public boolean deliverSelfNotifications() {
//            return true;
//        }
//
//        @Override
//        public void onChange(boolean selfChange) {
//            onContentChanged();
//        }
//    }
//
//    private class MyDataSetObserver extends DataSetObserver {
//        @Override
//        public void onChanged() {
//            mDataValid = true;
//            notifyDataSetChanged();
//        }
//
//        @Override
//        public void onInvalidated() {
//            mDataValid = false;
//            notifyDataSetInvalidated();
//        }
//    }
}
