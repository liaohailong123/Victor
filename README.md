# Victor

Victor是一个网络请求库（Android）
基本功能：
1，文件下载（断点） + 文件上传
2，短时间内：海量文本数据请求
3，手动移除队列中的请求
4，数据缓存，优化流量
5，全局的Http请求首部字段
6，全局的拦截器

Step1: 在第一个Activity中初始化



    private void init() {
        //初始化需要SD卡写入权限，控制缓存
        if (Util.requestPermissionIfNeed(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "", 0)) {
            String cacheDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            //初始化基本配置
            Victor.getInstance().initConfig(getApplicationContext())//生成全局配置对象
                    .createCacheDirectory(cacheDir, 50 * 1024 * 1024)//创建缓存路径，xxx/xxx/victor；设置最大存储容量
                    .setConnectTimeout(3 * 1000)//全局请求默认的连接超时
                    .setReadTimeout(3 * 1000)//全局请求默认的读取超时
                    .setLogEnable(true)//是否开启Log打印
                    .addInterceptor(new Interceptor() {
                        @Override
                        public void process(Request<?> request) {
                            Log.i("Victor", request.toString());
                        }
                    });//设置拦截器，所有通过网络的请求都会回调
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }



Step2: 文本请求



        Victor.getInstance().with(this)
                .newTextRequest()//创建一个文本数据的请求
                .doPost()//选择Http请求方式
                .setUrl(url)//设置Url
                .setUseCache(true)//是否使用缓存
                .setUseCookie(true)//是否开启Cookie
                .addParam("param", param)//添加参数
                .enqueue(new TextCallback<JsonObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess(JsonObject result) {
                        //do something you want
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onFailure(int code, String error) {
                        //do something you want
                    }
                });//添加进请求队列中



Step3: 文件下载(如果资源服务器支持请求部分资源，response code == 206，自动开启断点下载)



        Victor.getInstance().with(this)
                .newDownloadRequest()//创建一个文件下载请求
                .setUrl("url")//设置文件URL
                .doGet()//使用GET请求
                .setConnectTimeOut((int) DateUtils.DAY_IN_MILLIS)//设置连接超时，建议写大一点
                .setReadTimeOut((int) DateUtils.DAY_IN_MILLIS)//设置读取超时，建议写大一点
                .enqueue(new FileCallback() {
                    @Override
                    public void onPreLoading(String url) {
                        //do something you want
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLoading(String url, String tempFilePath, int progress) {
                        //do something you want; progress is [0,100]
                    }

                    @Override
                    public void onPostLoaded(String url, String resultInfo) {
                       //do something you want
                    }

                    @Override
                    public void onLoadingError(String url, String info) {
                        //do something you want
                    }
                });//添加进请求队列中



Step4: 文件上传



        Victor.getInstance().with(this)
                .newUploadRequest()//创建一个文件上传请求
                .setUrl("url")//设置文件URL
                .addFile("file", new File(filePath))//添加需要上传的文件（仅支持单个文件上传）
                .addParam("param", param)//添加参数
                .setConnectTimeOut((int) DateUtils.DAY_IN_MILLIS)//设置连接超时，建议写大一点
                .setReadTimeOut((int) DateUtils.DAY_IN_MILLIS)//设置读取超时，建议写大一点
                .enqueue(new FileCallback() {

                    @Override
                    public void onPreLoading(String url) {
                        //do something you want
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLoading(String url, String tempFilePath, int progress) {
                        //do something you want; progress is [0,100]
                    }

                    @Override
                    public void onPostLoaded(String url, String resultInfo) {
                        //do something you want
                    }

                    @Override
                    public void onLoadingError(String url, String info) {
                        //do something you want
                    }
                });//添加进请求队列中



Step5:移除已经提交的，但是又未开始的请求任务



        Victor.getInstance().removeRequest(this);//清除在当前Activity or Fragment 添加的请求任务
        Victor.getInstance().release();//清除所有任务，释放所有线程资源