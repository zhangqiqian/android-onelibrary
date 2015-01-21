package org.onelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowTitleEnabled(false);
        String content = null;
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getBundleExtra("message_item");
            if (bundle == null) {
                content = "不好意思，程序出错啦！";
            }else {
                content = bundle.getString("title")+"\n\n"
                        +bundle.getString("pubdate")+"\n\n"
                        +bundle.getString("description").replace('\n', ' ')
                        +"\n\n详细信息请访问以下网址：\n"
                        +bundle.getString("link");
            }
        } else {
            content = "不好意思，程序出错啦！";
        }

        TextView textView = (TextView) findViewById(R.id.content);
        textView.setText(content);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
