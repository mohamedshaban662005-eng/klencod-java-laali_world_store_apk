package com.laali.worldstore;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * لآلئ وورلد — تطبيق WebView مع شاشة بداية (Splash) عصرية.
 */
public class MainActivity extends Activity {

    private static final String START_URL = "https://laali-world-store.in-egypt.workers.dev/";

    // ألوان شاشة البداية (كريمي لؤلؤي + ذهبي)
    private static final int SPLASH_BG = 0xFFFAF6F0;
    private static final int GOLD = 0xFFB8935A;
    private static final int INK = 0xFF3B2F2A;
    private static final int MUTED = 0xFF9A8C82;

    private static final long MIN_SPLASH_MS = 1600;
    private static final long MAX_SPLASH_MS = 9000;

    private WebView web;
    private FrameLayout splash;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long splashStart;
    private boolean splashHidden = false;

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(SPLASH_BG);
        getWindow().setNavigationBarColor(SPLASH_BG);
        int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.WHITE);

        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));

        splash = buildSplash();
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));

        setContentView(root);

        splashStart = SystemClock.uptimeMillis();
        handler.postDelayed(this::hideSplash, MAX_SPLASH_MS); // أمان

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                String url = req.getUrl().toString();
                boolean external = !(url.startsWith("http://") || url.startsWith("https://"))
                        || url.contains("wa.me/")
                        || url.contains("api.whatsapp.com")
                        || url.contains("web.whatsapp.com");
                if (!external) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                long elapsed = SystemClock.uptimeMillis() - splashStart;
                handler.postDelayed(MainActivity.this::hideSplash,
                        Math.max(0, MIN_SPLASH_MS - elapsed));
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    v.loadData("<html><body style='font-family:sans-serif;text-align:center;"
                            + "padding-top:30%;direction:rtl;background:#FAF6F0;color:#3B2F2A'>"
                            + "<h2>تعذّر تحميل الصفحة</h2>"
                            + "<p>تحقق من اتصالك بالإنترنت ثم أعد المحاولة.</p></body></html>",
                            "text/html; charset=utf-8", "UTF-8");
                    hideSplash();
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient());

        if (state != null) {
            web.restoreState(state);
        } else {
            web.loadUrl(START_URL);
        }
    }

    // ============================================================
    // شاشة البداية
    // ============================================================
    private FrameLayout buildSplash() {
        FrameLayout layer = new FrameLayout(this);
        layer.setBackgroundColor(SPLASH_BG);
        layer.setClickable(true); // يمنع اللمس على الصفحة خلفها

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams colP = new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER);
        layer.addView(col, colP);

        // الشعار
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_icon);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        col.addView(logo, new LinearLayout.LayoutParams(dp(112), dp(112)));

        // الاسم العربي
        TextView nameAr = new TextView(this);
        nameAr.setText("لآلئ وورلد");
        nameAr.setTextColor(INK);
        nameAr.setTextSize(28);
        nameAr.setTypeface(Typeface.DEFAULT_BOLD);
        nameAr.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nameP = new LinearLayout.LayoutParams(-2, -2);
        nameP.topMargin = dp(22);
        col.addView(nameAr, nameP);

        // الاسم الإنجليزي
        TextView nameEn = new TextView(this);
        nameEn.setText("LA'ALI WORLD STORE");
        nameEn.setTextColor(GOLD);
        nameEn.setTextSize(12);
        nameEn.setLetterSpacing(0.28f);
        nameEn.setTypeface(Typeface.DEFAULT_BOLD);
        nameEn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams enP = new LinearLayout.LayoutParams(-2, -2);
        enP.topMargin = dp(6);
        col.addView(nameEn, enP);

        // الشعار النصي
        TextView tag = new TextView(this);
        tag.setText("عناية طبيعية فاخرة");
        tag.setTextColor(MUTED);
        tag.setTextSize(14);
        tag.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tagP = new LinearLayout.LayoutParams(-2, -2);
        tagP.topMargin = dp(14);
        col.addView(tag, tagP);

        // شريط تحميل رفيع أنيق
        FrameLayout track = new FrameLayout(this);
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setColor(0x26B8935A);
        trackBg.setCornerRadius(dp(3));
        track.setBackground(trackBg);
        track.setClipToOutline(true);
        LinearLayout.LayoutParams trackP = new LinearLayout.LayoutParams(dp(120), dp(3));
        trackP.topMargin = dp(40);
        col.addView(track, trackP);

        View bar = new View(this);
        GradientDrawable barBg = new GradientDrawable();
        barBg.setColor(GOLD);
        barBg.setCornerRadius(dp(3));
        bar.setBackground(barBg);
        track.addView(bar, new FrameLayout.LayoutParams(dp(44), dp(3)));

        // حركة الدخول
        View[] items = {logo, nameAr, nameEn, tag, track};
        for (int i = 0; i < items.length; i++) {
            View v = items[i];
            v.setAlpha(0f);
            v.setTranslationY(dp(14));
            v.animate().alpha(1f).translationY(0)
                    .setStartDelay(120L * i).setDuration(650)
                    .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        }

        // نبض خفيف للشعار
        ObjectAnimator px = ObjectAnimator.ofFloat(logo, View.SCALE_X, 1f, 1.06f);
        ObjectAnimator py = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 1f, 1.06f);
        for (ObjectAnimator a : new ObjectAnimator[]{px, py}) {
            a.setDuration(1100);
            a.setStartDelay(900);
            a.setRepeatCount(ObjectAnimator.INFINITE);
            a.setRepeatMode(ObjectAnimator.REVERSE);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.start();
        }

        // حركة الشريط الذهبي
        ObjectAnimator slide = ObjectAnimator.ofFloat(bar, View.TRANSLATION_X, 0f, dp(76));
        slide.setDuration(900);
        slide.setRepeatCount(ObjectAnimator.INFINITE);
        slide.setRepeatMode(ObjectAnimator.REVERSE);
        slide.setInterpolator(new AccelerateDecelerateInterpolator());
        slide.start();

        return layer;
    }

    private void hideSplash() {
        if (splashHidden) return;
        splashHidden = true;
        splash.animate().alpha(0f).setDuration(450).withEndAction(() -> {
            splash.setVisibility(View.GONE);
            // بعد الانتهاء: أشرطة النظام بيضاء لتناسب الموقع
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().setNavigationBarColor(Color.WHITE);
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) web.destroy();
        super.onDestroy();
    }
}