package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Context shelf below the original dock, populated off the UI thread. */
final class CategoryPreview extends LinearLayout {
    private static final ExecutorService LOADER=Executors.newSingleThreadExecutor();
    private final Activity activity;
    private final Runnable up;
    private String category="";
    private int generation;
    private View first;
    CategoryPreview(Activity a,Runnable up) { super(a);this.activity=a;this.up=up;setTag("category_preview");setOrientation(VERTICAL);setClipChildren(false); }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size){TextView t=new TextView(activity);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setFontFeatureSettings("kern");return t;}
    void refresh(){String current=category;category="";show(current);}
    boolean focusFirst(){if(first==null)return false;return first.requestFocus();}
    private GradientDrawable bg(boolean focused){GradientDrawable d=new GradientDrawable();d.setColor(focused?0x553E5264:0x22181C22);d.setCornerRadius(dp(10));d.setStroke(dp(focused?2:1),focused?0xFFFFFFFF:0x33FFFFFF);return d;}
    void show(String pkg) {
        if(pkg.equals(category))return;
        category=pkg;int ticket=++generation;first=null;
        LOADER.execute(()->{
            PackageManager pm=activity.getPackageManager();Map<String,ResolveInfo> catalogue=new LinkedHashMap<>();
            for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0))if(info.activityInfo!=null&&info.activityInfo.exported&&!info.activityInfo.packageName.equals(activity.getPackageName()))catalogue.putIfAbsent(info.activityInfo.packageName,info);
            Set<String> selected=CategoryDock.movies(activity);List<ResolveInfo> list=new ArrayList<>();
            String tv1=CategoryDock.tvPackage(activity,"tivimate"),tv2=CategoryDock.tvPackage(activity,"mediabox");
            for(ResolveInfo info:catalogue.values()){String key=info.activityInfo.packageName;if(pkg.equals(CategoryDock.APPS)&&CategoryDock.visible(activity,"apps",key)||pkg.equals(CategoryDock.MOVIES)&&selected.contains(key)||pkg.equals(CategoryDock.TV)&&CategoryDock.visible(activity,"tv",key)||pkg.equals("com.google.android.youtube.tv")&&key.equals(pkg))list.add(info);}
            Map<String,Integer> ranks=CategoryOrder.ranks(activity,pkg.equals(CategoryDock.TV)?"tv":pkg.equals(CategoryDock.MOVIES)?"movies":"apps");list.sort((a,b)->{int order=CategoryOrder.compare(ranks,a.activityInfo.packageName,b.activityInfo.packageName);return order!=0?order:a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());});
            List<String> names=new ArrayList<>();List<Drawable> icons=new ArrayList<>();for(ResolveInfo info:list){names.add(info.loadLabel(pm).toString());Drawable art=info.activityInfo.loadBanner(pm);if(art==null){try{art=pm.getApplicationBanner(info.activityInfo.packageName);}catch(PackageManager.NameNotFoundException ignored){}}if(art==null)art=info.loadIcon(pm);icons.add(art);}
            post(()->{if(ticket!=generation||activity.isFinishing())return;removeAllViews();
                String heading=pkg.equals(CategoryDock.MOVIES)?CategoryDock.text(activity,"אפליקציות לצפייה בסרטים וסדרות","Приложения для фильмов и сериалов"):pkg.equals(CategoryDock.TV)?CategoryDock.text(activity,"אפליקציות הטלוויזיה שלך","Ваши ТВ-приложения"):pkg.equals(CategoryDock.APPS)?CategoryDock.text(activity,"כל האפליקציות המותקנות","Все установленные приложения"):"YouTube";
                TextView title=text(heading,14);title.setGravity(Gravity.LEFT);title.setPadding(dp(4),0,dp(4),dp(8));addView(title,new LayoutParams(-1,dp(29)));
                if(list.isEmpty()){TextView empty=text(CategoryDock.text(activity,pkg.equals(CategoryDock.MOVIES)?"לחיצה ארוכה על סרטים וסדרות להוספת אפליקציות":"אפליקציות הטלוויזיה יופיעו כאן לאחר ההתקנה",pkg.equals(CategoryDock.MOVIES)?"Удерживайте Фильмы и сериалы, чтобы добавить приложения":"ТВ-приложения появятся после установки"),14);empty.setTextColor(0xFFAAAAAA);addView(empty);return;}
                HorizontalScrollView scroll=new HorizontalScrollView(activity);scroll.setHorizontalScrollBarEnabled(false);scroll.setClipToPadding(false);scroll.setClipChildren(false);scroll.setPadding(0,dp(6),0,dp(2));LinearLayout row=new LinearLayout(activity);row.setClipChildren(false);row.setClipToPadding(false);scroll.addView(row);addView(scroll,new LayoutParams(-1,-1));
                int tileWidth=Math.min(dp(96),Math.max(dp(64),(getWidth()-dp(8)*list.size())/list.size()));
                for(int n=0;n<list.size();n++){final String key=list.get(n).activityInfo.packageName;final String appName=names.get(n);LinearLayout tile=new LinearLayout(activity);tile.setOrientation(VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(0,dp(5),0,dp(3));tile.setBackgroundColor(Color.TRANSPARENT);tile.setFocusable(true);tile.setClickable(true);
                    LayoutParams params=new LayoutParams(tileWidth,dp(93));params.setMargins(dp(4),0,dp(4),0);row.addView(tile,params);ImageView icon=new ImageView(activity);icon.setImageDrawable(icons.get(n));icon.setScaleType(ImageView.ScaleType.CENTER_CROP);icon.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View v,android.graphics.Outline o){o.setRoundRect(0,0,v.getWidth(),v.getHeight(),dp(8));}});icon.setClipToOutline(true);tile.addView(icon,new LayoutParams(tileWidth,Math.round(tileWidth*0.62f)));View marker=new View(activity);marker.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00267BFF,0xB0267BFF,0xFFFFFFFF,0xFFFFFFFF,0xB0267BFF,0x00267BFF}));marker.setAlpha(0f);LayoutParams markerParams=new LayoutParams(tileWidth,dp(3));markerParams.topMargin=dp(3);tile.addView(marker,markerParams);TextView name=text(names.get(n),11);name.setSingleLine(true);name.setEllipsize(android.text.TextUtils.TruncateAt.END);name.setGravity(Gravity.CENTER);tile.addView(name,new LayoutParams(-1,dp(24)));tile.setContentDescription(names.get(n));
                    tile.setOnClickListener(v->CategoryDock.launch(activity,key));tile.setOnFocusChangeListener((v,focused)->{v.animate().scaleX(focused?1.10f:1f).scaleY(focused?1.10f:1f).translationY(focused?-dp(4):0).alpha(focused?1f:0.85f).setDuration(150).start();v.setTranslationZ(focused?dp(6):0);name.setTextColor(focused?Color.WHITE:0xFFCCCCCC);name.setTypeface(android.graphics.Typeface.DEFAULT,focused?android.graphics.Typeface.BOLD:android.graphics.Typeface.NORMAL);marker.animate().alpha(focused?1f:0f).setDuration(120).start();if(focused)title.setText(heading+"  ·  "+appName);});tile.setOnKeyListener((v,k,event)->{if(event.getAction()==KeyEvent.ACTION_DOWN&&k==KeyEvent.KEYCODE_DPAD_UP){up.run();return true;}return false;});if(first==null)first=tile;
                }
            });
        });
    }
}
