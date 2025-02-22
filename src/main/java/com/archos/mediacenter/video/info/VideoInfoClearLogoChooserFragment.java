package com.archos.mediacenter.video.info;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.archos.mediacenter.utils.imageview.ImageProcessor;
import com.archos.mediacenter.utils.imageview.ImageViewSetter;
import com.archos.mediacenter.utils.imageview.LoadResult;
import com.archos.mediacenter.utils.imageview.LoadTaskItem;
import com.archos.mediacenter.video.R;
import com.archos.mediacenter.video.browser.adapters.object.Base;
import com.archos.mediascraper.BaseTags;
import com.archos.mediascraper.ScraperImage;

import java.util.Collections;
import java.util.List;


public class VideoInfoClearLogoChooserFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        View.OnClickListener {
    private static final String TAG = VideoInfoClearLogoChooserFragment.class.getSimpleName();
    private static final boolean DBG = false;
    // debug fragment lifecycle
    private static final boolean DBG_LC = false;

    private GridView mGrid;
    private ClearLogoAdapter mAdapter;
    private BaseTags mTag;
    private View mView;

    public VideoInfoClearLogoChooserFragment() {
    }

    // ---------------------- FRAGMENT LIFECYCLE ---------------------------- //
    // onAttach

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DBG_LC) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // in case we get recreated restore the tag, may be null anyways.
        if (savedInstanceState != null)
            mTag = savedInstanceState.getParcelable(TAG);
        if(mTag==null)
            setVideo((Base) getActivity().getIntent().getSerializableExtra(VideoInfoPosterBackdropActivity.EXTRA_VIDEO));

        // init the adapter here so it does not get recreated when rotating and it keeps the list
        // using application context here since we keep the adapter around for longer and keeping
        // a reference to the whole activity could prevent GC
        mAdapter = new ClearLogoAdapter(getActivity().getApplicationContext(), null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.video_info_clearlogo_chooser, null);
        return mView;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (DBG_LC) Log.d(TAG, "onViewCreated");

        // cancel button
        mView.findViewById(R.id.cancel).setOnClickListener(this);

        // image grid
        mGrid = (GridView) mView.findViewById(R.id.list);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (DBG_LC) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        startLoadingIfReady();
    }

    @Override
    public void onStart() {
        if (DBG_LC) Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        if (DBG_LC) Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        if (DBG_LC) Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        if (DBG_LC) Log.d(TAG, "onStop");
        super.onStop();
        // tell adapter that it does not need to set images any more
        // prevents long running downloads from affecting the ui which is not
        // visible after here.
        mAdapter.stopLoading();
    }

    @Override
    public void onDestroyView() {
        if (DBG_LC) Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        // no more view = no more grid.
        mGrid = null;
    }

    @Override
    public void onDestroy() {
        if (DBG_LC) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    // onDetach

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DBG_LC) Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(TAG, mTag); // abuse our logtag
    }

    // ---------------------- PUBLIC API TO OUTSIDE ------------------------- //


    public void setVideo(Base item) {
        if (DBG) Log.d(TAG, "setInfoItem");
        mTag = item.getFullScraperTags(getActivity());
        startLoadingIfReady();
    }



    // ---------------------- EVENT HANDLER IMPLEMENTATION ------------------ //
    public void onClick(View v) {
        if (DBG) Log.d(TAG, "onClick - cancel");
        stop(false);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (DBG) Log.d(TAG, "onItemClick - " + position);
        ScraperImage clickedImage = (ScraperImage) mAdapter.getItem(position);
        new ClearLogoSaver(getActivity(), this).execute(clickedImage);
    }

    // ---------------------- INTERNAL UTILITY METHODS ---------------------- //
    /* default */ void stop(boolean hasClearLogoChanged) {

        mAdapter.cleanup();
        if(hasClearLogoChanged)
            getActivity().setResult(AppCompatActivity.RESULT_OK);
        else
            getActivity().setResult(AppCompatActivity.RESULT_CANCELED);
        getActivity().finish();
    }

    private void startLoadingIfReady() {
        if (mTag != null && getActivity() != null && mAdapter != null) {
            new ClearLogoListLoader(getActivity(), mAdapter).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mTag);
        }
    }

    // ---------------------- INTERNALLY USED CLASSES ----------------------- //

    /** Loads a List<ScraperImage> from the database and sets it to a ClearLogoAdapter */
    private static class ClearLogoListLoader extends AsyncTask<BaseTags, Void, List<ScraperImage>> {
        private final Context mContext;
        private final ClearLogoAdapter mTargetAdapter;

        public ClearLogoListLoader(Context context, ClearLogoAdapter target) {
            mContext = context;
            mTargetAdapter = target;
        }

        @Override
        protected List<ScraperImage> doInBackground(BaseTags... params) {
            if (params != null && params.length > 0) {
                return params[0].getAllClearLogosInDb(mContext);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ScraperImage> result) {
            if (mTargetAdapter != null)
                mTargetAdapter.setList(result);
        }
    }

    /** Saves a ClearLogo as default for a video and stops the hosting fragment */
    private static class ClearLogoSaver extends AsyncTask<ScraperImage, Void, Void> {
        private final Context mContext;
        private final VideoInfoClearLogoChooserFragment mHost;

        public ClearLogoSaver(Context context, VideoInfoClearLogoChooserFragment host) {
            mContext = context;
            mHost = host;
        }

        @Override
        protected Void doInBackground(ScraperImage... params) {
            if (params != null && params.length > 0) {
                params[0].setAsDefault(mContext);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mHost != null)
                mHost.stop(true);
        }
    }

    /** Adapter showing & loading images */
    static class ClearLogoAdapter extends BaseAdapter {
        private List<ScraperImage> mList;
        private final LayoutInflater mInflater;
        private final ImageViewSetter mSetter;
        private final ScraperImageThumbProcessor mLoader;

        public ClearLogoAdapter(Context context, List<ScraperImage> list) {
            mList = list != null ? list : Collections.<ScraperImage> emptyList();

            mInflater = LayoutInflater.from(context);
            mSetter = new ImageViewSetter(context, null);
            mLoader = new ScraperImageThumbProcessor(context);
        }

        public void setList(List<ScraperImage> list) {
            mList = list != null ? list : Collections.<ScraperImage> emptyList();
            notifyDataSetChanged();
        }

        /** stops setter from further setting images async */
        public void stopLoading() {
            mSetter.stopLoadingAll();
        }

        /** stops all tasks and clears the caches etc */
        public void cleanup() {
            mSetter.stopLoadingAll();
            mSetter.clearCache();
            mList = Collections.<ScraperImage> emptyList();
            notifyDataSetChanged();
        }

        public int getCount() {
            if (DBG) Log.d(TAG, "getCount = " + mList.size());
            return mList.size();
        }

        public Object getItem(int position) {
            if (DBG) Log.d(TAG, "getItem " + position);
            return mList.get(position);
        }

        public long getItemId(int position) {
            if (DBG) Log.d(TAG, "getItemId " + position);
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (DBG) Log.d(TAG, "getView " + position);

            View view;
            ViewHolder holder;

            view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.video_info_clearlogo_chooser_list_item, parent,
                        false);
                holder = new ViewHolder();
                holder.image = (ImageView) view.findViewById(R.id.image);
                holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            mSetter.set(holder.image, mLoader, mList.get(position));

            return view;
        }

        private static class ViewHolder {
            public ViewHolder() { /* empty */ }

            ImageView image;
        }

        private static class ScraperImageThumbProcessor extends ImageProcessor {
            private final Context mContext;
            private final int mWidth;
            private final int mHeight;

            public ScraperImageThumbProcessor(Context context) {
                mContext = context;
                mWidth = mContext.getResources().getDimensionPixelSize(R.dimen.video_info_backdrop_chooser_image_width_max);
                mHeight = mContext.getResources().getDimensionPixelSize(R.dimen.video_info_backdrop_chooser_image_height_max);
            }

            @Override
            public void loadBitmap(LoadTaskItem taskItem) {
                if (taskItem.loadObject instanceof ScraperImage) {
                    ScraperImage image = (ScraperImage) taskItem.loadObject;
                    String file = image.getLargeFile();
                    if (file != null) {
                        image.download(mContext);
                        taskItem.result.bitmap = BitmapFactory.decodeFile(file);
                    }
                    taskItem.result.status = taskItem.result.bitmap != null ?
                            LoadResult.Status.LOAD_OK : LoadResult.Status.LOAD_ERROR;
                } else {
                    taskItem.result.status = LoadResult.Status.LOAD_BAD_OBJECT;
                }
            }

            @Override
            public boolean canHandle(Object loadObject) {
                return loadObject instanceof ScraperImage;
            }

            @Override
            public String getKey(Object loadObject) {
                if (loadObject instanceof ScraperImage) {
                    ScraperImage image = (ScraperImage) loadObject;
                    // using the url here since images from same url may be used
                    // as different files. But for cache reasons urls are
                    // a better key
                    return image.getLargeUrl();
                }
                return null;
            }
        }
    }

}
