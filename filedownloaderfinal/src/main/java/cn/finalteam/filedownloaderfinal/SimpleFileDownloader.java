package cn.finalteam.filedownloaderfinal;

import android.text.TextUtils;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;

import cn.finalteam.toolsfinal.StringUtils;
import cn.finalteam.toolsfinal.io.FileUtils;

/**
 * Desction:简单的文件下载
 * Author:pengjianbo
 * Date:2016/2/2 0002 14:30
 */
public class SimpleFileDownloader {
    public SimpleFileDownloader() {
    }

    public static void downloadFile(String url, FileDownloaderCallback callback) {
        downloadFile(url, null, callback);
    }

    public static void downloadFile(final String url, String path, final FileDownloaderCallback callback) {

        if (StringUtils.isEmpty(path)) {
            path = createPath(url);
        } else {
            File file = new File(path);
            if (!file.exists()) {
                FileUtils.mkdirs(file.getParentFile());
            }
        }
        final BaseDownloadTask task = FileDownloader.getImpl().create(url)
                .setPath(path)
                .setCallbackProgressTimes(100)
                .setListener(new SimpleBridgeListener(callback));
        task.start();
    }

    /**
     * 创建下载保存地址
     * @param url
     * @return
     */
    private static String createPath(final String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        return FileDownloadUtils.getDefaultSaveFilePath(url);
    }

    static class SimpleBridgeListener extends FileDownloadListener {

        private FileDownloaderCallback mFileDownloaderCallback;

        public SimpleBridgeListener(final FileDownloaderCallback callback) {
            this.mFileDownloaderCallback = callback;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (mFileDownloaderCallback != null) {
                int preProgress = 0;
                if ( totalBytes != 0 ) {
                    preProgress = (int)(soFarBytes / (float)totalBytes * 100);
                }
                mFileDownloaderCallback.onStart(task.getDownloadId(), soFarBytes, totalBytes, preProgress);
            }
        }

        @Override
        protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, etag, isContinue, soFarBytes, totalBytes);

            if (mFileDownloaderCallback != null) {
                int preProgress = 0;
                if ( totalBytes != 0 ) {
                    preProgress = (int)(soFarBytes / (float)totalBytes * 100);
                }
                mFileDownloaderCallback.onStart(task.getDownloadId(), soFarBytes, totalBytes, preProgress);
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            if (mFileDownloaderCallback != null) {
                int progress = 0;
                if ( totalBytes != 0 ) {
                    progress = (int)(soFarBytes / (float)totalBytes * 100);
                }
                mFileDownloaderCallback.onProgress(task.getDownloadId(), soFarBytes, totalBytes, progress);
            }
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {

        }

        @Override
        protected void completed(BaseDownloadTask task) {
            if (mFileDownloaderCallback != null) {
                mFileDownloaderCallback.onFinish(task.getDownloadId(), task.getPath());
            }
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            stop(task.getDownloadId(), soFarBytes, totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            long totalBytes = task.getLargeFileTotalBytes();
            long soFarBytes = task.getLargeFileSoFarBytes();
            stop(task.getDownloadId(), soFarBytes, totalBytes);
        }

        @Override
        protected void warn(BaseDownloadTask task) {

        }

        protected void stop(int downloadId, long soFarBytes, long totalBytes) {
            int progress = 0;
            if ( totalBytes != 0 ) {
                progress = (int)(soFarBytes / (float)totalBytes * 100);
            }
            if (mFileDownloaderCallback != null) {
                mFileDownloaderCallback.onStop(downloadId, soFarBytes, totalBytes, progress);
            }
        }
    }



}
