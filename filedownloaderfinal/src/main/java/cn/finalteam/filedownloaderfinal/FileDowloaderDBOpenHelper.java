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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

import cn.finalteam.toolsfinal.StringUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/1/20 0020 15:41
 */
class FileDowloaderDBOpenHelper extends SQLiteOpenHelper {
    final static String DATABASE_NAME = "filedownloaderfinal.db";
    final Map<String, String> mDbExtFieldMap;
    private DbUpgradeListener mDbUpgradeListener;

    public FileDowloaderDBOpenHelper(Context context, final int dbVersion,
                                     Map<String, String> dbExtFieldMap,
                                     DbUpgradeListener dbUpgradeListener) {
        super(context, DATABASE_NAME, null, dbVersion);
        this.mDbExtFieldMap = dbExtFieldMap;
        this.mDbUpgradeListener = dbUpgradeListener;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Map<String, String> allFieldMap = new HashMap<>();
        allFieldMap.put(FileDownloaderModel.ID, "INTEGER PRIMARY KEY");
        allFieldMap.put(FileDownloaderModel.URL, "VARCHAR");
        allFieldMap.put(FileDownloaderModel.PATH, "VARCHAR");
        if(mDbExtFieldMap != null){
            allFieldMap.putAll(mDbExtFieldMap);
        }

        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("CREATE TABLE IF NOT EXISTS " + FileDownloaderDBController.TABLE_NAME);
        sqlSb.append("(");

        int index = 0;
        int max = allFieldMap.size();
        for (Map.Entry<String, String> entry : allFieldMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            index ++;
            if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                continue;
            }
            sqlSb.append(key);
            sqlSb.append(" ");
            sqlSb.append(value);
            if ( index != max ) {
                sqlSb.append(",");
            }
        }
        sqlSb.append(")");
        db.execSQL(sqlSb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (mDbUpgradeListener != null) {
            mDbUpgradeListener.onUpgrade(db, oldVersion, newVersion);
        }
    }
}
