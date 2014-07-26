package edu.alaska.gina.feeder.android.slideshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.spicelist.simple.BitmapSpiceManager;

import edu.alaska.gina.feeder.android.slideshow.network.JsonSpiceService;

/**
 * Feeder automated slideshow application.
 */
public class Show extends Activity {
    private String baseURL = "http://feeder-web-dev.x.gina.alaska.edu/feeds/snpp-day-night-band/";

    private WebView webContent;
    private View settingsButton;
    private SpiceManager jsonManager = new SpiceManager(JsonSpiceService.class);
    private BitmapSpiceManager imageManager = new BitmapSpiceManager();

    private Handler hideSysUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onCreate");
        setContentView(R.layout.activity_show);

        this.webContent = (WebView) findViewById(R.id.contentView);
        this.settingsButton = this.findViewById(R.id.settingsButton);

        this.webContent.getSettings().setJavaScriptEnabled(true);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    showUI();
                }
            }
        });

        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIDDialogue d = new UIDDialogue();
                d.show(getFragmentManager(), "uid_dialogue");
            }
        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.log_tag) + "-ui", "Click event registered.");
                if (settingsButton.getVisibility() == View.VISIBLE) {
                    hideUI();
                } else {
                    showUI();
                }
            }
        };
        this.webContent.setOnClickListener(clickListener);

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);

        SharedPreferences setting = getPreferences(0);
        if (setting.getString(getString(R.string.code_pref), "").equals("")) {
            UIDDialogue d = new UIDDialogue();
            d.show(getFragmentManager(), "uid_dialogue");
        } else {
            this.baseURL = getString(R.string.base_url) + setting.getString(getString(R.string.code_pref), "").toLowerCase() + "/carousel";
            this.webContent.loadUrl(baseURL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onResume");
        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onWindowFocusChanged");

        if (hasFocus) {
            delayedHide();
        } else {
            hideSysUIHandler.removeMessages(0);
        }
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
        getWindow().getDecorView().setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

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
    protected void onStop() {
        super.onStop();
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onStop");
        this.hideSysUIHandler.removeMessages(0);
    }

    //TODO Make dialog Fragment Static so we can remove the following line
    @SuppressLint("ValidFragment")
    public class UIDDialogue extends DialogFragment {
        private View v;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (jsonManager.isStarted())
                jsonManager.shouldStop();
            if (imageManager.isStarted())
                imageManager.shouldStop();

            v = getActivity().getLayoutInflater().inflate(R.layout.uid_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Enter Code:").setView(v)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (v.findViewById(R.id.newUID) != null) {
                                String s = ((EditText) v.findViewById(R.id.newUID)).getText().toString();
                                baseURL = getString(R.string.base_url) + s + "/carousel";
                                SharedPreferences.Editor setting = getPreferences(0).edit();
                                setting.putString(getString(R.string.code_pref), s).apply();
                            }
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    webContent.loadUrl(baseURL);
                                }
                            });
                            delayedHide();
                            dismiss();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    webContent.loadUrl(baseURL);
                                }
                            });
                            delayedHide();
                            dismiss();
                        }
                    }).setCancelable(false);
            return builder.create();
        }
    }
}
