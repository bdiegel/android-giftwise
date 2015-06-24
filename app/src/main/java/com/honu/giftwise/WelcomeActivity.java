package com.honu.giftwise;

import android.os.Bundle;

import com.honu.aloha.BaseWelcomeActivity;
import com.honu.aloha.PageDescriptor;

/**
 * Created by bdiegel on 6/23/15.
 */
public class WelcomeActivity extends BaseWelcomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void createPages() {
        addPage(new PageDescriptor(R.string.welcome_header_0, R.string.welcome_content_0, R.drawable.welcome_image_0, R.color.welcome_color_0));
        addPage(new PageDescriptor(R.string.welcome_header_1, R.string.welcome_content_1, R.drawable.welcome_image_0, R.color.welcome_color_1));
        addPage(new PageDescriptor(R.string.welcome_header_2, R.string.welcome_content_2, R.drawable.welcome_image_0, R.color.welcome_color_2));
    }

}
