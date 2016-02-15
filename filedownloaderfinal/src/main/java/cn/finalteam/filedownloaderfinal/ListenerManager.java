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

import android.util.SparseArray;

/**
 * Desction:下载文件监听器管理类，主要是实现一任务多事件回调功能
 * Author:pengjianbo
 * Date:16/1/3 下午11:59
 */
class ListenerManager {
    private SparseArray<BridgeListener> mListenerListArray;

    protected ListenerManager() {
        mListenerListArray = new SparseArray<>();
    }

    public BridgeListener getBridgeListener(int downloadId) {
        BridgeListener listener = mListenerListArray.get(downloadId);
        if ( listener == null ) {
            listener = new BridgeListener();
        }
        mListenerListArray.put(downloadId, listener);
        return listener;
    }

    public void removeDownloadListener(int downloadId, FileDownloaderCallback dlistener) {
        BridgeListener listener = mListenerListArray.get(downloadId);
        if ( listener != null ) {
            listener.removeDownloadListener(dlistener);
        }
    }

    public void removeAllDownloadListener(final int downloadId) {
        BridgeListener listener = mListenerListArray.get(downloadId);
        if ( listener != null ) {
            listener.removeAllDownloadListener();
        }
    }

    public void removeAllDownloadListener() {
        for (int i = 0; i < mListenerListArray.size(); i++ ) {
            int key = mListenerListArray.keyAt(i);
            BridgeListener listener = mListenerListArray.get(key);
            if (listener != null) {
                listener.removeAllDownloadListener();
            }
        }
        mListenerListArray.clear();
    }
}
