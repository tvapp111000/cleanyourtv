package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Full-screen banner catalogue; 160x90dp is the Android TV 320x180px xhdpi asset size. */
public final class CategoryScreenActivity extends Activity {
    private static final ExecutorService LOADER=Executors.newSingleThreadExecutor();
    private final List<App> apps=new ArrayList<>();
    private RecyclerView grid;
    private GridLayoutManager layout;
    private TextView empty;
    private View settings;
    private String mode,focused="";
    private int generation;
    private boolean rtl;
    private final class App {
        final String pkg,label; final Drawable art;
        App(String pkg,String label,Drawable art){this.pkg=pkg;this.label=label;this.art=art;}
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private String tr(String he,String ru){return CategoryDock.text(this,he,ru);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(size);return t;}
    private GradientDrawable surface(boolean focus){GradientDrawable d=new GradientDrawable();d.setColor(focus?0xFF36577B:0xCC292A2D);d.setCornerRadius(dp(24));d.setStroke(dp(1),focus?0xFFFFFFFF:0x55FFFFFF);return d;}
    private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextSize(15);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setBackground(surface(false));b.setOnFocusChangeListener((v,has)->v.setBackground(surface(has)));b.setOnClickListener(v->action.run());return b;}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);mode=getIntent().getStringExtra("mode");if(mode==null)mode="apps";if(saved!=null)focused=saved.getString("focused","");rtl=getResources().getBoolean(R.bool.rtl_app_grid);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(56),dp(26),dp(56),dp(18));root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xFF3A210D,0xFF100A05,0xFF000000}));
        View header=getLayoutInflater().inflate(R.layout.category_header,root,false);
        root.addView(header);
        for(int id:new int[]{R.id.btnInputs,R.id.btnVpn,R.id.btnReorder,R.id.btnLibraryTop})header.findViewById(id).setVisibility(View.GONE);
        settings=header.findViewById(R.id.btnSettings);settings.setOnClickListener(v->startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)));
        TextView wifi=header.findViewById(R.id.btnWifi);wifi.setOnClickListener(v->startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)));
        try{android.net.wifi.WifiManager manager=(android.net.wifi.WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);String ssid=manager.getConnectionInfo().getSSID();if(ssid!=null&&!ssid.equals("<unknown ssid>"))wifi.setText(ssid.replace("\"",""));}catch(RuntimeException ignored){}
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);int greetingId=hour<5?R.string.good_night:hour<12?R.string.good_morning:hour<17?R.string.good_afternoon:hour<22?R.string.good_evening:R.string.good_night;
        TextView greeting=header.findViewById(R.id.greeting);greeting.setText(greetingId);
        if(getResources().getBoolean(R.bool.rtl_menu_bar)){
            View clock=header.findViewById(R.id.clock);View clockRow=(View)clock.getParent();View.OnLayoutChangeListener center=(v,l,t,r,b,ol,ot,or,ob)->{ViewGroup.LayoutParams params=greeting.getLayoutParams();if(params.width!=clock.getWidth()){params.width=clock.getWidth();greeting.setLayoutParams(params);}greeting.setTranslationX(clockRow.getLeft()+clock.getLeft());};clock.addOnLayoutChangeListener(center);clockRow.addOnLayoutChangeListener(center);
        }
        LinearLayout titleRow=new LinearLayout(this);titleRow.setGravity(Gravity.CENTER_VERTICAL);titleRow.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        TextView title=text(mode.equals("movies")?tr("סרטים וסדרות","Фильмы и сериалы"):tr("אפליקציות","Приложения"),40);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);title.setSingleLine(true);title.setGravity(rtl?Gravity.RIGHT:Gravity.LEFT);titleRow.addView(title,new LinearLayout.LayoutParams(0,dp(58),1));
        root.addView(titleRow);
        TextView subtitle=text(mode.equals("movies")?tr("האפליקציות שבחרת לסרטים וסדרות","Ваши приложения для фильмов и сериалов"):tr("כל האפליקציות במקום אחד","Все приложения в одном месте"),16);subtitle.setTextColor(0xFFCCCCCC);subtitle.setGravity(rtl?Gravity.RIGHT:Gravity.LEFT);root.addView(subtitle,new LinearLayout.LayoutParams(-1,dp(30)));
        FrameLayout content=new FrameLayout(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));grid=new RecyclerView(this);grid.setLayoutDirection(rtl?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);int width=getResources().getDisplayMetrics().widthPixels-dp(112);int columns=Math.max(1,width/dp(190));layout=new GridLayoutManager(this,columns);grid.setLayoutManager(layout);grid.setItemAnimator(null);grid.setClipChildren(false);grid.setClipToPadding(false);grid.setPadding(dp(6),dp(10),dp(6),dp(16));grid.setAdapter(new Catalogue());content.addView(grid,new FrameLayout.LayoutParams(-1,-1));empty=text("",18);empty.setGravity(Gravity.CENTER);content.addView(empty,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
    }
    @Override public void onResume(){super.onResume();refresh();}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("focused",focused);}
    void refresh(){int ticket=++generation;String selectedMode=mode;LOADER.execute(()->{PackageManager pm=getPackageManager();Map<String,ResolveInfo> unique=new LinkedHashMap<>();for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0))if(info.activityInfo!=null&&info.activityInfo.exported&&!info.activityInfo.packageName.equals(getPackageName()))unique.putIfAbsent(info.activityInfo.packageName,info);
        Set<String> selected=CategoryDock.movies(this);List<App> result=new ArrayList<>();for(ResolveInfo info:unique.values()){String pkg=info.activityInfo.packageName;if(!CategoryDock.visible(this,selectedMode,pkg))continue;Drawable art;try{art=info.activityInfo.loadBanner(pm);if(art==null)art=pm.getApplicationBanner(pkg);if(art==null)art=info.loadIcon(pm);}catch(PackageManager.NameNotFoundException|RuntimeException missing){art=getDrawable(R.drawable.ic_apps);}result.add(new App(pkg,info.loadLabel(pm).toString(),art));}Map<String,Integer> ranks=CategoryOrder.ranks(this,selectedMode);result.sort((a,b)->{int order=CategoryOrder.compare(ranks,a.pkg,b.pkg);return order!=0?order:a.label.compareToIgnoreCase(b.label);});
        runOnUiThread(()->{if(ticket!=generation||isFinishing()||isDestroyed())return;apps.clear();apps.addAll(result);grid.getAdapter().notifyDataSetChanged();empty.setText(mode.equals("movies")?tr("התיקייה ריקה. הוסיפו אפליקציות בלחיצה ארוכה במסך הבית","Папка пуста. Добавьте приложения долгим нажатием на главном экране"):tr("לא נמצאו אפליקציות זמינות","Нет доступных приложений"));empty.setVisibility(apps.isEmpty()?View.VISIBLE:View.GONE);if(apps.isEmpty())return;int target=0;for(int i=0;i<apps.size();i++)if(apps.get(i).pkg.equals(focused)){target=i;break;}layout.scrollToPositionWithOffset(target,0);final int position=target;grid.post(()->{RecyclerView.ViewHolder holder=grid.findViewHolderForAdapterPosition(position);if(holder!=null)holder.itemView.requestFocus();});});
    });}
    private final class Catalogue extends RecyclerView.Adapter<Holder>{
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type){FrameLayout cell=new FrameLayout(CategoryScreenActivity.this);cell.setLayoutParams(new RecyclerView.LayoutParams(-1,dp(132)));cell.setFocusable(true);cell.setClickable(true);cell.setClipChildren(false);cell.setClipToPadding(false);
            LinearLayout contents=new LinearLayout(CategoryScreenActivity.this);contents.setOrientation(LinearLayout.VERTICAL);contents.setGravity(Gravity.CENTER_HORIZONTAL);FrameLayout.LayoutParams body=new FrameLayout.LayoutParams(dp(172),-1,Gravity.TOP|Gravity.CENTER_HORIZONTAL);body.topMargin=dp(6);cell.addView(contents,body);
            FrameLayout picture=new FrameLayout(CategoryScreenActivity.this);contents.addView(picture,new LinearLayout.LayoutParams(dp(172),dp(102)));ImageView image=new ImageView(CategoryScreenActivity.this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackgroundColor(0xFF101114);image.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View v,Outline o){o.setRoundRect(0,0,v.getWidth(),v.getHeight(),dp(8));}});image.setClipToOutline(true);FrameLayout.LayoutParams imageParams=new FrameLayout.LayoutParams(dp(160),dp(90),Gravity.CENTER);picture.addView(image,imageParams);
            GlassView ring=new GlassView(CategoryScreenActivity.this,null);ring.setStyle(Config.GLASS_HALO);ring.setRadius(dp(8));ring.setAlpha(0);picture.addView(ring,new FrameLayout.LayoutParams(-1,-1));TextView label=text("",13);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);label.setGravity(Gravity.CENTER);contents.addView(label,new LinearLayout.LayoutParams(-1,dp(24)));return new Holder(cell,image,label,ring,contents);}
        @Override public void onBindViewHolder(@NonNull Holder h,int position){App app=apps.get(position);h.image.setImageDrawable(app.art);h.label.setText(app.label);h.itemView.setContentDescription(app.label);h.itemView.setOnClickListener(v->CategoryDock.launch(CategoryScreenActivity.this,app.pkg));h.itemView.setOnFocusChangeListener((v,has)->{h.ring.animate().alpha(has?1:0).setDuration(120).start();h.contents.animate().scaleX(has?1.04f:1).scaleY(has?1.04f:1).setDuration(140).start();h.label.setTypeface(Typeface.DEFAULT,has?Typeface.BOLD:Typeface.NORMAL);if(has)focused=app.pkg;});h.itemView.setOnKeyListener((v,key,e)->{if(e.getAction()==KeyEvent.ACTION_DOWN&&key==KeyEvent.KEYCODE_DPAD_UP&&h.getBindingAdapterPosition()<layout.getSpanCount()){settings.requestFocus();return true;}return false;});h.ring.setAlpha(h.itemView.hasFocus()?1:0);}
        @Override public int getItemCount(){return apps.size();}
    }
    private static final class Holder extends RecyclerView.ViewHolder {final ImageView image;final TextView label;final GlassView ring;final View contents;Holder(View item,ImageView image,TextView label,GlassView ring,View contents){super(item);this.image=image;this.label=label;this.ring=ring;this.contents=contents;}}
}
