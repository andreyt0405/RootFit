package com.sportschule.rootfit;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastCustomMessage extends Activity {
    Context context;
    View layout;
    TextView textView;

    public ToastCustomMessage(Context context,View layout)
    {
        this.context = context;
        this.layout = layout;
        textView =  layout.findViewById(R.id.toast_test);
    }
    public void toastMessage(String string)
    {
        textView.setText(string);
        Toast toast = Toast.makeText(this.context,string, Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    public void toastMessage(int string)
    {
        textView.setText(string);
        Toast toast = Toast.makeText(this.context,string, Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    public void toastMessage(String string,int gravity)
    {
        textView.setText(string);
        Toast toast = Toast.makeText(this.context,string, Toast.LENGTH_LONG);
        toast.setGravity(gravity, 0, 0);
        toast.setView(layout);
        toast.show();
    }
    public void toastMessage(int string, int gravity)
    {
        textView.setText(string);
        Toast toast = Toast.makeText(this.context,string, Toast.LENGTH_LONG);
        toast.setGravity(gravity, 0, 0);
        toast.setView(layout);
        toast.show();
    }
}
