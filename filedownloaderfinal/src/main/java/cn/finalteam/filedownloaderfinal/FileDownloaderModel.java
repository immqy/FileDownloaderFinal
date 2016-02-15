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

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;
import java.util.Map;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/1/20 0020 15:37
 */
public class FileDownloaderModel implements Serializable{
    public final static String ID = "id";
    public final static String URL = "url";
    public final static String PATH = "path";

    private int id;
    private String url;
    private String path;
    //扩展字段键值对
    private ContentValues extFieldCv = new ContentValues();

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(URL, url);
        cv.put(PATH, path);
        Map<String, String> extFieldMap = DownloaderManager.getInstance().getDbExtFieldMap();
        if (extFieldMap == null || extFieldMap.size() == 0) {
            return cv;
        }
        for (Map.Entry<String, String> entry : extFieldMap.entrySet()) {
            String key = entry.getKey();
            if ( key == null ) {
                continue;
            }

            String value = extFieldCv.getAsString(key);
            cv.put(key, value);
        }
        return cv;
    }

    public void putExtField(String key, String value) {
        if ( key == null ) {
            return;
        }
        if(value == null){
            value = "";
        }
        extFieldCv.put(key, value);
    }

    public String getExtFieldValue(String key) {
        return extFieldCv.getAsString(key);
    }

    void parseExtField(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            return;
        }
        Map<String, String> extFieldMap = DownloaderManager.getInstance().getDbExtFieldMap();
        if (extFieldMap == null || extFieldMap.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : extFieldMap.entrySet()) {
            String key = entry.getKey();
            if ( key == null ) {
                continue;
            }
            String value = cursor.getString(cursor.getColumnIndex(key));
            extFieldCv.put(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileDownloaderModel) {
            FileDownloaderModel model = (FileDownloaderModel) o;
            if (model != null && model.getId() == getId()) {
                return true;
            }
        }
        return false;
    }
}
