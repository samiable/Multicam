package com.multicam.studio;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
public class MainActivity extends BridgeActivity {
  @Override public void onCreate(Bundle b){ registerPlugin(NetPlugin.class); super.onCreate(b); }
}
