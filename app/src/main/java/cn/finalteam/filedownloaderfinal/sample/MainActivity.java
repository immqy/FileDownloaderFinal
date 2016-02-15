package cn.finalteam.filedownloaderfinal.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.liulishuo.filedownloader.FileDownloadConnectListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.finalteam.filedownloaderfinal.DownloaderManager;
import cn.finalteam.filedownloaderfinal.FileDownloaderCallback;
import cn.finalteam.filedownloaderfinal.FileDownloaderModel;
import cn.finalteam.filedownloaderfinal.SimpleFileDownloader;
import cn.finalteam.toolsfinal.CountDownTimer;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.lv_task_list)
    ListView mLvTaskList;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.btn_simple_download)
    Button mBtnSimpleDownload;
    @Bind(R.id.pb_simple_download)
    ProgressBar mPbSimpleDownload;
    @Bind(R.id.fab)
    FloatingActionButton mFab;

    private DownloadManagerListAdapter mDownloadManagerListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);

        mDownloadManagerListAdapter = new DownloadManagerListAdapter(this);
        mLvTaskList.setAdapter(mDownloadManagerListAdapter);

        new CountDownTimer(2000, 2000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                DownloaderManager.getInstance().addServiceConnectListener(fileDownloadConnectListener);
                addDownloadTask();
                mDownloadManagerListAdapter.notifyDataSetChanged();

                cancel();
            }
        }.start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        destroy();
        super.onDestroy();
    }

    private void destroy() {
        DownloaderManager.getInstance().removeServiceConnectListener(fileDownloadConnectListener);
        DownloaderManager.getInstance().onDestroy();
    }

    /**
     * 连接下载服务事件监听
     */
    FileDownloadConnectListener fileDownloadConnectListener = new FileDownloadConnectListener() {
        @Override
        public void connected() {
            notifyDataSetChanged();
        }

        @Override
        public void disconnected() {
            notifyDataSetChanged();
        }
    };

    private void notifyDataSetChanged() {
        if (!isFinishing() && mDownloadManagerListAdapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDownloadManagerListAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    /**
     * 添加任务到下载管理器中
     */
    private void addDownloadTask() {

        //=====================1===================
        //最简单的添加下载任务
        String url1 = "http://219.128.78.33/apk.r1.market.hiapk.com/data/upload/2015/05_20/14/com.speedsoftware.rootexplorer_140220.apk";
        String url2 = "http://cdn6.down.apk.gfan.com/asdf/Pfiles/2015/10/23/1073061_071b1d56-7518-48b4-bb18-73a019d72941.apk";

        DownloaderManager.getInstance().addTask(url1);
        DownloaderManager.getInstance().addTask(url2);

        //=====================2===================
        //添加下载任务带扩展字段的
        String url3_apk = "http://cdn6.down.apk.gfan.com/asdf/Pfiles/2015/12/04/1087087_676144cf-a7dc-45df-8b7e-d56ddacf04f9.apk";
        String url3_icon = "http://cdn2.image.apk.gfan.com/asdf/PImages/2015/12/04/ldpi_ae934eca-1f51-4e3a-9d26-acd982352f57.png";
        String url3_name = "奔跑吧兄弟3-撕名牌大战";
        FileDownloaderModel model = new FileDownloaderModel();
        model.setUrl(url3_apk);
        model.putExtField("icon", url3_icon);
        model.putExtField("title", url3_name);//在application中配置的扩展字段名
        //添加到下载管理+
        DownloaderManager.getInstance().addTask(model);
    }

    @OnClick(R.id.btn_simple_download)
    public void onSimpleDownloadBtnClick(View view) {
        simpleDownloadFile();
    }

    /**
     * 简单下载文件，不列入下载下载管理
     */
    private void simpleDownloadFile() {
        String url = "http://cdn6.down.apk.gfan.com/asdf/Pfiles/2015/08/13/1055068_1960ffcc-f122-49e1-b13f-a5b35049e7f5.apk";
        SimpleFileDownloader.downloadFile(url,
                new FileDownloaderCallback() {
                    @Override
                    public void onProgress(int downloadId, long soFarBytes, long totalBytes, int progress) {
                        super.onProgress(downloadId, soFarBytes, totalBytes, progress);
                        ILogger.i("downloading download=%d, progress=%d", downloadId, progress);
                        mPbSimpleDownload.setProgress(progress);
                    }

                    @Override
                    public void onFinish(int downloadId, String path) {
                        super.onFinish(downloadId, path);
                        ILogger.i("download success path=%s", path);
                        mPbSimpleDownload.setProgress(100);
                        Toast.makeText(getBaseContext(), "下载完成", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
