
package com.octodev.cannonball;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.InputDevice;
import android.view.Window;
import android.view.WindowManager;
import android.os.Environment;
import java.io.File;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import android.content.res.AssetManager;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.util.Log;

import android.widget.TextView;
import java.lang.Thread;
import java.util.concurrent.locks.ReentrantLock;
import android.os.Build;
import java.lang.reflect.Method;
import java.util.LinkedList;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class DemoRenderer extends GLSurfaceView_SDL.Renderer
{
	public DemoRenderer(GameActivity _context)
	{
		context = _context;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceCreated(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart );
		mGlSurfaceCreated = true;
		mGl = gl;
		if( ! mPaused && ! mFirstTimeStart )
			nativeGlContextRecreated();
		mFirstTimeStart = false;
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceChanged(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart + " w " + w + " h " + h);
		if( w < h && Globals.HorizontalOrientation )
		{
			// Sometimes when Android awakes from lockscreen, portrait orientation is kept
			int x = w;
			w = h;
			h = x;
		}
		mWidth = w;
		mHeight = h;
		mGl = gl;
		nativeResize(w, h, Globals.KeepAspectRatio ? 1 : 0);
	}
	
	public void onSurfaceDestroyed() 
	{
		Log.i("SDL", "libSDL: DemoRenderer.onSurfaceDestroyed(): paused " + mPaused + " mFirstTimeStart " + mFirstTimeStart);
		mGlSurfaceCreated = false;
		mGlContextLost = true;
		nativeGlContextLost();
	};

	public void onDrawFrame(GL10 gl) 
	{
		mGl = gl;
		SwapBuffers();

		nativeInitJavaCallbacks();
		
		// Make main thread priority lower so audio thread won't get underrun
		// Thread.currentThread().setPriority((Thread.currentThread().getPriority() + Thread.MIN_PRIORITY)/2);
		
		mGlContextLost = false;

		if (Globals.CompatibilityHacksStaticInit)
		{
			GameActivity.LoadApplicationLibrary(context);
		}

		//Settings.Apply(context);
		//Settings.nativeSetEnv( "DISPLAY_RESOLUTION_WIDTH", String.valueOf(Math.max(mWidth, mHeight)) );
		//Settings.nativeSetEnv( "DISPLAY_RESOLUTION_HEIGHT", String.valueOf(Math.min(mWidth, mHeight)) ); // In Kitkat with immersive mode, getWindowManager().getDefaultDisplay().getMetrics() return inaccurate height

		accelerometer = new AccelerometerReader(context);
		// Tweak video thread priority, if user selected big audio buffer
		if(Globals.AudioBufferConfig >= 2)
		{
			Thread.currentThread().setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2); // Lower than normal
		}

		nativeInitKeymap();
		
        AssetManager mgr = GameActivity.instance.GetAssetManager();
        if (mgr == null)
        {
            Log.i("SDL", "libSDL: DemoRenderer.onDrawFrame(): AssetManager is null");
        }
        else   
        {
            Log.i("SDL", "libSDL: DemoRenderer.onDrawFrame(): AssetManager is assigned");
        }
        
        // Calls main() and never returns, hehe - we'll call eglSwapBuffers() from native code
		nativeInit(mgr);
            /*
            Globals.DataDir,
			Globals.CommandLine,
			((Globals.SwVideoMode && Globals.MultiThreadedVideo) || Globals.CompatibilityHacksVideo) ? 1 : 0,
			Globals.RedirectStdout ? 1 : 0);
            */
		System.exit(0); // The main() returns here - I don't bother with deinit stuff, just terminate process
	}

	public int swapBuffers() // Called from native code
	{
		if( ! super.SwapBuffers() && Globals.NonBlockingSwapBuffers )
		{
			if(mRatelimitTouchEvents)
			{
				synchronized(this)
				{
					this.notify();
				}
			}
			return 0;
		}

		if (mGlContextLost) 
		{
			mGlContextLost = false;
			super.SwapBuffers();
		}

		// Unblock event processing thread only after we've finished rendering
		if (mRatelimitTouchEvents)
		{
			synchronized(this)
			{
				this.notify();
			}
		}
		return 1;
	}
	
	
	public void showScreenKeyboardWithoutTextInputField() // Called from native code
	{
		class Callback implements Runnable
		{
			public GameActivity parent;
			public void run()
			{
				parent.showScreenKeyboardWithoutTextInputField();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public void showScreenKeyboard(final String oldText, int sendBackspace) // Called from native code
	{
		class Callback implements Runnable
		{
			public GameActivity parent;
			public String oldText;
			public boolean sendBackspace;
			public void run()
			{
				parent.showScreenKeyboard(oldText, sendBackspace);
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		cb.oldText = oldText;
		cb.sendBackspace = (sendBackspace != 0);
		context.runOnUiThread(cb);
	}

	public void hideScreenKeyboard() // Called from native code
	{
		class Callback implements Runnable
		{
			public GameActivity parent;
			public void run()
			{
				parent.hideScreenKeyboard();
			}
		}
		Callback cb = new Callback();
		cb.parent = context;
		context.runOnUiThread(cb);
	}

	public int isScreenKeyboardShown() // Called from native code
	{
		return context.isScreenKeyboardShown() ? 1 : 0;
	}

	public void setScreenKeyboardHintMessage(String s)
	{
		context.setScreenKeyboardHintMessage(s);
	}

	public void startAccelerometerGyroscope(int started)
	{
		accelerometer.openedBySDL = (started != 0);
		if( accelerometer.openedBySDL && !mPaused )
			accelerometer.start();
		else
			accelerometer.stop();
	}

	public void exitApp()
	{
		 nativeDone();
	}

	public void getAdvertisementParams(int params[])
	{
		context.getAdvertisementParams(params);
	}
	public void setAdvertisementVisible(int visible)
	{
		context.setAdvertisementVisible(visible);
	}
	public void setAdvertisementPosition(int left, int top)
	{
		context.setAdvertisementPosition(left, top);
	}
	public void requestNewAdvertisement()
	{
		context.requestNewAdvertisement();
	}
	
	private int PowerOf2(int i)
	{
		int value = 1;
		while (value < i)
		{
			value <<= 1;
		}
		return value;
	}
	
	private native void nativeInitJavaCallbacks();
	private native void nativeInit(AssetManager mgr);//String CurrentPath, String CommandLine, int multiThreadedVideo, int isDebuggerConnected);
	private native void nativeResize(int w, int h, int keepAspectRatio);
	private native void nativeDone();
	private native void nativeGlContextLost();
	public native void nativeGlContextRecreated();
	public native void nativeGlContextLostAsyncEvent();
	private static native void nativeInitKeymap();
	public static native void nativeTextInput( int ascii, int unicode );
	public static native void nativeTextInputFinished();

	private GameActivity context = null;
	public AccelerometerReader accelerometer = null;
	
	private GL10 mGl = null;
	private EGL10 mEgl = null;
	private EGLDisplay mEglDisplay = null;
	private EGLSurface mEglSurface = null;
	private EGLContext mEglContext = null;
	private boolean mGlContextLost = false;
	public boolean mGlSurfaceCreated = false;
	public boolean mPaused = false;
	private boolean mFirstTimeStart = true;
	public int mWidth = 0;
	public int mHeight = 0;

	public static final boolean mRatelimitTouchEvents = true; //(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);
}