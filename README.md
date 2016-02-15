# FileDownloaderFinal简介
一个Android文件下载管理库，具有扩展性强、简单易用的特性。是基于[FileDownloader](https://github.com/lingochamp/FileDownloader "FileDownloader")强大的下载引擎。

#如何使用
* **1、下载FileDownloaderFinal**

通过Gradle抓取:
<pre>
compile 'cn.finalteam:filedownloaderfinal:1.0.0'//请稍等~请求已发送到jcenter等待审核
</pre>
* 在App Application中添加一下配置
<pre>
private void initDownloaderManager() {
    //下载文件所保存的目录
    File storeFile = StorageUtils.getCacheDirectory(this, false, "FileDownloader");
    if (!storeFile.exists()) {
        storeFile.mkdirs();
    }

    final DownloaderManagerConfiguration.Builder dmBulder = new DownloaderManagerConfiguration.Builder(this)
            .setMaxDownloadingCount(3) //配置最大并行下载任务数，配置范围[1-100]
            .setDbExtField(...) //配置数据库扩展字段
            .setDbVersion(...)//配置数据库版本
            .setDbUpgradeListener(...) //配置数据库更新回调
            .setDownloadStorePath(storeFile.getAbsolutePath()); //配置下载文件存储目录

    //初始化下载管理最好放到线程中去执行防止卡顿情况
    new Thread() {
        @Override
        public void run() {
            super.run();
            DownloaderManager.getInstance().init(dmBulder.build());//必要语句
        }
    }.start();
}
</pre>

* 添加任务
<pre>
DownloaderManager.getInstance().addTask(url);
</pre>
* 执行任务
<pre>
DownloaderManager.getInstance().startTask(downloadId, myFileDownloadListener);
</pre>
* 暂停任务
<pre>
DownloaderManager.getInstance().pauseTask(downloadId);
</pre>
* 删除任务
<pre>
DownloaderManager.getInstance().deleteTask(downloadId);
</pre>
#扩展功能
##数据库表扩展
* 字段扩展

开发者可以添加一些扩展字段来满足业务需求，注意不能包含id,path,url，因为框架内部有定义不得重复
<pre>
Map<String, String> dbExFeildMap = new HashMap<>();
dbExFeildMap.put("title", "VARCHAR");
dbExFeildMap.put("icon", "VARCHAR");
</pre>
然后在DownloaderManagerConfiguration.Builder中配置setDbExtField即可

* 数据库更新

通过改变DownloaderManagerConfiguration.Builder的setDbVersion来修改数据库版本，setDbUpgradeListener来设置数据库更新监听

##简单文件下载（不加入下载管理）
<pre>
private void simpleDownloadFile() {
    String url = "http://cdn6.down.apk.gfan.com/asdf/Pfiles/2015/08/13/1055068_1960ffcc-f122-49e1-b13f-a5b35049e7f5.apk";
    SimpleFileDownloader.downloadFile(url, new FileDownloaderCallback());
}
</pre>

##添加带扩展字段的任务
<pre>
String url3_apk = "http://cdn6.down.apk.gfan.com/asdf/Pfiles/2015/12/04/1087087_676144cf-a7dc-45df-8b7e-d56ddacf04f9.apk";
String url3_icon = "http://cdn2.image.apk.gfan.com/asdf/PImages/2015/12/04/ldpi_ae934eca-1f51-4e3a-9d26-acd982352f57.png";
String url3_name = "奔跑吧兄弟3-撕名牌大战";
FileDownloaderModel model = new FileDownloaderModel();
model.setUrl(url3_apk);
model.putExtField("icon", url3_icon);
model.putExtField("title", url3_name);//在application中配置的扩展字段名
//添加到下载管理+
DownloaderManager.getInstance().addTask(model);
</pre>

#历史版本
## V1.0.0
* 文件下载管理功能
* 添加数据库表模型可扩展
* 支持多事件实例回调
* 添加简单文件下载（不加入下载管理器）
* 添加下载管理与FileDownloader引擎的事件桥接
* ……


#关于作者
大家如果对项目或项目文档有哪些意见和想法都可以发邮箱或QQ群联系到我

* **QQ:**172340021   
* **QQ群:**218801658  
* **Email:**<pengjianbo@finalteam.cn>

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.