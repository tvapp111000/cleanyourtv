package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import java.util.Calendar;

final class CategoryHeader {
    static View create(Activity a,ViewGroup parent){
        View header=a.getLayoutInflater().inflate(R.layout.category_header,parent,false);
        for(int id:new int[]{R.id.btnInputs,R.id.btnVpn,R.id.btnReorder,R.id.btnLibraryTop})header.findViewById(id).setVisibility(View.GONE);
        header.findViewById(R.id.btnSettings).setOnClickListener(v->a.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)));
        TextView wifi=header.findViewById(R.id.btnWifi);wifi.setOnClickListener(v->a.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)));
        try{android.net.wifi.WifiManager manager=(android.net.wifi.WifiManager)a.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);String ssid=manager.getConnectionInfo().getSSID();if(ssid!=null&&!ssid.equals("<unknown ssid>"))wifi.setText(ssid.replace("\"",""));}catch(RuntimeException ignored){}
        int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);int greetingId=hour<5?R.string.good_night:hour<12?R.string.good_morning:hour<17?R.string.good_afternoon:hour<22?R.string.good_evening:R.string.good_night;
        TextView greeting=header.findViewById(R.id.greeting);greeting.setText(greetingId);
        if(a.getResources().getBoolean(R.bool.rtl_menu_bar)){View clock=header.findViewById(R.id.clock);View row=(View)clock.getParent();View.OnLayoutChangeListener center=(v,l,t,r,b,ol,ot,or,ob)->{ViewGroup.LayoutParams params=greeting.getLayoutParams();if(params.width!=clock.getWidth()){params.width=clock.getWidth();greeting.setLayoutParams(params);}greeting.setTranslationX(row.getLeft()+clock.getLeft());};clock.addOnLayoutChangeListener(center);row.addOnLayoutChangeListener(center);}
        return header;
    }
}
