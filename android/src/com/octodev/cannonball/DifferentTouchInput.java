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

abstract class DifferentTouchInput
{
	public abstract void process(final MotionEvent event);
	public abstract void processGenericEvent(final MotionEvent event);

	public static int ExternalMouseDetected = 0;
	
	public static DifferentTouchInput touchInput = getInstance();

	public static DifferentTouchInput getInstance()
	{
		boolean multiTouchAvailable1 = false;
		boolean multiTouchAvailable2 = false;
		// Not checking for getX(int), getY(int) etc 'cause I'm lazy
		Method methods [] = MotionEvent.class.getDeclaredMethods();
		for(Method m: methods) 
		{
			if( m.getName().equals("getPointerCount") )
				multiTouchAvailable1 = true;
			if( m.getName().equals("getPointerId") )
				multiTouchAvailable2 = true;
		}
		try {
			Log.i("SDL", "Device: " + android.os.Build.DEVICE);
			Log.i("SDL", "Device name: " + android.os.Build.DISPLAY);
			Log.i("SDL", "Device model: " + android.os.Build.MODEL);
			Log.i("SDL", "Device board: " + android.os.Build.BOARD);
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH )
			{
				if( DetectCrappyDragonRiseDatexGamepad() )
					return CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad.Holder.sInstance;
				//return IcsTouchInput.Holder.sInstance;
				return AutoDetectTouchInput.Holder.sInstance;
			}
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD )
				return GingerbreadTouchInput.Holder.sInstance;
			if (multiTouchAvailable1 && multiTouchAvailable2)
				return MultiTouchInput.Holder.sInstance;
			else
				return SingleTouchInput.Holder.sInstance;
		} catch( Exception e ) {
			try {
				if (multiTouchAvailable1 && multiTouchAvailable2)
					return MultiTouchInput.Holder.sInstance;
				else
					return SingleTouchInput.Holder.sInstance;
			} catch( Exception ee ) {
				return SingleTouchInput.Holder.sInstance;
			}
		}
	}
	private static boolean DetectCrappyDragonRiseDatexGamepad()
	{
		if( CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad.Holder.sInstance == null )
			return false;
		return CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad.Holder.sInstance.detect();
	}

	private static class SingleTouchInput extends DifferentTouchInput
	{
		private static class Holder
		{
			private static final SingleTouchInput sInstance = new SingleTouchInput();
		}
		@Override
		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
		public void process(final MotionEvent event)
		{
			int action = -1;
			if( event.getAction() == MotionEvent.ACTION_DOWN )
				action = Mouse.SDL_FINGER_DOWN;
			if( event.getAction() == MotionEvent.ACTION_UP )
				action = Mouse.SDL_FINGER_UP;
			if( event.getAction() == MotionEvent.ACTION_MOVE )
				action = Mouse.SDL_FINGER_MOVE;
			if ( action >= 0 )
				DemoGLSurfaceView.nativeMotionEvent( (int)event.getX(), (int)event.getY(), action, 0, 
												(int)(event.getPressure() * 1024.0f),
												(int)(event.getSize() * 1024.0f) );
		}
	}
	private static class MultiTouchInput extends DifferentTouchInput
	{
		public static final int TOUCH_EVENTS_MAX = 16; // Max multitouch pointers

		private class touchEvent
		{
			public boolean down = false;
			public int x = 0;
			public int y = 0;
			public int pressure = 0;
			public int size = 0;
		}
		
		protected touchEvent touchEvents[];
		
		MultiTouchInput()
		{
			touchEvents = new touchEvent[TOUCH_EVENTS_MAX];
			for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
				touchEvents[i] = new touchEvent();
		}
		
		private static class Holder
		{
			private static final MultiTouchInput sInstance = new MultiTouchInput();
		}

		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
		public void process(final MotionEvent event)
		{
			int action = -1;

			//Log.i("SDL", "Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY());
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_CANCEL )
			{
				action = Mouse.SDL_FINGER_UP;
				for( int i = 0; i < TOUCH_EVENTS_MAX; i++ )
				{
					if( touchEvents[i].down )
					{
						touchEvents[i].down = false;
						DemoGLSurfaceView.nativeMotionEvent( touchEvents[i].x, touchEvents[i].y, action, i, touchEvents[i].pressure, touchEvents[i].size );
					}
				}
			}
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN )
			{
				action = Mouse.SDL_FINGER_DOWN;
				for( int i = 0; i < event.getPointerCount(); i++ )
				{
					int id = event.getPointerId(i);
					if( id >= TOUCH_EVENTS_MAX )
						id = TOUCH_EVENTS_MAX - 1;
					touchEvents[id].down = true;
					touchEvents[id].x = (int)event.getX(i);
					touchEvents[id].y = (int)event.getY(i);
					touchEvents[id].pressure = (int)(event.getPressure(i) * 1024.0f);
					touchEvents[id].size = (int)(event.getSize(i) * 1024.0f);
					DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
				}
			}
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
			{
				/*
				String s = "MOVE: ptrs " + event.getPointerCount();
				for( int i = 0 ; i < event.getPointerCount(); i++ )
				{
					s += " p" + event.getPointerId(i) + "=" + (int)event.getX(i) + ":" + (int)event.getY(i);
				}
				Log.i("SDL", s);
				*/
				int pointerReleased = -1;
				if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP )
					pointerReleased = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

				for( int id = 0; id < TOUCH_EVENTS_MAX; id++ )
				{
					int ii;
					for( ii = 0; ii < event.getPointerCount(); ii++ )
					{
						if( id == event.getPointerId(ii) )
							break;
					}
					if( ii >= event.getPointerCount() )
					{
						// Up event
						if( touchEvents[id].down )
						{
							action = Mouse.SDL_FINGER_UP;
							touchEvents[id].down = false;
							DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
						}
					}
					else
					{
						if( pointerReleased == id && touchEvents[pointerReleased].down )
						{
							action = Mouse.SDL_FINGER_UP;
							touchEvents[id].down = false;
						}
						else if( touchEvents[id].down )
						{
							action = Mouse.SDL_FINGER_MOVE;
						}
						else
						{
							action = Mouse.SDL_FINGER_DOWN;
							touchEvents[id].down = true;
						}
						touchEvents[id].x = (int)event.getX(ii);
						touchEvents[id].y = (int)event.getY(ii);
						touchEvents[id].pressure = (int)(event.getPressure(ii) * 1024.0f);
						touchEvents[id].size = (int)(event.getSize(ii) * 1024.0f);
						DemoGLSurfaceView.nativeMotionEvent( touchEvents[id].x, touchEvents[id].y, action, id, touchEvents[id].pressure, touchEvents[id].size );
					}
				}
			}
		}
	}
	private static class GingerbreadTouchInput extends MultiTouchInput
	{
		private static class Holder
		{
			private static final GingerbreadTouchInput sInstance = new GingerbreadTouchInput();
		}

		GingerbreadTouchInput()
		{
			super();
		}
		public void process(final MotionEvent event)
		{
			int hwMouseEvent = ((event.getSource() & InputDevice.SOURCE_STYLUS) == InputDevice.SOURCE_STYLUS) ? Mouse.MOUSE_HW_INPUT_STYLUS :
								((event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) ? Mouse.MOUSE_HW_INPUT_MOUSE :
								Mouse.MOUSE_HW_INPUT_FINGER;
			if( ExternalMouseDetected != hwMouseEvent )
			{
				ExternalMouseDetected = hwMouseEvent;
				DemoGLSurfaceView.nativeHardwareMouseDetected(hwMouseEvent);
			}
			super.process(event);
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_MOVE ) // Support bluetooth/USB mouse - available since Android 3.1
			{
				int action;
				// TODO: it is possible that multiple pointers return that event, but we're handling only pointer #0
				if( touchEvents[0].down )
					action = Mouse.SDL_FINGER_UP;
				else
					action = Mouse.SDL_FINGER_HOVER;
				touchEvents[0].down = false;
				touchEvents[0].x = (int)event.getX();
				touchEvents[0].y = (int)event.getY();
				touchEvents[0].pressure = 1024;
				touchEvents[0].size = 0;
				//if( event.getAxisValue(MotionEvent.AXIS_DISTANCE) != 0.0f )
				InputDevice device = InputDevice.getDevice(event.getDeviceId());
				if( device != null && device.getMotionRange(MotionEvent.AXIS_DISTANCE) != null &&
					device.getMotionRange(MotionEvent.AXIS_DISTANCE).getRange() > 0.0f )
					touchEvents[0].pressure = (int)((event.getAxisValue(MotionEvent.AXIS_DISTANCE) -
							device.getMotionRange(MotionEvent.AXIS_DISTANCE).getMin()) * 1024.0f / device.getMotionRange(MotionEvent.AXIS_DISTANCE).getRange());
				DemoGLSurfaceView.nativeMotionEvent( touchEvents[0].x, touchEvents[0].y, action, 0, touchEvents[0].pressure, touchEvents[0].size );
			}
		}
		public void processGenericEvent(final MotionEvent event)
		{
			process(event);
		}
	}
	private static class IcsTouchInput extends GingerbreadTouchInput
	{
		private static class Holder
		{
			private static final IcsTouchInput sInstance = new IcsTouchInput();
		}
		private int buttonState = 0;
		public void process(final MotionEvent event)
		{
			//Log.i("SDL", "Got motion event, type " + (int)(event.getAction()) + " X " + (int)event.getX() + " Y " + (int)event.getY() + " buttons " + buttonState + " source " + event.getSource());
			int buttonStateNew = event.getButtonState();
			if( buttonStateNew != buttonState )
			{
				for( int i = 1; i <= MotionEvent.BUTTON_FORWARD; i *= 2 )
				{
					if( (buttonStateNew & i) != (buttonState & i) )
						DemoGLSurfaceView.nativeMouseButtonsPressed(i, ((buttonStateNew & i) == 0) ? 0 : 1);
				}
				buttonState = buttonStateNew;
			}
			super.process(event); // Push mouse coordinate first
		}
		public void processGenericEvent(final MotionEvent event)
		{
			// Joysticks are supported since Honeycomb, but I don't care about it, because very little devices have it
			if( (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK )
			{
				DemoGLSurfaceView.nativeGamepadAnalogJoystickInput(
					event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y),
					event.getAxisValue(MotionEvent.AXIS_Z), event.getAxisValue(MotionEvent.AXIS_RZ),
					event.getAxisValue(MotionEvent.AXIS_RTRIGGER), event.getAxisValue(MotionEvent.AXIS_LTRIGGER) );
				return;
			}
			// Process mousewheel
			if( event.getAction() == MotionEvent.ACTION_SCROLL )
			{
				int scrollX = Math.round(event.getAxisValue(MotionEvent.AXIS_HSCROLL));
				int scrollY = Math.round(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
				DemoGLSurfaceView.nativeMouseWheel(scrollX, scrollY);
				return;
			}
			super.processGenericEvent(event);
		}
	}
	private static class IcsTouchInputWithHistory extends IcsTouchInput
	{
		private static class Holder
		{
			private static final IcsTouchInputWithHistory sInstance = new IcsTouchInputWithHistory();
		}
		public void process(final MotionEvent event)
		{
			int ptr = 0; // Process only one touch event, because that's typically a pen/mouse
			for( ptr = 0; ptr < TOUCH_EVENTS_MAX; ptr++ )
			{
				if( touchEvents[ptr].down )
					break;
			}
			if( ptr >= TOUCH_EVENTS_MAX )
				ptr = 0;
			//Log.i("SDL", "Got motion event, getHistorySize " + (int)(event.getHistorySize()) + " ptr " + ptr);

			for( int i = 0; i < event.getHistorySize(); i++ )
			{
				DemoGLSurfaceView.nativeMotionEvent( (int)event.getHistoricalX(i), (int)event.getHistoricalY(i),
					Mouse.SDL_FINGER_MOVE, ptr, (int)( event.getHistoricalPressure(i) * 1024.0f ), (int)( event.getHistoricalSize(i) * 1024.0f ) );
			}
			super.process(event); // Push mouse coordinate first
		}
	}
	private static class CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad extends IcsTouchInput
	{
		private static class Holder
		{
			private static final CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad sInstance = new CrappyDragonRiseDatexGamepadInputWhichNeedsItsOwnHandlerBecauseImTooCheapAndStupidToBuyProperGamepad();
		}
		float hatX = 0.0f, hatY = 0.0f;
		public void processGenericEvent(final MotionEvent event)
		{
			// Joysticks are supported since Honeycomb, but I don't care about it, because very little devices have it
			if( (event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK )
			{
				// event.getAxisValue(AXIS_HAT_X) and event.getAxisValue(AXIS_HAT_Y) are joystick arrow keys, they also send keyboard events
				DemoGLSurfaceView.nativeGamepadAnalogJoystickInput(
					event.getAxisValue(MotionEvent.AXIS_X), event.getAxisValue(MotionEvent.AXIS_Y),
					event.getAxisValue(MotionEvent.AXIS_RX), event.getAxisValue(MotionEvent.AXIS_RZ),
					0, 0);
				if( event.getAxisValue(MotionEvent.AXIS_HAT_X) != hatX )
				{
					hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X);
					if( hatX == 0.0f )
					{
						DemoGLSurfaceView.nativeKey(KeyEvent.KEYCODE_DPAD_LEFT, 0, 0);
						DemoGLSurfaceView.nativeKey(KeyEvent.KEYCODE_DPAD_RIGHT, 0, 0);
					}
					else
						DemoGLSurfaceView.nativeKey(hatX < 0.0f ? KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT, 1, 0);
				}
				if( event.getAxisValue(MotionEvent.AXIS_HAT_Y) != hatY )
				{
					hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y);
					if( hatY == 0.0f )
					{
						DemoGLSurfaceView.nativeKey(KeyEvent.KEYCODE_DPAD_UP, 0, 0);
						DemoGLSurfaceView.nativeKey(KeyEvent.KEYCODE_DPAD_DOWN, 0, 0);
					}
					else
						DemoGLSurfaceView.nativeKey(hatY < 0.0f ? KeyEvent.KEYCODE_DPAD_UP : KeyEvent.KEYCODE_DPAD_DOWN, 1, 0);
				}
				return;
			}
			super.processGenericEvent(event);
		}
		public boolean detect()
		{
			int[] devIds = InputDevice.getDeviceIds();
			for( int id : devIds )
			{
				InputDevice device = InputDevice.getDevice(id);
				if( device == null )
					continue;
				System.out.println("libSDL: input device ID " + id + " type " + device.getSources()  + " name " + device.getName() );
				if( (device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD &&
					(device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK && 
					device.getName().indexOf("DragonRise Inc") == 0 )
				{
					System.out.println("libSDL: Detected crappy DragonRise gamepad, enabling special hack for it. Please press button labeled 'Analog', otherwise it won't work, because it's cheap and crappy");
					return true;
				}
			}
			return false;
		}
	}
	private static class CrappyMtkTabletWithBrokenTouchDrivers extends IcsTouchInput
	{
		private static class Holder
		{
			private static final CrappyMtkTabletWithBrokenTouchDrivers sInstance = new CrappyMtkTabletWithBrokenTouchDrivers();
		}
		public void process(final MotionEvent event)
		{
			if( (event.getAction() & MotionEvent.ACTION_MASK) != MotionEvent.ACTION_HOVER_MOVE ) // Ignore hover events, they are broken
				super.process(event);
		}
		public void processGenericEvent(final MotionEvent event)
		{
			if( (event.getAction() & MotionEvent.ACTION_MASK) != MotionEvent.ACTION_HOVER_MOVE ) // Ignore hover events, they are broken
				super.processGenericEvent(event);
		}
	}
	private static class AutoDetectTouchInput extends IcsTouchInput
	{
		int tapCount = 0;
		boolean hover = false, fingerHover = false, tap = false;
		float hoverX = 0.0f, hoverY = 0.0f;
		long hoverTime = 0;
		float tapX = 0.0f, tapY = 0.0f;
		long tapTime = 0;
		float hoverTouchDistance = 0.0f;

		private static class Holder
		{
			private static final AutoDetectTouchInput sInstance = new AutoDetectTouchInput();
		}
		public void process(final MotionEvent event)
		{
			if( ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) )
			{
				tapCount ++;
				if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP )
				{
					tap = true;
					tapX = event.getX();
					tapY = event.getY();
					tapTime = System.currentTimeMillis();
					if( hover )
						Log.i("SDL", "Tap tapX " + event.getX() + " tapY " + event.getX());
				}
				else if( hover && System.currentTimeMillis() < hoverTime + 1000 )
				{
					hoverTouchDistance += Math.abs(hoverX - event.getX()) + Math.abs(hoverY - event.getY());
					Log.i("SDL", "Finger down event.getX() " + event.getX() + " hoverX " + hoverX + " event.getY() " + event.getY() + " hoverY " + hoverY + " hoverTouchDistance " + hoverTouchDistance);
				}
			}
			if( tapCount >= 4 )
			{
				int displayHeight = 800;
				try {
					DisplayMetrics dm = new DisplayMetrics();
					GameActivity.instance.getWindowManager().getDefaultDisplay().getMetrics(dm);
					displayHeight = Math.min(dm.widthPixels, dm.heightPixels);
				} catch (Exception eeeee) {}
				Log.i("SDL", "AutoDetectTouchInput: hoverTouchDistance " + hoverTouchDistance + " threshold " + displayHeight / 2 + " hover " + hover + " fingerHover " + fingerHover);
				if( hoverTouchDistance > displayHeight / 2 )
				{
					if( Globals.AppUsesMouse )
						Toast.makeText(GameActivity.instance, "Detected buggy touch panel, enabling workarounds", Toast.LENGTH_SHORT).show();
					touchInput = CrappyMtkTabletWithBrokenTouchDrivers.Holder.sInstance;
				}
				else
				{
					if( fingerHover )
					{
						if( Globals.AppUsesMouse )
							Toast.makeText(GameActivity.instance, "Finger hover capability detected", Toast.LENGTH_SHORT).show();
						// Switch away from relative mouse input
						if( Globals.RelativeMouseMovement || Globals.LeftClickMethod != Mouse.LEFT_CLICK_NORMAL )
						{
							if( Globals.RelativeMouseMovement )
								Globals.ShowScreenUnderFinger = Mouse.ZOOM_MAGNIFIER;
							Globals.RelativeMouseMovement = false;
							Globals.LeftClickMethod = Mouse.LEFT_CLICK_NORMAL;
						}
						//Settings.applyMouseEmulationOptions();
					}
					if ( Globals.GenerateSubframeTouchEvents )
						touchInput = IcsTouchInputWithHistory.Holder.sInstance;
					else
						touchInput = IcsTouchInput.Holder.sInstance;
				}
			}
			super.process(event);
		}
		public void processGenericEvent(final MotionEvent event)
		{
			super.processGenericEvent(event);
			if( (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_MOVE ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_ENTER ||
				(event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_EXIT )
			{
				hover = true;
				hoverX = event.getX();
				hoverY = event.getY();
				hoverTime = System.currentTimeMillis();
				if( ExternalMouseDetected == 0 && (event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_HOVER_MOVE )
					fingerHover = true;
				if( tap && System.currentTimeMillis() < tapTime + 1000 )
				{
					tap = false;
					hoverTouchDistance += Math.abs(tapX - hoverX) + Math.abs(tapY - hoverY);
					Log.i("SDL", "Hover hoverX " + hoverX + " tapX " + tapX + " hoverY " + hoverX + " tapY " + tapY + " hoverTouchDistance " + hoverTouchDistance);
				}
			}
		}
	}
}


