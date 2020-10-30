package mani.itachi.memories.recyclerviewparts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ManikantaInugurthi on 03-02-2017.
 */

public class RecyclerViewEmptyExtdener extends RecyclerView {

    private final Context mContext;
    private View view;

    public RecyclerViewEmptyExtdener(Context context) {
        super(context);
        mContext = context;
    }

    public RecyclerViewEmptyExtdener(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public RecyclerViewEmptyExtdener(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public void setAdapter(final Adapter adapter) {
        super.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onChanged() {
                Log.d("item count:", adapter.getItemCount() + "");
                if (adapter.getItemCount() != 0) {
                    if (view != null) {
                        view.setVisibility(GONE);
                        setVisibility(VISIBLE);
                    }
                } else {
                    if (view != null) {
                        setVisibility(GONE);
                        view.setVisibility(VISIBLE);
                    }
                }
            }
        });
    }

    public void setEmptyView(View emptyView) {
        view = emptyView;
    }

}
