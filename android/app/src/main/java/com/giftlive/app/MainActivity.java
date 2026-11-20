package com.giftlive.app;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.graphics.Color;
public class MainActivity extends Activity {
 private static final String SITE="https://giftlive-preview-qosay.onrender.com/";
 private static final int MEDIA=101;
 private WebView web;
 private PermissionRequest pending;
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout layout=new LinearLayout(this);
  layout.setOrientation(LinearLayout.VERTICAL);
  layout.setBackgroundColor(Color.rgb(24,7,17));
  TextView banner=new TextView(this);
  banner.setText("GiftLive • Diffusion caméra/micro uniquement avec votre accord. Vous pouvez arrêter à tout moment.");
  banner.setTextColor(Color.WHITE);
  banner.setBackgroundColor(Color.rgb(108,27,52));
  banner.setPadding(16,18,16,18);
  layout.addView(banner,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
  web=new WebView(this);
  layout.addView(web,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
  setContentView(layout);
  WebSettings settings=web.getSettings();
  settings.setJavaScriptEnabled(true);
  settings.setDomStorageEnabled(true);
  settings.setMediaPlaybackRequiresUserGesture(true);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView view,android.webkit.WebResourceRequest request){
    if(!"giftlive-preview-qosay.onrender.com".equals(request.getUrl().getHost())){
     startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,request.getUrl()));return true;
    }
    return false;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public void onPermissionRequest(PermissionRequest request){
    runOnUiThread(()->{
     if(!"https://giftlive-preview-qosay.onrender.com".equals(request.getOrigin().toString())){request.deny();return;}
     pending=request;
     new AlertDialog.Builder(MainActivity.this)
      .setTitle("Partage vidéo et audio")
      .setMessage("Autoriser la diffusion en direct de votre caméra et microphone à la personne disposant du lien de visionnage ?")
      .setNegativeButton("Refuser",(d,w)->{request.deny();pending=null;})
      .setPositiveButton("Autoriser",(d,w)->{
       if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED||
          checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
        requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO},MEDIA);
       else grantPending();
      }).show();
    });
   }
  });
  android.widget.EditText input=new android.widget.EditText(this);
  input.setSingleLine(true);input.setText(SITE);input.setSelectAllOnFocus(true);
  new AlertDialog.Builder(this).setTitle("Lien GiftLive")
   .setMessage("Collez le lien de partage reçu. La diffusion ne commence jamais automatiquement.")
   .setView(input).setCancelable(false)
   .setPositiveButton("Ouvrir",(d,w)->{
    String u=input.getText().toString().trim();
    android.net.Uri uri=android.net.Uri.parse(u);
    if("https".equals(uri.getScheme())&&"giftlive-preview-qosay.onrender.com".equals(uri.getHost())&&uri.getQueryParameter("share")!=null)web.loadUrl(u);
    else web.loadUrl(SITE);
   }).show();
 }
 private void grantPending(){
  if(pending!=null){
   java.util.ArrayList<String> allowed=new java.util.ArrayList<>();
   for(String resource:pending.getResources())
    if(PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)||PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource))allowed.add(resource);
   if(!allowed.isEmpty())pending.grant(allowed.toArray(new String[0]));else pending.deny();
   pending=null;
  }
 }
 @Override public void onRequestPermissionsResult(int req,String[] perms,int[] grants){
  super.onRequestPermissionsResult(req,perms,grants);
  if(req==MEDIA){
   boolean ok=grants.length==perms.length&&grants.length>0;
   for(int g:grants)ok &= g==PackageManager.PERMISSION_GRANTED;
   if(ok)grantPending();else if(pending!=null){pending.deny();pending=null;}
  }
 }
 @Override protected void onPause(){super.onPause();if(web!=null)web.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){if(pending!=null){pending.deny();pending=null;}if(web!=null){web.loadUrl("about:blank");web.destroy();}super.onDestroy();}
}