package com.gathin.tvhome;

import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import java.util.*;

/** Four category actions hosted by the unchanged home grid and glass library. */
final class CategoryDock {
    static final String TV="category.tv", MOVIES="category.movies", APPS="category.apps";
    private static CategoryDialog dialog;
    static android.content.SharedPreferences prefs(Context c) { return c.getSharedPreferences("category_dock",0); }
    static String text(Context c,String he,String ru) { return c.getResources().getConfiguration().getLocales().get(0).getLanguage().equals("ru")?ru:he; }
    static boolean isCategory(String pkg) { return pkg.equals(TV)||pkg.equals(MOVIES)||pkg.equals(APPS); }
    static Set<String> movies(Context c) { return new HashSet<>(prefs(c).getStringSet("movies",Collections.emptySet())); }
    static boolean visible(Context c,String category,String pkg) {
        if(category.equals("movies"))return movies(c).contains(pkg);
        String key=category+"_apps";
        if(prefs(c).contains(key))return prefs(c).getStringSet(key,Collections.emptySet()).contains(pkg);
        return category.equals("apps") || category.equals("tv")&&(pkg.equals(tvPackage(c,"tivimate"))||pkg.equals(tvPackage(c,"mediabox")));
    }
    static void defaults(Activity a){a.startActivity(new Intent(a,CategoryDefaultsActivity.class));}
    static void settings(Activity a,String category){a.startActivity(new Intent(a,CategorySettingsActivity.class).putExtra("category",category));}
    static boolean selectMovie(Context c,String pkg,boolean selected) { Set<String> all=movies(c);if(selected)all.add(pkg);else all.remove(pkg);return prefs(c).edit().putStringSet("movies",all).commit(); }
    static List<AppAdapter.Entry> entries(Context c) {
        List<AppAdapter.Entry> result=new ArrayList<>();
        result.add(entry(c,TV,text(c,"טלוויזיה","Телевидение"),R.drawable.category_tv));
        result.add(entry(c,MOVIES,text(c,"סרטים וסדרות","Фильмы и сериалы"),R.drawable.category_movies));
        Drawable youtube=null,icon=null; PackageManager pm=c.getPackageManager();
        try { youtube=pm.getApplicationBanner("com.google.android.youtube.tv");icon=pm.getApplicationIcon("com.google.android.youtube.tv"); }catch(PackageManager.NameNotFoundException ignored){}
        result.add(new AppAdapter.Entry("YouTube",new ComponentName("com.google.android.youtube.tv","category.youtube"),c.getDrawable(R.drawable.category_youtube),icon));
        result.add(entry(c,APPS,text(c,"אפליקציות","Приложения"),R.drawable.category_apps));
        return result;
    }
    private static AppAdapter.Entry entry(Context c,String pkg,String label,int drawable) { return new AppAdapter.Entry(label,new ComponentName(pkg,"category"),c.getDrawable(drawable),null); }
    static void show(Activity activity,String mode) {
        if(dialog!=null)dialog.close();
        Config cfg=Config.load(activity);
        dialog=new CategoryDialog(activity,cfg.animations,cfg.reducedMotion,mode,()->{dialog=null;if(activity instanceof CategoryScreenActivity)((CategoryScreenActivity)activity).refresh();android.view.View shelf=activity.getWindow().getDecorView().findViewWithTag("category_preview");if(shelf instanceof CategoryPreview)((CategoryPreview)shelf).refresh();});
        dialog.show();
    }
    static void action(Activity a,String pkg,boolean hold) {
        if(pkg.equals(TV)) {
            String preferred=prefs(a).getString("tv","");
            if(hold)defaults(a);else if(preferred.isEmpty())show(a,"tv");else if(visible(a,"tv",tvPackage(a,preferred)))launchTv(a,preferred);else defaults(a);
        } else if(pkg.equals(MOVIES)){if(hold)settings(a,"movies");else {String preferred=prefs(a).getString("movies_default","");if(preferred.isEmpty()||!visible(a,"movies",preferred)||!launch(a,preferred))a.startActivity(new Intent(a,CategoryScreenActivity.class).putExtra("mode","movies"));}}
        else if(pkg.equals(APPS)){if(hold)settings(a,"apps");else a.startActivity(new Intent(a,CategoryScreenActivity.class).putExtra("mode","apps"));}
        else launch(a,pkg);
    }
    static List<ResolveInfo> tvChoices(Context c) {
        if(prefs(c).contains("tv_apps")){List<ResolveInfo> result=new ArrayList<>();Set<String> seen=new HashSet<>();for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo info:c.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0))if(info.activityInfo!=null&&info.activityInfo.exported&&visible(c,"tv",info.activityInfo.packageName)&&seen.add(info.activityInfo.packageName))result.add(info);return result;}
 List<ResolveInfo> result=new ArrayList<>();for(String key:new String[]{"tivimate","mediabox"}) { ResolveInfo r=new ResolveInfo();r.activityInfo=new ActivityInfo();r.activityInfo.packageName=key;r.activityInfo.name=key;r.nonLocalizedLabel=key.equals("tivimate")?"TiviMate":"Media Box";result.add(r); }return result; }
    static String tvPackage(Context c,String key) {
        if(key==null||key.isEmpty())return null;
        if(key.contains("."))return key;
        if(key.equals("tivimate"))return "ar.tvplayer.tv";
        PackageManager pm=c.getPackageManager();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo info:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)) {
            String name=info.loadLabel(pm).toString().toLowerCase(Locale.ROOT).replace(" ","").replace("-","");
            String pkg=info.activityInfo.packageName;
            if(name.contains("mediabox")||name.contains("медиабокс")||name.contains("מדיהבוקס")||pkg.toLowerCase(Locale.ROOT).contains("mediabox"))return pkg;
        }
        return null;
    }
    static void launchTv(Context c,String key) { String pkg=tvPackage(c,key);if(pkg==null||!launch(c,pkg))Toast.makeText(c,text(c,"האפליקציה שנבחרה עדיין אינה מותקנת. הבחירה נשמרה.","Выбранное приложение ещё не установлено. Выбор сохранён."),Toast.LENGTH_LONG).show(); }
    static boolean launch(Context c,String pkg) { Intent i=c.getPackageManager().getLeanbackLaunchIntentForPackage(pkg);if(i==null)i=c.getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)return false;try{c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return true;}catch(RuntimeException unavailable){return false;} }
}
