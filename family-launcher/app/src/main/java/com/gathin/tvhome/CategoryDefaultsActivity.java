package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Explicit defaults chosen only from each category's installed launchable applications. */
public final class CategoryDefaultsActivity extends Activity {
    private String tv,movies;
    private int nextId=100;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private String tr(String he,String ru){return CategoryDock.text(this,he,ru);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);return t;}
    private GradientDrawable background(boolean focus){GradientDrawable d=new GradientDrawable();d.setColor(focus?0xFF123E78:0xFF181A20);d.setCornerRadius(dp(12));d.setStroke(dp(focus?2:1),focus?Color.WHITE:0xFF505050);return d;}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);tv=saved==null?CategoryDock.tvPackage(this,CategoryDock.prefs(this).getString("tv","")):saved.getString("tv");movies=saved==null?CategoryDock.prefs(this).getString("movies_default",""):saved.getString("movies");if(tv==null)tv="";
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(56),dp(26),dp(56),dp(18));root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xFF3A210D,0xFF100A05,Color.BLACK}));root.addView(CategoryHeader.create(this,root));TextView title=text(tr("אפליקציות ברירת מחדל","Приложения по умолчанию"),30);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));root.addView(text(tr("בחרו מה ייפתח בלחיצה קצרה על כל אריח","Выберите приложение для короткого нажатия"),15),new LinearLayout.LayoutParams(-1,dp(32)));
        ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(6),dp(6),dp(6),dp(6));scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));section(content,"tv",tr("טלוויזיה","Телевидение"));section(content,"movies",tr("סרטים וסדרות","Фильмы и сериалы"));
        LinearLayout footer=new LinearLayout(this);footer.addView(button(tr("ביטול","Отмена"),this::finish),new LinearLayout.LayoutParams(dp(180),dp(46)));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(220),dp(46));sp.leftMargin=dp(16);footer.addView(button(tr("✓  שמור בחירות","✓  Сохранить"),()->{CategoryDock.prefs(this).edit().putString("tv",tv).putString("movies_default",movies).apply();finish();}),sp);root.addView(footer);setContentView(root);
    }
    private TextView button(String label,Runnable action){TextView b=text(label,17);b.setGravity(Gravity.CENTER);b.setFocusable(true);b.setBackground(background(false));b.setOnFocusChangeListener((v,f)->v.setBackground(background(f)));b.setOnClickListener(v->action.run());return b;}
    private void section(LinearLayout parent,String category,String title){TextView heading=text(title,21);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);parent.addView(heading,new LinearLayout.LayoutParams(-1,dp(42)));RadioGroup group=new RadioGroup(this);group.setOrientation(1);parent.addView(group);String chosen=category.equals("tv")?tv:movies;
        option(group,category,"",tr("פתח את רשימת האפליקציות בקטגוריה","Открыть список приложений категории"),chosen);
        PackageManager pm=getPackageManager();Map<String,ResolveInfo> unique=new LinkedHashMap<>();for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0))if(r.activityInfo!=null&&r.activityInfo.exported&&CategoryDock.visible(this,category,r.activityInfo.packageName)&&!r.activityInfo.packageName.equals(getPackageName()))unique.putIfAbsent(r.activityInfo.packageName,r);
        List<ResolveInfo> apps=new ArrayList<>(unique.values());Map<String,Integer> ranks=CategoryOrder.ranks(this,category);apps.sort((a,b)->{int order=CategoryOrder.compare(ranks,a.activityInfo.packageName,b.activityInfo.packageName);return order!=0?order:a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());});for(ResolveInfo app:apps)option(group,category,app.activityInfo.packageName,app.loadLabel(pm).toString(),chosen);
        if(apps.isEmpty())parent.addView(text(tr("אין אפליקציות בקטגוריה. הוסיפו אותן בהגדרות התצוגה.","Категория пуста. Добавьте приложения в настройках отображения."),14));
    }
    private void option(RadioGroup group,String category,String pkg,String label,String chosen){RadioButton row=new RadioButton(this);row.setId(nextId++);row.setText(label);row.setTextColor(Color.WHITE);row.setTextSize(17);row.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF58A6FF));row.setPadding(dp(16),0,dp(16),0);row.setFocusable(true);row.setBackground(background(false));RadioGroup.LayoutParams lp=new RadioGroup.LayoutParams(-1,dp(48));lp.bottomMargin=dp(7);group.addView(row,lp);row.setChecked(pkg.equals(chosen));row.setOnFocusChangeListener((v,f)->v.setBackground(background(f)));row.setOnCheckedChangeListener((b,checked)->{if(checked){if(category.equals("tv"))tv=pkg;else movies=pkg;}});}
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putString("tv",tv);out.putString("movies",movies);}
}
