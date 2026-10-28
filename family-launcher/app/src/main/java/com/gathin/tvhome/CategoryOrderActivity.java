package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Pick up with OK, move with Up/Down, put down with OK. Parent saves the draft. */
public final class CategoryOrderActivity extends Activity {
    private static final ExecutorService LOADER=Executors.newSingleThreadExecutor();
    private final List<App> apps=new ArrayList<>();
    private RecyclerView list;
    private String carrying="";
    private TextView instruction;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private String tr(String he,String ru){return CategoryDock.text(this,he,ru);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);return t;}
    private GradientDrawable bg(boolean active){GradientDrawable d=new GradientDrawable();d.setColor(active?0xFF0B3569:0xFF181A20);d.setCornerRadius(dp(12));d.setStroke(dp(1),active?0xFF84BAFF:0xFF44464A);return d;}
    private TextView button(String name,Runnable action){TextView b=text(name,16);b.setGravity(Gravity.CENTER);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setFocusable(true);b.setClickable(true);b.setBackground(bg(false));b.setOnFocusChangeListener((v,has)->v.setBackground(bg(has)));b.setOnClickListener(v->action.run());return b;}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(56),dp(26),dp(56),dp(18));root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xFF3A210D,0xFF100A05,Color.BLACK}));root.addView(CategoryHeader.create(this,root));TextView title=text(tr("סידור האפליקציות","Порядок приложений"),34);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setGravity(Gravity.RIGHT);root.addView(title,new LinearLayout.LayoutParams(-1,dp(55)));instruction=text(tr("אישור: בחרו אפליקציה להזזה  ·  לאחר מכן חצים למעלה ולמטה","ОК: выбрать приложение  ·  Затем стрелки вверх и вниз"),15);instruction.setTextColor(0xFFCCCCCC);instruction.setGravity(Gravity.RIGHT);root.addView(instruction,new LinearLayout.LayoutParams(-1,dp(38)));list=new RecyclerView(this);list.setLayoutManager(new LinearLayoutManager(this));list.setClipToPadding(false);list.setPadding(dp(8),dp(6),dp(8),dp(6));list.setItemAnimator(null);list.setAdapter(new Rows());root.addView(list,new LinearLayout.LayoutParams(-1,0,1));LinearLayout footer=new LinearLayout(this);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(dp(210),dp(45));bp.rightMargin=dp(14);footer.addView(button(tr("✕  ביטול סידור","✕  Отмена"),this::finish),bp);footer.addView(button(tr("✓  החל סדר","✓  Применить порядок"),()->{ArrayList<String> order=new ArrayList<>();for(App app:apps)order.add(app.pkg);setResult(RESULT_OK,new Intent().putStringArrayListExtra("order",order).putExtra("category",getIntent().getStringExtra("category")));finish();}),new LinearLayout.LayoutParams(dp(210),dp(45)));TextView note=text(tr("הסדר נשמר סופית עם ״שמור שינויים״ במסך ההגדרות","Чтобы сохранить, нажмите «Сохранить» в настройках"),13);note.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);footer.addView(note,new LinearLayout.LayoutParams(0,dp(45),1));root.addView(footer);setContentView(root);
        ArrayList<String> packages=saved!=null?saved.getStringArrayList("packages"):getIntent().getStringArrayListExtra("packages");if(packages==null)packages=new ArrayList<>();final ArrayList<String> requested=packages;
        LOADER.execute(()->{PackageManager pm=getPackageManager();List<App> loaded=new ArrayList<>();for(String pkg:requested)try{ApplicationInfo info=pm.getApplicationInfo(pkg,0);loaded.add(new App(pkg,pm.getApplicationLabel(info).toString(),pm.getApplicationIcon(info)));}catch(PackageManager.NameNotFoundException|RuntimeException ignored){}runOnUiThread(()->{if(isFinishing()||isDestroyed())return;apps.addAll(loaded);list.getAdapter().notifyDataSetChanged();list.post(()->focus(0));});});
    }
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);ArrayList<String> packages=new ArrayList<>();for(App a:apps)packages.add(a.pkg);out.putStringArrayList("packages",packages);}
    private void focus(int index){RecyclerView.ViewHolder holder=list.findViewHolderForAdapterPosition(index);if(holder!=null)holder.itemView.requestFocus();}
    private void drop(){carrying="";instruction.setText(tr("אישור: בחרו אפליקציה להזזה  ·  לאחר מכן חצים למעלה ולמטה","ОК: выбрать приложение  ·  Затем стрелки вверх и вниз"));list.getAdapter().notifyDataSetChanged();}
    @Override public void onBackPressed(){if(!carrying.isEmpty())drop();else super.onBackPressed();}
    private static final class App{final String pkg,name;final Drawable icon;App(String p,String n,Drawable d){pkg=p;name=n;icon=d;}}
    private final class Rows extends RecyclerView.Adapter<Row>{Rows(){setHasStableIds(true);}@Override public long getItemId(int position){return apps.get(position).pkg.hashCode();}
        @NonNull @Override public Row onCreateViewHolder(@NonNull ViewGroup parent,int type){FrameLayout cell=new FrameLayout(CategoryOrderActivity.this);RecyclerView.LayoutParams params=new RecyclerView.LayoutParams(-1,dp(61));params.setMargins(dp(5),dp(4),dp(5),dp(4));cell.setLayoutParams(params);cell.setFocusable(true);cell.setClickable(true);LinearLayout body=new LinearLayout(CategoryOrderActivity.this);body.setPadding(dp(16),dp(6),dp(16),dp(6));body.setGravity(Gravity.CENTER_VERTICAL);cell.addView(body,new FrameLayout.LayoutParams(-1,-1));TextView number=text("",20);number.setGravity(Gravity.CENTER);body.addView(number,new LinearLayout.LayoutParams(dp(45),-1));ImageView icon=new ImageView(CategoryOrderActivity.this);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);body.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));TextView label=text("",17);LinearLayout.LayoutParams labelParams=new LinearLayout.LayoutParams(0,-2,1);labelParams.leftMargin=dp(20);body.addView(label,labelParams);TextView status=text("",15);status.setGravity(Gravity.CENTER);body.addView(status,new LinearLayout.LayoutParams(dp(150),-1));GlassView ring=new GlassView(CategoryOrderActivity.this,null);ring.setStyle(Config.GLASS_HALO);ring.setAlpha(0);cell.addView(ring,new FrameLayout.LayoutParams(-1,-1));return new Row(cell,number,icon,label,status,ring);}
        @Override public void onBindViewHolder(@NonNull Row h,int position){App app=apps.get(position);boolean moving=app.pkg.equals(carrying);h.number.setText(String.valueOf(position+1));h.icon.setImageDrawable(app.icon);h.label.setText(app.name);h.status.setText(moving?tr("↕  חצים להזזה","↕  Переместить"):tr("אישור להזזה","ОК: переместить"));h.itemView.setBackground(bg(moving));h.ring.setAlpha(h.itemView.hasFocus()?1:0);h.itemView.setContentDescription((position+1)+" · "+app.name);h.itemView.setOnFocusChangeListener((v,has)->h.ring.animate().alpha(has?1:0).setDuration(100).start());h.itemView.setOnClickListener(v->{if(app.pkg.equals(carrying))drop();else{carrying=app.pkg;instruction.setText(tr("חצים למעלה ולמטה להזזה  ·  אישור לסיום ההזזה","Вверх/вниз: переместить  ·  ОК: завершить"));notifyDataSetChanged();}});h.itemView.setOnKeyListener((v,key,event)->{if(event.getAction()!=KeyEvent.ACTION_DOWN||carrying.isEmpty())return false;if(key!=KeyEvent.KEYCODE_DPAD_UP&&key!=KeyEvent.KEYCODE_DPAD_DOWN)return key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT;int from=-1;for(int i=0;i<apps.size();i++)if(apps.get(i).pkg.equals(carrying)){from=i;break;}int to=from+(key==KeyEvent.KEYCODE_DPAD_UP?-1:1);if(from>=0&&to>=0&&to<apps.size()){Collections.swap(apps,from,to);notifyItemMoved(from,to);notifyItemRangeChanged(Math.min(from,to),Math.abs(from-to)+1);list.scrollToPosition(to);final int target=to;list.post(()->focus(target));}return true;});}
        @Override public int getItemCount(){return apps.size();}
    }
    private static final class Row extends RecyclerView.ViewHolder{final TextView number,label,status;final ImageView icon;final GlassView ring;Row(View view,TextView number,ImageView icon,TextView label,TextView status,GlassView ring){super(view);this.number=number;this.icon=icon;this.label=label;this.status=status;this.ring=ring;}}
}
