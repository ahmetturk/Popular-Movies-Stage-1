package com.example.ahmet.popularmovies.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.ahmet.popularmovies.R;

public class GridItemDecoration extends RecyclerView.ItemDecoration {

    private Context mContext;

    public GridItemDecoration(Context context) {
        this.mContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager.LayoutParams layoutParams
                = (GridLayoutManager.LayoutParams) view.getLayoutParams();

        int position = layoutParams.getViewLayoutPosition();
        if (position == RecyclerView.NO_POSITION) {
            outRect.set(0, 0, 0, 0);
            return;
        }

        int itemDivider = mContext.getResources().getDimensionPixelSize(R.dimen.grid_item_divider);

        int itemSpanIndex = layoutParams.getSpanIndex();

        if (itemSpanIndex == 0) {
            outRect.left = itemDivider;
            outRect.top = itemDivider;
            outRect.right = itemDivider;
            outRect.bottom = 0;
        } else {
            outRect.left = 0;
            outRect.top = itemDivider;
            outRect.right = itemDivider;
            outRect.bottom = 0;
        }
    }
}
