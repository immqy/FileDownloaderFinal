package cn.finalteam.filedownloaderfinal.sample;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.finalteam.filedownloaderfinal.DbUpgradeListener;
import cn.finalteam.filedownloaderfinal.DownloaderManager;
import cn.finalteam.filedownloaderfinal.DownloaderManagerConfiguration;
import cn.finalteam.toolsfinal.StorageUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2016/2/2 0002 14:33
 */
public class IApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initDownloaderManager();
    }

    /**
     * 初始化下载管理
     */
    private void initDownloaderManager() {
        //初始化下载管理
        //数据库表扩展字段，开发者可以添加一些扩展字段来满足业务需求，注意不能包含id,path,url，因为框架内部有定义不得重复
        Map<String, String> dbExFeildMap = new HashMap<>();
        dbExFeildMap.put("title", "VARCHAR");
        dbExFeildMap.put("icon", "VARCHAR");

        //下载文件所保存的目录
        File storeFile = StorageUtils.getCacheDirectory(this, false, "FileDownloader");
        if (!storeFile.exists()) {
            storeFile.mkdirs();
        }

        final DownloaderManagerConfiguration.Builder dmBulder = new DownloaderManagerConfiguration.Builder(this)
                .setMaxDownloadingCount(3) //配置最大并行下载任务数，配置范围[1-100]
                .setDbExtField(dbExFeildMap) //配置数据库扩展字段
                .setDbVersion(1)//配置数据库版本
                .setDbUpgradeListener(dbUpgradeListener) //配置数据库更新回调
                .setDownloadStorePath(storeFile.getAbsolutePath()); //配置下载文件存储目录

        //以上都是可选配置，如果开发者想使用默认配置可以DownloaderManagerConfiguration.Builder dmBulder = new DownloaderManagerConfiguration.Builder(this);即可

        //初始化下载管理,最好放到线程去执行，因为里面有操作数据库的可能会引起卡顿
        new Thread() {
            @Override
            public void run() {
                super.run();
                DownloaderManager.getInstance().init(dmBulder.build());//必要语句
            }
        }.start();
    }

    /**
     * 更新数据库监听
     */
    private DbUpgradeListener dbUpgradeListener = new DbUpgradeListener() {
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    };
}
