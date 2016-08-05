package com.example.multithreaddown;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * 为了实现多线程，程序可按如下步骤进行；
 * 1、创建URL对象。
 * 2、获取指定URL对象所指向资源的大小（由getContentLength()方法实现），此处用到了HttpURLConnection类。
 * 3、在本地磁盘上创建一个与网络资源相同大小的空文件。
 * 4、计算每条线程应该下载网络资源的哪个部分（从哪个字节开始，到哪个字节结束）。
 * 5、依次创建、启动多条线程来下载网络资源的指定部分。
 */

public class DownUtil {

	private String path; //定义下载资源的路径
	private String targetFile; //指定所下载文件的保存位置
	private int threadNum; //定义需要使用多少线程下载资源
	private DownThread[] threads; //定义下载的线程对象
	private int fileSize; //定义下载的文件的总大小
	
	public DownUtil(String path, String targetFile, int threadNum) 
	{
		this.path = path;
		this.targetFile = targetFile;
		threads = new DownThread[threadNum];  //初始化threads数组
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
		
		fileSize = conn.getContentLength(); //得到文件大小
		conn.disconnect();
		int currentPartSize = fileSize / threadNum + 1;
		RandomAccessFile file = new RandomAccessFile(targetFile, "rw");
		file.setLength(fileSize); //设置本地文件的大小
		file.close();
		
		for (int i = 0; i < threadNum; i++)
		{
			int startPosition = i * currentPartSize; //计算每条线程的下载的开始位置
			RandomAccessFile currentPart = new RandomAccessFile(targetFile, "rw");//每个线程使用一个RandomAccessFile进行下载
			currentPart.seek(startPosition); //定义该线程的下载位置
			threads[i] = new DownThread(startPosition, currentPartSize, currentPart); //创建下载线程
			threads[i].start(); //启动下载线程
		}
	}
	
	//获取下载线程的完成百分比
	public double getCompleteRate() 
	{
		int sumSize = 0;     //统计多条线程已经下载的总大小
		for (int i = 0; i < threadNum; i++)
		{
			sumSize += threads[i].length;
		}
		return sumSize * 1.0 / fileSize; //返回已经完成的百分比
	}
	
	private class DownThread extends Thread 
	{
		private int startPosition; //当前线程的下载位置
		private int currentPartSize; //定义当前线程负责下载的文件大小
		private RandomAccessFile currentPart; //当前线程需要下载的文件块
		public int length; //定义已经该线程已下载的字节数
		
		public DownThread(int startPosition, int currentPartSize, RandomAccessFile currentPart)
		{
			this.startPosition = startPosition;
			this.currentPartSize = currentPartSize;
			this.currentPart = currentPart;
		}
		
		@Override
		public void run() //负责打开远程资源的输入流
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
				inStream.skip(this.startPosition); //跳过startPosition个字节，表明该线程只下载自己负责的哪部分文件
				byte[] buffer = new byte[1024];
				int hasRead = 0;
				
				//读取网络数据，并写入本地文件
				while (length < currentPartSize && (hasRead = inStream.read(buffer)) > 0)
				{
					currentPart.write(buffer, 0, hasRead);
					length += hasRead;  //累计该线程下载的总大小
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
