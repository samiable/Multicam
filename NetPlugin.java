package com.multicam.studio;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CapacitorPlugin(name = "Net")
public class NetPlugin extends Plugin {
  static final Map<String, String> store = new ConcurrentHashMap<>();
  static final Map<String, OutputStream> outs = new HashMap<>();
  ServerSocket ss;

  // Local IPv4 on Wi-Fi, hotspot, USB tethering or USB Ethernet
  @PluginMethod public void ip(PluginCall c) {
    String best = "", any = "";
    try {
      for (NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
        if (!n.isUp() || n.isLoopback()) continue;
        for (InetAddress a : Collections.list(n.getInetAddresses())) {
          if (!(a instanceof Inet4Address)) continue;
          String h = a.getHostAddress();
          if (n.getName().matches("(ap|swlan|wlan|rndis|eth|usb|ncm).*")) { if (best.isEmpty()) best = h; }
          else if (a.isSiteLocalAddress() && any.isEmpty()) any = h;
        }
      }
    } catch (Exception e) {}
    JSObject o = new JSObject(); o.put("ip", best.isEmpty() ? any : best); c.resolve(o);
  }

  // Tiny offline pairing server (same job ntfy.sh did online)
  @PluginMethod public void start(PluginCall c) {
    if (ss == null) try {
      ss = new ServerSocket(8787);
      new Thread(() -> { while (true) try { Socket s = ss.accept(); new Thread(() -> handle(s)).start(); } catch (Exception e) { break; } }).start();
    } catch (Exception e) { c.reject(e.toString()); return; }
    c.resolve();
  }

  void handle(Socket s) {
    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
      String[] p = r.readLine().split(" "); String h; int len = 0;
      while ((h = r.readLine()) != null && !h.isEmpty()) if (h.toLowerCase().startsWith("content-length:")) len = Integer.parseInt(h.substring(15).trim());
      char[] b = new char[len]; int o = 0;
      while (o < len) { int n = r.read(b, o, len - o); if (n < 0) break; o += n; }
      String k = p[1].split("\\?")[0].substring(1).replace("/json", ""), body = "";
      if (p[0].equals("POST")) store.put(k, new String(b, 0, o));
      else if (p[0].equals("GET")) { String v = store.get(k); if (v != null) body = "{\"event\":\"message\",\"message\":\"" + v + "\"}\n"; }
      byte[] out = body.getBytes("UTF-8"); OutputStream w = s.getOutputStream();
      w.write(("HTTP/1.1 200 OK\r\nAccess-Control-Allow-Origin: *\r\nAccess-Control-Allow-Headers: *\r\nAccess-Control-Allow-Methods: *\r\nContent-Length: " + out.length + "\r\nConnection: close\r\n\r\n").getBytes());
      w.write(out); w.flush(); s.close();
    } catch (Exception e) {}
  }

  // Saves recordings to Downloads/MultiCam in chunks
  @PluginMethod public void save(PluginCall c) {
    try {
      String n = c.getString("name"); byte[] d = Base64.decode(c.getString("data"), Base64.DEFAULT);
      OutputStream o = outs.get(n);
      if (o == null) {
        if (Build.VERSION.SDK_INT >= 29) {
          ContentValues v = new ContentValues();
          v.put(MediaStore.MediaColumns.DISPLAY_NAME, n);
          v.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/MultiCam");
          Uri u = getContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
          o = getContext().getContentResolver().openOutputStream(u);
        } else {
          File dir = new File(getContext().getExternalFilesDir(null), "MultiCam"); dir.mkdirs();
          o = new FileOutputStream(new File(dir, n));
        }
        outs.put(n, o);
      }
      o.write(d);
      if (Boolean.TRUE.equals(c.getBoolean("done", false))) { o.close(); outs.remove(n); }
      c.resolve();
    } catch (Exception e) { c.reject(e.toString()); }
  }
}
