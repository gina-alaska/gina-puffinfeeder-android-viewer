package edu.alaska.gina.feeder.android.slideshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * Feeder automated slideshow application.
 */
public class Show extends Activity {
    private String baseURL = "http://feeder-web-dev.x.gina.alaska.edu/feeds/snpp-day-night-band/";

    private WebView webContent;
    private View settingsButton;
    private View progressBar;

    private Handler hideSysUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            hideUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onCreate");
        setContentView(R.layout.activity_show);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.webContent = (WebView) findViewById(R.id.contentView);
        this.settingsButton = this.findViewById(R.id.settingsButton);
        this.progressBar = findViewById(R.id.loadingIndicator);

        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIDDialog d = new UIDDialog();
                d.show(getFragmentManager(), "uid_dialogue");
            }
        });

        this.webContent.getSettings().setJavaScriptEnabled(true);
        this.webContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.animate().alpha(0f)
                        .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                progressBar.setVisibility(View.GONE);
                                webContent.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        final GestureDetector gd = new GestureDetector(this, new TouchDetector());
        this.webContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });

        SharedPreferences setting = getPreferences(0);
        if (setting.getString(getString(R.string.code_pref), "").equals("")) {
            UIDDialog d = new UIDDialog();
            d.show(getFragmentManager(), "uid_dialogue");
        } else {
            this.baseURL = getString(R.string.base_url) + setting.getString(getString(R.string.code_pref), "").toLowerCase() + "/carousel";
            this.webContent.loadUrl(baseURL);
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    showUI();
                    delayedHide();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUI();
        delayedHide();
    }

    private void delayedHide() {
        Log.d(getString(R.string.log_tag) + "-ui", "Sys UI Delayed Hide Start");
        this.hideSysUIHandler.removeMessages(0);
        this.hideSysUIHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void hideUI() {
        Log.d(getString(R.string.log_tag) + "-ui", "Sys UI Hidden");
        this.hideSysUIHandler.removeMessages(0);
        getWindow().getDecorView().setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        settingsButton.setAlpha(1f);
        settingsButton.animate().alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsButton.setVisibility(View.GONE);
                    }
                });
    }

    private void showUI() {
        Log.d(getString(R.string.log_tag) + "-ui", "Sys UI Shown");
        this.hideSysUIHandler.removeMessages(0);

        settingsButton.setVisibility(View.VISIBLE);
        settingsButton.setAlpha(0f);
        settingsButton.animate().alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsButton.setVisibility(View.VISIBLE);
                        delayedHide();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onStop");
        this.hideSysUIHandler.removeMessages(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hideSysUIHandler.removeMessages(0);
                break;
            case MotionEvent.ACTION_UP:
                delayedHide();
                break;
        }
        return super.onTouchEvent(event);
    }

    public static class UIDDialog extends DialogFragment {
        private View v;
        private String baseURL;

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.baseURL = getString(R.string.base_url);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            v = getActivity().getLayoutInflater().inflate(R.layout.uid_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Enter Code:").setView(v)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (v.findViewById(R.id.newUID) != null) {
                                String s = ((EditText) v.findViewById(R.id.newUID)).getText().toString();
                                ((Show) getActivity()).baseURL = baseURL + s + "/carousel";
                                SharedPreferences.Editor setting = getActivity().getPreferences(0).edit();
                                setting.putString(getString(R.string.code_pref), s).apply();
                            }
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((Show) getActivity()).webContent.loadUrl(((Show) getActivity()).baseURL);
                                }
                            });
                            ((Show) getActivity()).delayedHide();
                            dismiss();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((Show) getActivity()).webContent.loadUrl(((Show) getActivity()).baseURL);
                                }
                            });
                            ((Show) getActivity()).delayedHide();
                            dismiss();
                        }
                    }).setCancelable(false);
            return builder.create();
        }
    }

    private class TouchDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            delayedHide();
            return super.onDown(e);
        }
    }
}
