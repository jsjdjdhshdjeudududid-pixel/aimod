f="app/src/main/java/com/ai/mod/AiService.java"
s=open(f).read()
i=s.index(" JSONObject ask(")
N=r'''
 JSONObject ask(String goalText,String img,String hist)throws Exception{
  String sys="You control an Android phone. You get a screenshot and a goal. Reply ONLY one JSON: {\"a\":\"tap\",\"x\":0,\"y\":0} or {\"a\":\"swipe\",\"x1\":0,\"y1\":0,\"x2\":0,\"y2\":0} or {\"a\":\"type\",\"t\":\"text\"} or {\"a\":\"key\",\"k\":\"back|home\"} or {\"a\":\"wait\"} or {\"a\":\"done\",\"say\":\"short message\"}. Coordinates are screenshot pixels. To only describe the screen use done with say. Reply in the user's language.";
  JSONArray parts=new JSONArray().put(new JSONObject().put("inline_data",new JSONObject().put("mime_type","image/jpeg").put("data",img)))
   .put(new JSONObject().put("text","Goal: "+goalText+"\nDone so far:\n"+hist));
  JSONObject body=new JSONObject().put("systemInstruction",new JSONObject().put("parts",new JSONArray().put(new JSONObject().put("text",sys))))
   .put("contents",new JSONArray().put(new JSONObject().put("parts",parts)))
   .put("generationConfig",new JSONObject().put("maxOutputTokens",1000).put("responseMimeType","application/json"));
  HttpURLConnection u=(HttpURLConnection)new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent").openConnection();
  u.setRequestMethod("POST");u.setDoOutput(true);u.setConnectTimeout(30000);u.setReadTimeout(90000);
  u.setRequestProperty("x-goog-api-key",getSharedPreferences("a",0).getString("k",""));u.setRequestProperty("content-type","application/json");
  u.getOutputStream().write(body.toString().getBytes("UTF-8"));
  InputStream in=u.getResponseCode()<400?u.getInputStream():u.getErrorStream();
  ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))>0)o.write(buf,0,n);
  String r=o.toString("UTF-8");if(u.getResponseCode()>=400)throw new Exception(r);
  String t=new JSONObject(r).getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
  return new JSONObject(t.substring(t.indexOf('{'),t.lastIndexOf('}')+1));}
'''
open(f,"w").write(s[:i]+N+"}\n")
