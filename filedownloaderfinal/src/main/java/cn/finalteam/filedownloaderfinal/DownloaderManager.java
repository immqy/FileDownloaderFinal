/*
 * Copyright (C) 2015 彭建波(pengjianbo@finalteam.cn), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.finalteam.filedownloaderfinal;

import android.text.TextUtils;
import android.util.SparseArray;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadLog;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.finalteam.toolsfinal.StringUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/1/20 0020 15:39
 */
public class DownloaderManager {

    private static DownloaderManager mDownloadManager;
    private FileDownloaderDBController mDbController;
    private SparseArray<FileDownloaderModel> mAllTasks;
    private List<FileDownloadConnectListener> mConnectListenerList;
    private ListenerManager mListenerManager;

    private Queue<FileDownloaderModel> mWaitQueue;
    private List<FileDownloaderModel> mDownloadingList;

    private DownloaderManagerConfiguration mConfiguration;
    private FileDownloaderCallback mGlobalDownloadCallback;

    private Map<String, String> mExtFieldMap;
    /**
     * 获取DownloadManager实例
     * @return
     */
    public static DownloaderManager getInstance() {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloaderManager();
        }
        return mDownloadManager;
    }

    /**
     * 初始化DownloadManager
     */
    public synchronized void init(DownloaderManagerConfiguration configuration) {
        FileDownloader.init(configuration.getApplication(), configuration.getOkHttpClientCustomMaker());
        FileDownloader.getImpl().bindService();

        ILogger.DEBUG = configuration.isDebug();
        FileDownloadLog.NEED_LOG = ILogger.DEBUG;

        this.mConfiguration = configuration;
        this.mExtFieldMap = configuration.getDbExtField();

        mDbController = new FileDownloaderDBController(configuration.getApplication(),configuration.getDbVersion(),
                mExtFieldMap, configuration.getDbUpgradeListener());
        mAllTasks = mDbController.getAllTasks();
        mConnectListenerList = new ArrayList<>();
        mListenerManager = new ListenerManager();

        //设置下载保存目录
        if (!StringUtils.isEmpty(configuration.getDownloadStorePath())) {
            FileDownloadUtils.setDefaultSaveRootPath(configuration.getDownloadStorePath());
        }

        mWaitQueue = new LinkedList<>();
        mDownloadingList = Collections.synchronizedList(new ArrayList<FileDownloaderModel>());
        mDownloadManager = this;
    }

    private DownloaderManager() {
    }

    /**
     * 获取扩展字段map
     * @return
     */
    Map<String, String> getDbExtFieldMap() {
        return mExtFieldMap;
    }

    /**
     * 开始下载任务
     * @param downloadId
     */
    public void startTask(int downloadId) {
        startTask(downloadId, null);
    }

    /**
     * 开始下载任务
     * @param downloadId
     * @param callback
     */
    public void startTask(int downloadId, FileDownloaderCallback callback) {
        FileDownloaderModel model = getFileDownloaderModelById(downloadId);
        if (model != null) {
            BridgeListener bridgeListener = mListenerManager.getBridgeListener(downloadId);
            bridgeListener.addDownloadListener(callback);
            if (mDownloadingList.size() >= mConfiguration.getMaxDownloadingCount()) { //下载中队列已满
                if (!mWaitQueue.contains(model)) {
                    mWaitQueue.offer(model);
                }
                bridgeListener.wait(downloadId);
            } else {
                mDownloadingList.add(model);
                final BaseDownloadTask task = FileDownloader.getImpl().create(model.getUrl())
                        .setPath(model.getPath())
                        .setCallbackProgressTimes(100)
                        .setListener(bridgeListener);
                bridgeListener.setDownloadTask(task);
                task.start();
            }
        } else {
            ILogger.e("Task does not exist!");
        }
    }

    /**
     * 删除一个任务
     * @param downloadId
     */
    public void deleteTask(int downloadId) {
        if (mDbController.deleteTask(downloadId)) {
            FileDownloaderModel model = getFileDownloaderModelById(downloadId);
            if (model != null) {//删除文件
                new File(model.getPath()).delete();
            }
            pauseTask(downloadId);
            removeDownloadingTask(downloadId);
            removeWaitQueueTask(downloadId);
            try {
                mAllTasks.remove(downloadId);
            } catch (Exception e) {
                ILogger.e(e);
            }
        } else {
            ILogger.e("delete failure");
        }
    }

    /**
     * 添加下载监听
     * @param downloadId
     * @param listener
     */
    public void addFileDownloadListener(int downloadId, FileDownloaderCallback listener) {
        BridgeListener bridgeListener = mListenerManager.getBridgeListener(downloadId);
        bridgeListener.addDownloadListener(listener);
    }

    /**
     * 下一个任务
     * @return
     */
    protected synchronized FileDownloaderModel nextTask() {
        return mWaitQueue.poll();
    }

    /**
     * 将一个下载中的任务从下载中队列移除
     * @param downloadId
     */
    protected synchronized void removeDownloadingTask(int downloadId) {
        Iterator<FileDownloaderModel> iterator = mDownloadingList.iterator();
        while (iterator.hasNext()) {
            FileDownloaderModel model = iterator.next();
            if ( model != null && model.getId() == downloadId) {
                try {
                    iterator.remove();
                } catch (Exception e){
                    ILogger.e(e);
                }
                return;
            }
        }
    }

    /**
     * 将一个等待中的任务从下等待队列中移除
     * @param downloadId
     */
    protected synchronized void removeWaitQueueTask(int downloadId) {
        Iterator<FileDownloaderModel> iterator = mWaitQueue.iterator();
        while (iterator.hasNext()) {
            FileDownloaderModel model = iterator.next();
            if ( model != null && model.getId() == downloadId) {
                try {
                    iterator.remove();
                } catch (Exception e){
                    ILogger.e(e);
                }
                return;
            }
        }
    }

    /**
     * 暂停任务
     * @param downloadId
     */
    public synchronized void pauseTask(int downloadId) {
        if(isWaiting(downloadId)) {
            BridgeListener bridgeListener = mListenerManager.getBridgeListener(downloadId);
            removeWaitQueueTask(downloadId);
            bridgeListener.stop(downloadId, getSoFar(downloadId), getTotal(downloadId));
        } else {
            FileDownloader.getImpl().pause(downloadId);
        }
    }

    /**
     * 停止所有任务
     */
    public void pauseAllTask() {
        FileDownloader.getImpl().pauseAll();
    }

    /**
     * 根据任务ID获取任务
     * @param downloadId
     * @return
     */
    public BaseDownloadTask getDownloadTaskById(int downloadId) {
        BridgeListener bridgeListener = mListenerManager.getBridgeListener(downloadId);
        return bridgeListener.getDownloadTask();
    }

    /**
     * 添加service连接监听
     * @param listener
     */
    public void addServiceConnectListener(FileDownloadConnectListener listener) {
        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    /**
     * 移除sevice连接监听
     * @param listener
     */
    public void removeServiceConnectListener(FileDownloadConnectListener listener) {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
    }

    /**
     * 释放下载管理器
     */
    public void onDestroy() {
        try {
            mConnectListenerList.clear();
            pauseAllTask();
            FileDownloader.getImpl().unBindServiceIfIdle();
        } catch (Exception e){}
    }

    /**
     * 获取service是否已连接
     * @return
     */
    public boolean isReady() {
        return FileDownloader.getImpl().isServiceConnected();
    }

    /**
     * 根据索引获取下载信息
     * @param position
     * @return
     */
    public FileDownloaderModel getFileDownloaderModelByPostion(final int position) {
        int id = mAllTasks.keyAt(position);
        return getFileDownloaderModelById(id);
    }


    /**
     * 根据downloadId获取下载信息
     * @param downloadId
     * @return
     */
    public FileDownloaderModel getFileDownloaderModelById(final int downloadId) {
        if (mAllTasks != null && mAllTasks.size() > 0) {
            return mAllTasks.get(downloadId);
        }
        return null;
    }

    /**
     * 是否下载完成
     * @param downloadId
     * @return
     */
    public boolean isFinish(int downloadId) {
        FileDownloaderModel model = getFileDownloaderModelById(downloadId);
        if (model != null) {
            if (getStatus(downloadId) == FileDownloadStatus.completed && new File(model.getPath()).exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否在等待队列
     * @param downloadId
     * @return
     */
    public boolean isWaiting(int downloadId) {
        FileDownloaderModel model = new FileDownloaderModel();
        model.setId(downloadId);
        return mWaitQueue.contains(model);
    }

    /**
     * 判断一个任务是否在下载中
     * @param downloadId
     * @return
     */
    public boolean isDownloading(final int downloadId) {
        int status = getStatus(downloadId);
        switch (status) {
            case FileDownloadStatus.pending:
            case FileDownloadStatus.connected:
            case FileDownloadStatus.progress:
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取FileDownloader FileDownloadStatus下载状态
     * @param downloadId
     * @return
     */
    private int getStatus(final int downloadId) {
        return FileDownloader.getImpl().getStatus(downloadId);
    }

    /**
     * 根据downloadId获取文件总大小
     * @param downloadId
     * @return
     */
    private long getTotal(final int downloadId) {
        return FileDownloader.getImpl().getTotal(downloadId);
    }

    /**
     * 根据downloadId获取文件已下载大小
     * @param downloadId
     * @return
     */
    private long getSoFar(final int downloadId) {
        return FileDownloader.getImpl().getSoFar(downloadId);
    }

    /**
     * 根据downloadId获取文件下载进度
     * @param downloadId
     * @return
     */
    public int getProgress(int downloadId) {
        FileDownloaderModel model = getFileDownloaderModelById(downloadId);
        int progress = 0;
        if( model != null ) {
            if (!new File(model.getPath()).exists()) {
                return progress;
            }
        }

        long totalBytes = getTotal(downloadId);
        long soFarBytes = getSoFar(downloadId);

        if ( totalBytes != 0 ) {
            progress = (int)(soFarBytes / (float)totalBytes * 100);
        }

        return progress;
    }

    /**
     * 获取所任务数
     * @return
     */
    public int getTaskCounts() {
        if (mAllTasks == null){
            return 0;
        }
        return mAllTasks.size();
    }

    /**
     * 添加一个任务
     * 注：同样的URL，保存的目录不一样表示这两次addTask是不同的任务
     * @param url
     * @return
     */
    public FileDownloaderModel addTask(final String url) {
        FileDownloaderModel downloaderModel = new FileDownloaderModel();
        downloaderModel.setUrl(url);
        downloaderModel.setPath(createPath(url));
        return addTask(downloaderModel);
    }

    /**
     * 添加一个任务
     * 注：同样的URL，保存的目录不一样表示这两次addTask是不同的任务
     * @param url
     * @param path
     * @return
     */
    public FileDownloaderModel addTask(final String url, String path) {
        FileDownloaderModel downloaderModel = new FileDownloaderModel();
        downloaderModel.setUrl(url);
        downloaderModel.setPath(path);
        return addTask(downloaderModel);
    }


    /**
     * 添加一个任务
     * 注：同样的URL，保存的目录不一样表示这两次addTask是不同的任务
     * @param downloaderModel
     * @return
     */
    public FileDownloaderModel addTask(FileDownloaderModel downloaderModel) {
        String url = downloaderModel.getUrl();
        String path = downloaderModel.getPath();
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (TextUtils.isEmpty(path)) {
            path = createPath(url);
            downloaderModel.setPath(path);
        }

        final int id = FileDownloadUtils.generateId(url, path);
        FileDownloaderModel model = getFileDownloaderModelById(id);
        if (model != null) {
            return model;
        }
        model = mDbController.addTask(downloaderModel);
        mAllTasks.put(id, model);

        return model;
    }

    /**
     * 创建下载保存地址
     * @param url
     * @return
     */
    private String createPath(final String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        return FileDownloadUtils.getDefaultSaveFilePath(url);
    }

    /**
     * 设置全局下载事件监听
     * @param callback
     */
    public void setGlobalDownloadCallback(FileDownloaderCallback callback) {
        this.mGlobalDownloadCallback = callback;
    }

    /**
     * 获取全局下载事件监听
     * @return
     */
    protected FileDownloaderCallback getGlobalDownloadCallback() {
        return this.mGlobalDownloadCallback;
    }
}
