package ch.kurky.beerapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.baasbox.android.BaasBox;
import com.baasbox.android.BaasDocument;
import com.baasbox.android.BaasUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView lvItem;
    private ArrayAdapter<Beer> adapter;

    private BaasBox box;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BaasBox.Builder b = new BaasBox.Builder(this);
        b.setApiDomain("192.168.0.101").setAppCode("1234567890").setPort(9000).setAuthentication(BaasBox.Config.AuthType.SESSION_TOKEN);
        box = b.init();

        if ( !BaasUser.current().isAuthentcated()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvItem = (ListView)this.findViewById(R.id.lvQueue);
        adapter = new Adapter(this);
        lvItem.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BeerActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String name = data.getStringExtra("name");
                Double price = data.getDoubleExtra("price", 0.0);
                Beer beer = new Beer();
                beer.name = name;
                beer.price = price;
                adapter.add(beer);
                adapter.notifyDataSetChanged();
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public BaasBox getBox(){
        return box;
    }

    public class Adapter extends ArrayAdapter<Beer> {

        public Adapter(Context context) {
            super(context, android.R.layout.simple_list_item_2,	new ArrayList<Beer>());
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2,
                        null);

                BeerActivity.Tag tag = new BeerActivity.Tag();
                tag.text1 = (TextView) view.findViewById(android.R.id.text1);
                tag.text2 = (TextView) view.findViewById(android.R.id.text2);
                view.setTag(tag);
            }

            BeerActivity.Tag tag = (BeerActivity.Tag) view.getTag();
            Beer beer = getItem(position);
            beer = getItem(position);
            tag.text1.setText(beer.name);
            tag.text2.setText(String.valueOf(beer.price));

            return view;
        }

    }

    protected static class Tag {

        public TextView text1;
        public TextView text2;

    }


}
