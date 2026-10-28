package com.gathin.tvhome;

import android.view.*;

/** A four-second remote hold, measured by elapsed time rather than key-repeat counts. */
final class FourSecondHold {
    private final View view;
    private boolean active, fired, pointer;
    private long down;
    private final Runnable open;
    private final Runnable trigger;
    private FourSecondHold(View view,Runnable open){this.view=view;this.open=open;this.trigger=()->{if(active){fired=true;this.open.run();}};}
    private void start(boolean pointer){cancel();this.pointer=pointer;active=true;fired=false;down=android.os.SystemClock.uptimeMillis();view.postDelayed(trigger,4000);}
    private void finish(){boolean click=active&&!fired&&android.os.SystemClock.uptimeMillis()-down<500;cancel();if(click)view.performClick();}
    private void cancel(){active=false;view.removeCallbacks(trigger);}
    static void bind(View view,boolean enabled,Runnable open,View.OnKeyListener arrows){
        Object old=view.getTag(R.id.category_hold);if(old instanceof FourSecondHold)((FourSecondHold)old).cancel();
        FourSecondHold hold=new FourSecondHold(view,open);view.setTag(R.id.category_hold,hold);
        view.setOnKeyListener((v,key,event)->{
            boolean ok=key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_NUMPAD_ENTER||key==KeyEvent.KEYCODE_BUTTON_A;
            if(enabled&&ok){if(event.getAction()==KeyEvent.ACTION_DOWN){if(!hold.active)hold.start(false);}else if(event.getAction()==KeyEvent.ACTION_UP)hold.finish();return true;}
            if(hold.active)hold.cancel();return arrows.onKey(v,key,event);
        });
        view.setOnTouchListener(enabled?(v,event)->{switch(event.getActionMasked()){case android.view.MotionEvent.ACTION_DOWN:hold.start(true);v.setPressed(true);return true;case android.view.MotionEvent.ACTION_UP:v.setPressed(false);hold.finish();return true;case android.view.MotionEvent.ACTION_CANCEL:v.setPressed(false);hold.cancel();return true;default:return true;}}:null);
    }
    static void cancel(View view){Object state=view.getTag(R.id.category_hold);if(state instanceof FourSecondHold&&!((FourSecondHold)state).pointer)((FourSecondHold)state).cancel();}
}
