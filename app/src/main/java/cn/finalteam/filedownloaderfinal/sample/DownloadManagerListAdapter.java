package cn.finalteam.filedownloaderfinal.sample;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.finalteam.filedownloaderfinal.DownloaderManager;
import cn.finalteam.filedownloaderfinal.FileDownloaderCallback;
import cn.finalteam.filedownloaderfinal.FileDownloaderModel;
import cn.finalteam.toolsfinal.StringUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/8 上午9:26
 */
public class DownloadManagerListAdapter extends CommonBaseAdapter<DownloadManagerListAdapter.FileViewHolder, FileDownloaderModel> {

    private View mLastShowBottomBar;
    private int mLastShowBottomBarPos = -1;
//    private Map<String, MyDLTaskListener> mDListenerMap;

    private SparseArray<FileViewHolder> mViewHolderArray;

    public DownloadManagerListAdapter(Context context) {
        super(context, new ArrayList<FileDownloaderModel>());
        mViewHolderArray = new SparseArray<>();
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = inflate(R.layout.adapter_download_manager_list_item, parent);
        FileViewHolder holder = new FileViewHolder(view);
        holder.mBtnOperate.setOnClickListener(taskActionOnClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        final FileDownloaderModel model = DownloaderManager.getInstance().getFileDownloaderModelByPostion(position);
        holder.setDownloadId(model.getId());
        holder.mBtnOperate.setTag(holder);

        String title = model.getExtFieldValue("title");
        if (StringUtils.isEmpty(title)) {
            title = "name:" + model.getId();
        }
        holder.mTvTitle.setText(title);
        String icon = model.getExtFieldValue("icon");
        Picasso.with(mContext)
                .load(icon)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .resize(96, 96)
                .centerInside()
                .into(holder.mIvIcon);
        holder.mBtnOperate.setEnabled(true);
        mViewHolderArray.put(model.getId(), holder);

        if (DownloaderManager.getInstance().isReady()) {
            if (!new File(model.getPath()).exists()) {
                //在下载管理，下载没有开始过
                int progress = 0;
                holder.updateNotDownloaded(progress);
            } else if (DownloaderManager.getInstance().isFinish(model.getId())) {
                //下载完成
                holder.updateDownloaded();
            } else if (DownloaderManager.getInstance().isDownloading(model.getId())) {
                //正在下载
                holder.updateDownloading(DownloaderManager.getInstance().getProgress(model.getId()));
            } else if (DownloaderManager.getInstance().isWaiting(model.getId())) {//队列已满，等待下载
                holder.updateWaiting(DownloaderManager.getInstance().getProgress(model.getId()));
            } else {
                //已经在下载列表，可能因为某种原有导致下载停止
                int progress = DownloaderManager.getInstance().getProgress(model.getId());
                holder.updateNotDownloaded(progress);
            }
        } else {//加载中
            holder.mTvDownloadState.setText("Status: loading...");
            holder.mBtnOperate.setEnabled(false);
        }
    }

    @Override
    public int getCount() {
        return DownloaderManager.getInstance().getTaskCounts();
    }

    private FileDownloaderCallback myFileDownloadListener = new FileDownloaderCallback() {

        private FileViewHolder checkCurrentHolder(int downloadId) {
            final FileViewHolder holder = mViewHolderArray.get(downloadId);
            if (holder.id != downloadId) {
                return null;
            }

            return holder;
        }

        @Override
        public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {
            super.onStart(downloadId, soFarBytes, totalBytes, preProgress);
            final FileViewHolder holder = checkCurrentHolder(downloadId);
            if (holder == null) {
                return;
            }

            holder.updateDownloading(preProgress);
        }

        @Override
        public void onWait(int downloadId) {
            super.onWait(downloadId);
            final FileViewHolder holder = checkCurrentHolder(downloadId);
            if (holder == null) {
                return;
            }

            holder.updateWait(DownloaderManager.getInstance().getProgress(downloadId));
        }

        @Override
        public void onProgress(int downloadId, long soFarBytes, long totalBytes, int progress) {
            super.onProgress(downloadId, soFarBytes, totalBytes, progress);
            final FileViewHolder holder = checkCurrentHolder(downloadId);
            if (holder == null) {
                return;
            }
            holder.updateDownloading(progress);
        }

        @Override
        public void onStop(int downloadId, long soFarBytes, long totalBytes, int progress) {
            super.onStop(downloadId, soFarBytes, totalBytes, progress);
            final FileViewHolder holder = checkCurrentHolder(downloadId);
            if (holder == null) {
                return;
            }
            holder.updateNotDownloaded(progress);
        }

        @Override
        public void onFinish(int downloadId, String path) {
            super.onFinish(downloadId, path);
            final FileViewHolder tag = checkCurrentHolder(downloadId);
            if (tag == null) {
                return;
            }

            tag.updateDownloaded();
        }
    };

    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }

