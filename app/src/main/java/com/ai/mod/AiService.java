package com.ai.mod;
import android.accessibilityservice.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.util.Base64;
import android.view.*;import android.view.accessibility.*;import android.widget.*;import org.json.*;
import java.io.*;import java.net.*;import java.util.concurrent.*;
public class AiService extends AccessibilityService {
 WindowManager wm;TextView bubble;LinearLayout panel;TextView status;EditText goal;Handler ui=new Handler(Looper.getMainLooper());
 public void onAccessibilityEvent(AccessibilityEvent e){}
 public void onInterrupt(){}
 protected void onServiceConnected(){
  wm=(WindowManager)getSystemService(WINDOW_SERVICE);
  bubble=new TextView(this);bubble.setText("AI");bubble.setTextColor(Color.WHITE);bubble.setGravity(Gravity.CENTER);
  GradientDrawable g=new GradientDrawable();g.setShape(GradientDrawable.OVAL);g.setColor(0xFFE53935);bubble.setBackground(g);
  WindowManager.LayoutParams bp=lp(150,150);bp.gravity=Gravity.TOP|Gravity.START;bp.x=20;bp.y=300;
  bubble.setOnTouchListener(new View.OnTouchListener(){int sx,sy;float tx,ty;boolean mv;
   public boolean onTouch(View v,MotionEvent m){
    if(m.getAction()==0){sx=bp.x;sy=bp.y;tx=m.getRawX();ty=m.getRawY();mv=false;}
    else if(m.getAction()==2){float dx=m.getRawX()-tx,dy=m.getRawY()-ty;if(Math.abs(dx)+Math.abs(dy)>15)mv=true;bp.x=sx+(int)dx;bp.y=sy+(int)dy;wm.updateViewLayout(bubble,bp);}
    else if(m.getAction()==1&&!mv){panel.setVisibility(panel.getVisibility()==0?8:0);}
    return true;}});
  panel=new LinearLayout(this);panel.setOrientation(1);panel.setBackgroundColor(0xEE222222);panel.setPadding(20,20,20,20);
  goal=new EditText(this);goal.setHint("কী করবো?");goal.setTextColor(Color.WHITE);goal.setHintTextColor(Color.GRAY);
  Button go=new Button(this);go.setText("GO");status=new TextView(this);status.setTextColor(Color.WHITE);
  panel.addView(goal);panel.addView(go);panel.addView(status);panel.setVisibility(8);
  go.setOnClickListener(v->{String t=goal.getText().toString();panel.setVisibility(8);new Thread(()->run(t)).start();});
  WindowManager.LayoutParams pp=lp(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
  pp.gravity=Gravity.BOTTOM;pp.flags=WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
  wm.addView(panel,pp);wm.addView(bubble,bp);}
 WindowManager.LayoutParams lp(int w,int h){return new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);}
 void say(String s){ui.post(()->{status.setText(s);Toast.makeText(this,s,0).show();});}
 void run(String goalText){
  String hist="";
  for(int i=0;i<15;i++){
   try{
    say("Step "+(i+1));
    ui.post(()->bubble.setVisibility(4));Thread.sleep(300);
    final Bitmap[] bm=new Bitmap[1];final CountDownLatch cl=new CountDownLatch(1);
    takeScreenshot(0,getMainExecutor(),new TakeScreenshotCallback(){
     public void onSuccess(ScreenshotResult r){Bitmap h=Bitmap.wrapHardwareBuffer(r.getHardwareBuffer(),r.getColorSpace());bm[0]=h.copy(Bitmap.Config.ARGB_8888,false);cl.countDown();}
     public void onFailure(int c){cl.countDown();}});
    cl.await(10,TimeUnit.SECONDS);ui.post(()->bubble.setVisibility(0));
    if(bm[0]==null){say("Screenshot failed");return;}
    float s=720f/bm[0].getWidth();Bitmap sm=Bitmap.createScaledBitmap(bm[0],720,(int)(bm[0].getHeight()*s),true);
    ByteArrayOutputStream o=new ByteArrayOutputStream();sm.compress(Bitmap.CompressFormat.JPEG,70,o);
    String img=Base64.encodeToString(o.toByteArray(),Base64.NO_WRAP);
    JSONObject c=ask(goalText,img,hist);hist+=c.toString()+"\n";
    String a=c.optString("a");
    if(a.equals("tap"))gest(c.getInt("x")/s,c.getInt("y")/s,c.getInt("x")/s,c.getInt("y")/s,50);
    else if(a.equals("swipe"))gest(c.getInt("x1")/s,c.getInt("y1")/s,c.getInt("x2")/s,c.getInt("y2")/s,300);
    else if(a.equals("type")){AccessibilityNodeInfo f=findFocus(AccessibilityNodeInfo.FOCUS_INPUT);if(f!=null){Bundle b=new Bundle();b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,c.getString("t"));f.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b);}}
    else if(a.equals("key")){String k=c.optString("k");performGlobalAction(k.equals("home")?GLOBAL_ACTION_HOME:GLOBAL_ACTION_BACK);}
    else if(a.equals("done")){say("AI: "+c.optString("say","done"));return;}
    Thread.sleep(1500);
   }catch(Exception e){ui.post(()->bubble.setVisibility(0));say("Error: "+e);return;}
  }
  say("Step limit");}
 void gest(float x1,float y1,float x2,float y2,long d){Path p=new Path();p.moveTo(x1,y1);if(x1!=x2||y1!=y2)p.lineTo(x2,y2);else p.lineTo(x1+1,y1);
  dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,d)).build(),null,null);}

 JSONObject ask(String goalText,String img,String hist)throws Exception{
  String sys="You control an Android phone. You get a screenshot and a goal. Reply ONLY one JSON: {\"a\":\"tap\",\"x\":0,\"y\":0} or {\"a\":\"swipe\",\"x1\":0,\"y1\":0,\"x2\":0,\"y2\":0} or {\"a\":\"type\",\"t\":\"text\"} or {\"a\":\"key\",\"k\":\"back|home\"} or {\"a\":\"wait\"} or {\"a\":\"done\",\"say\":\"short message\"}. Coordinates are screenshot pixels. To only describe the screen use done with say. Reply in the user's language.";
  JSONArray parts=new JSONArray().put(new JSONObject().put("inline_data",new JSONObject().put("mime_type","image/jpeg").put("data",img)))
   .put(new JSONObject().put("text","Goal: "+goalText+"\nDone so far:\n"+hist));
  JSONObject body=new JSONObject().put("systemInstruction",new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text",sys))))
   .put("contents",new JSONArray().put(new JSONObject().put("parts",parts)))
   .put("generationConfig",new JSONObject().put("maxOutputTokens",1000).put("responseMimeType","application/json"));
  HttpURLConnection u=(HttpURLConnection)new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent").openConnection();
  u.setRequestMethod("POST");u.setDoOutput(true);u.setConnectTimeout(30000);u.setReadTimeout(90000);
  u.setRequestProperty("x-goog-api-key",getSharedPreferences("a",0).getString("k",""));u.setRequestProperty("content-type","application/json");
  u.getOutputStream().write(body.toString().getBytes("UTF-8"));
  InputStream in=u.getResponseCode()<400?u.getInputStream():u.getErrorStream();
  ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)o.write(buf,0,n);
  String r=o.toString("UTF-8");if(u.getResponseCode()>=400)throw new Exception(r);
  String t=new JSONObject(r).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
  return new JSONObject(t.substring(t.indexOf('{'),t.lastIndexOf('}')+1));}
}
