package edu.alaska.gina.feeder.puffinfeeder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Fragment containing WebView that displays full-sized image.
 * Created by bobby on 7/1/13.
 */
public class ImageViewerFragment extends SherlockFragment{
    protected String image_url;
    protected String title;
    protected WebView image_frame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_viewer, container, false);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        image_frame = (WebView) getActivity().findViewById(R.id.fragment_feed_image_webview);
        image_frame.getSettings().setBuiltInZoomControls(true);
        image_frame.getSettings().setLoadWithOverviewMode(true);
        image_frame.getSettings().setUseWideViewPort(true);

        image_frame.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                getSherlockActivity().setSupportProgress(newProgress * 100);
                if (newProgress >= 0)
                    getSherlockActivity().setSupportProgressBarIndeterminate(false);
                if (newProgress >= 100) {
                    getSherlockActivity().setSupportProgressBarVisibility(false);
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                }
            }
        });

        Bundle extra = getArguments();
        if (extra != null) {
            image_url = extra.getString("image_url");
            title = extra.getString("bar_title");
        }
        else {
            Log.d("Puffin Feeder", "No Image URL. Please Fix that...");
            Toast.makeText(getActivity(), "No Image URL. Please fix that...", Toast.LENGTH_SHORT).show();
            return;
        }

        getSherlockActivity().getSupportActionBar().setTitle(title);

        getSherlockActivity().setSupportProgressBarVisibility(true);
        getSherlockActivity().setSupportProgressBarIndeterminate(true);
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        image_frame.loadUrl(image_url);
    }

    public void sharePic() {
        /*
        Picasso.with(getActivity().getApplicationContext()).load(Uri.parse(image_url)).skipCache().into(new Target() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                SavePhotoTask s = new SavePhotoTask();
                s.execute(stream.toByteArray());
            }

            @Override
            public void onError() {
                Toast.makeText(getActivity().getApplicationContext(), "FAIL", Toast.LENGTH_SHORT).show();
            }
        });

        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_TEXT, "Powered by GINA (gina.alaska.edu).");
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "puffinfeeder_temp.jpg")));
        startActivity(Intent.createChooser(share, "Share Image")); */

        startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("image/*").putExtra(Intent.EXTRA_STREAM, Uri.parse(image_url)), "Share Image"));
    }

    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... params) {
            File photo = new File(Environment.getExternalStorageDirectory(), "puffinfeeder_temp.jpg");

            if (photo.exists())
                photo.delete();

            try {
                FileOutputStream foo = new FileOutputStream(photo.getPath());

                foo.write(params[0]);
                foo.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
            return null;
        }
    }

    public File getTempFile(Context c, String url) {
        File file;
        try {
            String filename = Uri.parse(url).getLastPathSegment();
            file = File.createTempFile(filename, null, c.getCacheDir());
        } catch (Exception e) {
            e.getStackTrace();
            return null;
        }
        return file;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_image:
                sharePic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
