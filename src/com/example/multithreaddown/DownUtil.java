package com.example.multithreaddown;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Ϊ��ʵ�ֶ��̣߳�����ɰ����²�����У�
 * 1������URL����
 * 2����ȡָ��URL������ָ����Դ�Ĵ�С����getContentLength()����ʵ�֣����˴��õ���HttpURLConnection�ࡣ
 * 3���ڱ��ش����ϴ���һ����������Դ��ͬ��С�Ŀ��ļ���
 * 4������ÿ���߳�Ӧ������������Դ���ĸ����֣����ĸ��ֽڿ�ʼ�����ĸ��ֽڽ�������
 * 5�����δ��������������߳�������������Դ��ָ�����֡�
 */

public class DownUtil {

	private String path; //����������Դ��·��
	private String targetFile; //ָ���������ļ��ı���λ��
	private int threadNum; //������Ҫʹ�ö����߳�������Դ
	private DownThread[] threads; //�������ص��̶߳���
	private int fileSize; //�������ص��ļ����ܴ�С
	
	public DownUtil(String path, String targetFile, int threadNum) 
	{
		this.path = path;
		this.targetFile = targetFile;
		threads = new DownThread[threadNum];  //��ʼ��threads����
		this.threadNum = threadNum;
	}
	
	public void download() throws Exception
	{
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		
		conn.setConnectTimeout(5*1000);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
				+ "appliction/x-shockwave-flash, application/xaml+xml, "
				+ "appliction/vnd.ms-xpsdocument, application/x-ms-xbap, "
				+ "appliction/x-ms-applicatin, application/vnd.ms-excel, "
				+ "appliction/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
		
		fileSize = conn.getContentLength(); //�õ��ļ���С
		conn.disconnect();
		int currentPartSize = fileSize / threadNum + 1;
		RandomAccessFile file = new RandomAccessFile(targetFile, "rw");
		file.setLength(fileSize); //���ñ����ļ��Ĵ�С
		file.close();
		
		for (int i = 0; i < threadNum; i++)
		{
			int startPosition = i * currentPartSize; //����ÿ���̵߳����صĿ�ʼλ��
			RandomAccessFile currentPart = new RandomAccessFile(targetFile, "rw");//ÿ���߳�ʹ��һ��RandomAccessFile��������
			currentPart.seek(startPosition); //������̵߳�����λ��
			threads[i] = new DownThread(startPosition, currentPartSize, currentPart); //���������߳�
			threads[i].start(); //���������߳�
		}
	}
	
	//��ȡ�����̵߳���ɰٷֱ�
	public double getCompleteRate() 
	{
		int sumSize = 0;     //ͳ�ƶ����߳��Ѿ����ص��ܴ�С
		for (int i = 0; i < threadNum; i++)
		{
			sumSize += threads[i].length;
		}
		return sumSize * 1.0 / fileSize; //�����Ѿ���ɵİٷֱ�
	}
	
	private class DownThread extends Thread 
	{
		private int startPosition; //��ǰ�̵߳�����λ��
		private int currentPartSize; //���嵱ǰ�̸߳������ص��ļ���С
		private RandomAccessFile currentPart; //��ǰ�߳���Ҫ���ص��ļ���
		public int length; //�����Ѿ����߳������ص��ֽ���
		
		public DownThread(int startPosition, int currentPartSize, RandomAccessFile currentPart)
		{
			this.startPosition = startPosition;
			this.currentPartSize = currentPartSize;
			this.currentPart = currentPart;
		}
		
		@Override
		public void run() //�����Զ����Դ��������
		{
			try 
			{
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(5*1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, "
						+ "appliction/x-shockwave-flash, application/xaml+xml, "
						+ "appliction/vnd.ms-xpsdocument, application/x-ms-xbap, "
						+ "appliction/x-ms-applicatin, application/vnd.ms-excel, "
						+ "appliction/vnd.ms-powerpoint, application/msword, */*");
				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Charset", "UTF-8");
				
				InputStream inStream = conn.getInputStream();
				inStream.skip(this.startPosition); //����startPosition���ֽڣ��������߳�ֻ�����Լ�������Ĳ����ļ�
				byte[] buffer = new byte[1024];
				int hasRead = 0;
				
				//��ȡ�������ݣ���д�뱾���ļ�
				while (length < currentPartSize && (hasRead = inStream.read(buffer)) > 0)
				{
					currentPart.write(buffer, 0, hasRead);
					length += hasRead;  //�ۼƸ��߳����ص��ܴ�С
				}
				
				currentPart.close();
				inStream.close();
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
