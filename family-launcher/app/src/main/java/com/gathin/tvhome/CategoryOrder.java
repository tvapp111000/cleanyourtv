package com.gathin.tvhome;

import android.content.Context;
import org.json.JSONArray;
import java.util.*;

/** Package identities keep the saved order independent of labels and language. */
final class CategoryOrder {
    static List<String> load(Context context,String category){List<String> order=new ArrayList<>();try{JSONArray values=new JSONArray(CategoryDock.prefs(context).getString("order_"+category,"[]"));for(int i=0;i<values.length();i++){String pkg=values.optString(i);if(!pkg.isEmpty()&&!order.contains(pkg))order.add(pkg);}}catch(org.json.JSONException ignored){}return order;}
    static Map<String,Integer> ranks(Context context,String category){Map<String,Integer> result=new HashMap<>();List<String> order=load(context,category);for(int i=0;i<order.size();i++)result.put(order.get(i),i);return result;}
    static int compare(Map<String,Integer> ranks,String a,String b){return Integer.compare(ranks.getOrDefault(a,Integer.MAX_VALUE),ranks.getOrDefault(b,Integer.MAX_VALUE));}
}
