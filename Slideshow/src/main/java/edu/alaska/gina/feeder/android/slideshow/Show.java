package edu.alaska.gina.feeder.android.slideshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.spicelist.simple.BitmapSpiceManager;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.File;

import edu.alaska.gina.feeder.android.core.data.Entry;
import edu.alaska.gina.feeder.android.slideshow.network.JSONRequest;
import edu.alaska.gina.feeder.android.slideshow.network.JsonSpiceService;

/**
 * Feeder automated slideshow application.
 */
public class Show extends Activity {
    private String baseURL = "http://feeder-web-dev.x.gina.alaska.edu/feeds/snpp-day-night-band/entries.json";

    private ViewFlipper contentView;
    private TextSwitcher timestamp;
    private ImageView image1, image2;
    private View progressBar, settingsButton;
    private SpiceManager jsonManager = new SpiceManager(JsonSpiceService.class);
    private BitmapSpiceManager imageManager = new BitmapSpiceManager();
    private Entry[] contentData;

    private Thread timer = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d(getString(R.string.log_tag), "Timer reset.");
            timerThreadRunning = true;
            contentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(getString(R.string.log_tag), "Timer complete.");
                    timerDone = true;
                    flipView();
                }
            }, DurationInMillis.ONE_MINUTE / 2);
        }
    });

    private Handler hideSysUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideUI();
        }
    };

    /**
     * Image that is currently being downloaded.
     */
    private int current = 0;

    /**
     * Flags indicating whether the timer, download, and timer thread are running or complete.
     */
    private boolean timerDone = true, downloadDone = false, timerThreadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onCreate");
        setContentView(R.layout.activity_show);

        TextView text1 = (TextView) findViewById(R.id.textView), text2 = (TextView) findViewById(R.id.textView2);
        text1.setSelected(true);
        text2.setSelected(true);

        if (timerThreadRunning)
            this.timer.interrupt();

        this.timerDone = true;
        this.downloadDone = false;
        this.timerThreadRunning = false;

        this.contentView = (ViewFlipper) findViewById(R.id.flipper);
        this.timestamp = (TextSwitcher) findViewById(R.id.timestamp);
        this.image1 = (ImageView) this.contentView.findViewById(R.id.image1);
        this.image2 = (ImageView) this.contentView.findViewById(R.id.image2);
        this.progressBar = this.findViewById(R.id.loadingIndicator);
        this.settingsButton = this.findViewById(R.id.settingsButton);

        //this.contentView.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        this.contentView.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        this.timestamp.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        this.timestamp.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);

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
        this.image1.setOnClickListener(clickListener);
        this.image2.setOnClickListener(clickListener);
        this.timestamp.setOnClickListener(clickListener);

        SharedPreferences setting = getPreferences(0);
        if (setting.getString(getString(R.string.code_pref), "").equals("")) {
            UIDDialogue d = new UIDDialogue();
            d.show(getFragmentManager(), "uid_dialogue");
        } else {
            this.baseURL = getString(R.string.base_url) + setting.getString(getString(R.string.code_pref), "").toLowerCase() + "/entries.json";
            tryRequestNextImage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onResume");

        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);
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

    private String formatDateTime(DateTime img) {
        Period diff = new Period(img, new DateTime(System.currentTimeMillis()));
        PeriodFormatter formatter;

        if (diff.getYears() > 0) {
            formatter = new PeriodFormatterBuilder().appendYears().appendSuffix(" year ago.", " years ago.").toFormatter();
        } else if (diff.getMonths() > 0) {
            formatter = new PeriodFormatterBuilder().appendMonths().appendSuffix(" month ago.", " months ago.").toFormatter();
        } else if (diff.getWeeks() > 0) {
            formatter = new PeriodFormatterBuilder().appendWeeks().appendSuffix(" week ago.", " weeks ago.").toFormatter();
        } else if (diff.getDays() > 0) {
            formatter = new PeriodFormatterBuilder().appendDays().appendSuffix(" day ago.", " days ago.").toFormatter();
        } else if (diff.getHours() > 0) {
            formatter = new PeriodFormatterBuilder().appendHours().appendSuffix(" hour ago.", " hours ago.").toFormatter();
        } else if (diff.getMinutes() > 0) {
            formatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix(" minute ago.", " minutes ago.").toFormatter();
        } else {
            formatter = new PeriodFormatterBuilder().appendSeconds().appendSuffix(" second ago.", " seconds ago.").toFormatter();
        }

        return formatter.print(diff);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(getString(R.string.log_tag) + "-lifecycle", "onStop");

        if (jsonManager.isStarted())
            jsonManager.shouldStop();
        if (imageManager.isStarted())
            imageManager.shouldStop();

        if (timerThreadRunning) {
            this.timer.interrupt();
        }
    }

    private void flipView() {
        Log.d(getString(R.string.log_tag), "Starting flipView method.");
        if (this.progressBar.getVisibility() == View.VISIBLE) {
            Log.d(getString(R.string.log_tag), "ProgressBar removal starting.");
            this.progressBar.setAlpha(1f);
            this.progressBar.animate().alpha(0f)
                    .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            progressBar.setVisibility(View.GONE);
                            contentView.setVisibility(View.VISIBLE);
                            timestamp.setVisibility(View.VISIBLE);
                            Log.d(getString(R.string.log_tag), "ProgressBar is gone.");
                        }
                    });
        }

        Log.d(getString(R.string.log_tag), "timerDone = " + timerDone);
        Log.d(getString(R.string.log_tag), "downloadDone = " + downloadDone);

        if (this.timerDone && this.downloadDone) {
            DateTime stamp = this.contentData[current - 1].event_at;
            if (this.contentData[current - 1].highlight_description == null) {
                this.timestamp.setText(formatDateTime(stamp));
            } else {
                this.timestamp.setText(this.contentData[current - 1].highlight_description + " - " + formatDateTime(stamp));
            }
            this.contentView.showNext();

            this.timerDone = false;
            this.downloadDone = true;

            Log.d(getString(R.string.log_tag), "Flipping triggered & flags reset.");

            if (!timerThreadRunning) {
                Log.d(getString(R.string.log_tag), "Timer started");
                timer.start();
            } else {
                timer.run();
            }
            tryRequestNextImage();
        }
    }

    private void tryRequestNextImage() {
        if (!this.jsonManager.isStarted())
            this.jsonManager.start(this);
        if (!this.imageManager.isStarted())
            this.imageManager.start(this);

        if (contentData != null && current < contentData.length) {
            this.imageManager.execute(new BitmapRequest(this.contentData[current++].preview_url + "?size=2048x2048", new File(getCacheDir().getAbsolutePath() + "images.cache")), new ImageListener());
        } else {
            this.jsonManager.execute(new JSONRequest<Entry[]>(Entry[].class, baseURL), "slideshow_entries", DurationInMillis.ALWAYS_EXPIRED, new EntriesRequestListener());
            //TODO Figure out what was supposed to be on this line.
        }
    }

    private void loadIntoView(Bitmap bitmap) {
        if (this.image1.getVisibility() != View.VISIBLE) {
            this.image1.setImageBitmap(bitmap);
        } else {
            this.image2.setImageBitmap(bitmap);
        }
    }

    private class EntriesRequestListener implements RequestListener<Entry[]> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.log_tag), "Entries download fail!");
            tryRequestNextImage();
        }

        @Override
        public void onRequestSuccess(Entry[] entries) {
            contentData = entries;
            current = 0;
            tryRequestNextImage();
        }
    }

    private class ImageListener implements RequestListener<Bitmap> {
        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Log.d(getString(R.string.log_tag), "Image download fail!");
            tryRequestNextImage();
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            downloadDone = true;
            loadIntoView(bitmap);
            flipView();
        }
    }

    private class UIDDialogue extends DialogFragment {
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
                                baseURL += s + "/entries.json";
                                SharedPreferences.Editor setting = getPreferences(0).edit();
                                setting.putString(getString(R.string.code_pref), s).commit();
                            }
                            v.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (contentData != null)
                                        current = contentData.length;
                                    tryRequestNextImage();
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
                                    tryRequestNextImage();
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