            FileViewHolder holder = (FileViewHolder) v.getTag();

            int downloadId = holder.id;

            if (DownloaderManager.getInstance().isFinish(downloadId)) {//文件已经下载完成，并且判断文件是存在的
                DownloaderManager.getInstance().deleteTask(downloadId);
                holder.mBtnOperate.setEnabled(true);
                int progress = 0;
                holder.updateNotDownloaded(progress);
                notifyDataSetChanged();
            } else if (DownloaderManager.getInstance().isDownloading(downloadId) || DownloaderManager.getInstance().isWaiting(downloadId)) {//暂停下载
                DownloaderManager.getInstance().pauseTask(downloadId);
            } else {//开始下载
                DownloaderManager.getInstance().startTask(downloadId, myFileDownloadListener);
            }
        }
    };

    static class FileViewHolder extends CommonBaseAdapter.ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIvIcon;
        @Bind(R.id.number_progress_bar)
        ProgressBar mNumberProgressBar;
        @Bind(R.id.tv_title)
        TextView mTvTitle;
        @Bind(R.id.btn_operate)
        Button mBtnOperate;
        @Bind(R.id.tv_download_scale)
        TextView mTvDownloadScale;
        @Bind(R.id.tv_download_state)
        TextView mTvDownloadState;
        @Bind(R.id.tv_game_detail)
        TextView mTvGameDetail;
        @Bind(R.id.tv_cancel)
        TextView mTvCancel;
        @Bind(R.id.ll_bottom_bar)
        RelativeLayout mLlBottomBar;
        @Bind(R.id.tv_download_speed)
        TextView mTvDownloadSpeed;

        /**
         * download id
         */
        private int id;

        public void setDownloadId(final int id) {
            this.id = id;
        }

        public void updateDownloaded() {
            mNumberProgressBar.setProgress(100);
            mTvDownloadState.setText("Status: completed");
            mBtnOperate.setText("Delete");
        }

        public void updateNotDownloaded(int progress) {
            mNumberProgressBar.setProgress(progress);
            mTvDownloadState.setText("Status:paused");
            mBtnOperate.setText("Start");
        }

        /**
         * 更新下载进度
         *
         * @param progress
         */
        public void updateDownloading(int progress) {
            mNumberProgressBar.setProgress(progress);
            mTvDownloadState.setText("Status:downloading");
            mBtnOperate.setText("Pause");
        }

        public void updateWait(int progress) {
            mNumberProgressBar.setProgress(progress);
            mTvDownloadState.setText("Status:wait");
            mBtnOperate.setText("Pause");
        }

        public void updateWaiting(int progress) {
            mNumberProgressBar.setProgress(progress);
            mTvDownloadState.setText("Status:waiting");
            mBtnOperate.setText("Pause");
        }

//        public void updateViewHolder(final int downloadId, final FileViewHolder holder) {
//            final BaseDownloadTask task = DownloaderManager.getInstance().getDownloadTaskById(downloadId);
//            if (task == null) {
//                return;
//            }
//
//            task.setTag(holder);
//        }

        public FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
