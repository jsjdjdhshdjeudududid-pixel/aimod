package com.ai.mod;
import android.app.Activity;import android.content.*;import android.os.Bundle;import android.provider.Settings;import android.widget.*;
public class MainActivity extends Activity {
 protected void onCreate(Bundle b){super.onCreate(b);
  LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(40,80,40,40);
  final EditText k=new EditText(this);k.setHint("Claude API key");
  final SharedPreferences sp=getSharedPreferences("a",0);k.setText(sp.getString("k",""));
  Button s=new Button(this);s.setText("Save key");
  s.setOnClickListener(v->{sp.edit().putString("k",k.getText().toString().trim()).apply();Toast.makeText(this,"Saved",0).show();});
  Button a=new Button(this);a.setText("Enable Accessibility (AI Mod Menu ON)");
  a.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  l.addView(k);l.addView(s);l.addView(a);setContentView(l);}
}
