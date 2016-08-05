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
        
        //����һ��Handler����
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
				//��ʼ��DownUtil�������һ������ָ���߳�����
				downUtil = new DownUtil(url.getText().toString(), target.getText().toString(), 6);
				
				new Thread()
				{
					@Override
					public void run() 
					{
						try 
						{
							downUtil.download(); //��ʼ���أ�ʹ��DownUtil�����Ƴ������أ�
						} 
						catch (Exception e) {
							e.printStackTrace();
						}
						
						final Timer timer = new Timer(); //����ÿ����Ȼ�ȡһ��ϵͳ����ɽ���
						timer.schedule(new TimerTask() //��������һ����ʱ�����ö�ʱ������ÿ��0.1���ѯһ�����ؽ���
						{	
							@Override
							public void run() 
							{
								double completeRate = downUtil.getCompleteRate(); //��ȡ�����������ɱ���
								mDownStatus = (int) (completeRate * 100);
								handler.sendEmptyMessage(0x123); //������Ϣ֪ͨ������½�����
								
								//������ȫ��ȡ���������
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
