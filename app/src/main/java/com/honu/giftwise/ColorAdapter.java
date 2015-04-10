package com.honu.giftwise;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.honu.giftwise.data.GiftwiseContract;


public class ColorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ColorAdapter.class.getSimpleName();

    private static LinearLayout.LayoutParams layoutParams;


    public ColorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        int dp24 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24, context.getResources().getDisplayMetrics());
        int dp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4, context.getResources().getDisplayMetrics());

        layoutParams = new LinearLayout.LayoutParams(dp24, dp24);
        layoutParams.rightMargin = dp4;
        layoutParams.gravity = Gravity.BOTTOM;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        // create circle drawable
        Drawable circle = context.getResources().getDrawable(R.drawable.shape_circle);
        LayerDrawable coloredCircle = new LayerDrawable(new Drawable[]{circle});

        // create image view from circle
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(coloredCircle);
        imageView.setLayoutParams(layoutParams);

        // use ViewHolder to save inflated views:
        ViewHolder viewHolder = new ViewHolder(imageView);
        imageView.setTag(viewHolder);

        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int color = cursor.getInt(cursor.getColumnIndex(GiftwiseContract.ColorEntry.COLUMN_COLOR_VALUE));
        ImageView imageView = viewHolder.iconView;
        imageView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static class ViewHolder {
        public final ImageView iconView;

        public ViewHolder(final View view) {
            iconView = (ImageView) view;
        }
    }
}
