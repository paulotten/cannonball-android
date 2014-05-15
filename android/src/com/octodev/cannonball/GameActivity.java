/*
Simple DirectMedia Layer
Java source code (C) 2009-2012 Sergii Pylypenko
  
This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
  
1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required. 
2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/

package com.octodev.cannonball;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.content.res.Configuration;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.View.OnKeyListener;
import android.view.MenuItem;
import android.view.Menu;
import android.view.Gravity;
import android.text.method.TextKeyListener;
import java.util.LinkedList;
import java.io.SequenceInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Set;
import android.text.SpannedString;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import android.view.inputmethod.InputMethodManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import java.util.concurrent.Semaphore;
import android.content.pm.ActivityInfo;
import android.view.Display;
import android.text.InputType;
import android.util.Log;
import android.content.res.Resources;
import android.content.res.AssetManager;

public class GameActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRequestedOrientation(Globals.HorizontalOrientation ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		instance = this;
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (Globals.InhibitSuspend)
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		Log.i("SDL", "libSDL: Creating startup screen");
		_layout = new LinearLayout(this);
		_layout.setOrientation(LinearLayout.VERTICAL);
		_layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

		ImageView img = new ImageView(this);

		img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		_layout.addView(img);
		
		_videoLayout = new FrameLayout(this);
		_videoLayout.addView(_layout);
		
		setContentView(_videoLayout);

		LoadLibraries();

		LoadApplicationLibrary(this);
		
		mAudioThread = new AudioThread(this);

		initSDL();
	}

	public void initSDL()
	{
		(new Thread(new Runnable()
		{
			public void run()
			{
				while (isCurrentOrientationHorizontal() != Globals.HorizontalOrientation)
				{
					Log.i("SDL", "libSDL: Waiting for screen orientation to change - the device is probably in the lockscreen mode");
					try 
					{
						Thread.sleep(500);
					} 
					catch (Exception e) {}

					if (_isPaused)
					{
						Log.i("SDL", "libSDL: Application paused, cancelling SDL initialization until it will be brought to foreground");
						return;
					}
				}
				runOnUiThread(new Runnable()
				{
					public void run()
					{
						initSDLInternal();
					}
				});
			}
		})).start();
	}

	private void initSDLInternal()
	{
		if (sdlInited)
		{
			return;
		}
		
		Log.i("SDL", "libSDL: Initializing video and SDL application");
		
		sdlInited = true;
		_inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		_videoLayout = new FrameLayout(this);
		setContentView(_videoLayout);
		m_GLView = new DemoGLSurfaceView(this);
		_videoLayout.addView(m_GLView);
		m_GLView.setFocusableInTouchMode(true);
		m_GLView.setFocusable(true);
		m_GLView.requestFocus();
	}

	@Override
	protected void onPause() 
	{
		_isPaused = true;
		if (m_GLView != null)
		{
			m_GLView.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		if (m_GLView != null)
		{
			m_GLView.onResume();
		}
		_isPaused = false;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{
		super.onWindowFocusChanged(hasFocus);
		Log.i("SDL", "libSDL: onWindowFocusChanged: " + hasFocus + " - sending onPause/onResume");
		if (hasFocus == false)
		{
			onPause();
		}
		else
		{
			onResume();
		}
	}
	
	public boolean isPaused()
	{
		return _isPaused;
	}

	@Override
	protected void onDestroy()
	{
		if (m_GLView != null)
		{
			m_GLView.exitApp();
		}
		
		super.onDestroy();
		try
		{
			Thread.sleep(2000); // The event is sent asynchronously, allow app to save it's state, and call exit() itself.
		} 
		catch (InterruptedException lException) {}
		
		System.exit(0);
	}

	public void showScreenKeyboardWithoutTextInputField()
	{
	}

	public void showScreenKeyboard(final String oldText, boolean sendBackspace)
	{
	};

	public void hideScreenKeyboard()
	{
	};

	public boolean isScreenKeyboardShown()
	{
		return false;
	};
	
	public void setScreenKeyboardHintMessage(String s)
	{
	}

	public void setAdvertisementPosition(int x, int y)
	{
	}
	
	public void setAdvertisementVisible(final int visible)
	{
	}

	public void getAdvertisementParams(int params[])
	{
	}
	
	public void requestNewAdvertisement()
	{
	}
	
	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event)
	{
		if (m_GLView != null)
		{
			if (m_GLView.nativeKey(keyCode, 1, event.getUnicodeChar()) == 0)
			{
				return super.onKeyDown(keyCode, event);
			}
		}
		else if (keyListener != null)
		{
			keyListener.onKeyEvent(keyCode);
		}
		return true;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event)
	{
		if (m_GLView != null)
		{
			if (m_GLView.nativeKey(keyCode, 0, event.getUnicodeChar()) == 0)
			{
				return super.onKeyUp(keyCode, event);
			}
		}
		return true;
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, final KeyEvent event)
	{
		if (m_GLView != null 
			&& event.getCharacters() != null)
		{
			for (int i = 0; i < event.getCharacters().length(); ++i)
			{
				m_GLView.nativeKey( event.getKeyCode(), 1, event.getCharacters().codePointAt(i) );
				m_GLView.nativeKey( event.getKeyCode(), 0, event.getCharacters().codePointAt(i) );
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev)
	{
		if(m_GLView != null)
		{
			m_GLView.onTouchEvent(ev);
		}
		else if( touchListener != null )
		{
			touchListener.onTouchEvent(ev);
		}
		return true;
	}
	
	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent ev)
	{
		if (m_GLView != null)
		{
			m_GLView.onGenericMotionEvent(ev);
		}
		return true;
	}

	public void LoadLibraries()
	{
		try
		{
			if(Globals.NeedGles2)
			{
				System.loadLibrary("GLESv2");
			}
			Log.i("SDL", "libSDL: loaded GLESv2 lib");
		}
		catch ( UnsatisfiedLinkError e )
		{
			Log.i("SDL", "libSDL: Cannot load GLESv2 lib");
		}

		try
		{
			for (String l : Globals.AppLibraries)
			{
				Log.i("SDL", l);
				
				try
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(getFilesDir().getAbsolutePath() + "/../lib/" + libname);
					Log.i("SDL", "libSDL: loading lib " + libpath.getAbsolutePath());
					System.load(libpath.getPath());
				}
				catch (UnsatisfiedLinkError e)
				{
					Log.i("SDL", "libSDL: error loading lib " + l + ": " + e.toString());
					try
					{
						String libname = System.mapLibraryName(l);
						File libpath = new File(getFilesDir().getAbsolutePath() + "/" + libname);
						Log.i("SDL", "libSDL: loading lib " + libpath.getAbsolutePath());
						System.load(libpath.getPath());
					}
					catch( UnsatisfiedLinkError ee )
					{
						Log.i("SDL", "libSDL: error loading lib " + l + ": " + ee.toString());
						System.loadLibrary(l);
					}
				}
			}
		}
		catch (UnsatisfiedLinkError e)
		{
			try 
			{
				Log.i("SDL", "libSDL: Extracting APP2SD-ed libs");
				
				InputStream in = null;
				try
				{
					for (int i = 0; ; i++)
					{
						InputStream in2 = getAssets().open("bindata" + String.valueOf(i));
						if (in == null)
						{
							in = in2;
						}
						else
						{
							in = new SequenceInputStream( in, in2 );
						}
					}
				}
				catch( IOException ee ) { }

				if( in == null )
					throw new RuntimeException("libSDL: Extracting APP2SD-ed libs failed, the .apk file packaged incorrectly");

				ZipInputStream zip = new ZipInputStream(in);

				File libDir = getFilesDir();
				try {
					libDir.mkdirs();
				} catch( SecurityException ee ) { };
				
				byte[] buf = new byte[16384];
				while(true)
				{
					ZipEntry entry = null;
					entry = zip.getNextEntry();
					/*
					if( entry != null )
						Log.i("SDL", "Extracting lib " + entry.getName());
					*/
					if (entry == null)
					{
						Log.i("SDL", "Extracting libs finished");
						break;
					}
					if (entry.isDirectory())
					{
						File outDir = new File(libDir.getAbsolutePath() + "/" + entry.getName());
						if (!(outDir.exists() && outDir.isDirectory()))
						{
							outDir.mkdirs();
						}
						continue;
					}

					OutputStream out = null;
					String path = libDir.getAbsolutePath() + "/" + entry.getName();
					try 
					{
						File outDir = new File(path.substring(0, path.lastIndexOf("/")));
						if (!(outDir.exists() && outDir.isDirectory()))
						{
							outDir.mkdirs();
						}
					} 
					catch (SecurityException eeeee) { };

					Log.i("SDL", "Saving to file '" + path + "'");

					out = new FileOutputStream(path);
					int len = zip.read(buf);
					while (len >= 0)
					{
						if (len > 0)
						{
							out.write(buf, 0, len);
						}
						len = zip.read(buf);
					}

					out.flush();
					out.close();
				}

				for (String l : Globals.AppLibraries)
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(libDir, libname);
					Log.i("SDL", "libSDL: loading lib " + libpath.getPath());
					System.load(libpath.getPath());
					libpath.delete();
				}
			}
			catch ( Exception ee )
			{
				Log.i("SDL", "libSDL: Error: " + ee.toString());
			}
		}

		// ----- VCMI hack -----
		String [] binaryZipNames = { "binaries-" + android.os.Build.CPU_ABI + ".zip", "binaries-" + android.os.Build.CPU_ABI2 + ".zip", "binaries.zip" };
		for(String binaryZip: binaryZipNames)
		{
			try {
				Log.i("SDL", "libSDL: Trying to extract binaries from assets " + binaryZip);
				
				InputStream in = null;
				try
				{
					for( int i = 0; ; i++ )
					{
						InputStream in2 = getAssets().open(binaryZip + String.format("%02d", i));
						if( in == null )
							in = in2;
						else
							in = new SequenceInputStream( in, in2 );
					}
				}
				catch( IOException ee )
				{
					try
					{
						if( in == null )
							in = getAssets().open(binaryZip);
					}
					catch( IOException eee ) {}
				}

				if( in == null )
					throw new RuntimeException("libSDL: Extracting binaries failed, the .apk file packaged incorrectly");

				ZipInputStream zip = new ZipInputStream(in);

				File libDir = getFilesDir();
				try {
					libDir.mkdirs();
				} catch( SecurityException ee ) { };
				
				byte[] buf = new byte[16384];
				while(true)
				{
					ZipEntry entry = null;
					entry = zip.getNextEntry();
					/*
					if( entry != null )
						Log.i("SDL", "Extracting lib " + entry.getName());
					*/
					if( entry == null )
					{
						Log.i("SDL", "Extracting binaries finished");
						break;
					}
					if( entry.isDirectory() )
					{
						File outDir = new File( libDir.getAbsolutePath() + "/" + entry.getName() );
						if( !(outDir.exists() && outDir.isDirectory()) )
							outDir.mkdirs();
						continue;
					}

					OutputStream out = null;
					String path = libDir.getAbsolutePath() + "/" + entry.getName();
					try 
					{
						File outDir = new File( path.substring(0, path.lastIndexOf("/") ));
						if( !(outDir.exists() && outDir.isDirectory()) )
							outDir.mkdirs();
					} 
					catch (SecurityException eeeeeee) { };

					try 
					{
						CheckedInputStream check = new CheckedInputStream(new FileInputStream(path), new CRC32());
						while (check.read(buf, 0, buf.length) > 0) {};
						check.close();
						if (check.getChecksum().getValue() != entry.getCrc())
						{
							File ff = new File(path);
							ff.delete();
							throw new Exception();
						}
						Log.i("SDL", "File '" + path + "' exists and passed CRC check - not overwriting it");
						continue;
					} 
					catch (Exception eeeeee) { }

					Log.i("SDL", "Saving to file '" + path + "'");

					out = new FileOutputStream(path);
					int len = zip.read(buf);
					while (len >= 0)
					{
						if (len > 0)
						{
							out.write(buf, 0, len);
						}
						len = zip.read(buf);
					}

					out.flush();
					out.close();
				}
				break;
			}
			catch (Exception eee)
			{
				//Log.i("SDL", "libSDL: Error: " + eee.toString());
			}
		}
		// ----- VCMI hack -----
	};

	public static void LoadApplicationLibrary(final Context context)
	{
		for (String l : Globals.AppMainLibraries)
		{
			try
			{
				String libname = System.mapLibraryName(l);
				File libpath = new File(context.getFilesDir().getAbsolutePath() + "/../lib/" + libname);
				Log.i("SDL", "libSDL: loading lib " + libpath.getAbsolutePath());
				System.load(libpath.getPath());
			}
			catch (UnsatisfiedLinkError e)
			{
				Log.i("SDL", "libSDL: error loading lib " + l + ": " + e.toString());
				try
				{
					String libname = System.mapLibraryName(l);
					File libpath = new File(context.getFilesDir().getAbsolutePath() + "/" + libname);
					Log.i("SDL", "libSDL: loading lib " + libpath.getAbsolutePath());
					System.load(libpath.getPath());
				}
				catch (UnsatisfiedLinkError ee)
				{
					Log.i("SDL", "libSDL: error loading lib " + l + ": " + ee.toString());
					System.loadLibrary(l);
				}
			}
		}
	}

	public int getApplicationVersion()
	{
		try 
		{
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			return packageInfo.versionCode;
		} 
		catch (PackageManager.NameNotFoundException e)
		{
			Log.i("SDL", "libSDL: Cannot get the version of our own package: " + e);
		}
		return 0;
	}
    
    public AssetManager GetAssetManager()
    {
        return getResources().getAssets();
    }

	public boolean isCurrentOrientationHorizontal()
	{
		Display getOrient = getWindowManager().getDefaultDisplay();
		return getOrient.getWidth() >= getOrient.getHeight();
	}

	public FrameLayout getVideoLayout() { return _videoLayout; }

	static int NOTIFY_ID = 12367098; // Random ID

	private static DemoGLSurfaceView m_GLView = null;
	private static AudioThread mAudioThread = null;

	private LinearLayout _layout = null;
	private LinearLayout _layout2 = null;

	private FrameLayout _videoLayout = null;
	private boolean sdlInited = false;

	public interface TouchEventsListener
	{
		public void onTouchEvent(final MotionEvent ev);
	}

	public interface KeyEventsListener
	{
		public void onKeyEvent(final int keyCode);
	}

	public TouchEventsListener touchListener = null;
	public KeyEventsListener keyListener = null;
	boolean _isPaused = false;
	private InputMethodManager _inputManager = null;

	public LinkedList<Integer> textInput = new LinkedList<Integer> ();
	
	public static GameActivity instance = null;
}
