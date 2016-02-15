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

/**
 * Desction:
 * Author:pengjianbo
 * Date:16/1/3 下午10:31
 */
public class FileDownloaderCallback {

    /**
     * onPending 等待，已经进入下载队列	数据库中的soFarBytes与totalBytes
     * connected 已经连接上	ETag, 是否断点续传, soFarBytes, totalBytes
     * @param downloadId
     * @param soFarBytes
     * @param totalBytes
     * @param preProgress
     */
    public void onStart(int downloadId, long soFarBytes, long totalBytes, int preProgress) {

    }

    /**
     * 下载进度回调	soFarBytes
     * @param downloadId
     * @param soFarBytes
     * @param totalBytes
     * @param progress
     */
    public void onProgress(int downloadId, long soFarBytes, long totalBytes, int progress) {

    }

    /**
     * 等待
     * @param downloadId
     */
    public void onWait(int downloadId) {
    }

    /**
     * paused	暂停下载	soFarBytes
     * error	下载出现错误	抛出的Throwable
     * @param downloadId
     * @param soFarBytes
     * @param totalBytes
     * @param progress
     */
    public void onStop(int downloadId, long soFarBytes, long totalBytes, int progress) {

    }

    /**
     * 完成整个下载过程
     * @param downloadId
     * @param path
     */
    public void onFinish(int downloadId, String path) {

    }
}
