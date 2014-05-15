
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

class DemoGLSurfaceView extends GLSurfaceView_SDL 
{
	public DemoGLSurfaceView(GameActivity context) 
	{
		super(context);
		mParent = context;
		setEGLConfigChooser(Globals.VideoDepthBpp, Globals.NeedDepthBuffer, Globals.NeedStencilBuffer, Globals.NeedGles2);
		mRenderer = new DemoRenderer(context);
		setRenderer(mRenderer);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) 
	{
		DifferentTouchInput.touchInput.process(event);
		if( DemoRenderer.mRatelimitTouchEvents )
		{
			limitEventRate(event);
		}
		return true;
	};

	@Override
	public boolean onGenericMotionEvent (final MotionEvent event)
	{
		DifferentTouchInput.touchInput.processGenericEvent(event);
		if (DemoRenderer.mRatelimitTouchEvents)
		{
			limitEventRate(event);
		}
		return true;
	}
	
	public void limitEventRate(final MotionEvent event)
	{
		// Wait a bit, and try to synchronize to app framerate, or event thread will eat all CPU and we'll lose FPS
		// With Froyo the rate of touch events seems to be limited by OS, but they are arriving faster then we're redrawing anyway
		if((event.getAction() == MotionEvent.ACTION_MOVE ||
			event.getAction() == MotionEvent.ACTION_HOVER_MOVE))
		{
			synchronized(mRenderer)
			{
				try
				{
					mRenderer.wait(300L); // And sometimes the app decides not to render at all, so this timeout should not be big.
				} catch (InterruptedException e) { }
			}
		}
	}

	public void exitApp() 
	{
		mRenderer.exitApp();
	};

	@Override
	public void onPause() 
	{
		Log.i("SDL", "libSDL: DemoGLSurfaceView.onPause(): mRenderer.mGlSurfaceCreated " + mRenderer.mGlSurfaceCreated + " mRenderer.mPaused " + mRenderer.mPaused + (mRenderer.mPaused ? " - not doing anything" : ""));
		if (mRenderer.mPaused)
		{
			return;
		}
		mRenderer.mPaused = true;
		mRenderer.nativeGlContextLostAsyncEvent();
		
		if (mRenderer.accelerometer != null) // For some reason it crashes here often - are we getting this event before initialization?
		{
			mRenderer.accelerometer.stop();
		}
		
		super.onPause();
	};
	
	public boolean isPaused() 
	{
		return mRenderer.mPaused;
	}

	@Override
	public void onResume() 
	{
		Log.i("SDL", "libSDL: DemoGLSurfaceView.onResume(): mRenderer.mGlSurfaceCreated " + mRenderer.mGlSurfaceCreated + " mRenderer.mPaused " + mRenderer.mPaused + (!mRenderer.mPaused ? " - not doing anything" : ""));
		
		if(!mRenderer.mPaused)
		{
			return;
		}
		
		mRenderer.mPaused = false;
		super.onResume();
		
		if (mRenderer.mGlSurfaceCreated 
			&& ! mRenderer.mPaused 
			|| Globals.NonBlockingSwapBuffers)
		{
			mRenderer.nativeGlContextRecreated();
		}
		
		if (mRenderer.accelerometer != null 
			&& mRenderer.accelerometer.openedBySDL) // For some reason it crashes here often - are we getting this event before initialization?
		{
			mRenderer.accelerometer.start();
		}
	};

	DemoRenderer mRenderer;
	GameActivity mParent;

	public static native void nativeMotionEvent( int x, int y, int action, int pointerId, int pressure, int radius );
	public static native int nativeKey( int keyCode, int down, int unicode );
	public static native void initJavaCallbacks();
	public static native void nativeHardwareMouseDetected( int detected );
	public static native void nativeMouseButtonsPressed( int buttonId, int pressedState );
	public static native void nativeMouseWheel( int scrollX, int scrollY );
	public static native void nativeGamepadAnalogJoystickInput( float stick1x,  float stick1y, float stick2x, float stick2y, float rtrigger, float ltrigger );
}


