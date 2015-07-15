package com.utils.ruialmeida.stickerapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.squareup.picasso.Picasso;
import com.utils.ruialmeida.stickerapp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity {

    public static final String ANDROID_RESOURCE = "android.resource://";
    public static final String FORESLASH = "/";
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Button m_btnSelectImage, m_btnSDeleteImage, m_btnZoom;
    private Context m_context;
    private LinearLayout m_llTopLayout;
    private ImageView m_ivImage, m_ivtmpImage;
    private Display m_screen;
    private int m_DisplayWidth, m_ImageCount, m_viewsAddedHeightEmotions = 0,
            m_height, m_absHeight = 0, m_AddedViewsHeightText = 0,
            m_deleteEditHeightwidth;
    private Dialog m_dialog;
    private OnTouchListener m_touchImagListener, m_strecthArrowListener;
    private AbsoluteLayout m_absolutelayout, m_absTextlayout, m_absZoomlayout;
    private int m_widthDelete = 0, m_totalTextViewCount = 0;
    private float m_oldDist = 1f, m_scale, m_oldX = 0, m_oldY = 0, m_dX, m_dY,
            m_posX, m_posY, m_prevX = 0, m_prevY = 0, m_newX, m_newY;
    ViewTreeObserver m_vtoTree;
    private AbsoluteLayout.LayoutParams m_layoutparams, m_layoutparamsDelete,
            m_layoutParamsEdit;
    private ArrayList<ViewsVo> m_arrSignObjects;
    private Bitmap m_bitmap;
    private Intent intent;
    private Button m_btnTakePhoto;
    private int m_heightDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_context = MainActivity.this;

        m_btnSelectImage = (Button) findViewById(R.id.btn_image);
        m_btnTakePhoto = (Button) findViewById(R.id.btn_photo);
        m_ivImage = (ImageView) findViewById(R.id.ivCardView);
        m_absolutelayout = (AbsoluteLayout) findViewById(R.id.relative1);
        m_llTopLayout = (LinearLayout) findViewById(R.id.llBottomLayout);
        m_arrSignObjects = new ArrayList<ViewsVo>();

        // Set the layout parameters to the Absolute layout for adding images.
        RelativeLayout.LayoutParams rl_pr = new LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        rl_pr.addRule(RelativeLayout.ABOVE, R.id.llBottomLayout);
        rl_pr.addRule(RelativeLayout.BELOW, R.id.layout_title);

        m_absolutelayout.setLayoutParams(rl_pr);

        m_screen = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
        m_DisplayWidth = m_screen.getWidth();
        m_AddedViewsHeightText = m_llTopLayout.getHeight();

        // Get the absoulte layout height according to the device screen density
        // to set the layout.
        m_vtoTree = m_absolutelayout.getViewTreeObserver();
        m_vtoTree.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                m_absHeight = m_absolutelayout.getHeight();
                m_absolutelayout.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });

        m_dialog = new Dialog(this, R.style.Dialog);
        m_dialog.setCancelable(true);

        m_btnSelectImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(
//                        Intent.ACTION_PICK,
//                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(i, 0);

                m_bitmap = null;

                Uri selectedImages = resIdToUri(m_context,R.drawable.mario);

                System.err.println("Image Path =====>" + selectedImages.getPath());
                m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mario);
                getImageLayout(m_bitmap);


            }
        });

        m_btnTakePhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Launch camera
                //create new Intent
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);  // create a file to save the video
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // set the image file name
                intent.putExtra(MediaStore.EXTRA_MEDIA_TITLE, fileUri.toString());
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high

                // start the Video Capture Intent
                startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

            }
        });
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:

                if (requestCode == 0 && resultCode == RESULT_OK && null != data) {

                }
                break;

            default:



                //Launch ImageEdit Activity
                String u = intent.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE);
                Picasso.with(m_context).load(u).fit().centerCrop().into(m_ivImage);

        }

    }

    /**
     * Method to set the view's height dynamically according to screen size.
     */
    private void setViewsHeightDynamically() {
        if (m_absHeight <= 500) {
            m_layoutparamsDelete = new AbsoluteLayout.LayoutParams(20, 20, 110,
                    0);
            m_layoutParamsEdit = new AbsoluteLayout.LayoutParams(20, 20, 110,
                    110);
            m_deleteEditHeightwidth = 20;
        } else if (m_absHeight >= 900) {
            m_layoutparamsDelete = new AbsoluteLayout.LayoutParams(70, 70, 400,
                    0);
            m_layoutParamsEdit = new AbsoluteLayout.LayoutParams(70, 70, 400,
                    200);
            m_deleteEditHeightwidth = 100;
        } else {
            m_layoutparamsDelete = new AbsoluteLayout.LayoutParams(35, 35, 140,
                    0);
            m_layoutParamsEdit = new AbsoluteLayout.LayoutParams(35, 35, 120,
                    120);
            m_deleteEditHeightwidth = 35;
        }
    }

    public static Uri resIdToUri(Context context, int resId) {
        return Uri.parse(ANDROID_RESOURCE + context.getPackageName()
                + FORESLASH + resId);
    }

    /**
     * Method to add the image by setting and creating the views dynamically
     * with delete and zoom option.
     */
    @SuppressWarnings("deprecation")
    private void getImageLayout(Bitmap p_bitmap) {
        ViewsVo m_signVo;
        //Check for images count .Set the count for limiting the number of images to add on screen.
        if (m_ImageCount < 1) {
            m_viewsAddedHeightEmotions = m_viewsAddedHeightEmotions + 90;
            m_ImageCount++;
        } /*
         * else { Toast.makeText(m_context, "No enough space for images.",
		 * Toast.LENGTH_LONG).show(); }
		 */
        m_btnSDeleteImage = new Button(m_context);
        m_btnZoom = new Button(m_context);
        m_ivtmpImage = new ImageView(m_context);

        setViewsHeightDynamically();
        // System.err.println("Height of Layout------" + m_absHeight);
        m_btnSDeleteImage.setLayoutParams(m_layoutparamsDelete);
        m_btnSDeleteImage.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.ic_deletered));
        m_btnSDeleteImage.setId(0);
        m_btnSDeleteImage.setOnClickListener(new ImageDeleteListener());

        m_btnZoom.setLayoutParams(m_layoutParamsEdit);
        m_btnZoom.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.right_arrow));
        m_btnZoom.setId(0);

        m_absTextlayout = new AbsoluteLayout(m_context);
        m_absZoomlayout = new AbsoluteLayout(m_context);
        m_ivtmpImage.setImageBitmap(Bitmap.createScaledBitmap(p_bitmap, 400, 180,
                true));
        m_absTextlayout.setLayoutParams(new AbsoluteLayout.LayoutParams(
                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0));
        m_absZoomlayout.setLayoutParams(new AbsoluteLayout.LayoutParams(
                AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                AbsoluteLayout.LayoutParams.WRAP_CONTENT, 0, 0));

        if (m_absHeight >= 900)
            m_ivtmpImage
                    .setLayoutParams(new FrameLayout.LayoutParams(400, 200));
        else
            m_ivtmpImage.setLayoutParams(new FrameLayout.LayoutParams(80, 80));

        m_ivtmpImage.setBackgroundColor(Color.TRANSPARENT);
        m_absTextlayout.addView(m_btnSDeleteImage);
        if (m_absHeight >= 900)
            m_absZoomlayout.setPadding(0, 0, 0, 0);
        else
            m_absZoomlayout.setPadding(0, 0, 0, 0);

        m_absZoomlayout.setBackgroundResource(R.drawable.dashedbordersmall);
        m_absZoomlayout.addView(m_ivtmpImage);

        m_absTextlayout.addView(m_absZoomlayout);
        m_absTextlayout.addView(m_btnZoom);
        m_absTextlayout.setDrawingCacheEnabled(true);
        m_absTextlayout.setClickable(true);
        m_absTextlayout.setId(0);
        m_ivtmpImage.setId(0);

        m_vtoTree = m_absTextlayout.getViewTreeObserver();
        m_vtoTree.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                m_absTextlayout.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });

        /**
         * Add all the views into arraylist which are added into the screen for
         * further to perform deletion of each views.
         */
        m_signVo = new ViewsVo();
        m_arrSignObjects.add(0, m_signVo);
        m_absolutelayout.addView(m_absTextlayout);

        // Image touch listener to move image onTouch event on screen.
        m_touchImagListener = new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        m_oldX = event.getX();
                        m_oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_POINTER_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        m_dX = event.getX() - m_oldX;
                        m_dY = event.getY() - m_oldY;

                        m_posX = m_prevX + m_dX;
                        m_posY = m_prevY + m_dY;

                        if (m_posX > 0
                                && m_posY > 0
                                && (m_posX + v.getWidth()) < m_absolutelayout
                                .getWidth()
                                && (m_posY + v.getHeight()) < m_absolutelayout
                                .getHeight()) {
                            v.setLayoutParams(new AbsoluteLayout.LayoutParams(v
                                    .getMeasuredWidth(), v.getMeasuredHeight(),
                                    (int) m_posX, (int) m_posY));

                            m_prevX = m_posX;
                            m_prevY = m_posY;

                        }
                        break;
                }
                return false;
            }
        };

        // Listener for the arrow ontouch of arrow ZoomIn and ZoomOut the image.
        m_strecthArrowListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                View view;
                // RemoveBorders();
                view = v;
                v.setClickable(true);
                v.setDrawingCacheEnabled(true);
                AbsoluteLayout m_absLayout = null;
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:

                        m_oldX = event.getX();
                        m_oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_POINTER_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        m_newX = event.getX();
                        m_newY = event.getY();

                        float newDist = m_newX - m_oldX;
                        if (m_newX > m_oldX && m_newY > m_oldY) {
                            if (newDist > 0.0f) {
                                m_scale = 1;
                                m_absLayout = (AbsoluteLayout) v.getParent();
                                int m_hightOfImage = (int) (m_scale + (((ImageView) ((AbsoluteLayout) m_absLayout
                                        .getChildAt(1)).getChildAt(0)).getHeight()));
                                int m_widthOfImage = (int) (m_scale + (((ImageView) ((AbsoluteLayout) m_absLayout
                                        .getChildAt(1)).getChildAt(0)).getWidth()));
                                m_widthDelete = (int) (m_scale + ((((AbsoluteLayout) m_absLayout
                                        .getChildAt(1))).getWidth()));
                                m_heightDelete = (int) (m_scale + ((((AbsoluteLayout) m_absLayout
                                        .getChildAt(1)).getHeight())));
                                if (m_absLayout.getBottom() <= (m_ivImage
                                        .getBottom())
                                        && m_absLayout.getRight() <= (m_DisplayWidth)) {
                                    m_layoutparams = new AbsoluteLayout.LayoutParams(
                                            m_widthOfImage, m_hightOfImage, 0, 0);
                                    ((ImageView) ((AbsoluteLayout) m_absLayout
                                            .getChildAt(1)).getChildAt(0))
                                            .setLayoutParams(m_layoutparams);

                                    m_layoutparams = new AbsoluteLayout.LayoutParams(
                                            AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                                            AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                                            m_absLayout.getLeft(), m_absLayout
                                            .getTop());
                                    m_absLayout.setLayoutParams(m_layoutparams);
                                    ((Button) m_absLayout.getChildAt(0))
                                            .setLayoutParams(new AbsoluteLayout.LayoutParams(
                                                    m_deleteEditHeightwidth,
                                                    m_deleteEditHeightwidth,
                                                    m_widthDelete, 0));
                                    ((Button) m_absLayout.getChildAt(2))
                                            .setLayoutParams(new AbsoluteLayout.LayoutParams(
                                                    m_deleteEditHeightwidth,
                                                    m_deleteEditHeightwidth,
                                                    m_widthDelete, m_heightDelete));

                                    m_hightOfImage = (int) (m_scale + (((AbsoluteLayout) m_absLayout
                                            .getChildAt(1)).getHeight()));
                                    m_widthOfImage = (int) (m_scale + (((AbsoluteLayout) m_absLayout
                                            .getChildAt(1)).getWidth()));
                                    m_layoutparams = new AbsoluteLayout.LayoutParams(
                                            m_widthOfImage, m_hightOfImage,
                                            ((AbsoluteLayout) m_absLayout
                                                    .getChildAt(1)).getLeft(),
                                            ((AbsoluteLayout) m_absLayout
                                                    .getChildAt(1)).getTop());
                                    ((AbsoluteLayout) m_absLayout.getChildAt(1))
                                            .setLayoutParams(m_layoutparams);
                                }
                            }
                        }
                        if (m_newX < m_oldX && m_newY < m_oldY) {
                            m_absLayout = (AbsoluteLayout) view.getParent();

                            int m_hightOfImage = (int) (((ImageView) ((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getChildAt(0)).getHeight() - m_scale);
                            int m_widthOfImage = (int) (((ImageView) ((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getChildAt(0)).getWidth() - m_scale);

                            m_widthDelete = (int) (((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getWidth() - m_scale);
                            m_layoutparams = new AbsoluteLayout.LayoutParams(
                                    m_widthOfImage, m_hightOfImage, 0, 0);
                            ((ImageView) ((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getChildAt(0))
                                    .setLayoutParams(m_layoutparams);

                            m_layoutparams = new AbsoluteLayout.LayoutParams(
                                    AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                                    AbsoluteLayout.LayoutParams.WRAP_CONTENT,
                                    m_absLayout.getLeft(), m_absLayout.getTop());
                            m_absLayout.setLayoutParams(m_layoutparams);

                            ((Button) m_absLayout.getChildAt(0))
                                    .setLayoutParams(new AbsoluteLayout.LayoutParams(
                                            m_deleteEditHeightwidth,
                                            m_deleteEditHeightwidth, m_widthDelete,
                                            0));
                            ((Button) m_absLayout.getChildAt(2))
                                    .setLayoutParams(new AbsoluteLayout.LayoutParams(
                                            m_deleteEditHeightwidth,
                                            m_deleteEditHeightwidth, m_widthDelete,
                                            m_widthDelete));

                            m_hightOfImage = (int) ((((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getHeight()) - m_scale);
                            m_widthOfImage = (int) ((((AbsoluteLayout) m_absLayout
                                    .getChildAt(1)).getWidth()) - m_scale);
                            m_layoutparams = new AbsoluteLayout.LayoutParams(
                                    m_widthOfImage, m_hightOfImage,
                                    ((AbsoluteLayout) m_absLayout.getChildAt(1))
                                            .getLeft(),
                                    ((AbsoluteLayout) m_absLayout.getChildAt(1))
                                            .getTop());
                            ((AbsoluteLayout) m_absLayout.getChildAt(1))
                                    .setLayoutParams(m_layoutparams);

                        }

                        break;
                }

                return false;
            }
        };
        m_absTextlayout.setOnTouchListener(m_touchImagListener);
        m_btnZoom.setOnTouchListener(m_strecthArrowListener);
    }

    // Delete button listener to show the alert and confirmation for deleting
    // the items.
    private class ImageDeleteListener implements OnClickListener {
        @Override
        public void onClick(final View v) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    m_context);
            alertDialogBuilder.setTitle("Brazzers");
            alertDialogBuilder
                    .setMessage("Are you sure you want to delete this brazzer?")
                    .setCancelable(false)
                    .setPositiveButton("Hell, yes!",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    m_ImageCount--;
                                    for (int counter = 0; counter < m_arrSignObjects
                                            .size(); counter++) {
                                        if (v.getId() == m_arrSignObjects.get(
                                                counter).getViewId()) {
                                            if (m_totalTextViewCount <= 0) {
                                                m_AddedViewsHeightText = m_AddedViewsHeightText
                                                        - m_arrSignObjects
                                                        .get(counter)
                                                        .getViewHeight();
                                            } else {
                                                m_totalTextViewCount--;
                                            }

                                            m_absolutelayout
                                                    .removeView((View) v
                                                            .getParent());
                                            m_arrSignObjects.remove(counter);

                                            break;
                                        }
                                    }
                                }
                            })
                    .setNegativeButton("Nope",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();
            // show it
            alertDialog.show();
        }

    }
}
