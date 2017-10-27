ErrorWatcher
==================

--------------------
    一个可以帮助开发者捕获app异常crash的工具类，使用此类可以在app发生未捕获的异常时进行可控的操作
    使用了此类，当app发生异常时，不再弹出常见的crash弹窗，而是跳转到一个开发者指定的activity，或者直接退出程序

    对于发生异常时的日志文件，可以选在上传到server以供分析，或者保存到本地文件，或者直接由开发者接受处理

一般用法：
------------------
### 1.保存日志信息到文件

    ErrorWatcher errorWatcher = new ErrorWatcher(this);
            //获取日志文件的路径
            String filePath = errorWatcher.getLogFilePath();
            errorWatcher.setErrorMsgHandleMode(ErrorWatcher.ErrorMsgHandleMode.FILE)  //设置log信息的处理模式为文件
                    .setfileNamePrefix("log") //设置log文件的文件名前缀
                    .setActivity(Main2Activity.class) //设置crash时跳转到的activity
                    .startWatcher(); //开始监听捕获
                
### 2.上传日志信息到server，目前只支持post
		    ErrorWatcher errorWatcher = new ErrorWatcher(this);
        		    errorWatcher.setErrorMsgHandleMode(ErrorWatcher.ErrorMsgHandleMode.REPORT_TO_SERVER)
                		    .setUrlAndParas("http://xxx.xxx.xxx","errorlog")  //设置server的URL和post的key，value为日志信息，参考注释
                	      .setActivity(Main2Activity.class)
                		    .startWatcher();
                
### 3.开发者自己处理error信息

    ErrorWatcher errorWatcher = new ErrorWatcher(this);
            errorWatcher.setErrorMsgHandleMode(ErrorWatcher.ErrorMsgHandleMode.USER)
                    .setOnCrashCatchedListener(new ErrorWatcher.OnCrashCatchListener() {
                       @Override
                        public void crashOccurred(JSONObject crashInfo) {
                            //处理crashInfo
                        }
                    })
                    .setActivity(Main2Activity.class)
                    .startWatcher();

### 其他模式：LOG，输出错误日志到终端，适用于debug阶段
          
