package com.blueserial;

import java.io.*;
import java.util.*;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.text.*;

// ?�스??로그 ?�성 ?�틸리티
// 1.?�티비티??onCreate?�서 init(this) ?�출?�고 ?�성 ?�정
// 2.로그�??�길 ??TextLog.o("~") 메서???�출
// 3.로그 ?�인??TextLog.ViewLog() 메서???�출
public class TextLog_Mov {
	static Context mMain;
	static final int LOG_FILE = 1;
	static final int LOG_SYSTEM = 2;
	static int mWhere = LOG_FILE | LOG_SYSTEM;
	// 기록 경로. ?�폴?�는 SD 루트??andlog.txt?�나 ?��? 경로�?�?�� �?��
	// SD 카드�??�는 경우 "/data/data/?�키�?files/?�일" 경로�?�?��??�?
	static String mPath = "";
	static String mTag = "textlog_Mov_ard";
	static boolean mAppendTime = false;
	static float mViewTextSize = 6.0f;
	static int mMaxFileSize = 100;			// KB
	static boolean mReverseReport = false;
	static long mStartTime;
	static long mLastTime;
		
	// mPath??SD카드??루트�?기본 초기?�한?? SD 카드�??�으�?�?문자?�이??
	static {
		boolean HaveSD = Environment.getExternalStorageState()
		.equals(Environment.MEDIA_MOUNTED);
		if (HaveSD) {
			String SDPath = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
			Calendar calendar = Calendar.getInstance();
			String startTimeFileName = String.format("%02d%02d%02d%02d%02d", calendar.get(Calendar.MONTH)+1, 
					calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
			startTimeFileName = startTimeFileName+"Shink.txt";
			//mPath = SDPath + "/" + "Shink.txt";
			mPath = SDPath + "/" + startTimeFileName;
		}
	}

	// ?�폴???�정??�� 초기?�한??
	public static void init(Context main) {
		mMain = main;
	
		// ?�정 ?�기 ?�상?�면 로그 ?�일???��?분을 ?�라?�다.
//		if (mMaxFileSize != 0 && (mWhere & LOG_FILE) != 0) {
//			File file = new File(mPath);
//			if (file.length() > mMaxFileSize * 1024) {
//				String log = "";
//				try {
//					FileInputStream fis = new FileInputStream(mPath);
//					int avail = fis.available();
//					byte[] data = new byte[avail];
//					while (fis.read(data) != -1) {;}
//					fis.close();
//					log = new String(data);
//				}
//				catch (Exception e) {;}
//				
//				// ?�쪽 90%�??�라?�다.
//				log = log.substring(log.length() * 9 / 10);
//				
//				try {
//					FileOutputStream fos = new FileOutputStream(file);
//					fos.write(log.getBytes());
//					fos.close();
//				} 
//				catch (Exception e) {;}
//				
//			}
//		}

		o("---------- start time : " + getNowTime());
	}

	// 로그 ?�일????��?�여 초기?�한??
	public static void reset() {
		if ((mWhere & LOG_FILE) != 0) {
			File file = new File(mPath);
			file.delete();
		}
		o("---------- reset time : " + getNowTime());
	}
	
	static String getNowTime() {
		Calendar calendar = Calendar.getInstance();
		String Time = String.format("%d-%d %d:%d:%d",calendar.get(Calendar.MONTH)+1, 
				calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
		return Time;
	}

	// write string to log.
	public static void o(String text, Object ... args) {
		// 릴리즈에??로그 기록문을 ?�거?�으�?바로 리턴?�다.
		if (mWhere == 0) {
			Log.d("TAG", "e1");
			return;
		}
		
		// ?�외??getMessage�?null??리턴?�는 경우�??�어 ???��? ?�요?�다.
		if (text == null) {
			Log.d("TAG", "e2");
			return;
		}
		
		if (args.length != 0) {
			Log.d("TAG", "e3");
			text = String.format(text, args);
		}

		if (mAppendTime) {
			Calendar calendar = Calendar.getInstance();
			String Time = String.format("%d:%d:%02d.%03d = ", 
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 
					calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
			text = Time + text;
		}

		if ((mWhere & LOG_FILE) != 0 && mPath.length() != 0) {
			Log.d("TAG", "e5");
			File file = new File(mPath);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file, true);
				if (fos != null) {
					Log.d("TAG", "e6");
					fos.write(text.getBytes());
					fos.write("\n".getBytes());
				}
			} 
			catch (Exception e) {
				Log.d("TAG", "e7");
				// silent fail
			}
			finally {
				try {
					if(fos != null) 
					{
						Log.d("TAG", "e8");
						fos.close();
					}
				}
				catch (Exception e) 
				{ 
					Log.d("TAG", "e9");
				}
			}
		}

		if ((mWhere & LOG_SYSTEM) != 0) {
			Log.d("mTag", text);
		}
	}
	
	public static void lapstart(String text) {
		mStartTime = System.currentTimeMillis();
		mLastTime = mStartTime;
		o("St=0000,gap=0000 " + text);
	}
	
	public static void lap(String text) {
		long now = System.currentTimeMillis();
		String sText = String.format("St=%4d,gap=%4d " + text, 
			now - mStartTime, now - mLastTime);
		mLastTime = now;
		o(sText);
	}

	// 로그 ?�일 보기
	public static void ViewLog() {
		String path;
		int ch;

		StringBuilder Result = new StringBuilder();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(mPath));
			if (in != null) {
				for (;;) {
					ch = in.read();
					if (ch == -1) break;
					Result.append((char)ch);
				}
			}
		}
		catch (Exception e) {
			Result.append("log file not found");
		}
		finally {
			try {
				if(in != null) in.close();
			}
			catch (Exception e) { ; }
		}

		String sResult = Result.toString();
		if (mReverseReport) {
			String[] lines = sResult.split("\n");
			Result.delete(0, Result.length());
			for (int i = lines.length - 1;i >= 0; i--) {
				Result.append(lines[i]);
				Result.append("\n");
			}
			sResult = Result.toString();
		}
		
		ScrollView scroll = new ScrollView(mMain); 
		TextView text = new TextView(mMain);
		text.setTextSize(TypedValue.COMPLEX_UNIT_PT, mViewTextSize);
		text.setTextColor(Color.WHITE);
		text.setText("length = " + sResult.length() + "\n" + sResult);
		scroll.addView(text);

		new AlertDialog.Builder(mMain)
		.setTitle("Log")
		.setView(scroll)
		.setPositiveButton("OK", null)
		.show();        
	}

	public static void addMenu(Menu menu) {
		menu.add(0,101092+1,0,"ViewLog");
		menu.add(0,101092+2,0,"ResetLog");
	}

	public static boolean execMenu(MenuItem item) {
		switch (item.getItemId()) {
		case 101092+1:
			ViewLog();
			return true;
		case 101092+2:
			reset();
			return true;
		}
		return false;
	}
}

//?��? ?�키�?��??간단?�게 ?�출?????�는 ?�퍼 ?�래??
//TextLog.o() ??�� lg.o()�??�출 �?��?�다.
class lg_Mov {
	public static void o(String text, Object ... args) {
		TextLog_Mov.o(text, args);
	}
}

