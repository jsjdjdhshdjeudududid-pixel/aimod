package com.ai.mod;
import android.accessibilityservice.*;import android.content.*;import android.content.pm.*;import android.graphics.*;
import android.graphics.drawable.GradientDrawable;import android.os.*;import android.util.Base64;
import android.view.*;import android.view.accessibility.*;import android.widget.*;import org.json.*;
import java.io.*;import java.net.*;import java.util.concurrent.*;
public class AiService extends AccessibilityService {
 WindowManager wm;TextView bubble,typing;LinearLayout panel,chat;ScrollView sv;EditText input;WindowManager.LayoutParams pp;
 Handler ui=new Handler(Looper.getMainLooper());StringBuilder convo=new StringBuilder();
 volatile boolean busy=false,typingOn=false;boolean isOpen=false;
 public void onAccessibilityEvent(AccessibilityEvent e){}
 public void onInterrupt(){}
 int dp(int v){return (int)(v*getResources().getDisplayMetrics().density);}
 GradientDrawable rd(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
 WindowManager.LayoutParams lp(int w,int h,int f){return new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,f,PixelFormat.TRANSLUCENT);}
 void setFocus(boolean f){int b=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;pp.flags=f?b:(b|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);try{wm.updateViewLayout(panel,pp);}catch(Exception e){}}
 protected void onServiceConnected(){
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  bubble=new TextView(this);bubble.setText("AI");bubble.setTextColor(Color.WHITE);bubble.setTextSize(16);bubble.setGravity(Gravity.CENTER);
  GradientDrawable g=new GradientDrawable();g.setShape(GradientDrawable.OVAL);g.setColor(0xFFE53935);bubble.setBackground(g);
  WindowManager.LayoutParams bp=lp(dp(56),dp(56),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);bp.gravity=Gravity.TOP|Gravity.START;bp.x=20;bp.y=500;
  bubble.setOnTouchListener(new View.OnTouchListener(){int sx,sy;float tx,ty;boolean mv;
   public boolean onTouch(View v,MotionEvent m){
    if(m.getAction()==0){sx=bp.x;sy=bp.y;tx=m.getRawX();ty=m.getRawY();mv=false;}
    else if(m.getAction()==2){float dx=m.getRawX()-tx,dy=m.getRawY()-ty;if(Math.abs(dx)+Math.abs(dy)>15)mv=true;bp.x=sx+(int)dx;bp.y=sy+(int)dy;wm.updateViewLayout(bubble,bp);}
    else if(m.getAction()==1&&!mv){toggle();}
    return true;}});
  panel=new LinearLayout(this);panel.setOrientation(1);panel.setBackground(rd(0xF2141A2E,26));panel.setPadding(dp(12),dp(10),dp(12),dp(10));
  LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);
  TextView title=new TextView(this);title.setText("🤖 AI Mod Menu");title.setTextColor(0xFFFFFFFF);title.setTextSize(15);
  head.addView(title,new LinearLayout.LayoutParams(0,-2,1f));
  TextView x=new TextView(this);x.setText("✕");x.setTextColor(0xFFBBBBBB);x.setTextSize(18);x.setPadding(dp(10),0,dp(4),0);x.setOnClickListener(v->toggle());
  head.addView(x);panel.addView(head);
  chat=new LinearLayout(this);chat.setOrientation(1);sv=new ScrollView(this);sv.addView(chat);
  panel.addView(sv,new LinearLayout.LayoutParams(-1,dp(240)));
  typing=new TextView(this);typing.setTextColor(0xFF9AA4C8);typing.setTextSize(12);typing.setVisibility(8);panel.addView(typing);
  LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(8),0,0);
  input=new EditText(this);input.setHint("এখানে লেখো...");input.setTextColor(Color.WHITE);input.setHintTextColor(0xFF7F88AA);input.setTextSize(14);
  input.setBackground(rd(0xFF232A45,22));input.setPadding(dp(16),dp(8),dp(16),dp(8));input.setMaxLines(3);
  row.addView(input,new LinearLayout.LayoutParams(0,-2,1f));
  TextView send=new TextView(this);send.setText("➤");send.setTextColor(Color.WHITE);send.setTextSize(18);send.setGravity(Gravity.CENTER);
  GradientDrawable sg=new GradientDrawable();sg.setShape(GradientDrawable.OVAL);sg.setColor(0xFF3B6CFF);send.setBackground(sg);
  LinearLayout.LayoutParams sl=new LinearLayout.LayoutParams(dp(44),dp(44));sl.leftMargin=dp(8);row.addView(send,sl);
  send.setOnClickListener(v->send());panel.addView(row);panel.setVisibility(8);
  pp=lp(getResources().getDisplayMetrics().widthPixels-dp(20),-2,WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
  pp.gravity=Gravity.TOP|Gravity.CENTER_HORIZONTAL;pp.y=dp(24);
  wm.addView(panel,pp);wm.addView(bubble,bp);}
 void toggle(){isOpen=!isOpen;panel.setVisibility(isOpen?0:8);setFocus(isOpen&&!busy);
  if(isOpen&&chat.getChildCount()==0)aiSay("হাই ভাই! 👋 কী করবো বলো, আমি তোমার ফোনে সব করে দিবো।");}
 void send(){String t=input.getText().toString().trim();if(t.isEmpty()||busy)return;input.setText("");
  addMsg(t,true);convo.append("User: ").append(t).append("\n");if(convo.length()>3000)convo.delete(0,convo.length()-3000);
  busy=true;new Thread(()->run(t)).start();}
 TextView addMsg(String s,boolean user){
  TextView tv=new TextView(this);tv.setText(s);tv.setTextColor(Color.WHITE);tv.setTextSize(14);tv.setMaxWidth(dp(250));
  tv.setPadding(dp(14),dp(9),dp(14),dp(9));tv.setBackground(rd(user?0xFF3B6CFF:0xFF2A3152,18));
  LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(-2,-2);l.gravity=user?Gravity.END:Gravity.START;l.bottomMargin=dp(6);
  chat.addView(tv,l);tv.setAlpha(0);tv.setTranslationY(-dp(18));tv.animate().alpha(1).translationY(0).setDuration(300).start();down();return tv;}
 void down(){sv.post(()->sv.fullScroll(View.FOCUS_DOWN));}
 void aiSay(String s){ui.post(()->{TextView tv=addMsg("",false);final int[] i={0};
  Runnable r=new Runnable(){public void run(){i[0]=Math.min(s.length(),i[0]+2);tv.setText(s.substring(0,i[0]));down();if(i[0]<s.length())ui.postDelayed(this,25);}};
  ui.post(r);});}
 void typingStart(){ui.post(()->{typing.setVisibility(0);typingOn=true;ui.post(new Runnable(){int k=0;public void run(){
  if(!typingOn){typing.setVisibility(8);return;}String[] f={"●","● ●","● ● ●"};typing.setText("AI লিখছে "+f[k++%3]);ui.postDelayed(this,400);}});});}
 void typingStop(){typingOn=false;ui.post(()->typing.setVisibility(8));}
 String norm(String s){return s.toLowerCase().replaceAll("[^a-z0-9\\u0980-\\u09FF]","");}
 String openApp(String name){
  PackageManager pm=getPackageManager();String n=norm(name);if(n.isEmpty())return null;
  Intent q=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);String best=null,bl=null;int bs=0;
  for(ResolveInfo ri:pm.queryIntentActivities(q,0)){
   String l=ri.loadLabel(pm).toString();String m=norm(l);if(m.isEmpty())continue;int s=0;
   if(m.equals(n))s=100;else if(m.startsWith(n))s=70;else if(m.contains(n))s=50;else if(m.length()>=3&&n.contains(m))s=40;
   if(s>bs){bs=s;best=ri.activityInfo.packageName;bl=l;}}
  if(best==null)return null;Intent li=pm.getLaunchIntentForPackage(best);if(li==null)return null;
  li.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(li);return bl;}
 void run(String goalText){
  StringBuilder hist=new StringBuilder();typingStart();ui.post(()->setFocus(false));
  try{
   for(int i=0;i<15;i++){
    ui.post(()->{bubble.setVisibility(4);panel.setVisibility(4);});Thread.sleep(350);
    final Bitmap[] bm=new Bitmap[1];final CountDownLatch cl=new CountDownLatch(1);
    takeScreenshot(0,getMainExecutor(),new TakeScreenshotCallback(){
     public void onSuccess(ScreenshotResult r){Bitmap h=Bitmap.wrapHardwareBuffer(r.getHardwareBuffer(),r.getColorSpace());bm[0]=h.copy(Bitmap.Config.ARGB_8888,false);cl.countDown();}
     public void onFailure(int c){cl.countDown();}});
    cl.await(10,TimeUnit.SECONDS);
    ui.post(()->{bubble.setVisibility(0);panel.setVisibility(isOpen?0:8);});
    if(bm[0]==null){aiSay("স্ক্রিনশট নিতে পারলাম না 😕");return;}
    float s=720f/bm[0].getWidth();Bitmap sm=Bitmap.createScaledBitmap(bm[0],720,(int)(bm[0].getHeight()*s),true);
    ByteArrayOutputStream o=new ByteArrayOutputStream();sm.compress(Bitmap.CompressFormat.JPEG,70,o);
    String img=Base64.encodeToString(o.toByteArray(),Base64.NO_WRAP);
    JSONObject c=ask(goalText,img,hist.toString());hist.append(c).append("\n");
    String a=c.optString("a");
    if(c.has("say")&&!a.equals("done")&&c.optString("say").length()>0)aiSay(c.optString("say"));
    if(a.equals("tap"))gest(c.getInt("x")/s,c.getInt("y")/s,c.getInt("x")/s,c.getInt("y")/s,50);
    else if(a.equals("swipe"))gest(c.getInt("x1")/s,c.getInt("y1")/s,c.getInt("x2")/s,c.getInt("y2")/s,300);
    else if(a.equals("open")){String r=openApp(c.optString("name"));
     if(r==null){aiSay("\""+c.optString("name")+"\" নামে অ্যাপ খুঁজে পেলাম না 😕");return;}hist.append("(opened ").append(r).append(")\n");Thread.sleep(1500);}
    else if(a.equals("type")){AccessibilityNodeInfo f=findFocus(AccessibilityNodeInfo.FOCUS_INPUT);if(f!=null){Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,c.getString("t"));f.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b);}}
    else if(a.equals("key")){String k=c.optString("k");performGlobalAction(k.equals("home")?GLOBAL_ACTION_HOME:GLOBAL_ACTION_BACK);}
    else if(a.equals("done")){String t=c.optString("say","ঠিক আছে");aiSay(t);convo.append("AI: ").append(t).append("\n");return;}
    Thread.sleep(1500);
   }
   aiSay("১৫ ধাপে শেষ করতে পারলাম না, আবার বলো।");
  }catch(Exception e){aiSay("Error: "+e);}
  finally{busy=false;typingStop();ui.post(()->{bubble.setVisibility(0);panel.setVisibility(isOpen?0:8);setFocus(isOpen);});}
 }
 void gest(float x1,float y1,float x2,float y2,long d){Path p=new Path();p.moveTo(x1,y1);if(x1!=x2||y1!=y2)p.lineTo(x2,y2);else p.lineTo(x1+1,y1);
  dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,d)).build(),null,null);}
 JSONObject ask(String goalText,String img,String hist)throws Exception{
  String sys="You are an AI friend living inside the user's Android phone. You chat with the user and can control the phone. "
   +"You get a screenshot, the conversation and the latest request. Reply ONLY one JSON object: "
   +"{\"a\":\"open\",\"name\":\"app name\"} or {\"a\":\"tap\",\"x\":0,\"y\":0} or {\"a\":\"swipe\",\"x1\":0,\"y1\":0,\"x2\":0,\"y2\":0} or {\"a\":\"type\",\"t\":\"text\"} or {\"a\":\"key\",\"k\":\"back|home\"} or {\"a\":\"wait\"} or {\"a\":\"done\",\"say\":\"message\"}. "
   +"Rules: To open any app ALWAYS use open with the exact app name the user said, never tap app icons to open apps. Coordinates are screenshot pixels. "
   +"For normal chat, questions, or describing the screen, reply with done and say. When the task is finished reply done with a short say. "
   +"You may add a very short \"say\" progress note to any other action. Reply in the user's language (Bengali if they write Bengali or Banglish). Be friendly and short.";
  JSONArray parts=new JSONArray().put(new JSONObject().put("inline_data",new JSONObject().put("mime_type","image/jpeg").put("data",img)))
   .put(new JSONObject().put("text","Conversation so far:\n"+convo+"\nLatest request: "+goalText+"\nActions done in this task:\n"+hist));
  JSONObject body=new JSONObject().put("systemInstruction",new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text",sys))))
   .put("contents",new JSONArray().put(new JSONObject().put("parts",parts)))
   .put("generationConfig",new JSONObject().put("maxOutputTokens",1000).put("responseMimeType","application/json"));
  HttpURLConnection u=(HttpURLConnection)new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent").openConnection();
  u.setRequestMethod("POST");u.setDoOutput(true);u.setConnectTimeout(30000);u.setReadTimeout(90000);
  u.setRequestProperty("x-goog-api-key",getSharedPreferences("a",0).getString("k",""));u.setRequestProperty("content-type","application/json");
  u.getOutputStream().write(body.toString().getBytes("UTF-8"));
  InputStream in=u.getResponseCode()<400?u.getInputStream():u.getErrorStream();
  ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)o.write(buf,0,n);
  String r=o.toString("UTF-8");if(u.getResponseCode()>=400)throw new Exception(r);
  String t=new JSONObject(r).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
  return new JSONObject(t.substring(t.indexOf('{'),t.lastIndexOf('}')+1));}
}
