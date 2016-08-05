package com.example.multithreaddown;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MultiThreadDown extends Activity 
{	
	private EditText url;
	private EditText target;
	private Button down;
	private ProgressBar bar;
	DownUtil downUtil;
	private int mDownStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        url = (EditText)findViewById(R.id.url);
        target = (EditText)findViewById(R.id.target);
        down = (Button)findViewById(R.id.down);
        bar = (ProgressBar)findViewById(R.id.bar);
        
        //创建一个Handler对象
        final Handler handler = new Handler()
        {
			@Override
			public void handleMessage(Message msg) 
			{
				if (msg.what == 0x123)
				{
					bar.setProgress(mDownStatus);
				}
			}
        	
        };
        
        down.setOnClickListener(new OnClickListener() 
        {	
			@Override
			public void onClick(View v) 
			{
				//初始化DownUtil对象（最后一个参数指定线程数）
				downUtil = new DownUtil(url.getText().toString(), target.getText().toString(), 6);
				
				new Thread()
				{
					@Override
					public void run() 
					{
						try 
						{
							downUtil.download(); //开始下载（使用DownUtil来控制程序下载）
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
						
						final Timer timer = new Timer(); //定义每秒调度获取一次系统的完成进度
						timer.schedule(new TimerTask() //程序启动一个定时器，该定时器控制每隔0.1秒查询一次下载进度
						{	
							@Override
							public void run() 
							{
								double completeRate = downUtil.getCompleteRate(); //获取下载任务的完成比率
								mDownStatus = (int) (completeRate * 100);
								handler.sendEmptyMessage(0x123); //发送消息通知界面更新进度条
								
								//下载完全后取消任务调度
								if (mDownStatus >= 100)
								{
									timer.cancel();
								}
							}
						}, 0, 100);
						
					}
					
				}.start();
			}
		});
        
    }
}
